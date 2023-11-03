package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmDistanceData
import com.base.sdk.entity.data.WmStepData
import com.base.sdk.entity.data.WmSyncData
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_SPORT_ACTIVITY_LEN
import com.sjbt.sdk.spp.cmd.URN_SPORT_DISTANCE
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncDistanceData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<WmDistanceData>>() {

    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<WmSyncData<WmDistanceData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmDistanceData>>? = null
    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    override fun syncData(startTime: Long): Single<WmSyncData<WmDistanceData>> {
        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                CmdHelper.getReadSportSyncData(  startTime, lastSyncTime,
                    childUrn =  URN_SPORT_DISTANCE
                )
            )
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmDistanceData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }


}