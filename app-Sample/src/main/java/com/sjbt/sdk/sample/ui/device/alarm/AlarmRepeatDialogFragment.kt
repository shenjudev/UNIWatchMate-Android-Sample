package com.sjbt.sdk.sample.ui.device.alarm

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.base.sdk.entity.apps.AlarmRepeatOption
import com.base.sdk.entity.apps.WmAlarm
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjbt.sdk.sample.R

class AlarmRepeatDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = arrayOf(
            getString(R.string.ds_alarm_repeat_00),
            getString(R.string.ds_alarm_repeat_01),
            getString(R.string.ds_alarm_repeat_02),
            getString(R.string.ds_alarm_repeat_03),
            getString(R.string.ds_alarm_repeat_04),
            getString(R.string.ds_alarm_repeat_05),
            getString(R.string.ds_alarm_repeat_06),
        )
        var repeat = (parentFragment as? Listener)?.dialogGetAlarmRepeat()?:AlarmHelper.getDefaultRepeatOption()

        val checkedItems = BooleanArray(items.size) { index ->
            AlarmHelper.repeatToBoolean(index,repeat)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.ds_alarm_repeat)
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                repeat = AlarmHelper.booleanItems2Options(checkedItems)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                (parentFragment as? Listener)?.dialogSetAlarmRepeat(repeat)
            }
            .create()
    }



    interface Listener {
        fun dialogGetAlarmRepeat(): Set<AlarmRepeatOption>
        fun dialogSetAlarmRepeat(options: Set<AlarmRepeatOption>)
    }

}