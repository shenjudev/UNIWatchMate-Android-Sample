package com.sjbt.sdk.sample.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.BindType
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.apps.WmConnectState
import com.blankj.utilcode.util.LogUtils
import com.lensmoo.business.ui.device.OtaDemo2Activity
import com.lensmoo.business.ui.device.OtaDemoActivity
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.di.internal.CoroutinesInstance.applicationScope
import com.sjbt.sdk.sample.entity.MediaCountBean
import com.sjbt.sdk.sample.ui.bind.DeviceConnectDialogFragment
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

    private var compositeDisposableGet = CompositeDisposable()
    private var isOpenRecording = false
    private var isOpenVideo = false


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
        viewBind.btnCustomMessage.setOnClickListener(blockClick)
        viewBind.itemPhotoLibrary.setOnClickListener(blockClick)

        viewBind.itemDevicePreview.setOnClickListener(blockClick)
        viewBind.itemDeviceChat.setOnClickListener(blockClick)
        viewBind.itemDeviceChatNew.setOnClickListener(blockClick)
        viewBind.itemOtaSdkDemo.setOnClickListener(blockClick)
        viewBind.itemOtaSdkDemo2.setOnClickListener(blockClick)

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
                        } else {
                            viewBind.btnRecord.text = getString(R.string.start_recorded)
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
                //无存储设备 开始录音后，设备会一直给APP发送录音数据
                UNIWatchMate.wmApps.appAIAssistant.observeAudioDataOfNoStorageDevice.collect{
                    LogUtils.eTag(tag,"收到无存储设备的录音数据 = ${it.size}")
                    //模拟接收图片耗时，接收完照片后，退出传输模式


                }
            }
            launch {
                //无存储设备拍照后，设备会主动给APP发送 该照片的分片数量，APP拿到分片数量后，依次向设备获取每一分片的数据（代码可参考PhotoLibraryViewModel）
                UNIWatchMate.wmApps.appPhotoLibrary.observeDeviceTakePhotoElementCount.asFlow()
                    .collect { count ->
                        LogUtils.e("收到无存储设备的拍照后的照片分片数量= $count")
                        withContext(Dispatchers.Main) {
                            delay(3000)
                            UNIWatchMate.wmApps.appPhotoLibrary.letDeviceEndSendPhotoState()
                                .toObservable().asFlow().catch {
                                    withContext(Dispatchers.Main) {

                                    }
                                }.collect { result ->
                                    LogUtils.d("End send photo state result: $result")
                                    // 可以在此处添加结束发送状态的事件通知
                                    // 例如：_events.emit(PhotoLibraryEvent.PhotoSendStateEnded(result == 0))
                                }
                        }
                    }
            }
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
                                //success
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
            // 点击 OTA SDK Demo 入口，跳转到 OtaDemoActivity
            viewBind.itemOtaSdkDemo -> {
                val intent = Intent(requireContext(), OtaDemoActivity::class.java)
                startActivity(intent)
            }
            viewBind.itemOtaSdkDemo2 -> {
                val intent = Intent(requireContext(), OtaDemo2Activity::class.java)
                startActivity(intent)
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
                val isNoStorageDevice = UNIWatchMate.getGlassesFunctionSupportState().noStorageDevice == 1
                
                // 根据无存储设备状态控制相关UI的显示/隐藏
                if (isNoStorageDevice) {
                    // 如果是无存储设备，隐藏照片数量行、存储信息行和底部操作按钮
                    viewBind.layoutMediaCount.visibility = View.GONE
                    viewBind.layoutStorage.visibility = View.GONE
                    viewBind.btnVideo.visibility = View.GONE
                    
                    // 显示无存储设备提示文字
                    viewBind.tvStorageDeviceStatus.visibility = View.VISIBLE
                    viewBind.tvStorageDeviceStatus.text = getString(R.string.no_storage_device)
                } else {
                    // 如果不是无存储设备，显示这些行和底部操作按钮
                    viewBind.layoutMediaCount.visibility = View.VISIBLE
                    viewBind.layoutStorage.visibility = View.VISIBLE
                    viewBind.btnVideo.visibility = View.VISIBLE
                    
                    // 隐藏无存储设备提示文字
                    viewBind.tvStorageDeviceStatus.visibility = View.GONE
                }
                
            } catch (e: Exception) {
                // 如果获取功能支持状态失败，记录错误日志，默认显示这些行
                Timber.e("DeviceFragment", "Failed to get function support state: ${e.message}")
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
        viewBind.tvStorageDeviceStatus.visibility = View.GONE
    }


}

