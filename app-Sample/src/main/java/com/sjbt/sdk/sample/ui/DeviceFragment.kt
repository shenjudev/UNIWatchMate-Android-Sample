package com.sjbt.sdk.sample.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.BindType
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.apps.WmConnectState
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
import kotlinx.coroutines.launch
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
                        getMediaCount()
                        getStore()
                        getDeviceVideoPreviewState()
                        viewBind.tvDeviceReset.visibility = View.VISIBLE
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


}

