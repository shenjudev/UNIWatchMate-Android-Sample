package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.settings.WmSleepSettings
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.databinding.FragmentSleepConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.dialog.*
import com.sjbt.sdk.sample.utils.*
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

class SleepConfigFragment : BaseFragment(R.layout.fragment_sleep_config),
    CompoundButton.OnCheckedChangeListener, TimePickerDialogFragment.Listener {

    private val viewBind: FragmentSleepConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: WmSleepSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    viewBind.layoutContent.setAllChildEnabled(it)
                    updateUI()
                }
            }
            launch {
                UNIWatchMate.wmSettings.settingSleepSettings.observeChange().asFlow().collect {
                    config = it
                    updateUI()
                }
            }
            launch {
                UNIWatchMate.wmSettings.settingSleepSettings.get().toFlowable().asFlow().collect {
                    config = it
                    updateUI()
                }
            }
        }

        viewBind.itemSleepEnable.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemStartTime.setOnClickListener(blockClick)
        viewBind.itemEndTime.setOnClickListener(blockClick)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemSleepEnable.getSwitchView()) {
                config?.let {
                    it.open = isChecked
                    it.saveConfig()
                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStartTime -> {
                config?.let {
                    showStartTimeDialog(it.startHour * 60 + it.startMinute)
                }
            }

            viewBind.itemEndTime -> {
                config?.let {
                    showEndTimeDialog(it.endHour * 60 + it.endMinute)
                }
            }
        }
    }

    override fun onDialogTimePicker(tag: String?, timeMinute: Int) {
        if (DIALOG_START_TIME == tag) {
            config!!.startMinute = timeMinute % 60
            config!!.startHour = timeMinute / 60
        } else if (DIALOG_END_TIME == tag) {
            config!!.endMinute = timeMinute % 60
            config!!.endHour = timeMinute / 60
        }
        config?.saveConfig()
    }

    private fun WmSleepSettings.saveConfig() {
        applicationScope.launchWithLog {
            UNIWatchMate.wmSettings.settingSleepSettings.set(config!!).await()
        }
        updateUI()
    }

    private fun updateUI() {
        config?.let {
            viewBind.itemSleepEnable.getSwitchView().isChecked = it.open
            viewBind.itemStartTime.getTextView().text =
                FormatterUtil.hmm(it.startHour, it.startMinute)
            viewBind.itemEndTime.getTextView().text = FormatterUtil.hmm(it.endHour, it.endMinute)
        }

    }

}