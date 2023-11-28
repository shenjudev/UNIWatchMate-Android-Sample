package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.base.sdk.port.sync.AbWmSyncs
import com.sjbt.sdk.SJUniWatch

class SJSyncData(val sjUniWatch: SJUniWatch) : AbWmSyncs() {

    override var syncStepData: AbSyncData<WmSyncData<WmStepData>> = SyncStepData(sjUniWatch)
    override var syncOxygenData: AbSyncData<WmSyncData<WmOxygenData>> = SyncOxygenData(sjUniWatch)
    override var syncCaloriesData: AbSyncData<WmSyncData<WmCaloriesData>> = SyncCaloriesData(sjUniWatch)
    override var syncSleepData: AbSyncData<WmSyncData<WmSleepData>> = SyncSleepData(sjUniWatch)
    override var syncRealtimeRateData: AbSyncData<WmSyncData<WmRealtimeRateData>> = SyncRealtimeRateData(sjUniWatch)
    override var syncHeartRateData: AbSyncData<WmSyncData<WmHeartRateData>> = SyncHeartRateData(sjUniWatch)
    override var syncDistanceData: AbSyncData<WmSyncData<WmDistanceData>> = SyncDistanceData(sjUniWatch)
    override var syncActivityDurationData: AbSyncData<WmSyncData<WmActivityDurationData>> = SyncActivityDurationData(sjUniWatch)
    override var syncSportSummaryData: AbSyncData<WmSyncData<WmSportSummaryData>> = SyncSportSummaryData(sjUniWatch)
    override var syncAllData: AbSyncData<WmSyncData<out WmBaseSyncData>> = SyncAllData(sjUniWatch)
    override val syncDailyActivityDuration: AbSyncData<WmSyncData<WmDailyActivityDurationData>> = SyncDailyActivityDurationData(sjUniWatch)
}