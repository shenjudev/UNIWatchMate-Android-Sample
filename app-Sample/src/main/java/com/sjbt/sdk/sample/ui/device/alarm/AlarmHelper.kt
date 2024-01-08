package com.sjbt.sdk.sample.ui.device.alarm

import android.content.Context
import android.text.TextUtils
import android.text.format.DateFormat
import com.base.sdk.entity.apps.AlarmRepeatOption
import com.base.sdk.entity.apps.WmAlarm
import com.sjbt.sdk.sample.R
import java.util.*

object AlarmHelper {

    private var is24HourFormat: Boolean? = null

    fun is24HourFormat(context: Context): Boolean {
        return is24HourFormat ?: DateFormat.is24HourFormat(context).also { is24HourFormat = it }
    }

    private var dayValuesSimple: Array<Int> = arrayOf(
        R.string.ds_alarm_repeat_00_simple,
        R.string.ds_alarm_repeat_01_simple,
        R.string.ds_alarm_repeat_02_simple,
        R.string.ds_alarm_repeat_03_simple,
        R.string.ds_alarm_repeat_04_simple,
        R.string.ds_alarm_repeat_05_simple,
        R.string.ds_alarm_repeat_06_simple
    )

    fun findNewAlarmId(alarms: ArrayList<WmAlarm>?): Int {
        var maxAlarmId = -1
        alarms?.let {
            for (alarm in it) {
//                if (alarm.alarmId > maxAlarmId) {
//                    maxAlarmId = alarm.alarmId
//                }
            }
        }
        return maxAlarmId + 1
    }

    fun getDefaultRepeatOption(): Set<AlarmRepeatOption> {
        val repeatOptions = mutableSetOf<AlarmRepeatOption>()
//        repeatOptions.plus(AlarmRepeatOption.SUNDAY)
//        repeatOptions.plus(AlarmRepeatOption.MONDAY)
//        repeatOptions.plus(AlarmRepeatOption.TUESDAY)
//        repeatOptions.plus(AlarmRepeatOption.WEDNESDAY)
//        repeatOptions.plus(AlarmRepeatOption.THURSDAY)
//        repeatOptions.plus(AlarmRepeatOption.FRIDAY)
//        repeatOptions.plus(AlarmRepeatOption.SATURDAY)
        return repeatOptions
    }

    fun newAlarm(wmAlarm: WmAlarm): WmAlarm {
        val alarm = WmAlarm(
            wmAlarm.alarmName,
            wmAlarm.hour,
            wmAlarm.minute,
            wmAlarm.repeatOptions
        )
        alarm.isOn = wmAlarm.isOn
//        alarm.alarmId = wmAlarm.alarmId
        return alarm
    }

    /**
     * Display [FcAlarm.repeat] as a readable String
     */
    fun repeatToSimpleStr(repeats: Set<AlarmRepeatOption>,context: Context): String {
        var text = StringBuilder()
        if (repeats.contains(AlarmRepeatOption.SUNDAY)) {
            text.append("${context.getString(dayValuesSimple[6])},")
        }
        if (repeats.contains(AlarmRepeatOption.MONDAY)) {
            text.append("${context.getString(dayValuesSimple[0])},")
        }
        if (repeats.contains(AlarmRepeatOption.TUESDAY)) {
            text.append("${context.getString(dayValuesSimple[1])},")
        }
        if (repeats.contains(AlarmRepeatOption.WEDNESDAY)) {
            text.append("${context.getString(dayValuesSimple[2])},")
        }
        if (repeats.contains(AlarmRepeatOption.THURSDAY)) {
            text.append("${context.getString(dayValuesSimple[3])},")
        }
        if (repeats.contains(AlarmRepeatOption.FRIDAY)) {
            text.append("${context.getString(dayValuesSimple[4])},")
        }
        if (repeats.contains(AlarmRepeatOption.SATURDAY)) {
            text.append("${context.getString(dayValuesSimple[5])},")
        }
        val repeatString = text.toString()
        if (!TextUtils.isEmpty(repeatString)) {
            return repeatString.substring(0, repeatString.length - 1)
        }else{
            return context.getString(R.string.alarm_no_repetition)
        }

        return repeatString
    }

    fun sort(list: List<WmAlarm>): List<WmAlarm> {
        Collections.sort(list, comparator)
        return list
    }

    fun repeatToBoolean(index: Int, repeats: Set<AlarmRepeatOption>): Boolean {
        var state = false
        when (index) {
            6 -> {
                state = repeats.contains(AlarmRepeatOption.SUNDAY)
            }

            0 -> {
                state = repeats.contains(AlarmRepeatOption.MONDAY)
            }

            1 -> {
                state = repeats.contains(AlarmRepeatOption.TUESDAY)
            }

            2 -> {
                state = repeats.contains(AlarmRepeatOption.WEDNESDAY)
            }

            3 -> {
                state = repeats.contains(AlarmRepeatOption.THURSDAY)
            }

            4 -> {
                state = repeats.contains(AlarmRepeatOption.FRIDAY)
            }

            5 -> {
                state = repeats.contains(AlarmRepeatOption.SATURDAY)
            }
        }
        return state
    }

    fun booleanItems2Options(checkedItems: BooleanArray): Set<AlarmRepeatOption> {
        val setOptions = mutableSetOf<AlarmRepeatOption>()
        for ((index, bean) in checkedItems.withIndex()) {
            if (bean) {
                setOptions.add(getAlarmRepeatOptionByIndex(index))
            }
        }
        return setOptions
    }

    private fun getAlarmRepeatOptionByIndex(index: Int): AlarmRepeatOption {
        var state = AlarmRepeatOption.SUNDAY
        when (index) {
            6 -> {
                state = AlarmRepeatOption.SUNDAY
            }

            0 -> {
                state = AlarmRepeatOption.MONDAY
            }

            1 -> {
                state = AlarmRepeatOption.TUESDAY
            }

            2 -> {
                state = AlarmRepeatOption.WEDNESDAY
            }

            3 -> {
                state = AlarmRepeatOption.THURSDAY
            }

            4 -> {
                state = AlarmRepeatOption.FRIDAY
            }

            5 -> {
                state = AlarmRepeatOption.SATURDAY
            }
        }
        return state
    }

    val comparator: Comparator<WmAlarm> by lazy {
        Comparator { o1, o2 ->
            //first sort by time ,and then by id
            val v1: Int = o1.hour * 60 + o1.minute
            val v2: Int = o2.hour * 60 + o2.minute
            if (v1 > v2) {
                1
            } else if (v1 < v2) {
                -1
            } else {
                0
            }
        }
    }

}