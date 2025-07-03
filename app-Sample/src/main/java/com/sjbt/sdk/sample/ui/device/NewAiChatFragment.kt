package com.sjbt.sdk.sample.ui.device

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.lifecycle.lifecycleScope
import com.base.api.UNIWatchMate
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.shenju.opus.OpusDecoderJni
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentAiChatBinding
import com.sjbt.sdk.sample.databinding.FragmentDevicePreviewBinding
import com.sjbt.sdk.sample.databinding.FragmentNewAiChatBinding
import com.sjbt.sdk.sample.utils.AudioPlayer
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.collect
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

//长对话AI聊天流程。
//1.通过语音唤醒进入语音聊天模式（不间断对话），APP端收到observeLongChatAssistantState为true的消息，设备端开始录音。observeLongChatAudioData()会收到解码后的pcm数据。
//2. 设备端通过vad检测到没有人声的时候，observeLongChatAssistantState为false的消息， 告诉手机本次说话完成。
//3. 手机端收到observeLongChatAssistantState为false的消息后开始调用大模型对话，App在开始播放tts时调用notifyTtsPlayingStatus(0: 开始播放)命令到设备端，设备端将忽略有人说话并不再采集语音数据
//4. 手机端播放tts完成后，给设备端发送notifyTtsPlayingStatus(1)方法表示手机tts播放完成，此时设备又重新开始采集语音数据
//5. 在聊天过程中，手机端可以调用stopRecording给设备来结束语音聊天
//6. 设备端在6秒内如果检测不到声音设备主动退出聊天模式（再次进入聊天需要语音唤醒）
//7. 手机端可以调用takePhotoAndSend()方法，让设备拍照并将数据传输给APP，observeLongChatVideoFrame方法可以收到图片数据
class NewAiChatFragment : BaseFragment(R.layout.fragment_new_ai_chat) {
    private val viewBind: FragmentNewAiChatBinding by viewBinding()
    private var tempCachePcmData = ByteArray(0)
    private var mediaPath = MyApplication.instance.mediaPath
    private var curWaveFilePath = ""
    private val audioPlayer = AudioPlayer()
    private var remainingData = ByteArray(0)
    private val SAMPLE_RATE = 16000
    private val CHANNELS = 1
    private val BYTES_PER_SAMPLE = 2
    private val MAX_FRAME_SIZE = 6 * 320
    private val opusHandle = OpusDecoderJni.createDecoder(16000, 1)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化离线语音开关状态
        initOfflineVoiceSwitch()
        
        // 初始化离线语音支持状态
        initOfflineVoiceSupportStatus()
        
        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.wmApps.appAIAssistant.observeLongChatAssistantState.collect {
                    if (it) {
                        tempCachePcmData = ByteArray(0)
                        ToastUtil.showToast("start")
                        viewBind.btnAudio.text = getString(R.string.action_loading)
                        curWaveFilePath = ""
                    } else {
                        viewBind.btnImage.isEnabled = true
                        ToastUtil.showToast("stop")
                        viewBind.btnAudio.text = getString(R.string.play)
                        curWaveFilePath = "${mediaPath}/audio_bt_${System.currentTimeMillis()}.wav"
                        generateWavFile(tempCachePcmData, File(curWaveFilePath))
                    }
                }
            }
            launch {
                UNIWatchMate.wmApps.appAIAssistant.observeLongChatVideoFrame.collect {
                    LogUtils.eTag(tag,"observeLongChatVideoFrame 的data frameType = ${it.frameType}")
                    var jpegData: ByteArray? = null
                    if (it.frameType == 2) {
                        // 转换 I 帧为 JPEG
                        jpegData = convertH264ToJpeg(it.frameData!!)
                    } else if (it.frameType == 8 || it.frameType == 9) {
                        jpegData = it.frameData
                    }
                    Glide.with(viewBind.ivPhoto).load(jpegData).into(viewBind.ivPhoto)
                }
            }
            launch {
                UNIWatchMate.wmApps.appAIAssistant.observeLongChatAudioData.collect {
                    LogUtils.eTag(tag,"收到observeLongChatAudioData 的data size = ${it.size}")
                    // 将剩余的数据和新到的音频数据合并
                    var cachePcmData = ByteArray(0)

                    val combinedData = remainingData + it

                    var index = 0
                    val pcmData = ByteArray(MAX_FRAME_SIZE * CHANNELS * BYTES_PER_SAMPLE)
                    // 处理合并后的数据
                    while (index + 41 <= combinedData.size) {
                        // 获取当前帧的大小，第一字节是帧长度
                        val frameLength = combinedData[index].toInt() and 0xFF  // 获取帧长度
                        if (frameLength != 40) {
                            // 如果帧长度不是 40，跳过这帧（可以根据需求处理异常情况）
                            Log.e(tag, "Invalid frame length: $frameLength")
                            index += 1
                            continue
                        }
                        val frame =
                            combinedData.copyOfRange(index + 1, index + 1 + frameLength) // 获取帧数据
                        // 调用 decode 解码每一帧数据
                        val frameSize = OpusDecoderJni.decode(
                            decoder = opusHandle,
                            opusData = frame,
                            pcmData = pcmData,
                            frameSize = 40
                        )
                        val acatualFrameSize = frameSize * CHANNELS * BYTES_PER_SAMPLE
                        cachePcmData = cachePcmData.plus(pcmData.copyOfRange(0, acatualFrameSize))

                        //现将pcmData缓存起来，最后一次一起写入文件
                        index += 41
                    }
                    tempCachePcmData = tempCachePcmData.plus(cachePcmData)
                    // 保存剩余的数据（不完整的一部分帧）
                    remainingData = if (index < combinedData.size) {
                        LogUtils.e("JNI", "remainingData: ${combinedData.size - index}")
                        combinedData.copyOfRange(index, combinedData.size)
                    } else {
                        ByteArray(0)  // 如果没有剩余数据，清空缓存
                    }
                }
            }
        }
        viewBind.btnImage.setOnClickListener {
            lifecycleScope.launch {
                try {
                    // 这里调用 UNIWatchMate API 来让设备发送
                    // 使用 withContext(Dispatchers.IO) 确保在后台线程执行
                    val result = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        UNIWatchMate.wmApps.appAIAssistant.takePhotoAndSend()
                            .blockingGet()
                    }
                    LogUtils.e(
                        "NewAiChatFragment",
                        "takePhotoAndSend result: ${result}"
                    )
                } catch (e: Exception) {
                    LogUtils.e(
                        "NewAiChatFragment",
                        "Failed to read wakeup word status: ${e.message}"
                    )
                    false
                }
            }
        }
        viewBind.btnAudio.setOnClickListener {
            if (curWaveFilePath.isEmpty())
                return@setOnClickListener

            viewBind.btnAudio.text = getString(R.string.playing)
            playbackAudio(curWaveFilePath, object : AudioPlayer.AudioPlayerListener {
                override fun onComplete() {
                    viewBind.btnAudio.text = getString(R.string.play)
                }

                override fun onError(error: String) {
                    viewBind.btnAudio.text = getString(R.string.play)
                }
            })
        }

        // 离线语音开关事件处理
        viewBind.switchOfflineVoice.setOnCheckedChangeListener { _, isChecked ->
            switchWakeupWord(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //停止设备录音，退出ai对话
        UNIWatchMate.wmApps.appAIAssistant.stopRecording().subscribe( { res: Boolean ->
            LogUtils.i("ChatManagerViewModel", "stopRecording result: $res")
        })
    }
    /**
     * 初始化离线语音开关状态
     */
    private fun initOfflineVoiceSwitch() {
        lifecycleScope.launch {
            try {
                val isEnabled = readWakeupWordStatus()
                viewBind.switchOfflineVoice.isChecked = isEnabled
            } catch (e: Exception) {
                LogUtils.e("NewAiChatFragment", "Failed to read wakeup word status: ${e.message}")
                ToastUtil.showToast(getString(R.string.offline_voice_read_failed))
            }
        }
    }

    /**
     * 设置离线语音开关
     */
    private fun switchWakeupWord(enabled: Boolean) {
        lifecycleScope.launch {
            try {
                // 这里调用 UNIWatchMate API 来设置离线语音开关
                // 使用 withContext(Dispatchers.IO) 确保在后台线程执行
                // 由于文档中没有具体的 API 方法，这里使用占位符
                // 实际使用时需要根据真实的 API 进行调用
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    UNIWatchMate.wmApps.appAIAssistant.switchWakeupWord(enabled)
                        .subscribe(
                            { success ->
                                // 在主线程中更新 UI
                                lifecycleScope.launch {
                                    if (success) {
                                        ToastUtil.showToast(if (enabled) getString(R.string.offline_voice_enabled) else getString(R.string.offline_voice_disabled))
                                    } else {
                                        ToastUtil.showToast(getString(R.string.offline_voice_switch_failed))
                                        // 恢复开关状态
                                        viewBind.switchOfflineVoice.isChecked = !enabled
                                    }
                                }
                            },
                            { error ->
                                LogUtils.e("NewAiChatFragment", "Failed to switch wakeup word: ${error.message}")
                                // 在主线程中更新 UI
                                lifecycleScope.launch {
                                    ToastUtil.showToast(getString(R.string.offline_voice_switch_failed))
                                    // 恢复开关状态
                                    viewBind.switchOfflineVoice.isChecked = !enabled
                                }
                            }
                        )
                }
            } catch (e: Exception) {
                LogUtils.e("NewAiChatFragment", "Exception in switchWakeupWord: ${e.message}")
                ToastUtil.showToast(getString(R.string.offline_voice_switch_failed))
                // 恢复开关状态
                viewBind.switchOfflineVoice.isChecked = !enabled
            }
        }
    }

    /**
     * 读取离线语音开关状态
     */
    private suspend fun readWakeupWordStatus(): Boolean {
        return try {
            // 这里调用 UNIWatchMate API 来读取离线语音开关状态
            // 使用 withContext(Dispatchers.IO) 确保在后台线程执行
            // 由于文档中没有具体的 API 方法，这里使用占位符
            // 实际使用时需要根据真实的 API 进行调用
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                UNIWatchMate.wmApps.appAIAssistant.readWakeupWordStatus()
                    .blockingGet()
            }
        } catch (e: Exception) {
            LogUtils.e("NewAiChatFragment", "Failed to read wakeup word status: ${e.message}")
            false
        }
    }

    /**
     * 初始化离线语音支持状态
     */
    private fun initOfflineVoiceSupportStatus() {
        lifecycleScope.launch {
            try {
                val functionSupport = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    UNIWatchMate.getGlassesFunctionSupportState()
                }
                
                // 根据 supportVolcEngine 判断是否支持离线语音
                // supportVolcEngine == 1 表示支持
                val isSupported = functionSupport.supportVolcEngine == 1
                
                viewBind.tvOfflineVoiceSupport.text = if (isSupported) {
                    getString(R.string.offline_voice_supported)
                } else {
                    getString(R.string.offline_voice_not_supported)
                }
                
                // 如果不支持离线语音，禁用开关
                viewBind.switchOfflineVoice.isEnabled = isSupported
                
            } catch (e: Exception) {
                LogUtils.e("NewAiChatFragment", "Failed to get function support state: ${e.message}")
                viewBind.tvOfflineVoiceSupport.text = getString(R.string.offline_voice_not_supported)
                viewBind.switchOfflineVoice.isEnabled = false
            }
        }
    }

    private fun playbackAudio(wavePath: String, listener: AudioPlayer.AudioPlayerListener) {
        if (audioPlayer.isPlaying()) {
            audioPlayer.stop()
            listener?.onComplete()
            return
        }
        audioPlayer.setListener(listener)
        audioPlayer.playFile(wavePath)
    }

    private fun convertH264ToJpeg(h264Data: ByteArray): ByteArray? {
        val width = 320
        val height = 240
        val codec = MediaCodec.createDecoderByType("video/avc")
        val format = MediaFormat.createVideoFormat("video/avc", width, height)
        codec.configure(format, null, null, 0)
        codec.start()

        val inputBufferIndex = codec.dequeueInputBuffer(10000)
        if (inputBufferIndex >= 0) {
            val inputBuffer = codec.getInputBuffer(inputBufferIndex)!!
            inputBuffer.clear()
            inputBuffer.put(h264Data)
            codec.queueInputBuffer(inputBufferIndex, 0, h264Data.size, 0, 0)
        }

        val bufferInfo = MediaCodec.BufferInfo()
        val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)

        var jpegData: ByteArray? = null
        if (outputBufferIndex >= 0) {
            val outputBuffer = codec.getOutputBuffer(outputBufferIndex)!!
            val outputData = ByteArray(bufferInfo.size)
            outputBuffer.get(outputData)

            jpegData = yuvToJpeg(outputData, width, height)
            codec.releaseOutputBuffer(outputBufferIndex, false)
        }

        codec.stop()
        codec.release()

        return jpegData
    }

    private fun yuvToJpeg(yuvData: ByteArray, width: Int, height: Int): ByteArray {
        val yuvImage = YuvImage(yuvData, ImageFormat.NV21, width, height, null)
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, outputStream)
        return outputStream.toByteArray()
    }


    private fun generateWavFile(
        pcmData: ByteArray,
        outputFile: File,
        sampleRate: Int = 16000,
        numChannels: Short = 1,
        bitsPerSample: Short = 16
    ) {
        val pcmDataSize = pcmData.size
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = (numChannels * bitsPerSample / 8).toShort()

        // RIFF Chunk
        val riffHeader = "RIFF".toByteArray(Charsets.UTF_8)
        val chunkSize =
            4 + (8 + 16 + 8 + pcmDataSize) // RIFF header + fmt + data chunk header + data size
        val chunkSizeBytes =
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(chunkSize).array()

        // WAVE Chunk
        val waveHeader = "WAVE".toByteArray(Charsets.UTF_8)

        // fmt Chunk
        val fmtHeader = "fmt ".toByteArray(Charsets.UTF_8)
        val fmtSize = 16 // for PCM
        val audioFormat = 1.toShort() // PCM format
        val numChannelsBytes =
            ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(numChannels).array()
        val sampleRateBytes =
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate).array()
        val byteRateBytes =
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(byteRate).array()
        val blockAlignBytes =
            ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(blockAlign).array()
        val bitsPerSampleBytes =
            ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(bitsPerSample).array()

        // data Chunk
        val dataHeader = "data".toByteArray(Charsets.UTF_8)
        val dataSize = pcmDataSize
        val dataSizeBytes =
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(dataSize).array()

        // Write WAV header and PCM data to file
        FileOutputStream(outputFile).use { fos ->
            BufferedOutputStream(fos).use { bos ->
                bos.write(riffHeader)
                bos.write(chunkSizeBytes)
                bos.write(waveHeader)
                bos.write(fmtHeader)
                bos.write(
                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(fmtSize).array()
                )

                // Write audioFormat (PCM format)
                bos.write(
                    ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(audioFormat)
                        .array()
                )

                // Write remaining fmt chunk data
                bos.write(numChannelsBytes)
                bos.write(sampleRateBytes)
                bos.write(byteRateBytes)
                bos.write(blockAlignBytes)
                bos.write(bitsPerSampleBytes)

                // Write data chunk
                bos.write(dataHeader)
                bos.write(dataSizeBytes)
                bos.write(pcmData)
            }
        }
    }

}