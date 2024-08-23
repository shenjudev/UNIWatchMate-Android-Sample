package com.sjbt.sdk.sample.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.StringRes
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.BindType
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.apps.WmNotification
import com.base.sdk.entity.apps.WmWeatherTime
import com.base.sdk.entity.settings.WmUnitInfo
import com.blankj.utilcode.util.TimeUtils
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.databinding.FragmentDeviceBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.di.internal.CoroutinesInstance.applicationScope
import com.sjbt.sdk.sample.dialog.WeatherCodeTestDialog
import com.sjbt.sdk.sample.ui.bind.DeviceConnectDialogFragment
import com.sjbt.sdk.sample.ui.camera.CameraActivity
import com.sjbt.sdk.sample.ui.fileTrans.FileTransferActivity
import com.sjbt.sdk.sample.ui.muslim.MuslimWorshipActivity
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.getTestWeatherdata
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewLifecycleScope
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.await
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        requireActivity().addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.menu_device_add, menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                if (menuItem.itemId == R.id.menu_add_device) {
//                    findNavController().navigate(DeviceFragmentDirections.toDeviceBind())
//                    return true
//                }
//                return false
//            }
//        }, viewLifecycleOwner)

        viewBind.itemDeviceBind.setOnClickListener(blockClick)
//      viewBind.imgDeviceAdd.setOnClickListener(blockClick)
        viewBind.itemDeviceInfo.setOnClickListener(blockClick)
        viewBind.itemDeviceConfig.setOnClickListener(blockClick)
        viewBind.itemQrCodes.setOnClickListener(blockClick)
        viewBind.itemAlarm.setOnClickListener(blockClick)
        viewBind.itemContacts.setOnClickListener(blockClick)
        viewBind.itemTestSendNotification.setOnClickListener(blockClick)
        viewBind.itemSportPush.setOnClickListener(blockClick)
        viewBind.itemDial.setOnClickListener(blockClick)
        viewBind.itemBasicDeviceInfo.setOnClickListener(blockClick)
        viewBind.itemCamera.setOnClickListener(blockClick)
        viewBind.itemTransferFile.setOnClickListener(blockClick)
        viewBind.itemTestWeather.setOnClickListener(blockClick)
        viewBind.itemPushDateTime.setOnClickListener(blockClick)
        viewBind.itemSyncTestFile.setOnClickListener(blockClick)
        viewBind.itemOtherFeatures.setOnClickListener(blockClick)
        viewBind.itemWidget.setOnClickListener(blockClick)
        viewBind.itemFeature.setOnClickListener(blockClick)
        viewBind.itemMuslim.setOnClickListener(blockClick)
        viewBind.tvDeviceReset.setOnClickListener(blockClick)

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
                deviceManager.flowConnectorState.collect {
                    this::class.simpleName?.let { it1 ->
                        Timber.i("flowConnectorState=$it")
                    }
                    viewBind.tvDeviceState.setText(it.toStringRes())

                    if (it == WmConnectState.DISCONNECTED) {
                        viewBind.tvDeviceState.setText("重连")
                        viewBind.tvDeviceState.setOnClickListener {
//                            deviceManager.reconnect()
                        }
                    }

                    viewBind.layoutContent.setAllChildEnabled(it == WmConnectState.BIND_SUCCESS)
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
                                    "OSW-802N",
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

            viewBind.itemDeviceConfig -> {
            }

            viewBind.itemOtherFeatures -> {
                findNavController().navigate(DeviceFragmentDirections.toOtherFeatures())
            }

            viewBind.itemAlarm -> {
                findNavController().navigate(DeviceFragmentDirections.toAlarm())
            }

            viewBind.itemTransferFile -> {
                activity?.let {
                    PermissionHelper.requestAppStorageAudio(this@DeviceFragment) { permission ->
                        if (permission) {
                            FileTransferActivity.launchActivity(it)
                        }
                    }
                }
            }

            viewBind.itemContacts -> {
                findNavController().navigate(DeviceFragmentDirections.toPageContacts())
            }

            viewBind.itemTestSendNotification -> {
                applicationScope.launchWithLog {
                    UNIWatchMate.wmApps.appNotification.sendNotification(
                            WmNotification(
                                    "test.notification",
                                    "title_notification${TimeUtils.millis2String(System.currentTimeMillis())}",
                                    "content_notification${TimeUtils.millis2String(System.currentTimeMillis())}",
                                    "sub_content_notification"
                            )
                    ).toFlowable().asFlow().collect {
                        Timber.tag("appNotification").i("appNotification result=$it")
                        ToastUtil.showToast("TestSendNotification $it")
                    }

                    UNIWatchMate.wmApps.appNotification.getNotificationSetting().subscribe({
                        UNIWatchMate.wmLog.logE(TAG, "获取通知消息配置:${it}")
                    }, {
                        UNIWatchMate.wmLog.logE(TAG, "通知消息配置异常:${it.message}")
                    })
                }
            }

            viewBind.itemCamera -> {
                PermissionHelper.requestAppCameraAndStoreage(this@DeviceFragment) {
                    if (it) {
                        CacheDataHelper.cameraLaunchedBySelf = true
                        CameraActivity.launchActivity(activity)
                    }
                }
            }

            viewBind.itemDial -> {
                findNavController().navigate(DeviceFragmentDirections.toDialHomePage())
            }

            viewBind.itemSportPush -> {
                findNavController().navigate(DeviceFragmentDirections.toSportHomePage())
            }

            viewBind.itemPushDateTime -> {
                applicationScope.launchWithLog {
                    val result = UNIWatchMate.wmApps.appDateTime.setDateTime(null).await()
                    Timber.tag(TAG).i("settingDateTime result=${result}")
                    ToastUtil.showToast("push date time result = $result")
                }
            }

            viewBind.itemTestWeather -> {
                showChooseWeatherDialog()
            }

            viewBind.itemSyncTestFile -> {
                UNIWatchMate.wmSyncTestFile.startSync(0).subscribe {
                    if (it.sendingFile != null) {
                        ToastUtil.showToast("startSync success:${it.sendingFile?.absoluteFile}")
                    }
                    UNIWatchMate.wmLog.logE("SYNC_FILE", "startSync state=$it")
                }
            }

            viewBind.itemWidget -> {
                findNavController().navigate(DeviceFragmentDirections.toWidget())
            }

            viewBind.itemFeature -> {
                findNavController().navigate(DeviceFragmentDirections.toFeature())
            }

            viewBind.itemMuslim -> {
                val inttent = Intent(activity,MuslimWorshipActivity::class.java)
                startActivity(inttent)
            }

            viewBind.tvDeviceReset -> {
                if(CacheDataHelper.deviceConnectState == WmConnectState.BIND_SUCCESS){
                    applicationScope.launchWithLog {
                        deviceManager?.reset()
                    }
                }else{
                    applicationScope.launchWithLog {
                        deviceManager?.delDevice()
                    }
                }
            }
        }
    }

    private fun getNavigation() {
        UNIWatchMate.wmApps.appNavigation.openCloseNavigation(true).subscribe(object :
                SingleObserver<Boolean> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "onSuccess: $e")
            }

            override fun onSuccess(t: Boolean) {
                Log.e(TAG, "onSuccess: $t")
            }

        })
    }

    private fun showChooseWeatherDialog() {
        context?.let {
            WeatherCodeTestDialog(
                    it
            ) { weatherCode ->
                applicationScope.launchWithLog {
                    val result = UNIWatchMate.wmApps.appWeather.pushTodayWeather(
                            getTestWeatherdata(WmWeatherTime.TODAY, weatherCode.code),
                            WmUnitInfo.TemperatureUnit.CELSIUS
                    ).await()
                    Timber.e("push today weather result = $result")
                    ToastUtil.showToast(
                            "push today weather test ${
                                if (result) getString(R.string.tip_success) else getString(
                                        R.string.tip_failed
                                )
                            }"
                    )
                    val result2 = UNIWatchMate.wmApps.appWeather.pushSevenDaysWeather(
                            getTestWeatherdata(WmWeatherTime.SEVEN_DAYS, weatherCode.code),
                            WmUnitInfo.TemperatureUnit.CELSIUS
                    ).await()
                    Timber.e("push seven_days weather result = $result2")
                    ToastUtil.showToast(
                            "push seven_days weather test ${
                                if (result2) getString(R.string.tip_success) else getString(
                                        R.string.tip_failed
                                )
                            }"
                    )
                }
            }.show()
        }
    }

    override fun navToConnectHelp() {
        findNavController().navigate(DeviceFragmentDirections.toConnectHelp())
    }

    override fun navToBgRunSettings() {
        findNavController().navigate(DeviceFragmentDirections.toBgRunSettings())
    }


}

