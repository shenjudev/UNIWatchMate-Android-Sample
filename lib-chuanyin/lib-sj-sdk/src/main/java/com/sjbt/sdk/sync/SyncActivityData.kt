package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmActivityData
import com.base.sdk.entity.data.WmSyncData
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_SPORT_ACTIVITY_LEN
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncActivityData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<WmActivityData>>(),ReadSubPkMsg {

    private var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<WmSyncData<WmActivityData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmActivityData>>? = null
    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun setHasNext(hasNext: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getHasNext(): Boolean {
        TODO("Not yet implemented")
    }

    fun onTimeOut(nodeData: NodeData) {
    }

    override fun syncData(startTime: Long): Single<WmSyncData<WmActivityData>> {

        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(this,
                CmdHelper.getReadSportSyncData(
                    startTime, lastSyncTime,
                    childUrn = URN_SPORT_ACTIVITY_LEN
                )
            )
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmActivityData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}