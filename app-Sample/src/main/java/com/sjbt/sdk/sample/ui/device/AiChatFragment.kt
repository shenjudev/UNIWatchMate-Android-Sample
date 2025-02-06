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
import androidx.lifecycle.lifecycleScope
import com.base.api.UNIWatchMate
import com.blankj.utilcode.util.AppUtils
import com.bumptech.glide.Glide
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentAiChatBinding
import com.sjbt.sdk.sample.databinding.FragmentDevicePreviewBinding
import com.sjbt.sdk.sample.utils.AudioPlayer
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.collect
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


class AiChatFragment : BaseFragment(R.layout.fragment_ai_chat) {
    private val viewBind: FragmentAiChatBinding by viewBinding()
    private var tempCachePcmData = ByteArray(0)
    private var mediaPath = MyApplication.instance.mediaPath
    private var curWaveFilePath = ""
    private val audioPlayer = AudioPlayer()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.wmApps.appAIAssistant.observeAssistantState.collect {
                    if (it) {
                        tempCachePcmData = ByteArray(0)
                        ToastUtil.showToast("start")
                        viewBind.btnAudio.text = getString(R.string.action_loading)
                        curWaveFilePath = ""
                    } else {
                        ToastUtil.showToast("stop")
                        viewBind.btnAudio.text = getString(R.string.play)
                        curWaveFilePath = "${mediaPath}/audio_bt_${System.currentTimeMillis()}.wav"
                        generateWavFile(tempCachePcmData, File(curWaveFilePath))
                    }
                }
            }
            launch {
                UNIWatchMate.wmApps.appAIAssistant.observeVideoFrame.collect {
                    var jpegData: ByteArray? = null
                    if (it.frameType == 2) {
                        // 转换 I 帧为 JPEG
                        jpegData = convertH264ToJpeg(it.frameData!!)
                    } else if (it.frameType == 9) {
                        jpegData = it.frameData
                    }
                    Glide.with(viewBind.ivPhoto).load(jpegData).into(viewBind.ivPhoto)
                }
            }
            launch {
                UNIWatchMate.wmApps.appAIAssistant.observeAudioData.collect {
                    tempCachePcmData = tempCachePcmData.plus(it)
                }
            }
        }

        viewBind.btnAudio.setOnClickListener {
            if (curWaveFilePath.isEmpty())
                return@setOnClickListener
            playbackAudio(curWaveFilePath, object : AudioPlayer.AudioPlayerListener {
                override fun onComplete() {
                    viewBind.btnAudio.text = getString(R.string.action_start)
                }

                override fun onError(error: String) {
                    viewBind.btnAudio.text = getString(R.string.action_start)
                }
            })
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