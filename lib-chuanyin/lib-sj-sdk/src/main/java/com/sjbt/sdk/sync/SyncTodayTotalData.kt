package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmTodayTotalData
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_SPORT_TODAY
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncTodayTotalData(val sjUniWatch: SJUniWatch) : AbSyncData<WmTodayTotalData>() {

    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<WmTodayTotalData>? = null
    private var observeChangeEmitter: ObservableEmitter<WmTodayTotalData>? = null
    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    override fun syncData(startTime: Long): Single<WmTodayTotalData> {
        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                CmdHelper.getReadSportSyncData(
                    startTime,
                    lastSyncTime,
                    childUrn = URN_SPORT_TODAY
                )
            )
        }
    }

    override var observeSyncData: Observable<WmTodayTotalData> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}