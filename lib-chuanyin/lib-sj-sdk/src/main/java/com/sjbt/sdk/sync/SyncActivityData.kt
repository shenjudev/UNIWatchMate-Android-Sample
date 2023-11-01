package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmActivityData
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_SPORT_ACTIVITY_LEN
import com.sjbt.sdk.spp.cmd.URN_SPORT_RATE
import com.sjbt.sdk.spp.cmd.URN_SPORT_RATE_RECORD
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncActivityData(val sjUniWatch: SJUniWatch) : AbSyncData<List<WmActivityData>>() {

    private var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<List<WmActivityData>>? = null
    private var observeChangeEmitter: ObservableEmitter<List<WmActivityData>>? = null
    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(nodeData: NodeData) {
    }

    override fun syncData(startTime: Long): Single<List<WmActivityData>> {

        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                CmdHelper.getReadSportSyncData(
                    startTime, lastSyncTime,
                    childUrn = URN_SPORT_ACTIVITY_LEN
                )
            )
        }
    }

    override var observeSyncData: Observable<List<WmActivityData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}