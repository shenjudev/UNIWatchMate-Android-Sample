package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.entity.settings.WmDeviceInfo
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
    override var syncActivityData: AbSyncData<WmSyncData<WmActivityData>> = SyncActivityData(sjUniWatch)
    override var syncSportSummaryData: AbSyncData<WmSyncData<WmSportSummaryData>> = SyncSportSummaryData(sjUniWatch)
    override var syncTodayInfoData: AbSyncData<WmSyncData<WmTodayTotalData>> = SyncTodayTotalData(sjUniWatch)

}