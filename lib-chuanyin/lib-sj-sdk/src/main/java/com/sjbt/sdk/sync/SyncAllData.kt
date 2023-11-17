package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_SPORT_10S_DISTANCE
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncAllData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<out WmBaseSyncData>>(),
    ReadSubPkMsg {

    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<WmSyncData<out WmBaseSyncData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<out WmBaseSyncData>>? = null
    private var hasNext = false

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
    }

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    override fun syncData(startTime: Long): Single<WmSyncData<out WmBaseSyncData>> {
        return Single.create { emitter ->
            activityObserveEmitter = emitter

            sjUniWatch.wmSync.syncStepData.syncData(startTime).subscribe { wmStepData ->
                observeChangeEmitter?.onNext(wmStepData)

                sjUniWatch.wmSync.syncCaloriesData.syncData(startTime).subscribe { wmCalories ->
                    observeChangeEmitter?.onNext(wmCalories)

                    sjUniWatch.wmSync.syncDistanceData.syncData(startTime).subscribe { wmDistance ->
                        observeChangeEmitter?.onNext(wmDistance)

                        sjUniWatch.wmSync.syncOxygenData.syncData(startTime).subscribe { wmOxygenData ->
                            observeChangeEmitter?.onNext(wmOxygenData)

                            sjUniWatch.wmSync.syncHeartRateData.syncData(startTime).subscribe { wmRate ->
                                observeChangeEmitter?.onNext(wmRate)

                                sjUniWatch.wmSync.syncRealtimeRateData.syncData(startTime).subscribe { wmRealTimeRate ->
                                    observeChangeEmitter?.onNext(wmRealTimeRate)

                                    sjUniWatch.wmSync.syncSleepData.syncData(startTime).subscribe { wmSleepData->
                                        observeChangeEmitter?.onNext(wmSleepData)

                                        sjUniWatch.wmSync.syncActivityDurationData.syncData(startTime).subscribe { wmActivityDuration->
                                            observeChangeEmitter?.onNext(wmActivityDuration)

                                            sjUniWatch.wmSync.syncSportSummaryData.syncData(startTime).subscribe { wmSportSummary->
                                                observeChangeEmitter?.onNext(wmSportSummary)
                                                observeChangeEmitter?.onComplete()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override var observeSyncData: Observable<WmSyncData<out WmBaseSyncData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}