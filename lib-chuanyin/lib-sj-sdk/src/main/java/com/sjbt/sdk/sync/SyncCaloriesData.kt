package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmCaloriesData
import com.base.sdk.entity.data.WmSyncData
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_SPORT_ACTIVITY_LEN
import com.sjbt.sdk.spp.cmd.URN_SPORT_CALORIES
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncCaloriesData (val sjUniWatch: SJUniWatch): AbSyncData<WmSyncData<WmCaloriesData>>() {
    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<WmSyncData<WmCaloriesData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmCaloriesData>>? = null
    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    override fun syncData(startTime: Long): Single<WmSyncData<WmCaloriesData>> {
        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                CmdHelper.getReadSportSyncData(
                    startTime, lastSyncTime,
                    childUrn = URN_SPORT_CALORIES
                )
            )
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmCaloriesData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}