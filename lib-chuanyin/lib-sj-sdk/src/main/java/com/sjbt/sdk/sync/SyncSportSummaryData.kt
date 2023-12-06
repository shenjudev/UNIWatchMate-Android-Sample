package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ExceptionStateListener
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
    AbSyncData<WmSyncData<WmSportSummaryData>>(), ReadSubPkMsg ,
    ExceptionStateListener {
    var lastSyncTime: Long = 0
    private var syncSportSummaryObserveEmitter: ObservableEmitter<WmSyncData<WmSportSummaryData>>? =
        null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmSportSummaryData>>? = null

    private var wmSyncData: WmSyncData<WmSportSummaryData>? = null
    private val TAG = "SyncSportSummaryData"
    private val msgListSummary = mutableListOf<MsgBean>()

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

    var withTenSeconds = true

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun observeDisconnectState() {
        syncSportSummaryObserveEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }
    }

    override fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        observeDisconnectState()
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")
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
//                    sjUniWatch.wmLog.logE(TAG, "sport summary back msg:$t")
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
//                                sjUniWatch.wmLog.logE(
//                                    TAG,
//                                    "sport summary data:" + BtUtils.bytesToHexString(it.originData)
//                                )

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

                            parseSportSummaryData()
                        }

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

        val realTimeStamp = (calendar.timeInMillis + timestamp) / 1000 * 1000

        val activitySportSummaryList = mutableListOf<WmSportSummaryData>()

        while (byteBufferSummarySyncData.hasRemaining()) {

            val year = byteBufferSummarySyncData.short.toInt()
            val mon = byteBufferSummarySyncData.get().toInt() - 1
            val day = byteBufferSummarySyncData.get().toInt()

            val calendar = Calendar.getInstance()
            calendar.set(year, mon, day, 0, 0, 0)

            val dateTime = calendar.timeInMillis / 1000 * 1000

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

            sjUniWatch.wmLog.logD(TAG, "activity detail info：${wmSportSummaryData}")

            activitySportSummaryList.add(wmSportSummaryData)
        }

        wmSyncData =
            WmSyncData(
                WmSyncDataType.SPORT_SUMMARY,
                realTimeStamp,
                WmIntervalType.UNKNOWN,
                activitySportSummaryList
            )

        lastSyncTime = System.currentTimeMillis()

        wmSyncData?.let {

            sportIndex = 0
            sportSize = it.value.size

            tenSecondsRealtimeRateMap.clearMap()
            tenSecondsStepFrequencyMap.clearMap()
            tenSecondsDistanceMap.clearMap()
            tenSecondsCaloriesMap.clearMap()

            if (withTenSeconds) {
                if (sportSize > 0) {

                    val syncTimes = mutableListOf<SyncTime>()

                    it.value.forEach { summary ->
                        val syncTime = SyncTime(summary.startTime, summary.endTime)
                        syncTimes.add(syncTime)
                    }

                    syncTenSecondsData(URN_SPORT_10S_RATE, syncTimes)
                }
            } else {
                wmSyncData?.value?.forEach { summary ->
                    val heartRateList = mutableListOf<WmRealtimeRateData>()
                    val distanceList = mutableListOf<WmDistanceData>()
                    val caloriesList = mutableListOf<WmCaloriesData>()
                    val stepFrequencyList = mutableListOf<WmStepFrequencyData>()

                    summary.tenSecondsHeartRate = heartRateList
                    summary.tenSecondsCaloriesData = caloriesList
                    summary.tenSecondsDistanceData = distanceList
                    summary.tenSecondsStepFrequencyData = stepFrequencyList
                }

                syncSportSummaryObserveEmitter?.onNext(wmSyncData)
                syncSportSummaryObserveEmitter?.onComplete()

                sjUniWatch.wmLog.logD(TAG, "All data wmSyncData：$wmSyncData")
            }
        }
    }

    private fun syncTenSecondsData(urn: Byte, syncTimes: List<SyncTime>) {

        sjUniWatch.wmLog.logE(
            TAG,
            "++++++++++++++++++++++++++++++++++++++++++START DATA SYNC URN:$urn syncTime:${syncTimes} +++++++++++++++++++++++++++"
        )

        sjUniWatch.sendReadSubPkObserveNode(
            this,
            CmdHelper.getReadSportMultiTimesSyncData(
                syncTimes,
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

                        it.payloadPackage?.itemList?.forEach {
                            parseTenSecondsDataType(it, urn)
                        }

                    } else {

                        val byteBuffer = ByteBuffer.wrap(it.payload).order(ByteOrder.LITTLE_ENDIAN)

                        parseTenSecondsMiddleData(byteBuffer, urn)

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
                            "++++++++++++++++++++++++++++++++++++++++++END SYNC URN_SPORT_10S_RATE - onComplete +++++++++++++++++++++++++++"
                        )

                        sportIndex++

                        syncTenSecondsData(URN_SPORT_10S_DISTANCE, syncTimes)
                    }

                    URN_SPORT_10S_DISTANCE -> {
                        sjUniWatch.wmLog.logE(
                            TAG,
                            "++++++++++++++++++++++++++++++++++++++++++END SYNC URN_SPORT_10S_DISTANCE - onComplete +++++++++++++++++++++++++++"
                        )

                        sportIndex++

                        syncTenSecondsData(URN_SPORT_10S_CALORIES, syncTimes)
                    }

                    URN_SPORT_10S_CALORIES -> {
                        sjUniWatch.wmLog.logE(
                            TAG,
                            "++++++++++++++++++++++++++++++++++++++++++END SYNC URN_SPORT_10S_CALORIES - onComplete +++++++++++++++++++++++++++"
                        )
                        sportIndex++

                        syncTenSecondsData(URN_SPORT_10S_STEP_FREQUENCY, syncTimes)
                    }

                    URN_SPORT_10S_STEP_FREQUENCY -> {

                        sjUniWatch.wmLog.logE(
                            TAG,
                            "++++++++++++++++++++++++++++++++++++++++++END SYNC URN_SPORT_10S_STEP_FREQUENCY - onComplete +++++++++++++++++++++++++++"
                        )

                        sportIndex++
                        tenSecondAllComplete()
                    }
                }
            }
        })
    }

    private fun tenSecondAllComplete() {

        sjUniWatch.wmLog.logE(
            TAG,
            "ten seconds rate map size: ${tenSecondsRealtimeRateMap.size()}"
        )

        sjUniWatch.wmLog.logE(
            TAG,
            "ten seconds distance map size: ${tenSecondsDistanceMap.size()}"
        )

        sjUniWatch.wmLog.logE(
            TAG,
            "ten seconds calories map size: ${tenSecondsCaloriesMap.size()}"
        )

        sjUniWatch.wmLog.logE(
            TAG,
            "ten seconds frequency map size: ${tenSecondsStepFrequencyMap.size()}"
        )

        wmSyncData?.value?.forEach {

            val rateTimeStampList =
                tenSecondsRealtimeRateMap.getBetween(it.startTime, it.endTime)
            val distanceTimeStampList =
                tenSecondsDistanceMap.getBetween(it.startTime, it.endTime)
            val caloriesTimeStampList =
                tenSecondsCaloriesMap.getBetween(it.startTime, it.endTime)
            val stepFrequencyTimeStampList =
                tenSecondsStepFrequencyMap.getBetween(it.startTime, it.endTime)

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

        sjUniWatch.wmLog.logD(TAG, "All data wmSyncData：$wmSyncData")
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

        val calendar = Calendar.getInstance()
        calendar.set(baseYear, baseMon, baseDay, 0, 0, 0)

        tenSecondsStartTimeStamp = calendar.timeInMillis + timestamp
        tenSecondsDataIndex = 0

        parseTenSecondsMiddleData(byteBuffer, urn)

    }

    private fun parseTenSecondsMiddleData(byteBuffer: ByteBuffer, urn: Byte) {

        while (byteBuffer.hasRemaining()) {

            tenSecondsRealTimeStamp =
                tenSecondsStartTimeStamp + tenSecondsDataIndex * SYNC_DATA_INTERVAL_TEN_SECONDS

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
            parseSportSummaryData()
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            defaultBackData()
        }
    }

    fun parseTenSecondsDataType(nodeData: NodeData, urn: Byte) {
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
                    WmIntervalType.UNKNOWN,
                    mutableListOf<WmSportSummaryData>()
                )
        }
        syncSportSummaryObserveEmitter?.onNext(wmSyncData)
        syncSportSummaryObserveEmitter?.onComplete()
    }

}