package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.sync.AbSyncData
import com.google.gson.Gson
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.*
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

    private val dailyActivitySummaryMap = mutableMapOf<Long, List<WmSportSummaryData>>()
    private val dailyActivityDurationMap = mutableMapOf<Long, List<WmActivityDurationData>>()

    private val sportSummaryDataList = mutableListOf<WmSportSummaryData>()
    private val activityDurationDataList = mutableListOf<WmActivityDurationData>()

    private val dailyActivityDurationDataList = mutableListOf<WmDailyActivityDurationData>()

    private val sportTypeMap = mutableMapOf<Int, Short>()

    private fun getSportTypeById(sportId: Int): Short {
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
        activityDurationDataList.clear()

        readSportTypeJsonFromAssets(sjUniWatch.mContext)?.let {
            sjUniWatch.wmLog.logE(TAG, "readJsonFromAssets:$it")
            val sportTypeData = Gson().fromJson(it, SportTypeData::class.java)
            sportTypeData.sports.forEach {
                sportTypeMap.put(it.id, it.sport_type.toShort())
            }
        }

        return Observable.create { emitter ->
            dailyActivityDurationObserveEmitter = emitter

            sjUniWatch.syncActivityDuration.syncData(startTime).subscribe { activityDuration ->
                sjUniWatch.wmLog.logE(TAG, "activity duration:$activityDuration")

                sjUniWatch.syncSportSummaryData.withTenSeconds = false
                sjUniWatch.syncSportSummaryData.syncData(startTime).subscribe { summaryData ->
                    sjUniWatch.wmLog.logE(TAG, "activity summary:$summaryData")
                    sjUniWatch.syncSportSummaryData.withTenSeconds = true

                    val dailyActivityList = mutableListOf<WmDailyActivityDurationData>()

                    summaryData.value.forEach {
                        val dailyActivityDurationData =
                            WmDailyActivityDurationData(
                                getSportTypeById(it.sportId),
                                (it.endTime - it.startTime).toShort()
                            )
                        dailyActivityList.add(dailyActivityDurationData)

                        sjUniWatch.wmLog.logE(
                            TAG,
                            "daily activity duration:$dailyActivityDurationData"
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