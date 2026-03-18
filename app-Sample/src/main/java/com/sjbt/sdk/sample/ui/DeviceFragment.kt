package com.sjbt.sdk.sample.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.BindType
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.apps.WmConnectState
import com.blankj.utilcode.util.LogUtils
import com.shenju.opus.OpusDecoderJni
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.SharedAPPPhotoViewModel
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.di.internal.CoroutinesInstance.applicationScope
import com.sjbt.sdk.sample.entity.MediaCountBean
import com.sjbt.sdk.sample.ui.bind.DeviceConnectDialogFragment
import com.sjbt.sdk.sample.utils.AudioPlayer
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.utils.log.GsonUtil
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.collect
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

@StringRes
fun WmConnectState.toStringRes(): Int {
    return when (this) {
        WmConnectState.DISCONNECTED -> R.string.device_state_disconnected
        WmConnectState.CONNECTING -> R.string.device_state_connecting
        WmConnectState.CONNECTED -> R.string.device_state_connected
        WmConnectState.BIND_SUCCESS -> R.string.device_state_verified
        else -> {
            R.string.device_state_other
        }
    }
}

const val TAG = "DeviceFragment"

class DeviceFragment : BaseFragment(R.layout.fragment_device),
    DeviceConnectDialogFragment.Listener {

    private val viewBind: FragmentDeviceBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()

    private val userInfoRepository = Injector.getUserInfoRepository()

    /** 使用全局 SharedAPPPhotoViewModel 接收无存储设备拍照后设备下发的照片分片 */
    private val sharedAPPPhotoViewModel: SharedAPPPhotoViewModel
        get() = MyApplication.instance.sharedAPPPhotoViewModel

    private var compositeDisposableGet = CompositeDisposable()
    private var isOpenRecording = false
    private var isOpenVideo = false

    /** 是否为无存储设备，用于无存储设备录音时累积 PCM 并在停止时保存为 WAV */
    private var isNoStorageDevice = false

    /** 无存储设备录音：累积的 PCM 数据（Opus 解码后） */
    private var noStoragePcmData = ByteArray(0)
    /** 无存储设备录音：未处理完的一帧剩余数据 */
    private var noStorageRemainingData = ByteArray(0)

    private val noStorageSampleRate = 16000
    private val noStorageChannels = 1
    private val noStorageBytesPerSample = 2
    private val noStorageMaxFrameSize = 6 * 320
    private val noStorageOpusHandle = OpusDecoderJni.createDecoder(16000, 1)
    private val mediaPath: String
        get() = MyApplication.instance.mediaPath

    /** 无存储设备下最近一次保存的录音文件路径，用于「播放刚才的录音」按钮 */
    private var latestSavedRecordPath: String? = null

    private val audioPlayer = AudioPlayer()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.itemDeviceBind.setOnClickListener(blockClick)
        viewBind.itemDeviceInfo.setOnClickListener(blockClick)
        viewBind.itemDeviceConfig.setOnClickListener(blockClick)
        viewBind.itemBasicDeviceInfo.setOnClickListener(blockClick)
        viewBind.itemOtherFeatures.setOnClickListener(blockClick)
        viewBind.tvDeviceReset.setOnClickListener(blockClick)

        viewBind.btnCount.setOnClickListener(blockClick)
        viewBind.btnStore.setOnClickListener(blockClick)

        viewBind.btnTakePhoto.setOnClickListener(blockClick)
        viewBind.btnVideo.setOnClickListener(blockClick)
        viewBind.btnRecord.setOnClickListener(blockClick)
        viewBind.btnPlayLatestRecord.setOnClickListener(blockClick)
        viewBind.btnCustomMessage.setOnClickListener(blockClick)
        viewBind.itemPhotoLibrary.setOnClickListener(blockClick)

        viewBind.itemDevicePreview.setOnClickListener(blockClick)
        viewBind.itemDeviceChat.setOnClickListener(blockClick)
        viewBind.itemDeviceChatNew.setOnClickListener(blockClick)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowDevice?.collect {
                    this::class.simpleName?.let { it1 -> Timber.i("flowDevice=$it") }
                    if (it == null) {
                        viewBind.itemDeviceBind.visibility = View.VISIBLE
                        viewBind.itemDeviceInfo.visibility = View.GONE
                    } else {
                        viewBind.itemDeviceBind.visibility = View.GONE
                        viewBind.itemDeviceInfo.visibility = View.VISIBLE
                        viewBind.tvDeviceName.text = it.name
                    }
                }
            }

            launch {
                deviceManager.flowConnectorStateInfo.collect {
                    this::class.simpleName?.let { it1 ->
                        Timber.i("flowConnectorState=$it")
                    }
                    viewBind.tvDeviceState.setText(it.state.toStringRes())

                    if (it.state == WmConnectState.DISCONNECTED) {
                        viewBind.tvDeviceState.setText("重连")
                        viewBind.tvDeviceState.setOnClickListener {
//                            deviceManager.reconnect()
                        }
                    }
                    viewBind.tvDeviceReset.visibility = View.GONE
                    if (it.state == WmConnectState.BIND_SUCCESS) {
                        // 设备绑定成功后，检查是否为无存储设备
                        getMediaCount()
                        getStore()
                        getDeviceVideoPreviewState()
                        viewBind.tvDeviceReset.visibility = View.VISIBLE
                        withContext(Dispatchers.Main) {
                            delay(100)
                            initNoStorageDeviceStatus()
                        }

                    } else {
                        // 设备未绑定或断开连接时，重置UI状态（显示所有元素，隐藏提示）
//                        resetStorageDeviceUI()
                    }

                    viewBind.layoutContent.setAllChildEnabled(it.state == WmConnectState.BIND_SUCCESS)
                }
            }

            launch {
                deviceManager.flowBattery.collect {
                    this::class.simpleName?.let { it1 -> Timber.i("flowBattery=$it") }
                    if (it == null) {
                        viewBind.batteryView.setBatteryUnknown()
                    } else {
                        viewBind.batteryView.setBatteryStatus(it.isCharge, it.currValue)
                    }
                }
            }


            launch {
                UNIWatchMate.wmApps.appVideoPreview.observeVideoPreviewShootState
                    .subscribe { status: Int ->
                        Log.e(TAG,"observeVideoPreviewShootState:$status")
                        viewBind.btnVideo.isClickable = true
                        if (!viewBind.btnRecord.isClickable){
                            viewBind.btnRecord.isClickable = true
                        }
                        //设备通知
                        if (status == 1) {
                            viewBind.btnVideo.text = getString(R.string.stop_video_recording)
                        } else {
                            viewBind.btnVideo.text = getString(R.string.start_video_recording)
                            getMediaCount()
                        }
                    }
            }

            launch {
                UNIWatchMate.wmApps.appVideoPreview.observeAudioState
                    .subscribe { status: Int ->
                        Log.e(TAG,"observeAudioState:$status")
                        viewBind.btnRecord.isClickable = true
                        if (!viewBind.btnVideo.isClickable){
                            viewBind.btnVideo.isClickable = true
                        }
                        //设备通知
                        if (status == 1) {
                            viewBind.btnRecord.text = getString(R.string.stop_recorded)
                            // 无存储设备：开始新一轮录音，清空上次的 PCM 缓存
                            if (isNoStorageDevice) {
                                noStoragePcmData = ByteArray(0)
                                noStorageRemainingData = ByteArray(0)
                            }
                        } else {
                            viewBind.btnRecord.text = getString(R.string.start_recorded)
                            // 无存储设备：停止录音时把累积的 PCM 保存为 WAV 并提示，并显示「播放刚才的录音」按钮
                            if (this@DeviceFragment.isNoStorageDevice && noStoragePcmData.isNotEmpty()) {
                                val savePath = "$mediaPath/audio_no_storage_${System.currentTimeMillis()}.wav"
                                generateWavFile(noStoragePcmData, File(savePath))
                                ToastUtil.showToast(getString(R.string.record_saved_toast, savePath), true)
                                noStoragePcmData = ByteArray(0)
                                noStorageRemainingData = ByteArray(0)
                                latestSavedRecordPath = savePath
                                viewBind.btnPlayLatestRecord.visibility = View.VISIBLE
                                viewBind.btnPlayLatestRecord.text = getString(R.string.play_latest_record)
                            }
                            getMediaCount()
                        }
                    }
            }
            launch {
                UNIWatchMate.wmApps.appVideoPreview.observeCameraState.subscribe {
                    Log.e(TAG, "observeCameraState:$it")
                    viewBind.btnTakePhoto.isClickable = true
                    when (it) {
                        1 -> {
                            getMediaCount()
                        }

                        2 -> {
                            ToastUtil.showToast("busy")
                        }

                        0 -> {
                            ToastUtil.showToast("fail")
                        }
                    }
                }
            }

            launch {
                // 无存储设备：开始录音后设备会一直发送录音数据，解码为 PCM 并缓存，停止时保存为 WAV
                UNIWatchMate.wmApps.appAIAssistant.observeAudioDataOfNoStorageDevice.collect { audioData ->
                    if (!isNoStorageDevice || audioData.isEmpty()) return@collect
                    LogUtils.eTag(TAG, "收到无存储设备的录音数据 = ${audioData.size}")
                    val combinedData = noStorageRemainingData + audioData
                    var cachePcmData = ByteArray(0)
                    var index = 0
                    val pcmData = ByteArray(noStorageMaxFrameSize * noStorageChannels * noStorageBytesPerSample)
                    while (index + 41 <= combinedData.size) {
                        val frameLength = combinedData[index].toInt() and 0xFF
                        if (frameLength != 40) {
                            Log.e(TAG, "Invalid frame length: $frameLength")
                            index += 1
                            continue
                        }
                        val frame = combinedData.copyOfRange(index + 1, index + 1 + frameLength)
                        val frameSize = OpusDecoderJni.decode(
                            decoder = noStorageOpusHandle,
                            opusData = frame,
                            pcmData = pcmData,
                            frameSize = 40
                        )
                        val actualFrameSize = frameSize * noStorageChannels * noStorageBytesPerSample
                        cachePcmData = cachePcmData.plus(pcmData.copyOfRange(0, actualFrameSize))
                        index += 41
                    }
                    noStoragePcmData = noStoragePcmData.plus(cachePcmData)
                    noStorageRemainingData = if (index < combinedData.size) {
                        LogUtils.e("JNI", "remainingData: ${combinedData.size - index}")
                        combinedData.copyOfRange(index, combinedData.size)
                    } else {
                        ByteArray(0)
                    }
                }
            }
            // 无存储设备拍照后，设备通过 observeDeviceTakePhotoElementCount 下发分片数量，由 SharedAPPPhotoViewModel 接收并保存照片
        }

    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemDeviceBind -> {
                findNavController().navigate(DeviceFragmentDirections.toDeviceBind())
            }

            viewBind.imgDeviceAdd -> {
                findNavController().navigate(DeviceFragmentDirections.toDeviceBind())
            }

            viewBind.itemDeviceInfo -> {
                if (UNIWatchMate.getConnectState() == WmConnectState.BIND_SUCCESS) {
                    DeviceConnectDialogFragment().show(childFragmentManager, null)
                } else {

                    val userInfo = userInfoRepository.flowCurrent.value
                    val currentDevice = deviceManager.flowDevice?.value
                    userInfo?.let { userInfo ->
                        currentDevice?.let {
                            val bindInfo = WmBindInfo(
                                userInfo.id.toString(),
                                userInfo.name,
                                it.address,
                                BindType.CONNECT_BACK,
                                MyApplication.instance.deviceType,
                                it.wmDeviceMode
                            )
                            UNIWatchMate.connectBtDevice(bindInfo)
                        }
                    }
                }
            }

            viewBind.itemDeviceConfig -> {
                findNavController().navigate(DeviceFragmentDirections.toDeviceConfig())
            }

            viewBind.itemBasicDeviceInfo -> {
                findNavController().navigate(DeviceFragmentDirections.toDeviceInfo())
            }

            viewBind.itemOtherFeatures -> {
                findNavController().navigate(DeviceFragmentDirections.toOtherFeatures())
            }

            viewBind.tvDeviceReset -> {
                applicationScope.launchWithLog {
                    deviceManager?.reset{
                        when(it){
                            0 ->{
                                ToastUtil.showToast("UNBIND SUCCESS")
                            }
                            1 ->{
                                ToastUtil.showToast("UNBIND FAIL")
                            }
                            2 ->{
                                ToastUtil.showToast("UNBIND REFUSE")
                            }
                            3 ->{
                                ToastUtil.showToast("UNBIND OTHER ERROR")
                            }
                            -1 ->{
                                ToastUtil.showToast("UNBIND TIME OUT")
                            }
                        }
                    }
                }
            }

            viewBind.itemDevicePreview -> {
                findNavController().navigate(DeviceFragmentDirections.toDevicePreview())
            }

            viewBind.itemDeviceChat -> {
                findNavController().navigate(DeviceFragmentDirections.toAiChat())
            }
            viewBind.itemDeviceChatNew -> {
                findNavController().navigate(DeviceFragmentDirections.toNewAiChat())
            }
            viewBind.btnCount -> {
                getMediaCount()
            }

            viewBind.btnStore -> {
                getStore()
            }

            viewBind.btnTakePhoto -> {
                viewBind.btnTakePhoto.isClickable = false
                UNIWatchMate.wmApps.appVideoPreview.toggleVideoPreviewPicture()
                    .subscribe { res: Int ->
                        //error result
                        viewBind.btnTakePhoto.isClickable = true
                        Log.e(TAG, "toggleVideoPreviewPicture:$res")
                    }
            }

            viewBind.btnVideo -> {
                isOpenVideo = !isOpenVideo
                viewBind.btnVideo.isClickable = false
                UNIWatchMate.wmApps.appVideoPreview.toggleVideoPreviewShoot(isOpenVideo)
                    .subscribe { res: Int ->
                        //error result
                        Log.e(TAG, "toggleVideoPreviewShoot:$res")
                        viewBind.btnVideo.isClickable = true
                        when (res) {
                            0 -> {
                                //success
                            }

                            else -> {
                                //fail
                                isOpenVideo = !isOpenVideo
                            }
                        }
                    }
            }

            viewBind.btnPlayLatestRecord -> {
                val path = latestSavedRecordPath
                if (!path.isNullOrEmpty()) {
                    playbackLatestRecord(path)
                }
            }

            viewBind.btnRecord -> {
                isOpenRecording = !isOpenRecording
                viewBind.btnRecord.isClickable = false
                UNIWatchMate.wmApps.appVideoPreview.toggleAudio(isOpenRecording)
                    .subscribe { res: Int ->
                        //error result
                        Log.e(TAG, "toggleAudio:$res")
                        viewBind.btnRecord.isClickable = true
                        when (res) {
                            0 -> {
                                // 开始录音成功后，按钮改为「停止录音」
                                if (isOpenRecording) {
                                    viewBind.btnRecord.text = getString(R.string.stop_recorded)
                                }
                            }
                            else -> {
                                //fail
                                isOpenRecording = !isOpenRecording
                            }
                        }
                    }
            }

            viewBind.btnCustomMessage -> {
                findNavController().navigate(DeviceFragmentDirections.toCustomMessage())
            }
            viewBind.itemPhotoLibrary -> {
                findNavController().navigate(DeviceFragmentDirections.toPhotoLibrary())
            }
        }
    }


    private fun getMediaCount() {
        UNIWatchMate.getMediaCountInfo().subscribe({ it ->
            Log.e(
                TAG,
                "device Media Count:${GsonUtil.toJson(it)}"
            )
            val mediaCountBean = MediaCountBean(
                it.photo_num,
                it.video_num,
                it.record_num,
                it.music_num
            )
            viewBind.tvCount.text = mediaCountBean.toString()
        }, { error ->
            Log.d(
                TAG,
                "device Media Count:${error.message} / ${error.printStackTrace()}"
            )

        })
    }

    private fun getStore() {
        UNIWatchMate.getDeviceSDInfo().subscribe({ it ->
            Log.d(
                TAG,
                "device SD Capacity:${GsonUtil.toJson(it)}"
            )
            viewBind.tvStore.text = "used/total:${it.remain_memory}/${it.total_memory}"
        }, { error ->
            Log.d(
                TAG,
                "device SD Capacity:${error.message} / ${error.printStackTrace()}"
            )
        })
    }

    private fun getDeviceVideoPreviewState() {
        compositeDisposableGet.dispose() // 清除所有订阅
        compositeDisposableGet = CompositeDisposable()
        compositeDisposableGet.add(
            UNIWatchMate.wmApps.appVideoPreview.getVideoPreviewShootState()
                .subscribe { open: Boolean ->
                    Log.e(TAG, "getVideoPreviewShootState:$open")
                    isOpenVideo = open
                    //设备通知
                    if (open) {
                        viewBind.btnVideo.text = getString(R.string.stop_video_recording)
                    } else {
                        viewBind.btnVideo.text = getString(R.string.start_video_recording)
                        getMediaCount()
                    }
                }
        )
        compositeDisposableGet.add(
            UNIWatchMate.wmApps.appVideoPreview.getAudioState()
                .subscribe { open: Boolean ->
                    Log.e(TAG, "getAudioState:$open")
                    isOpenRecording = open
                    //设备通知
                    if (open) {
                        viewBind.btnRecord.text = getString(R.string.stop_recorded)
                    } else {
                        viewBind.btnRecord.text = getString(R.string.start_recorded)
                        getMediaCount()
                    }
                }
        )
    }

    override fun navToConnectHelp() {
        findNavController().navigate(DeviceFragmentDirections.toConnectHelp())
    }

    override fun navToBgRunSettings() {
        findNavController().navigate(DeviceFragmentDirections.toBgRunSettings())
    }

    /**
     * 初始化无存储设备状态检查
     * 注意：此方法需要在设备绑定成功（BIND_SUCCESS）后才能调用
     * 如果设备无存储（noStorageDevice == 1），则隐藏照片数量和存储相关的行
     */
    private fun initNoStorageDeviceStatus() {
        lifecycleScope.launch {
            try {
                // 获取设备功能支持状态（需要在设备绑定后才能获取）

                // 判断是否为无存储设备：noStorageDevice == 1 表示无存储设备
                val noStorage = UNIWatchMate.getGlassesFunctionSupportState().noStorageDevice == 1
                this@DeviceFragment.isNoStorageDevice = noStorage
                // 根据无存储设备状态控制相关UI的显示/隐藏
                if (noStorage) {
                    // 如果是无存储设备，隐藏照片数量行、存储信息行和底部操作按钮
                    viewBind.layoutMediaCount.visibility = View.GONE
                    viewBind.layoutStorage.visibility = View.GONE
                    viewBind.btnVideo.visibility = View.GONE
                    
                    // 显示无存储设备提示文字
                    viewBind.tvStorageDeviceStatus.visibility = View.VISIBLE
                    viewBind.tvStorageDeviceStatus.text = getString(R.string.no_storage_device)
                } else {
                    // 如果不是无存储设备，显示这些行和底部操作按钮，隐藏「播放刚才的录音」
                    viewBind.layoutMediaCount.visibility = View.VISIBLE
                    viewBind.layoutStorage.visibility = View.VISIBLE
                    viewBind.btnVideo.visibility = View.VISIBLE
                    viewBind.btnPlayLatestRecord.visibility = View.GONE
                    // 隐藏无存储设备提示文字
                    viewBind.tvStorageDeviceStatus.visibility = View.GONE
                }
                
            } catch (e: Exception) {
                // 如果获取功能支持状态失败，记录错误日志，默认显示这些行
                Timber.e("Failed to get function support state: ${e.message}")
                // 发生错误时，默认显示这些行（假设设备有存储）
                resetStorageDeviceUI()
            }
        }
    }

    /**
     * 重置存储设备相关UI状态
     * 当设备未绑定或断开连接时调用，显示所有UI元素，隐藏提示文字
     */
    private fun resetStorageDeviceUI() {
        viewBind.layoutMediaCount.visibility = View.VISIBLE
        viewBind.layoutStorage.visibility = View.VISIBLE
        viewBind.btnVideo.visibility = View.VISIBLE
        viewBind.btnPlayLatestRecord.visibility = View.GONE
        viewBind.tvStorageDeviceStatus.visibility = View.GONE
    }

    /**
     * 播放无存储设备下最近一次保存的录音（仅最新一条）
     */
    private fun playbackLatestRecord(wavePath: String) {
        if (audioPlayer.isPlaying()) {
            audioPlayer.stop()
            return
        }
        audioPlayer.setListener(object : AudioPlayer.AudioPlayerListener {
            override fun onComplete() {
                viewBind.btnPlayLatestRecord.text = getString(R.string.play_latest_record)
            }
            override fun onError(error: String) {
                ToastUtil.showToast(error)
                viewBind.btnPlayLatestRecord.text = getString(R.string.play_latest_record)
            }
        })
        viewBind.btnPlayLatestRecord.text = getString(R.string.playing)
        audioPlayer.playFile(wavePath)
    }

    /**
     * 将 PCM 数据写入为 WAV 文件（无存储设备录音保存用）
     */
    private fun generateWavFile(
        pcmData: ByteArray,
        outputFile: File,
        sampleRate: Int = noStorageSampleRate,
        numChannels: Short = noStorageChannels.toShort(),
        bitsPerSample: Short = 16
    ) {
        val pcmDataSize = pcmData.size
        val byteRate = sampleRate * numChannels.toInt() * (bitsPerSample.toInt() / 8)
        val blockAlign = (numChannels.toInt() * bitsPerSample.toInt() / 8).toShort()

        val riffHeader = "RIFF".toByteArray(Charsets.UTF_8)
        val chunkSize = 4 + (8 + 16 + 8 + pcmDataSize)
        val chunkSizeBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(chunkSize).array()
        val waveHeader = "WAVE".toByteArray(Charsets.UTF_8)
        val fmtHeader = "fmt ".toByteArray(Charsets.UTF_8)
        val fmtSize = 16
        val audioFormat = 1.toShort()
        val numChannelsBytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(numChannels).array()
        val sampleRateBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate).array()
        val byteRateBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(byteRate).array()
        val blockAlignBytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(blockAlign).array()
        val bitsPerSampleBytes = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(bitsPerSample).array()
        val dataHeader = "data".toByteArray(Charsets.UTF_8)
        val dataSizeBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(pcmDataSize).array()

        FileOutputStream(outputFile).use { fos ->
            BufferedOutputStream(fos).use { bos ->
                bos.write(riffHeader)
                bos.write(chunkSizeBytes)
                bos.write(waveHeader)
                bos.write(fmtHeader)
                bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(fmtSize).array())
                bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(audioFormat).array())
                bos.write(numChannelsBytes)
                bos.write(sampleRateBytes)
                bos.write(byteRateBytes)
                bos.write(blockAlignBytes)
                bos.write(bitsPerSampleBytes)
                bos.write(dataHeader)
                bos.write(dataSizeBytes)
                bos.write(pcmData)
            }
        }
    }
}

