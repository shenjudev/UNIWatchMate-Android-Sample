package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.databinding.FragmentHeartRateConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.dialog.*
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import timber.log.Timber


class HeartRateConfigFragment : BaseFragment(R.layout.fragment_heart_rate_config),
    SelectIntDialogFragment.Listener {

    private val viewBind: FragmentHeartRateConfigBinding by viewBinding()
    private val applicationScope = Injector.getApplicationScope()

    private val deviceManager = Injector.getDeviceManager()
    private val userManager = Injector.getUserInfoRepository()
    private var isLengthMetric: Boolean = true
    private var wmHeartRateAlerts: WmHeartRateAlerts? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        isLengthMetric = !deviceManager.configFeature.getFunctionConfig().isFlagEnabled(FcFunctionConfig.Flag.LENGTH_UNIT)
//        exerciseGoal=WmSportGoal(1,2.0,3.0,4)
    }

    private fun updateUi() {
        wmHeartRateAlerts?.let {
            viewBind.itemAutoHeartRateMeasurementSwitch.getSwitchView().isChecked =
                it.isEnableHrAutoMeasure

            viewBind.itemMaxHeartRate.getTextView().text =
                it.maxHeartRate.toString() + getString(R.string.unit_bmp)
            viewBind.itemHeartRateInterval.text =
                getString(R.string.ds_heart_rate_interva) + ":\n0-${WmHeartRateAlerts.HEART_RATE_INTERVALS[0]}-${WmHeartRateAlerts.HEART_RATE_INTERVALS[1]}" +
                        "-${WmHeartRateAlerts.HEART_RATE_INTERVALS[2]}-${WmHeartRateAlerts.HEART_RATE_INTERVALS[3]}-${WmHeartRateAlerts.HEART_RATE_INTERVALS[4]}"

            viewBind.itemExerciseHeartRateHighAlertSwitch.getSwitchView().isChecked =
                it.exerciseHeartRateAlert.isEnable
            viewBind.itemExerciseHeartRateHighAlert.getTextView().text =
                it.exerciseHeartRateAlert.threshold.toString() + getString(R.string.unit_bmp)
            viewBind.itemExerciseHeartRateHighAlert.isEnabled =
                it.exerciseHeartRateAlert.isEnable

            viewBind.itemQuietHeartRateHighAlertSwitch.getSwitchView().isChecked =
                it.restingHeartRateAlert.isEnable
            viewBind.itemQuietHeartRateHighAlert.getTextView().text =
                it.restingHeartRateAlert.threshold.toString() + getString(R.string.unit_bmp)
            viewBind.itemQuietHeartRateHighAlert.isEnabled = it.restingHeartRateAlert.isEnable
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    viewBind.layoutContent.setAllChildEnabled(it)
                }
            }
            launch {
                UNIWatchMate.wmSettings.settingHeartRate.observeChange().asFlow().collect {
                    wmHeartRateAlerts = it
                    updateUi()
                }
            }
            launch {
                UNIWatchMate.wmSettings.settingHeartRate.get().toFlowable().asFlow().collect {
                    wmHeartRateAlerts = it
                    Timber.d("get $it")
                    updateUi()
                }
            }
        }

        viewBind.itemMaxHeartRate.setOnClickListener {
            wmHeartRateAlerts?.let {
                showHeartRateDialog(it.maxHeartRate, 10, 22, DIALOG_MAX_HEART_RATE)
            }
        }
        viewBind.itemExerciseHeartRateHighAlert.setOnClickListener {
            wmHeartRateAlerts?.let {
                showHeartRateDialog(
                    it.exerciseHeartRateAlert.threshold,
                    10,
                    15,
                    DIALOG_EXERCISE_HEART_RATE_HIGH_ALERT
                )
            }
        }
        viewBind.itemQuietHeartRateHighAlert.setOnClickListener {
            wmHeartRateAlerts?.let {
                showHeartRateDialog(
                    it.restingHeartRateAlert.threshold,
                    10,
                    15,
                    DIALOG_QUIET_HEART_RATE_HIGH_ALERT
                )
            }
        }
        viewBind.itemAutoHeartRateMeasurementSwitch.getSwitchView()
            .setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    wmHeartRateAlerts?.let {
                        it.isEnableHrAutoMeasure = isChecked
                        updateUi()
                    }
                }
            }

        viewBind.itemExerciseHeartRateHighAlertSwitch.getSwitchView()
            .setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    wmHeartRateAlerts?.let {
                        if (isChecked) {
                            it.exerciseHeartRateAlert.threshold =
                                WmHeartRateAlerts.THRESHOLDS[1]
                        } else {
                            it.exerciseHeartRateAlert.threshold = 0
                        }
                        it.exerciseHeartRateAlert.isEnable = isChecked
                        it.save()
                        updateUi()
                    }
                }
            }
        viewBind.itemQuietHeartRateHighAlertSwitch.getSwitchView()
            .setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    wmHeartRateAlerts?.let {
                        if (isChecked) {
                            it.restingHeartRateAlert.threshold =
                                WmHeartRateAlerts.THRESHOLDS[1]
                        } else {
                            it.restingHeartRateAlert.threshold = 0
                        }
                        it.restingHeartRateAlert.isEnable = isChecked
                        it.save()
                        updateUi()
                    }
                }
            }
    }

    private fun WmHeartRateAlerts.save() {
        applicationScope.launchWithLog {
            UNIWatchMate.wmSettings.settingHeartRate.set(this@save).await()
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_MAX_HEART_RATE == tag) {
            wmHeartRateAlerts?.let {
                it.maxHeartRate = selectValue
                it.refreshIntervals()
                it.save()
            }
            updateUi()
        } else if (DIALOG_EXERCISE_HEART_RATE_HIGH_ALERT == tag) {
            wmHeartRateAlerts?.let {
                it.exerciseHeartRateAlert.threshold = selectValue
                it.save()
            }
            updateUi()
        } else if (DIALOG_QUIET_HEART_RATE_HIGH_ALERT == tag) {
            wmHeartRateAlerts?.let {
                it.restingHeartRateAlert.threshold = selectValue
                it.save()
            }
            updateUi()
        }
    }

}



