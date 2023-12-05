package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.base.sdk.port.sync.AbWmSyncs
import com.sjbt.sdk.SJUniWatch

class SJSyncData(val sjUniWatch: SJUniWatch) : AbWmSyncs() {

    override val syncStepData: AbSyncData<WmSyncData<WmStepData>> = SyncStepData(sjUniWatch)
    override val syncOxygenData: AbSyncData<WmSyncData<WmOxygenData>> = SyncOxygenData(sjUniWatch)
    override val syncCaloriesData: AbSyncData<WmSyncData<WmCaloriesData>> = SyncCaloriesData(sjUniWatch)
    override val syncSleepData: AbSyncData<WmSyncData<WmSleepData>> = SyncSleepData(sjUniWatch)
    override val syncRealtimeRateData: AbSyncData<WmSyncData<WmRealtimeRateData>> = SyncRealtimeRateData(sjUniWatch)
    override val syncHeartRateData: AbSyncData<WmSyncData<WmHeartRateData>> = SyncHeartRateData(sjUniWatch)
    override val syncDistanceData: AbSyncData<WmSyncData<WmDistanceData>> = SyncDistanceData(sjUniWatch)
    override val syncActivityDurationData: AbSyncData<WmSyncData<WmActivityDurationData>> = SyncActivityDurationData(sjUniWatch)
    override val syncSportSummaryData: AbSyncData<WmSyncData<WmSportSummaryData>> = SyncSportSummaryData(sjUniWatch)
    override val syncAllData: AbSyncData<WmSyncData<out WmBaseSyncData>> = SyncAllData(sjUniWatch)
    override val syncDailyActivityDuration: AbSyncData<WmSyncData<WmDailyActivityDurationData>> = SyncDailyActivityDurationData(sjUniWatch)
}