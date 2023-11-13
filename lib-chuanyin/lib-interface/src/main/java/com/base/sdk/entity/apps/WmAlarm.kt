package com.base.sdk.entity.apps

/**
 * AlarmData structure 闹钟数据结构
 */
class WmAlarm(
    var alarmName: String,//限制最长20
    var hour: Int,
    var minute: Int,
    var repeatOptions: Set<AlarmRepeatOption>//重复模式
) {
    var isOn: Boolean = false
    override fun toString(): String {
        return "WmAlarm(alarmName='$alarmName', hour=$hour, minute=$minute, repeatOptions=$repeatOptions, isOn=$isOn)"
    }
}

/**
 * 重复模式
 */
enum class AlarmRepeatOption(val value: Int) {
    NONE(0),
    MONDAY(1 shl 0),
    TUESDAY(1 shl 1),
    WEDNESDAY(1 shl 2),
    THURSDAY(1 shl 3),
    FRIDAY(1 shl 4),
    SATURDAY(1 shl 5),
    SUNDAY(1 shl 6);

    companion object {
        fun fromValue(value: Int): Set<AlarmRepeatOption> {
            return AlarmRepeatOption.values().filter { option -> option.value and value != 0 }
                .toSet()
        }

        fun toValue(options:Set<AlarmRepeatOption>) :Int{
            var value = 0;
            options.forEach{
                value =  value or it.value
            }
            return value
        }
    }
}
