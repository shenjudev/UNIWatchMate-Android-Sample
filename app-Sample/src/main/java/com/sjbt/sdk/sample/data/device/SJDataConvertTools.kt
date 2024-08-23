package com.sjbt.sdk.sample.data.device

import com.base.sdk.entity.apps.AlarmRepeatOption
import com.base.sdk.entity.apps.WmAllahCollect
import com.base.sdk.entity.apps.WmPrayReminder
import com.base.sdk.entity.apps.WmRosaryReminder
import com.base.sdk.entity.data.WmCaloriesData
import com.base.sdk.entity.data.WmDistanceData
import com.base.sdk.entity.data.WmStepData
import com.sjbt.sdk.sample.model.MuslimAllahInfo
import com.sjbt.sdk.sample.model.muslim.MuslimPrayRemind
import com.sjbt.sdk.sample.model.muslim.MuslimPrayReminder
import com.sjbt.sdk.sample.model.muslim.MuslimRepeatRule
import com.sjbt.sdk.sample.model.muslim.MuslimTasbihReminder
import com.sjbt.sdk.sample.utils.CacheDataHelper
import java.util.Locale


class SJDataConvertTools {
    companion object {
        private const val TAG = "TSDataConvertTools"
        val instance: SJDataConvertTools by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { SJDataConvertTools() }
    }

    fun transWeekRepeat(weekRepeat: BooleanArray): MutableSet<AlarmRepeatOption> {
        val repeatSet = mutableSetOf<AlarmRepeatOption>()
        if (weekRepeat.size == 7) {
            for (index in 0 until 7) {
                if (weekRepeat[index]) {
                    when (index) {
                        0 -> repeatSet.add(AlarmRepeatOption.MONDAY)
                        1 -> repeatSet.add(AlarmRepeatOption.TUESDAY)
                        2 -> repeatSet.add(AlarmRepeatOption.WEDNESDAY)
                        3 -> repeatSet.add(AlarmRepeatOption.THURSDAY)
                        4 -> repeatSet.add(AlarmRepeatOption.FRIDAY)
                        5 -> repeatSet.add(AlarmRepeatOption.SATURDAY)
                        6 -> repeatSet.add(AlarmRepeatOption.SUNDAY)
                    }
                }
            }
        }
        return repeatSet
    }

    fun trans2AlarmRepeatBooleanArray(repeatSet: Set<AlarmRepeatOption>): BooleanArray {
        val booleanArray = BooleanArray(7)
        for (option in repeatSet) {
            when (option) {
                AlarmRepeatOption.SUNDAY -> booleanArray[6] = true
                AlarmRepeatOption.MONDAY -> booleanArray[0] = true
                AlarmRepeatOption.TUESDAY -> booleanArray[1] = true
                AlarmRepeatOption.WEDNESDAY -> booleanArray[2] = true
                AlarmRepeatOption.THURSDAY -> booleanArray[3] = true
                AlarmRepeatOption.FRIDAY -> booleanArray[4] = true
                AlarmRepeatOption.SATURDAY -> booleanArray[5] = true
                else -> {

                }
            }
        }

        return booleanArray
    }


    private fun roundingToInt(value: Double): Int { //四舍五入取整
//        return Integer.parseInt(new BigDecimal(value).setScale(0, BigDecimal.ROUND_HALF_UP).toString());
        val format = "%.0f"
        val padding = 0.000005f
        return String.format(Locale.US, format, value + padding).toInt()
    }


    private fun stepList2Array(stepList: MutableList<WmStepData>): List<Int> {
        val valueList = mutableListOf<Int>()
        for (step in stepList) {
            valueList.add(step.step)
        }
        return valueList
    }

    private fun calorieList2Array(caloriesList: MutableList<WmCaloriesData>): List<Int> {
        val valueList = mutableListOf<Int>()
        for (cakirie in caloriesList) {
            valueList.add(cakirie.calorie)
        }
        return valueList
    }

    private fun distanceList2Array(distanceList: MutableList<WmDistanceData>): List<Int> {
        val valueList = mutableListOf<Int>()
        for (distance in distanceList) {
            valueList.add(distance.distance)
        }
        return valueList
    }


    fun convertPrayReminder(wmPrayReminder: WmPrayReminder): MuslimPrayReminder {
        val muslimPrayReminds: MutableList<MuslimPrayRemind> =
            java.util.ArrayList()
        val prayReminds = wmPrayReminder.prayReminds
        for (prayRemind in prayReminds) {
            val muslimPrayRemind =
                MuslimPrayRemind(prayRemind.id, prayRemind.open)
            muslimPrayReminds.add(muslimPrayRemind)
        }
        return MuslimPrayReminder(wmPrayReminder.version, wmPrayReminder.switch, muslimPrayReminds)
    }

    fun convertTasbihReminder(wmRosaryReminder: WmRosaryReminder): MuslimTasbihReminder {
        val muslimRepeatRules: MutableList<MuslimRepeatRule> = ArrayList()

        for ((weekId, repeat) in wmRosaryReminder.repeatRules) {
            muslimRepeatRules.add(MuslimRepeatRule(weekId, repeat))
        }

        val muslimTasbihReminder = MuslimTasbihReminder(
            wmRosaryReminder.version,
            wmRosaryReminder.switch,
            wmRosaryReminder.startHour,
            wmRosaryReminder.startMinute,
            wmRosaryReminder.endHour,
            wmRosaryReminder.endMinute,
            wmRosaryReminder.frequency.time,
            muslimRepeatRules
        )
        return muslimTasbihReminder
    }

    fun convertAllahList(wmAllahCollect: WmAllahCollect): List<MuslimAllahInfo> {
        val muslimAllahInfos: MutableList<MuslimAllahInfo> = java.util.ArrayList()

        for (i in wmAllahCollect.allahList.indices) {
            val wmAllah = wmAllahCollect.allahList[i]

            val muslimAllahInfo = CacheDataHelper.allahInfoMap[wmAllah.id]

            if (muslimAllahInfo != null) {
                muslimAllahInfo.isFavorite = wmAllah.collect == 1
                muslimAllahInfos.add(muslimAllahInfo)
            }
        }
        return muslimAllahInfos
    }

}