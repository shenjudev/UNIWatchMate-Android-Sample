package com.sjbt.sdk.sample

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.apps.WmMusicControlType
import com.base.sdk.entity.apps.WmWeatherTime
import com.base.sdk.entity.settings.WmUnitInfo
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.Utils
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.dialog.CallBack
import com.sjbt.sdk.sample.ui.camera.CameraActivity
import com.sjbt.sdk.sample.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import timber.log.Timber


class MyApplication : Application() {
    val TAG: String = "MyApplication"
    private lateinit var applicationScope: CoroutineScope

    companion object {
        lateinit var instance: MyApplication
            private set
        val mHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        applicationScope = Injector.getApplicationScope()

        uniWatchInit(this)

        observeDeviceState()
        Utils.init(instance)

        UNIWatchMate.observeUniWatchChange().subscribe {
            it.setLogEnable(true)
        }

        UNIWatchMate.wmLog.logI(TAG, "APP onCreate")

        initAllProcess()
    }

    private fun initAllProcess() {
        FormatterUtil.init(Resources.getSystem().configuration.locale)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        FormatterUtil.init(Resources.getSystem().configuration.locale)
    }

    private fun observeDeviceState() {

        UNIWatchMate.observeConnectState.subscribe {

            Timber.e(TAG, it.name)

            when (it) {
                WmConnectState.BT_DISABLE -> {

                }

                WmConnectState.VERIFIED -> {
                    UNIWatchMate.wmApps.appCamera.observeCameraOpenState.subscribe {
                        Timber.e("Device camera status：$it")
                    }
                }

                WmConnectState.CONNECTED -> {

                }
            }
        }

        applicationScope.launch {
            launchWithLog {
                UNIWatchMate.wmApps.appWeather.observeWeather.asFlow().collect {
                    if (it.wmWeatherTime == WmWeatherTime.SEVEN_DAYS) {
                        val result2 = UNIWatchMate?.wmApps?.appWeather?.pushSevenDaysWeather(
                            getTestWeatherdata(WmWeatherTime.SEVEN_DAYS, 32),
                            WmUnitInfo.TemperatureUnit.CELSIUS
                        )?.await()
                        Timber.e("push seven_days weather result = $result2")
                        ToastUtil.showToast(
                            "push seven_days weather test ${
                                if (result2) getString(R.string.tip_success) else getString(
                                    R.string.tip_failed
                                )
                            }"
                        )
                    } else if (it.wmWeatherTime == WmWeatherTime.TODAY) {
                        val result = UNIWatchMate?.wmApps?.appWeather?.pushTodayWeather(
                            getTestWeatherdata(WmWeatherTime.TODAY, 32),
                            WmUnitInfo.TemperatureUnit.CELSIUS
                        )?.await()
                        UNIWatchMate.wmLog.logE(
                            TAG,
                            "push today weather result = $result"
                        )
                        ToastUtil.showToast(
                            "push today weather test ${
                                if (result) getString(R.string.tip_success) else getString(
                                    R.string.tip_failed
                                )
                            }"
                        )
                    }
                }
            }
            launchWithLog {
                UNIWatchMate.wmApps.appCamera.observeCameraOpenState.asFlow().collect {
                    if (it) {//
                        if (ActivityUtils.getTopActivity() != null) {
                            Timber.e("Device camera status：$it")
                            CacheDataHelper.cameraLaunchedByDevice = true
                            CameraActivity.launchActivity(ActivityUtils.getTopActivity())
                        }
                    } else if (ActivityUtils.getTopActivity() is CameraActivity) {
                        ActivityUtils.getTopActivity().finish()
                    }
                }
            }

            launchWithLog {
                UNIWatchMate.wmApps.appFind.observeFindMobile.asFlow().catch {
                    it.message?.let { it1 ->
                        UNIWatchMate.wmLog.logE(
                            TAG,
                            it1
                        )
                    }
                    ToastUtil.showToast(it.toString(), false)
                }.collect {
                    ToastUtil.showToast("FindMobile $it", true)
                    val topActivity = ActivityUtils.getTopActivity()
                    if (topActivity != null && topActivity is BaseActivity) {
                        topActivity.showFindPhoneDialogWithCallback(getString(R.string.ds_find_phone_found),
                            getString(R.string.ds_find_phone_stop), object : CallBack<String> {
                                override fun callBack(o: String) {
                                    applicationScope.launch {
                                        val result =
                                            UNIWatchMate.wmApps.appFind.stopFindMobile().await()
                                        ToastUtil.showToast(
                                            "reply observeFindMobile result: $result",
                                            true
                                        )
                                    }
                                }
                            })
                    }
                }
            }

            launchWithLog {
                UNIWatchMate.wmApps.appFind.observeStopFindMobile.asFlow().onCompletion {
                    Timber.e(
                        "onCompletion"
                    )
                }.catch {
                    it.message?.let { it1 ->
                        UNIWatchMate.wmLog.logE(
                            TAG,
                            it1
                        )
                    }
                    ToastUtil.showToast(it.toString(), false)
                }.collect {
                    val topActivity = ActivityUtils.getTopActivity()
                    if (topActivity != null && topActivity is BaseActivity) {
                        (topActivity as BaseActivity).hideStopFindPhonemDialog()
                    }
                    ToastUtil.showToast("stopFindMobile $it", true)
                }
            }
            launchWithLog {
                UNIWatchMate.wmApps.appMusicControl.observableMusicControl.asFlow().collect {
                    simulateMediaButton(it)
                    Timber.e(
                        "receive music control type= $it"
                    )
                }
            }
        }
    }

    private fun simulateMediaButton(musicType: WmMusicControlType) {
        var keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        when (musicType) {
            WmMusicControlType.PREV_SONG -> {
                keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS
                ToastUtil.showToast("PREV_SONG")
            }

            WmMusicControlType.NEXT_SONG -> {
                keyCode = KeyEvent.KEYCODE_MEDIA_NEXT
                ToastUtil.showToast("NEXT_SONG")
            }

            WmMusicControlType.PLAY -> {
                keyCode = KeyEvent.KEYCODE_MEDIA_PLAY
                ToastUtil.showToast("PLAY")
            }

            WmMusicControlType.PAUSE -> {
                keyCode = KeyEvent.KEYCODE_MEDIA_PAUSE
                ToastUtil.showToast("PAUSE")
            }

            WmMusicControlType.VOLUME_UP -> {
                keyCode = KeyEvent.KEYCODE_VOLUME_UP
                ToastUtil.showToast("VOLUME_UP")
            }

            WmMusicControlType.VOLUME_DOWN -> {
                keyCode = KeyEvent.KEYCODE_VOLUME_DOWN
                ToastUtil.showToast("VOLUME_DOWN")
            }
        }
        sendKeyCode(keyCode)
    }

}