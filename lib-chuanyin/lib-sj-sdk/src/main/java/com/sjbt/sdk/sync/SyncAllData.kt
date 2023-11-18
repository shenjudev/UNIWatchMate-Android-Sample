package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

class SyncAllData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<out WmBaseSyncData>>(),
    ReadSubPkMsg {

    var lastSyncTime: Long = 0
    private var syncDataEmitter: ObservableEmitter<WmSyncData<out WmBaseSyncData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<out WmBaseSyncData>>? = null
    private var hasNext = false
    private val TAG = "SyncAllData"

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

    override fun syncData(startTime: Long): Observable<WmSyncData<out WmBaseSyncData>> {

        return Observable.create { emitter ->
            syncDataEmitter = emitter

            val words = arrayOf(
                WmSyncDataType.STEP,
//                WmSyncDataType.DISTANCE,
                WmSyncDataType.CALORIE,
                WmSyncDataType.HEART_RATE_ONE_HOUR,
                WmSyncDataType.HEART_RATE_FIVE_MINUTES,
                WmSyncDataType.OXYGEN,
                WmSyncDataType.ACTIVITY_DURATION,
                WmSyncDataType.SPORT_SUMMARY,
                WmSyncDataType.SLEEP
            )

            val characters: Observable<WmSyncData<out WmBaseSyncData>> = Observable
                .fromIterable(words.toList()) // create an observable from the input list
                .concatMap { word ->

                    sjUniWatch.wmLog.logE(TAG, "请求了:$word")

                    when (word) {
                        WmSyncDataType.STEP -> {
                            sjUniWatch.wmSync.syncStepData.syncData(startTime)
                        }
//                        WmSyncDataType.DISTANCE -> {
//                            sjUniWatch.wmSync.syncDistanceData.syncData(startTime)
//                        }
                        WmSyncDataType.CALORIE -> {
                            sjUniWatch.wmSync.syncCaloriesData.syncData(startTime)
                        }
                        WmSyncDataType.HEART_RATE_ONE_HOUR -> {
                            sjUniWatch.wmSync.syncHeartRateData.syncData(startTime)
                        }
                        WmSyncDataType.HEART_RATE_FIVE_MINUTES -> {
                            sjUniWatch.wmSync.syncRealtimeRateData.syncData(startTime)
                        }
                        WmSyncDataType.OXYGEN -> {
                            sjUniWatch.wmSync.syncOxygenData.syncData(startTime)
                        }
                        WmSyncDataType.ACTIVITY_DURATION -> {
                            sjUniWatch.wmSync.syncActivityDurationData.syncData(startTime)
                        }
                        WmSyncDataType.SPORT_SUMMARY -> {
                            sjUniWatch.wmSync.syncSportSummaryData.syncData(startTime)
                        }
                        WmSyncDataType.SLEEP -> {
                            sjUniWatch.wmSync.syncSleepData.syncData(startTime)
                        }
                        else -> {
                            sjUniWatch.wmSync.syncSportSummaryData.syncData(startTime)
                        }
                    }
                }

            characters.subscribe { wmSyncData ->
                sjUniWatch.wmLog.logE(TAG, "sync All back data${wmSyncData}")
                emitter.onNext(wmSyncData)

                if (wmSyncData.type == WmSyncDataType.SLEEP) {
                    emitter.onComplete()
                }
            }
        }
    }

    override var observeSyncData: Observable<WmSyncData<out WmBaseSyncData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}