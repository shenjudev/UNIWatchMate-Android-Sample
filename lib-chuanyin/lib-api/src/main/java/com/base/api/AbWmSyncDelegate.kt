package com.base.api

import com.base.sdk.AbUniWatch
import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.base.sdk.port.sync.AbWmSyncs

internal class AbWmSyncDelegate(
   private val watchObservable: BehaviorObservable<AbUniWatch>
) : AbWmSyncs() {

    override val syncStepData: AbSyncData<WmSyncData<WmStepData>>
        get() = watchObservable.value!!.wmSync.syncStepData
    override val syncOxygenData: AbSyncData<WmSyncData<WmOxygenData>>
        get() = watchObservable.value!!.wmSync.syncOxygenData
    override val syncCaloriesData: AbSyncData<WmSyncData<WmCaloriesData>>
        get() = watchObservable.value!!.wmSync.syncCaloriesData
    override val syncSleepData: AbSyncData<WmSyncData<WmSleepData>>
        get() = watchObservable.value!!.wmSync.syncSleepData
    override val syncRealtimeRateData: AbSyncData<WmSyncData<WmRealtimeRateData>>
        get() = watchObservable.value!!.wmSync.syncRealtimeRateData
    override val syncHeartRateData: AbSyncData<WmSyncData<WmHeartRateData>>
        get() = watchObservable.value!!.wmSync.syncHeartRateData
    override val syncDistanceData: AbSyncData<WmSyncData<WmDistanceData>>
        get() = watchObservable.value!!.wmSync.syncDistanceData
    override val syncActivityDurationData: AbSyncData<WmSyncData<WmActivityDurationData>>
        get() = watchObservable.value!!.wmSync.syncActivityDurationData
    override val syncSportSummaryData: AbSyncData<WmSyncData<WmSportSummaryData>>
        get() = watchObservable.value!!.wmSync.syncSportSummaryData
    override val syncAllData: AbSyncData<WmSyncData<out WmBaseSyncData>>
        get() = watchObservable.value!!.wmSync.syncAllData
}