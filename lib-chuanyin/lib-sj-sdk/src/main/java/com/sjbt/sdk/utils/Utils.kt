package com.sjbt.sdk.utils

import com.sjbt.sdk.entity.SyncTime
import java.util.*

fun generateTimeList(startTime: Long): List<SyncTime> {
    val currentTime = System.currentTimeMillis()
    val calendar = Calendar.getInstance()

    calendar.timeInMillis = startTime
    val resultList = mutableListOf<SyncTime>()

    while (calendar.timeInMillis < currentTime) {
        val startOfDay = calendar.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0)
        startOfDay.set(Calendar.MILLISECOND, 0)

        val endOfDay = calendar.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59)
        endOfDay.set(Calendar.MILLISECOND, 999)

        if (endOfDay.timeInMillis > currentTime) {
            resultList.add(SyncTime(startOfDay.timeInMillis, currentTime))
            break
        } else {
            resultList.add(SyncTime(startOfDay.timeInMillis, endOfDay.timeInMillis))
            calendar.add(Calendar.DATE, 1)
        }
    }

    return resultList
}

fun getTimestampOfDaysAgo(days: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -days + 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun main() {
    val startTime = 1701828000000
    val timeList = generateTimeList(startTime)
    timeList.forEach { pair ->
        println("Start time: ${Date(pair.startTime)}, End time: ${Date(pair.endTime)}")
    }
}
