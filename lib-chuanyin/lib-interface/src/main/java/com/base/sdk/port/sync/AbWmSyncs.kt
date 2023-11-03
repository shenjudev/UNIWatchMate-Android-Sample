package com.base.sdk.port.sync

import com.base.sdk.entity.data.*
import com.base.sdk.entity.settings.WmDeviceInfo

/**
 * 同步数据
 */
abstract class AbWmSyncs {
    /**
     * sync step(同步步数)
     */
    abstract val syncStepData: AbSyncData<WmSyncData<WmStepData>>

    /**
     * sync oxygen(同步血氧)
     */
    abstract val syncOxygenData: AbSyncData<WmSyncData<WmOxygenData>>

    /**
     * syncCalories(同步卡路里)
     */
    abstract val syncCaloriesData: AbSyncData<WmSyncData<WmCaloriesData>>

    /**
     * syncSleep(同步睡眠)
     */
    abstract val syncSleepData: AbSyncData<WmSyncData<WmSleepData>>

    /**
     * syncRealtimeRate(同步实时心率)
     */
    abstract val syncRealtimeRateData: AbSyncData<WmSyncData<WmRealtimeRateData>>

    /**
     * syncAvgHeartRate(同步平均心率)
     */
    abstract val syncHeartRateData: AbSyncData<WmSyncData<WmHeartRateData>>

    /**
     * syncDistance(同步距离)
     */
    abstract val syncDistanceData: AbSyncData<WmSyncData<WmDistanceData>>

    /**
     * syncActivity(同步日常活动)
     */
    abstract val syncActivityData: AbSyncData<WmSyncData<WmActivityData>>

    /**
     * syncSportSummary(同步运动小结)
     */
    abstract val syncSportSummaryData: AbSyncData<WmSyncData<WmSportSummaryData>>

    /**
     * syncTodayInfo(同步当日数据)
     */
    abstract val syncTodayInfoData: AbSyncData<WmSyncData<WmTodayTotalData>>

}