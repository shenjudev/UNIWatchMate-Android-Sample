package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.sync.AbSyncData
import com.google.gson.Gson
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.utils.TimeUtils
import com.sjbt.sdk.utils.readSportTypeJsonFromAssets
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable

/**
 * 每日活动时长
 */
class SyncDailyActivityDurationData(val sjUniWatch: SJUniWatch) :
    AbSyncData<WmSyncData<WmDailyActivityDurationData>>(), ReadSubPkMsg {

    var lastSyncTime: Long = 0
    private var dailyActivityDurationObserveEmitter: ObservableEmitter<WmSyncData<WmDailyActivityDurationData>>? =
        null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmDailyActivityDurationData>>? =
        null

    var wmSyncData: WmSyncData<WmDailyActivityDurationData>? = null

    private val TAG = "SyncDailyActivityDurationData"
    private var hasNext: Boolean = false
    private val msgListSummary = mutableListOf<MsgBean>()

    /**
     * 按天分类的活动时长
     */
    private val dailyActivityDurationMap = mutableMapOf<Long, Int>()

    /**
     * 按天分类的运动概览
     */
    private val dailyActivitySummaryMap = mutableMapOf<Long, Int>()

    private val sportTypeMap = mutableMapOf<Int, Int>()

    private fun getSportTypeById(sportId: Int): Int {
        sportTypeMap.get(sportId)?.let {
            return it
        } ?: kotlin.run {
            return -1
        }
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        dailyActivityDurationObserveEmitter?.onError(WmTimeOutException("$TAG time out exception"))
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")
    }

    override fun syncData(startTime: Long): Observable<WmSyncData<WmDailyActivityDurationData>> {
        msgListSummary.clear()
        dailyActivityDurationMap.clear()
        dailyActivitySummaryMap.clear()

        readSportTypeJsonFromAssets(sjUniWatch.mContext)?.let {
            sjUniWatch.wmLog.logE(TAG, "readJsonFromAssets:$it")
            val sportTypeData = Gson().fromJson(it, SportTypeData::class.java)
            sportTypeData.sports.forEach { sportType ->
                sportTypeMap[sportType.id] = sportType.sport_type
            }
        }

        return Observable.create { emitter ->
            dailyActivityDurationObserveEmitter = emitter

            sjUniWatch.syncActivityDuration.syncData(startTime).subscribe { activityDuration ->
                sjUniWatch.wmLog.logE(TAG, "activity duration:$activityDuration")

                activityDuration.value.forEach { durationData ->
                    if (durationData.timestamp % 10000 < 1000) {

                        val timeStamp = durationData.timestamp / 1000 * 1000

                        var duration = dailyActivityDurationMap.get(timeStamp)

                        if (duration != null) {
                            duration += durationData.duration
                            dailyActivityDurationMap.put(timeStamp, duration)
                        } else {
                            dailyActivityDurationMap.put(
                                timeStamp,
                                durationData.duration
                            )
                        }
                    }
                }

                sjUniWatch.syncSportSummaryData.withTenSeconds = false
                sjUniWatch.syncSportSummaryData.syncData(startTime).subscribe { summaryData ->

                    sjUniWatch.syncSportSummaryData.withTenSeconds = true

                    val dailyActivityList = mutableListOf<WmDailyActivityDurationData>()

                    summaryData.value.forEach {

                        val dailyActivityDurationData =
                            WmDailyActivityDurationData(
                                getSportTypeById(it.sportId),
                                it.actTime.toInt()
                            )

                        dailyActivityDurationData.timestamp = it.timestamp

                        dailyActivityList.add(dailyActivityDurationData)

                        var actTime = dailyActivitySummaryMap.get(it.timestamp)

                        if (actTime != null) {
                            actTime += it.actTime
                            dailyActivitySummaryMap.put(it.timestamp, actTime)
                        } else {
                            dailyActivitySummaryMap.put(it.timestamp, it.actTime.toInt())
                        }
                    }

                    sjUniWatch.wmLog.logE(
                        TAG,
                        "daily activity duration map :$dailyActivityDurationMap"
                    )

                    sjUniWatch.wmLog.logE(
                        TAG,
                        "daily activity summary map :$dailyActivitySummaryMap"
                    )

                    for ((timeStamp, duration) in dailyActivitySummaryMap) {

                        var notSportDuration = dailyActivityDurationMap.get(timeStamp)?.let {
                            it.minus(duration)
                        }

                        notSportDuration?.let {
                            val dailyActivityDurationData =
                                WmDailyActivityDurationData(
                                    -99,
                                    it
                                )

                            dailyActivityDurationData.timestamp = timeStamp
                            dailyActivityList.add(dailyActivityDurationData)
                        }
                    }

                    dailyActivityList.forEach {
                        sjUniWatch.wmLog.logE(
                            TAG,
                            "daily activity duration :$it"
                        )
                    }

                    wmSyncData = WmSyncData(
                        WmSyncDataType.SPORT_SUMMARY,
                        summaryData.timestamp,
                        WmIntervalType.UNKNOWN,
                        dailyActivityList
                    )

                    dailyActivityDurationObserveEmitter?.onNext(wmSyncData)
                    dailyActivityDurationObserveEmitter?.onComplete()
                }
            }
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmDailyActivityDurationData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}