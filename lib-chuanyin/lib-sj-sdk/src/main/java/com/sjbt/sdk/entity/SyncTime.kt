package com.sjbt.sdk.entity

data class SyncTime(val startTime: Long,
                    val endTime: Long){
    override fun toString(): String {
        return "SyncTime(startTime=$startTime, endTime=$endTime)"
    }
}
