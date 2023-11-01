package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmSleepData
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_SPORT_SLEEP
import com.sjbt.sdk.spp.cmd.URN_SPORT_STEP
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncSleepData(val sjUniWatch: SJUniWatch) : AbSyncData<List<WmSleepData>>() {
    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<List<WmSleepData>>? = null
    private var observeChangeEmitter: ObservableEmitter<List<WmSleepData>>? = null
    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    override fun syncData(startTime: Long): Single<List<WmSleepData>> {

        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(CmdHelper.getReadSportSyncData(  startTime, lastSyncTime,childUrn = URN_SPORT_SLEEP))
        }
    }

    override var observeSyncData: Observable<List<WmSleepData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}