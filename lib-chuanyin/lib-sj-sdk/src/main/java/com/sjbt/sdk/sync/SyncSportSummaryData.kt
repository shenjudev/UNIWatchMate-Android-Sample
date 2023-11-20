package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.BtUtils
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SyncSportSummaryData(val sjUniWatch: SJUniWatch) :
    AbSyncData<WmSyncData<WmSportSummaryData>>(), ReadSubPkMsg {
    var lastSyncTime: Long = 0
    private var syncSportSummaryObserveEmitter: ObservableEmitter<WmSyncData<WmSportSummaryData>>? =
        null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmSportSummaryData>>? = null

    private var wmSyncData: WmSyncData<WmSportSummaryData>? = null
    private val TAG = "SyncSportSummaryData"
    private val msgListSummary = mutableListOf<MsgBean>()

    //    private val msgListTenSeconds = mutableSetOf<MsgBean>()
    private var hasNext: Boolean = false
    private lateinit var byteBufferSummarySyncData: ByteBuffer

    private var mStartTime: Long = 0

    private val tenSecondsRealtimeRateMap = TimestampedMap()
    private val tenSecondsDistanceMap = TimestampedMap()
    private val tenSecondsStepFrequencyMap = TimestampedMap()
    private val tenSecondsCaloriesMap = TimestampedMap()

    private var tenSecondsTimeType = 0
    private var tenSecondsStartTimeStamp = 0L
    private var tenSecondsRealTimeStamp = 0L
    private var tenSecondsDataIndex = 0

    private var sportIndex = 0
    private var sportSize = 0

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
//        syncSportSummaryObserveEmitter?.onError(WmTimeOutException())
    }

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    override fun syncData(startTime: Long): Observable<WmSyncData<WmSportSummaryData>> {
        mStartTime = startTime
        wmSyncData = null
        return Observable.create { emitter ->
            syncSportSummaryObserveEmitter = emitter
            msgListSummary.clear()
            sjUniWatch.sendReadSubPkObserveNode(
                this@SyncSportSummaryData,
                CmdHelper.getReadSportSyncData(
                    startTime,
                    0,
                    childUrn = URN_SPORT_SUMMARY
                )
            ).subscribe(object :
                Observer<MsgBean> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: MsgBean) {
                    sjUniWatch.wmLog.logE(TAG, "sport summary back msg:$t")
                    msgListSummary.add(t)
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                    sjUniWatch.wmLog.logE(TAG, "summary back msg:" + msgListSummary.size)

                    if (msgListSummary.size > 0) {

                        if (msgListSummary.size == 1) {
                            msgListSummary[0].payloadPackage?.itemList?.forEach {
                                syncOnePkSportSummaryData(it)
                            }

                        } else {

                            var bufferSize = 0
                            msgListSummary.forEach {
                                if (it.divideType == DIVIDE_N_2 || it.divideType == DIVIDE_Y_F_2) {
                                    bufferSize += it.payloadLen - 17
                                } else {
                                    bufferSize += it.payloadLen
                                }
                            }

                            byteBufferSummarySyncData =
                                ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)

                            msgListSummary.forEachIndexed { index, it ->
                                sjUniWatch.wmLog.logE(
                                    TAG,
                                    "sport summary data:" + BtUtils.bytesToHexString(it.originData)
                                )

                                if (index == 0) {
                                    byteBufferSummarySyncData.put(
                                        it.payload.copyOfRange(
                                            17,
                                            it.payload.size
                                        )
                                    )
                                } else {
                                    byteBufferSummarySyncData.put(it.payload)
                                }
                            }
                        }

                        parseSportSummaryData()

                    } else {
                        defaultBackData()
                    }
                }
            })
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmSportSummaryData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

    private fun parseSportSummaryData() {
        sjUniWatch.wmLog.logE(
            TAG,
            "all payload len:" + byteBufferSummarySyncData.limit() + " :data:" + BtUtils.bytesToHexString(
                byteBufferSummarySyncData.array()
            )
        )
        byteBufferSummarySyncData.rewind()
        //0: 只有一个时间戳
        //1：每天一个时间戳
        //2：每小时一个时间戳
        val timestampType = byteBufferSummarySyncData.get().toInt()

        val baseYear = byteBufferSummarySyncData.short.toInt()
        val baseMon = byteBufferSummarySyncData.get().toInt() - 1
        val baseDay = byteBufferSummarySyncData.get().toInt()

        //时间戳
        val timestamp = byteBufferSummarySyncData.int
        val dataLen = byteBufferSummarySyncData.short

        sjUniWatch.wmLog.logD(
            TAG,
            "timestampType:$timestampType --> baseDate:$baseYear$baseMon$baseDay  timestamp:$timestamp  dataLen:$dataLen"
        )

        val calendar = Calendar.getInstance()
        calendar.set(baseYear, baseMon, baseDay, 0, 0, 0)

        val realTimeStamp = calendar.timeInMillis + timestamp

        val activitySportSummaryList = mutableListOf<WmSportSummaryData>()

        while (byteBufferSummarySyncData.hasRemaining()) {

            val year = byteBufferSummarySyncData.short.toInt()
            val mon = byteBufferSummarySyncData.get().toInt() - 1
            val day = byteBufferSummarySyncData.get().toInt()

            val calendar = Calendar.getInstance()
            calendar.set(year, mon, day, 0, 0, 0)

            val dateTime = calendar.timeInMillis

            val startTime = dateTime + byteBufferSummarySyncData.int.toLong()
            val endTime = dateTime + byteBufferSummarySyncData.int.toLong()

            val sportId = byteBufferSummarySyncData.short.toInt()

            val sportType = byteBufferSummarySyncData.get()
            val step = byteBufferSummarySyncData.int
            val calories = byteBufferSummarySyncData.int
            val distance = byteBufferSummarySyncData.int
            val actTime = byteBufferSummarySyncData.short
            val maxRate = byteBufferSummarySyncData.get()
            val averageRate = byteBufferSummarySyncData.get()
            val minRate = byteBufferSummarySyncData.get()

            val rateLimitTime = byteBufferSummarySyncData.short
            val rateUnAerobic = byteBufferSummarySyncData.short

            val rateAerobic = byteBufferSummarySyncData.short
            val rateFatBurning = byteBufferSummarySyncData.short
            val rateWarmUp = byteBufferSummarySyncData.short
            val maxStepSpeed = byteBufferSummarySyncData.short
            val minStepSpeed = byteBufferSummarySyncData.short
            val averageStepSpeed = byteBufferSummarySyncData.short
            val fastPace = byteBufferSummarySyncData.short
            val slowestPace = byteBufferSummarySyncData.short
            val averagePace = byteBufferSummarySyncData.short
            val fastSpeed = byteBufferSummarySyncData.short
            val slowestSpeed = byteBufferSummarySyncData.short
            val averageSpeed = byteBufferSummarySyncData.short
//            val paces:ShortArray = byteBufferSyncData.short

            val wmSportSummaryData = WmSportSummaryData(
                dateTime,
                startTime,
                endTime,
                sportId,
                sportType,
                step,
                calories,
                distance,
                actTime,
                maxRate,
                averageRate,
                minRate,
                rateLimitTime,
                rateUnAerobic,
                rateAerobic,
                rateFatBurning,
                rateWarmUp,
                maxStepSpeed,
                minStepSpeed,
                averageStepSpeed,
                fastPace,
                slowestPace,
                averagePace,
                fastSpeed,
                slowestSpeed,
                averageSpeed
            )

            wmSportSummaryData.timestamp = dateTime

            activitySportSummaryList.add(wmSportSummaryData)
        }

        wmSyncData =
            WmSyncData(
                WmSyncDataType.SPORT_SUMMARY,
                realTimeStamp,
                WmIntervalType.TEN_SECONDS,
                activitySportSummaryList
            )

        lastSyncTime = System.currentTimeMillis()

        wmSyncData?.let {
            sjUniWatch.wmLog.logE(
                TAG,
                "$it"
            )

            it.value.forEach {
                sjUniWatch.wmLog.logD(TAG, "activity detail info:$it")
            }

            sportIndex = 0
            sportSize = it.value.size

            tenSecondsRealtimeRateMap.clearMap()
            tenSecondsStepFrequencyMap.clearMap()
            tenSecondsDistanceMap.clearMap()
            tenSecondsCaloriesMap.clearMap()

            if (sportSize > 0) {
                syncTenSecondsData(URN_SPORT_10S_RATE, it.value[0].startTime, it.value[0].endTime)
            }
        }
    }

    private fun syncTenSecondsData(urn: Byte, startTime: Long, endTime: Long) {

        sjUniWatch.wmLog.logE(
            TAG,
            "++++++++++++++++++++++++++++++++++++++++++START DATA SYNC URN:$urn startTime:$startTime endTime:$endTime +++++++++++++++++++++++++++"
        )

        sjUniWatch.sendReadSubPkObserveNode(
            this,
            CmdHelper.getReadSportSyncData(
                startTime,
                endTime,
                childUrn = urn
            )
        ).subscribe(object :
            Observer<MsgBean> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(it: MsgBean) {
                sjUniWatch.wmLog.logE(TAG, "ten seconds back msg:$urn")

                try {
                    if (it.divideType == DIVIDE_Y_F_2 || it.divideType == DIVIDE_N_2) {

//                        val byteBuffer = ByteBuffer.wrap(
//                            it.payload.copyOfRange(
//                                17,
//                                it.payload.size
//                            )
//                        ).order(ByteOrder.LITTLE_ENDIAN)

                        sjUniWatch.wmLog.logE(
                            TAG,
                            "++++++++++++++++++++++++++++++++++++++++++START PARSE DATA urn:$urn - onComplete +++++++++++++++++++++++++++"
                        )

                        it.payloadPackage?.itemList?.forEach {
                            syncTenSecondsOnePkDataBusiness(it, urn)
                        }

                    } else {

                        val byteBuffer = ByteBuffer.wrap(it.payload).order(ByteOrder.LITTLE_ENDIAN)

                        parseTenSecondsDataNoHead(byteBuffer, urn)

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    syncSportSummaryObserveEmitter?.onError(e)
                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

                when (urn) {
                    URN_SPORT_10S_RATE -> {

                        sjUniWatch.wmLog.logE(
                            TAG,
                            "++++++++++++++++++++++++++++++++++++++++++END DATA URN_SPORT_10S_RATE - onComplete +++++++++++++++++++++++++++"
                        )

                        syncTenSecondsData(URN_SPORT_10S_DISTANCE, startTime, endTime)
                    }

                    URN_SPORT_10S_DISTANCE -> {
                        sjUniWatch.wmLog.logE(
                            TAG,
                            "++++++++++++++++++++++++++++++++++++++++++END DATA URN_SPORT_10S_DISTANCE - onComplete +++++++++++++++++++++++++++"
                        )

                        syncTenSecondsData(URN_SPORT_10S_CALORIES, startTime, endTime)
                    }

                    URN_SPORT_10S_CALORIES -> {
                        sjUniWatch.wmLog.logE(
                            TAG,
                            "++++++++++++++++++++++++++++++++++++++++++END DATA URN_SPORT_10S_CALORIES - onComplete +++++++++++++++++++++++++++"
                        )

                        syncTenSecondsData(URN_SPORT_10S_STEP_FREQUENCY, startTime, endTime)
                    }

                    URN_SPORT_10S_STEP_FREQUENCY -> {

                        sportIndex++

                        sjUniWatch.wmLog.logE(
                            TAG,
                            "++++++++++++++++++++++++++++++++++++++++++END DATA URN_SPORT_10S_STEP_FREQUENCY sportSize：$sportSize ->> sportIndex：$sportIndex  - onComplete +++++++++++++++++++++++++++"
                        )

                        if (sportIndex < sportSize) {
                            wmSyncData?.let {
                                syncTenSecondsData(
                                    URN_SPORT_10S_RATE,
                                    it.value[sportIndex].startTime,
                                    it.value[sportIndex].endTime
                                )
                            }
                        } else {
                            tenSecondAllComplete()
                        }
                    }
                }
            }
        })
    }

    private fun tenSecondAllComplete() {
        wmSyncData?.value?.forEach {

            sjUniWatch.wmLog.logE(
                TAG,
                "search sport startTime:${it.startTime} endTime：${it.endTime}"
            )

            val rateTimeStampList =
                tenSecondsRealtimeRateMap.getBetween(it.startTime, it.endTime)
            val distanceTimeStampList =
                tenSecondsDistanceMap.getBetween(it.startTime, it.endTime)
            val caloriesTimeStampList =
                tenSecondsCaloriesMap.getBetween(it.startTime, it.endTime)
            val stepFrequencyTimeStampList =
                tenSecondsStepFrequencyMap.getBetween(it.startTime, it.endTime)

            sjUniWatch.wmLog.logE(
                TAG,
                "ten seconds rate size2: ${tenSecondsRealtimeRateMap.size()}"
            )

            sjUniWatch.wmLog.logE(
                TAG,
                "ten seconds distance size2: ${tenSecondsDistanceMap.size()}"
            )

            sjUniWatch.wmLog.logE(
                TAG,
                "ten seconds calories size2: ${tenSecondsCaloriesMap.size()}"
            )

            sjUniWatch.wmLog.logE(
                TAG,
                "ten seconds frequency size2: ${tenSecondsStepFrequencyMap.size()}"
            )

            val heartRateList = mutableListOf<WmRealtimeRateData>()
            val distanceList = mutableListOf<WmDistanceData>()
            val caloriesList = mutableListOf<WmCaloriesData>()
            val stepFrequencyList = mutableListOf<WmStepFrequencyData>()

            rateTimeStampList.forEach { timeData ->
                heartRateList.add(timeData.data as WmRealtimeRateData)
            }

            distanceTimeStampList.forEach { timeData ->
                distanceList.add(timeData.data as WmDistanceData)
            }

            caloriesTimeStampList.forEach { timeData ->
                caloriesList.add(timeData.data as WmCaloriesData)
            }

            stepFrequencyTimeStampList.forEach { timeData ->
                stepFrequencyList.add(timeData.data as WmStepFrequencyData)
            }

            it.tenSecondsHeartRate = heartRateList
            it.tenSecondsCaloriesData = caloriesList
            it.tenSecondsDistanceData = distanceList
            it.tenSecondsStepFrequencyData = stepFrequencyList
        }

        syncSportSummaryObserveEmitter?.onNext(wmSyncData)
        syncSportSummaryObserveEmitter?.onComplete()

        sjUniWatch.wmLog.logD(TAG, "final data wmSyncData：$wmSyncData")
    }

    private fun parseTenSecondsDataWithHead(byteBuffer: ByteBuffer, urn: Byte) {

        byteBuffer.rewind()
        tenSecondsTimeType = byteBuffer.get().toInt()
        val baseYear = byteBuffer.short.toInt()
        val baseMon = byteBuffer.get().toInt() - 1
        val baseDay = byteBuffer.get().toInt()

        //时间戳
        val timestamp = byteBuffer.int * 1000
        val dataLen = byteBuffer.short

        sjUniWatch.wmLog.logD(
            TAG,
            "urn:$urn tenSecondsTimestampType:$tenSecondsTimeType --> baseDate:$baseYear$baseMon$baseDay timestamp:$timestamp dataLen:$dataLen dataSize:${byteBuffer.array().size} data:${
                BtUtils.bytesToHexString(
                    byteBuffer.array()
                )
            }"
        )

        val calendar = Calendar.getInstance()
        calendar.set(baseYear, baseMon, baseDay, 0, 0, 0)

        tenSecondsStartTimeStamp = calendar.timeInMillis + timestamp
        tenSecondsDataIndex = 0

        sjUniWatch.wmLog.logE(
            TAG,
            "++++++++++++++++++++++++++++++++++++++++++START PARSE DATA urn:$urn parseTenSecondsDataWithHead:$tenSecondsStartTimeStamp +++++++++++++++++++++++++++"
        )

        parseTenSecondsDataNoHead(byteBuffer, urn)

    }

    private fun parseTenSecondsDataNoHead(byteBuffer: ByteBuffer, urn: Byte) {

        while (byteBuffer.hasRemaining()) {

            tenSecondsRealTimeStamp =
                tenSecondsStartTimeStamp + tenSecondsDataIndex * SYNC_DATA_INTERVAL_TEN_SECONDS

//            sjUniWatch.wmLog.logE(
//                TAG,
//                "urn：$urn tenSecondsDataIndex $tenSecondsDataIndex tenSecondsRealTimeStamp: $tenSecondsRealTimeStamp "
//            )

            when (urn) {
                URN_SPORT_10S_RATE -> {

                    val wmHeartRateData =
                        WmRealtimeRateData(byteBuffer.get().toInt() and 0XFF)

                    if (tenSecondsTimeType == 0) {//只有一个时间戳
                        wmHeartRateData.timestamp =
                            tenSecondsRealTimeStamp

                    }

                    val timeStampRateData =
                        TimestampedData(wmHeartRateData.timestamp, wmHeartRateData)

                    tenSecondsRealtimeRateMap.put(timeStampRateData)
                }

                URN_SPORT_10S_CALORIES -> {

                    val wmCalorieData =
                        WmCaloriesData(byteBuffer.short.toInt())

                    if (tenSecondsTimeType == 0) {//只有一个时间戳
                        wmCalorieData.timestamp = tenSecondsRealTimeStamp
                    }

                    val timeStampRateData =
                        TimestampedData(wmCalorieData.timestamp, wmCalorieData)

                    tenSecondsCaloriesMap.put(timeStampRateData)
                }

                URN_SPORT_10S_DISTANCE -> {
                    val wmDistanceData =
                        WmDistanceData(byteBuffer.get().toInt())

                    if (tenSecondsTimeType == 0) {//只有一个时间戳
                        wmDistanceData.timestamp = tenSecondsRealTimeStamp
                    }

                    val timeStampDistanceData =
                        TimestampedData(wmDistanceData.timestamp, wmDistanceData)

                    tenSecondsDistanceMap.put(timeStampDistanceData)
                }

                URN_SPORT_10S_STEP_FREQUENCY -> {
                    val wmStepFrequencyData =
                        WmStepFrequencyData(byteBuffer.short.toInt())

                    if (tenSecondsTimeType == 0) {//只有一个时间戳

                        wmStepFrequencyData.timestamp = tenSecondsRealTimeStamp
                    }

                    val timeStampRateData =
                        TimestampedData(wmStepFrequencyData.timestamp, wmStepFrequencyData)

                    tenSecondsStepFrequencyMap.put(timeStampRateData)
                }
            }

            tenSecondsDataIndex++
        }
    }

    fun syncOnePkSportSummaryData(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSummarySyncData =
                ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            defaultBackData()
        }
    }

    fun syncTenSecondsOnePkDataBusiness(nodeData: NodeData, urn: Byte) {
        when (urn) {
            URN_SPORT_10S_DISTANCE -> {
                if (nodeData.dataFmt == DataFormat.FMT_BIN) {
                    val byteBufferSyncDataDistance =
                        ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                    sjUniWatch.wmLog.logE(
                        TAG,
                        "++++++++++++++++++++++++++++++++++++++++++START PARSE DATA urn:${URN_SPORT_10S_DISTANCE} - syncTenSecondsDistanceBusiness +++++++++++++++++++++++++++"
                    )

                    parseTenSecondsDataWithHead(byteBufferSyncDataDistance, URN_SPORT_10S_DISTANCE)

                } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
                    defaultBackData()
                }

            }

            URN_SPORT_10S_STEP_FREQUENCY -> {
                if (nodeData.dataFmt == DataFormat.FMT_BIN) {
                    val byteBufferSyncDataFrequency =
                        ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                    sjUniWatch.wmLog.logE(
                        TAG,
                        "++++++++++++++++++++++++++++++++++++++++++START PARSE DATA urn:${URN_SPORT_10S_STEP_FREQUENCY} - syncTenSecondsStepFrequencyBusiness +++++++++++++++++++++++++++"
                    )

                    parseTenSecondsDataWithHead(
                        byteBufferSyncDataFrequency,
                        URN_SPORT_10S_STEP_FREQUENCY
                    )

                } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
                    defaultBackData()
                }

            }

            URN_SPORT_10S_RATE -> {
                if (nodeData.dataFmt == DataFormat.FMT_BIN) {
                    val byteBufferSyncDataRate =
                        ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                    sjUniWatch.wmLog.logE(
                        TAG,
                        "++++++++++++++++++++++++++++++++++++++++++START PARSE DATA urn:${URN_SPORT_10S_RATE} - syncTenSecondsRateBusiness +++++++++++++++++++++++++++"
                    )

                    parseTenSecondsDataWithHead(byteBufferSyncDataRate, URN_SPORT_10S_RATE)

                } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
                    defaultBackData()
                }
            }

            URN_SPORT_10S_CALORIES -> {
                if (nodeData.dataFmt == DataFormat.FMT_BIN) {
                    val byteBufferSyncDataCalorie =
                        ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                    sjUniWatch.wmLog.logE(
                        TAG,
                        "++++++++++++++++++++++++++++++++++++++++++START PARSE DATA urn:${URN_SPORT_10S_CALORIES} - syncTenSecondsCaloriesBusiness +++++++++++++++++++++++++++"
                    )

                    parseTenSecondsDataWithHead(byteBufferSyncDataCalorie, URN_SPORT_10S_CALORIES)

                } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
                    defaultBackData()
                }
            }
        }
    }

    private fun defaultBackData() {
        if (wmSyncData == null) {
            wmSyncData =
                WmSyncData(
                    WmSyncDataType.SPORT_SUMMARY,
                    0,
                    WmIntervalType.TEN_SECONDS,
                    mutableListOf<WmSportSummaryData>()
                )
        }
        syncSportSummaryObserveEmitter?.onNext(wmSyncData)
        syncSportSummaryObserveEmitter?.onComplete()
    }

}