package com.sjbt.sdk.sample.ui.device.alarm

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.AlarmRepeatOption
import com.base.sdk.entity.apps.WmAlarm
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.github.kilnn.wheellayout.WheelIntConfig
import com.github.kilnn.wheellayout.WheelIntFormatter
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentAlarmDetailBinding
import com.sjbt.sdk.sample.utils.FormatterUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import java.util.*

class AlarmDetailFragment : BaseFragment(R.layout.fragment_alarm_detail),
    AlarmLabelDialogFragment.Listener, AlarmRepeatDialogFragment.Listener {

    private val viewBind: FragmentAlarmDetailBinding by viewBinding()
    private val viewModel: AlarmViewModel by viewModels({requireParentFragment()})
    private val args: AlarmDetailFragmentArgs by navArgs()
    private val formatter = FormatterUtil.get02dWheelIntFormatter()
    private val calendar = Calendar.getInstance()
    private val is24HourFormat by lazy { AlarmHelper.is24HourFormat(requireContext()) }

    private lateinit var alarm: WmAlarm

    /**
     * Is edit alarm or add a new alarm
     */
    private var isEditMode: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is AlarmEvent.AlarmInserted  -> {
                            promptProgress.dismiss()
                            findNavController().navigateUp()
                        }
                        is AlarmEvent.AlarmMoved -> {
                            promptProgress.dismiss()
                            findNavController().navigateUp()
                        }
                        is AlarmEvent.AlarmRemoved -> {
                            promptProgress.dismiss()
                            findNavController().navigateUp()
                        }
                        is AlarmEvent.AlarmFial -> {
                            promptProgress.dismiss()
                        }

                        else -> {}
                    }
                }
            }
        }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    findNavController().navigateUp()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)

        val alarms = viewModel.state.requestAlarms()
        if (alarms != null && args.position >= 0 && args.position < alarms.size) {
            //Edit Mode
            alarm = AlarmHelper.newAlarm(alarms[args.position])
            isEditMode = true
        } else {
            //Add Mode
            alarm = WmAlarm(
                getString(R.string.ds_alarm_label_default),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                AlarmHelper.getDefaultRepeatOption()
            )
            alarm.isOn = true
            isEditMode = false
        }

        if (isEditMode) {
            (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_alarm_edit)
        } else {
            (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_alarm_add)
        }

        //Add mode does not display delete button
        viewBind.btnDelete.isVisible = isEditMode

        if (is24HourFormat) {
            viewBind.wheelAmPm.visibility = View.GONE
            viewBind.wheelHour.setConfig(
                WheelIntConfig(
                    0,
                    23,
                    true,
                    getString(R.string.unit_hour),
                    formatter
                )
            )
        } else {
            viewBind.wheelAmPm.setConfig(
                WheelIntConfig(
                    0,
                    1,
                    false,
                    null,
                    object : WheelIntFormatter {
                        override fun format(index: Int, value: Int): String {
                            return if (index == 0) {
                                requireContext().getString(R.string.ds_alarm_am)
                            } else {
                                requireContext().getString(R.string.ds_alarm_pm)
                            }
                        }
                    })
            )
            viewBind.wheelHour.setConfig(
                WheelIntConfig(
                    1,
                    12,
                    true,
                    getString(R.string.unit_hour),
                    formatter
                )
            )
        }
        viewBind.wheelMinute.setConfig(
            WheelIntConfig(
                0,
                59,
                true,
                getString(R.string.unit_minute),
                formatter
            )
        )

        viewBind.btnSave.clickTrigger(block = blockClick)
        viewBind.btnDelete.clickTrigger(block = blockClick)
        viewBind.itemRepeat.clickTrigger(block = blockClick)
        viewBind.itemLabel.clickTrigger(block = blockClick)
        updateUI()
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.btnSave -> {
                var hour = viewBind.wheelHour.getValue()
                if (!is24HourFormat) { //12 Hour format
                    if (viewBind.wheelAmPm.getValue() == 0) { //AM
                        if (hour == 12) {
                            hour = 0
                        }
                    } else {
                        if (hour < 12) {
                            hour += 12
                        }
                    }
                }

                alarm.hour = hour
                alarm.minute = viewBind.wheelMinute.getValue()
                UNIWatchMate.wmLog.logI("TAG", "isEditMode")
                promptProgress.showProgress("")
                if (isEditMode) {
                    viewModel.modifyAlarm(args.position, alarm)
                } else {
                    alarm.isOn = true
                    viewModel.addAlarm(alarm)
                }
                UNIWatchMate.wmLog.logI("TAG", "edit end")
            }

            viewBind.btnDelete -> {
                promptProgress.showProgress("")
                viewModel.deleteAlarm(args.position)
            }

            viewBind.itemRepeat -> {
                AlarmRepeatDialogFragment().show(childFragmentManager, null)
            }

            viewBind.itemLabel -> {
                AlarmLabelDialogFragment().show(childFragmentManager, null)
            }
        }
    }

    private fun updateUI() {
        var hour: Int = alarm.hour
        val minute: Int = alarm.minute
        if (hour == 24 && minute == 0) {
            if (is24HourFormat) {
                viewBind.wheelHour.setValue(23)
                viewBind.wheelMinute.setValue(59)
            } else {
                viewBind.wheelAmPm.setValue(0)
                viewBind.wheelHour.setValue(12)
                viewBind.wheelMinute.setValue(0)
            }
        } else {
            if (is24HourFormat) {
                viewBind.wheelHour.setValue(hour)
                viewBind.wheelMinute.setValue(minute)
            } else {
                if (hour < 12) { //AM
                    viewBind.wheelAmPm.setValue(0)
                    if (hour == 0) {
                        hour = 12
                    }
                } else {
                    viewBind.wheelAmPm.setValue(1)
                    if (hour > 12) {
                        hour -= 12
                    }
                }
                viewBind.wheelHour.setValue(hour)
                viewBind.wheelMinute.setValue(minute)
            }
        }
        viewBind.itemLabel.getTextView().text = getAlarmLabel()
        viewBind.itemRepeat.getTextView().text =
            context?.let { AlarmHelper.repeatToSimpleStr(alarm.repeatOptions, it) }
    }

    private fun getAlarmLabel(): String {
        return if (alarm.alarmName.isNullOrEmpty()) {
            getString(R.string.ds_alarm_label_default)
        } else {
            alarm.alarmName
        }
        return ""
    }

    override fun dialogGetAlarmLabel(): String? {
        return getAlarmLabel()
    }

    override fun dialogSetAlarmLabel(label: String?) {
        label?.let {
            alarm.alarmName = it
            viewBind.itemLabel.getTextView().text = it
        }
    }

    override fun dialogGetAlarmRepeat(): Set<AlarmRepeatOption> {
        return alarm.repeatOptions
    }

    override fun dialogSetAlarmRepeat(options: Set<AlarmRepeatOption>) {
        alarm.repeatOptions = options
        viewBind.itemRepeat.getTextView().text =
            context?.let { AlarmHelper.repeatToSimpleStr(alarm.repeatOptions, it) }
    }

}
