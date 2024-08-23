package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.common.WmNoDisturb
import com.base.sdk.entity.common.WmTimeFrequency
import com.base.sdk.entity.common.WmTimeRange
import com.base.sdk.entity.settings.WmSedentaryReminder
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.databinding.FragmentSedentaryConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.dialog.DIALOG_END_TIME
import com.sjbt.sdk.sample.dialog.DIALOG_END_TIME_NDLB
import com.sjbt.sdk.sample.dialog.DIALOG_INTERVAL_TIME
import com.sjbt.sdk.sample.dialog.DIALOG_START_TIME
import com.sjbt.sdk.sample.dialog.DIALOG_START_TIME_NDLB
import com.sjbt.sdk.sample.dialog.SelectIntDialogFragment
import com.sjbt.sdk.sample.dialog.TimePickerDialogFragment
import com.sjbt.sdk.sample.dialog.showEndTimeDialog
import com.sjbt.sdk.sample.dialog.showIntervalDialog
import com.sjbt.sdk.sample.dialog.showStartTimeDialog
import com.sjbt.sdk.sample.utils.FormatterUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

class SedentaryConfigFragment : BaseFragment(R.layout.fragment_sedentary_config),
    CompoundButton.OnCheckedChangeListener,
    TimePickerDialogFragment.Listener, SelectIntDialogFragment.Listener {

    private val viewBind: FragmentSedentaryConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: WmSedentaryReminder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        config = WmSedentaryReminder(
            true, WmTimeRange(1, 21, 12, 24), WmTimeFrequency.EVERY_30_MINUTES,
            WmNoDisturb()
        )
        updateUI()
        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                  viewBind.layoutContent.setAllChildEnabled(it)
                }
            }
            launch {
                UNIWatchMate.wmSettings.settingSedentaryReminder.observeChange().asFlow().collect {
                    config = it
                    updateUI()
                }
            }
            launch {
                UNIWatchMate.wmSettings.settingSedentaryReminder.get().toFlowable().asFlow()
                    .collect {
                        config = it
                        updateUI()
                    }
            }
        }

        viewBind.itemIsEnabled.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemStartTime.clickTrigger(block = blockClick)
        viewBind.itemEndTime.clickTrigger(block = blockClick)
        viewBind.itemIntervalTime.clickTrigger(block = blockClick)

        viewBind.itemNdlbEnabled.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemNdlbStartTime.clickTrigger(block = blockClick)
        viewBind.itemNdlbEndTime.clickTrigger(block = blockClick)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemIsEnabled.getSwitchView()) {
                config?.let {
                    it.isEnabled = isChecked
                    it.saveConfig()
                }
            } else if (buttonView == viewBind.itemNdlbEnabled.getSwitchView()) {
                config?.let {
                    it.noDisturbLunchBreak.isEnabled = isChecked
                    it.saveConfig()
                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStartTime -> {
                config?.apply {
                    showStartTimeDialog(timeRange.startHour * 60 + timeRange.startMinute)
                }
            }

            viewBind.itemEndTime -> {
                config?.apply {
                    showEndTimeDialog(timeRange.endHour * 60 + timeRange.endMinute)
                }
            }

            viewBind.itemIntervalTime -> {
                config?.apply {
                    showIntervalDialog(timeFrequency2Minute(frequency), 30, 90)
                }
            }

            viewBind.itemNdlbStartTime -> {
                config?.apply {
                    showStartTimeDialog(
                        noDisturbLunchBreak.timeRange.startHour * 60 + noDisturbLunchBreak.timeRange.startMinute,
                        DIALOG_START_TIME_NDLB
                    )
                }
            }

            viewBind.itemNdlbEndTime -> {
                config?.apply {
                    showEndTimeDialog(
                        noDisturbLunchBreak.timeRange.endHour * 60 + noDisturbLunchBreak.timeRange.endMinute,
                        DIALOG_END_TIME_NDLB
                    )
                }
            }
        }
    }

    override fun onDialogTimePicker(tag: String?, timeMinute: Int) {
        if (DIALOG_START_TIME == tag) {
            config?.let {
                it.timeRange.startHour = timeMinute / 60
                it.timeRange.startMinute = timeMinute % 60
                it.saveConfig()
                updateUI()
            }
        } else if (DIALOG_END_TIME == tag) {
            config?.let {
                it.timeRange.endHour = timeMinute / 60
                it.timeRange.endMinute = timeMinute % 60
                it.saveConfig()
                updateUI()
            }
        } else if (DIALOG_START_TIME_NDLB == tag) {
            config?.let {
                it.noDisturbLunchBreak.timeRange.startHour = timeMinute / 60
                it.noDisturbLunchBreak.timeRange.startMinute = timeMinute % 60
                it.saveConfig()
                updateUI()
            }
        } else if (DIALOG_END_TIME_NDLB == tag) {
            config?.let {
                it.noDisturbLunchBreak.timeRange.endHour = timeMinute / 60
                it.noDisturbLunchBreak.timeRange.endMinute = timeMinute % 60
                it.saveConfig()
                updateUI()
            }
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_INTERVAL_TIME == tag) {
            config?.let {
                it.frequency = minute2TimeFrequency(selectValue)
                it.saveConfig()
                updateUI()
            }
        }
    }

    private fun WmSedentaryReminder.saveConfig() {
        applicationScope.launchWithLog {
            UNIWatchMate.wmSettings.settingSedentaryReminder.set(this@saveConfig).await()
        }
        updateUI()
    }

    private fun updateUI() {
        config?.let {
            viewBind.itemIsEnabled.getSwitchView().isChecked = it.isEnabled
            viewBind.itemStartTime.isEnabled = it.isEnabled
            viewBind.itemEndTime.isEnabled = it.isEnabled
            viewBind.itemIntervalTime.isEnabled = it.isEnabled
            viewBind.itemStartTime.getTextView().text =
                FormatterUtil.minute2Hmm(it.timeRange.startHour * 60 + it.timeRange.startMinute)
            viewBind.itemEndTime.getTextView().text =
                FormatterUtil.minute2Hmm(it.timeRange.endHour * 60 + it.timeRange.endMinute)
            viewBind.itemIntervalTime.getTextView().text =
                getString(R.string.unit_minute_param, timeFrequency2Minute(it.frequency))

            viewBind.itemNdlbStartTime.getTextView().text =
                FormatterUtil.minute2Hmm(it.noDisturbLunchBreak.timeRange.startHour * 60 + it.noDisturbLunchBreak.timeRange.startMinute)
            viewBind.itemNdlbEndTime.getTextView().text =
                FormatterUtil.minute2Hmm(it.noDisturbLunchBreak.timeRange.endHour * 60 + it.noDisturbLunchBreak.timeRange.endMinute)
            viewBind.itemNdlbEnabled.getSwitchView().isChecked = it.noDisturbLunchBreak.isEnabled
            viewBind.itemNdlbStartTime.isEnabled = it.noDisturbLunchBreak.isEnabled
            viewBind.itemNdlbEndTime.isEnabled = it.noDisturbLunchBreak.isEnabled

        }

    }

    private fun timeFrequency2Minute(frequency: WmTimeFrequency): Int {
        return when (frequency) {
            WmTimeFrequency.EVERY_30_MINUTES ->
                30

            WmTimeFrequency.EVERY_1_HOUR ->
                60

            WmTimeFrequency.EVERY_1_HOUR_30_MINUTES ->
                90
        }
    }

    private fun minute2TimeFrequency(mintes: Int): WmTimeFrequency {
        return when (mintes) {
            30 ->
                WmTimeFrequency.EVERY_30_MINUTES

            60 ->
                WmTimeFrequency.EVERY_1_HOUR

            90 -> WmTimeFrequency.EVERY_1_HOUR_30_MINUTES
            else -> WmTimeFrequency.EVERY_30_MINUTES
        }
    }

}