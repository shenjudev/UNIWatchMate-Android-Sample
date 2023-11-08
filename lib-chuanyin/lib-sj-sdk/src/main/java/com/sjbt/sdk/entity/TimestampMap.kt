package com.sjbt.sdk.entity

import com.base.sdk.entity.data.WmBaseSyncData

class TimestampedData(val timestamp: Long, val data: WmBaseSyncData)

class TimestampedMap {
    private val dataMap = mutableMapOf<Long, WmBaseSyncData>()

    fun put(timestampedData: TimestampedData) {
        dataMap[timestampedData.timestamp] = timestampedData.data
    }

    fun getBetween(startTimestamp: Long, endTimestamp: Long): List<TimestampedData> {
        val result = mutableListOf<TimestampedData>()
        for ((timestamp, data) in dataMap) {
            if (timestamp in startTimestamp..endTimestamp) {
                result.add(TimestampedData(timestamp, data))
            }
        }
        return result
    }

    fun clearMap() {
        dataMap.clear()
    }

    fun remove(timestamp: Long) {
        dataMap.remove(timestamp)
    }
}