package com.sjbt.sdk.sync

import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.data.*
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

class SyncAllData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<out WmBaseSyncData>>(),
    ExceptionStateListener,
    ReadSubPkMsg {

    var lastSyncTime: Long = 0
    private var syncDataEmitter: ObservableEmitter<WmSyncData<out WmBaseSyncData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<out WmBaseSyncData>>? = null
    private var hasNext = false
    private val TAG = "SyncAllData"
    private var progress = 0
    private val words = arrayOf(
        WmSyncDataType.STEP,
        WmSyncDataType.DISTANCE,
        WmSyncDataType.CALORIE,
        WmSyncDataType.HEART_RATE_ONE_HOUR,
        WmSyncDataType.HEART_RATE_FIVE_MINUTES,
        WmSyncDataType.OXYGEN,
        WmSyncDataType.ACTIVITY_DURATION,
        WmSyncDataType.SPORT_SUMMARY,
        WmSyncDataType.SLEEP
    )

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        observeDisconnectState()
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")
    }

    override fun observeDisconnectState() {
        syncDataEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("$TAG time out exception"))
            }
        }
    }

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    override fun syncData(startTime: Long): Observable<WmSyncData<out WmBaseSyncData>> {

        sjUniWatch.observeConnectState.subscribe {
            if (it == WmConnectState.DISCONNECTED) {
                syncDataEmitter?.onError(WmTimeOutException("$TAG time out exception"))
            }
        }

        return Observable.create { emitter ->
            syncDataEmitter = emitter

            val characters: Observable<WmSyncData<out WmBaseSyncData>> = Observable
                .fromIterable(words.toList()) // create an observable from the input list
                .concatMap { word ->

                    sjUniWatch.wmLog.logE(TAG, "请求了:$word")

                    when (word) {
                        WmSyncDataType.STEP -> {
                            sjUniWatch.wmSync.syncStepData.syncData(startTime)
                        }
                        WmSyncDataType.DISTANCE -> {
                            progress = 100/9
                            sjUniWatch.wmSync.syncDistanceData.syncData(startTime)
                        }
                        WmSyncDataType.CALORIE -> {
                            progress = 100/9*2
                            sjUniWatch.wmSync.syncCaloriesData.syncData(startTime)
                        }
                        WmSyncDataType.HEART_RATE_ONE_HOUR -> {
                            progress = 100/9*3
                            sjUniWatch.wmSync.syncHeartRateData.syncData(startTime)
                        }
                        WmSyncDataType.HEART_RATE_FIVE_MINUTES -> {
                            progress = 100/9*3
                            sjUniWatch.wmSync.syncRealtimeRateData.syncData(startTime)
                        }
                        WmSyncDataType.OXYGEN -> {
                            progress = 100/9*5
                            sjUniWatch.wmSync.syncOxygenData.syncData(startTime)
                        }
                        WmSyncDataType.ACTIVITY_DURATION -> {
                            progress = 100/9*6
                            sjUniWatch.wmSync.syncActivityDurationData.syncData(startTime)
                        }
                        WmSyncDataType.SPORT_SUMMARY -> {
                            progress = 100/9*7
                            sjUniWatch.wmSync.syncSportSummaryData.syncData(startTime)
                        }
                        WmSyncDataType.SLEEP -> {
                            progress = 100/9*8
                            sjUniWatch.wmSync.syncSleepData.syncData(startTime)
                        }
                        else -> {
                            sjUniWatch.wmSync.syncSportSummaryData.syncData(startTime)
                        }
                    }
                }

            characters.subscribe { wmSyncData ->
                sjUniWatch.wmLog.logE(TAG, "sync All back data -> ${wmSyncData}")
                emitter.onNext(wmSyncData)

                if (wmSyncData.type == WmSyncDataType.SLEEP) {
                    progress = 100
                    emitter.onComplete()
                }
            }
        }
    }

    override var observeSyncData: Observable<WmSyncData<out WmBaseSyncData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}