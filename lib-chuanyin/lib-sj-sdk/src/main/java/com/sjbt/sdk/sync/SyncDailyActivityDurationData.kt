package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.sync.AbSyncData
import com.google.gson.Gson
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.MAX_SYNC_DAYS
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.*
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * 每日活动时长
 */
class SyncDailyActivityDurationData(val sjUniWatch: SJUniWatch) :
    AbSyncData<WmSyncData<WmDailyActivityDurationData>>(),
    ExceptionStateListener, ReadSubPkMsg {

    var lastSyncTime: Long = 0
    private var syncDailyActivityDurationObserveEmitter: ObservableEmitter<WmSyncData<WmDailyActivityDurationData>>? =
        null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmDailyActivityDurationData>>? =
        null

    private val TAG = "SyncDailyActivityDurationData"
    private var hasNext: Boolean = false
    private lateinit var byteBufferSyncData: ByteBuffer

    private val sportTypeMap = mutableMapOf<Int, Int>()
    private val mActivityDurationDataList = mutableListOf<WmDailyActivityDurationData>()
    private var dayCount = 0
    private var dayIndex = 0

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

    override fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        observeDisconnectState()
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")
    }

    override fun observeDisconnectState() {
        syncDailyActivityDurationObserveEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("$TAG time out exception"))
            }
        }
    }

    override fun syncData(startTime: Long): Observable<WmSyncData<WmDailyActivityDurationData>> {
        mActivityDurationDataList.clear()
        val times = mutableListOf<SyncTime>()

        if (startTime != 0L) {
            val generateTimes = generateTimeList(startTime)

            if (generateTimes.size > MAX_SYNC_DAYS) {
                times.addAll(
                    generateTimes.subList(
                        generateTimes.size - MAX_SYNC_DAYS,
                        generateTimes.size
                    )
                )
            } else {
                times.addAll(generateTimes)
            }
        } else {
            times.addAll(generateTimeList(getTimestampOfDaysAgo(MAX_SYNC_DAYS)))
        }

        dayCount = times.size
        dayIndex = 0

        return Observable.create { emitter ->
            syncDailyActivityDurationObserveEmitter = emitter

            readSportTypeJsonFromAssets(sjUniWatch.mContext)?.let {
                val sportTypeData = Gson().fromJson(it, SportTypeData::class.java)
                sportTypeData.sports.forEach { sportType ->
                    sportTypeMap[sportType.id] = sportType.sport_type
                }
            }

            sjUniWatch.sendReadNodeCmdList(
                CmdHelper.getReadSportMultiTimesSyncData(
                    times,
                    childUrn = URN_SPORT_DAILY_ACTIVITY_LEN
                )
            )
        }

    }

    override var observeSyncData: Observable<WmSyncData<WmDailyActivityDurationData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

    private fun parseStepData() {

        val activityDurationDataList = mutableListOf<WmDailyActivityDurationData>()

        sjUniWatch.wmLog.logE(
            TAG,
            "all payload len:" + byteBufferSyncData.limit() + " :data:" + BtUtils.bytesToHexString(
                byteBufferSyncData.array()
            )
        )
        byteBufferSyncData.rewind()
        //0: 只有一个时间戳
        //1：每天一个时间戳
        //2：每小时一个时间戳
        val timestampType = byteBufferSyncData.get().toInt()

        val baseYear = byteBufferSyncData.short.toInt()
        val baseMon = byteBufferSyncData.get().toInt() - 1
        val baseDay = byteBufferSyncData.get().toInt()

        //相对时间戳
        val timestamp = byteBufferSyncData.int * 1000
        val dataLen = byteBufferSyncData.short

        val calendar = Calendar.getInstance()
        calendar.set(baseYear, baseMon, baseDay, 0, 0, 0)

        val realTimeStamp = (calendar.timeInMillis + timestamp) / 1000 * 1000

        sjUniWatch.wmLog.logD(
            TAG,
            "timestampType:$timestampType --> realTimeStamp:$realTimeStamp  dataLen:$dataLen"
        )

        var dataIndex = 0
        while (byteBufferSyncData.hasRemaining()) {

            val sportId = byteBufferSyncData.short.toInt()

            val wmDailyActivityDurationData =
                WmDailyActivityDurationData(getSportTypeById(sportId), byteBufferSyncData.int)

            if (timestampType == 0) {//只有一个时间戳
//                sjUniWatch.wmLog.logD(
//                    TAG,
//                    "date time:" + TimeUtils.date2String(Date(realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR))
//                )

                wmDailyActivityDurationData.timestamp =
                    realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "daily activity duration data sportId: $sportId -> ${wmDailyActivityDurationData}"
            )

            activityDurationDataList.add(wmDailyActivityDurationData)

            dataIndex++
        }

        val result = activityDurationDataList.groupBy { it.sportType }
            .mapValues { it.value.sumOf { data -> data.duration } }
            .map {
                val dailyActivityDurationData = WmDailyActivityDurationData(it.key, it.value)
                dailyActivityDurationData.timestamp = realTimeStamp
                dailyActivityDurationData
            }

        mActivityDurationDataList.addAll(result)

        if (dayIndex == dayCount) {
            val wmSyncData =
                WmSyncData(
                    WmSyncDataType.DAILY_ACTIVITY_DURATION,
                    realTimeStamp,
                    WmIntervalType.ONE_HOUR,
                    mActivityDurationDataList
                )

            syncDailyActivityDurationObserveEmitter?.onNext(wmSyncData)
            syncDailyActivityDurationObserveEmitter?.onComplete()

            lastSyncTime = System.currentTimeMillis()

            sjUniWatch.wmLog.logE(
                TAG,
                "daily activity duration ${wmSyncData}"
            )
        }

    }

    fun syncDailyActivityDurationDataBusiness(nodeData: NodeData) {
        dayIndex++
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseStepData()
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            if (dayIndex == dayCount) {
                defaultBack()
            }
        }
    }

    private fun defaultBack() {
        val wmSyncData =
            WmSyncData(
                WmSyncDataType.DAILY_ACTIVITY_DURATION,
                0,
                WmIntervalType.ONE_HOUR,
                mutableListOf<WmDailyActivityDurationData>()
            )

        syncDailyActivityDurationObserveEmitter?.onNext(wmSyncData)
        syncDailyActivityDurationObserveEmitter?.onComplete()
    }

}