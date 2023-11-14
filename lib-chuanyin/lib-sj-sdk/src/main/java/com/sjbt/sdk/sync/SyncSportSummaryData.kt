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
    private var syncSportSummaryObserveEmitter: SingleEmitter<WmSyncData<WmSportSummaryData>>? =
        null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmSportSummaryData>>? = null

    private var wmSyncData: WmSyncData<WmSportSummaryData>? = null
    private val TAG = "SyncSportSummaryData"
    private val msgList = mutableSetOf<MsgBean>()
    private var hasNext: Boolean = false
    private lateinit var byteBufferSyncData: ByteBuffer

    private var mStartTime: Long = 0
    private var mEndTime: Long = System.currentTimeMillis()

    private val tenSecondsRealtimeRateMap = TimestampedMap()
    private val tenSecondsDistanceMap = TimestampedMap()
    private val tenSecondsStepFrequencyMap = TimestampedMap()
    private val tenSecondsCaloriesMap = TimestampedMap()

    private val mTenUrnArray: ByteArray = byteArrayOf(
        URN_SPORT_10S_RATE,
        URN_SPORT_10S_STEP_FREQUENCY,
        URN_SPORT_10S_DISTANCE,
        URN_SPORT_10S_CALORIES
    )

    private var tenSecondsRequestIndex = 0

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

    override fun syncData(startTime: Long): Single<WmSyncData<WmSportSummaryData>> {
        mStartTime = startTime
        tenSecondsRequestIndex = 0
        return Single.create { emitter ->
            syncSportSummaryObserveEmitter = emitter
            msgList.clear()
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
                    msgList.add(t)
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                    sjUniWatch.wmLog.logE(TAG, "back msg:" + msgList.size)

                    if (msgList.size > 0) {

                        var bufferSize = 0
                        msgList.forEach {
                            bufferSize += it.payloadLen
                        }

                        byteBufferSyncData =
                            ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)

                        msgList.forEachIndexed { index, it ->
                            sjUniWatch.wmLog.logE(
                                TAG,
                                "sport summary data:" + BtUtils.bytesToHexString(it.originData)
                            )

                            if (index == 0) {
                                byteBufferSyncData.put(
                                    it.payload.copyOfRange(
                                        17,
                                        it.payload.size
                                    )
                                )
                            } else {
                                byteBufferSyncData.put(it.payload)
                            }
                        }

                        parseSportSummaryData()
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

        //时间戳
        val timestamp = byteBufferSyncData.int
        val dataLen = byteBufferSyncData.short

        sjUniWatch.wmLog.logD(
            TAG,
            "timestampType:$timestampType --> baseDate:$baseYear$baseMon$baseDay  timestamp:$timestamp  dataLen:$dataLen"
        )

        val calendar = Calendar.getInstance()
        calendar.set(baseYear, baseMon, baseDay, 0, 0, 0)

        val realTimeStamp = calendar.timeInMillis + timestamp

        val activitySportSummaryList = mutableListOf<WmSportSummaryData>()

        while (byteBufferSyncData.hasRemaining()) {

            val year = byteBufferSyncData.short.toInt()
            val mon = byteBufferSyncData.get().toInt() - 1
            val day = byteBufferSyncData.get().toInt()

            val calendar = Calendar.getInstance()
            calendar.set(year, mon, day, 0, 0, 0)

            val dateTime = calendar.timeInMillis

            val startTime = dateTime + byteBufferSyncData.int.toLong()
            val endTime = dateTime + byteBufferSyncData.int.toLong()

            val sportId = byteBufferSyncData.short.toInt()

            val sportType = byteBufferSyncData.get()
            val step = byteBufferSyncData.int
            val calories = byteBufferSyncData.int
            val distance = byteBufferSyncData.int
            val actTime = byteBufferSyncData.short
            val maxRate = byteBufferSyncData.get()
            val averageRate = byteBufferSyncData.get()
            val minRate = byteBufferSyncData.get()

            val rateLimitTime = byteBufferSyncData.short
            val rateUnAerobic = byteBufferSyncData.short

            val rateAerobic = byteBufferSyncData.short
            val rateFatBurning = byteBufferSyncData.short
            val rateWarmUp = byteBufferSyncData.short
            val maxStepSpeed = byteBufferSyncData.short
            val minStepSpeed = byteBufferSyncData.short
            val averageStepSpeed = byteBufferSyncData.short
            val fastPace = byteBufferSyncData.short
            val slowestPace = byteBufferSyncData.short
            val averagePace = byteBufferSyncData.short
            val fastSpeed = byteBufferSyncData.short
            val slowestSpeed = byteBufferSyncData.short
            val averageSpeed = byteBufferSyncData.short
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

            activitySportSummaryList.add(wmSportSummaryData)
        }

        wmSyncData =
            WmSyncData(
                WmSyncDataType.SPORT_SUMMARY,
                realTimeStamp,
                WmIntervalType.FIVE_MINUTES,
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

            syncTenSecondsData(mTenUrnArray[tenSecondsRequestIndex])
        }
    }

    private fun syncTenSecondsData(urn: Byte) {
        msgList.clear()

        when (urn) {
            URN_SPORT_10S_RATE -> {
                tenSecondsRealtimeRateMap.clearMap()
            }

            URN_SPORT_10S_STEP_FREQUENCY -> {
                tenSecondsRealtimeRateMap.clearMap()
            }

            URN_SPORT_10S_DISTANCE -> {
                tenSecondsDistanceMap.clearMap()
            }

            URN_SPORT_10S_CALORIES -> {
                tenSecondsCaloriesMap.clearMap()
            }
        }

        sjUniWatch.sendReadSubPkObserveNode(
            this,
            CmdHelper.getReadSportSyncData(
                mStartTime,
                mEndTime,
                childUrn = urn
            )
        ).subscribe(object :
            Observer<MsgBean> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: MsgBean) {
                sjUniWatch.wmLog.logE(TAG, "ten seconds back msg:$urn")
                msgList.add(t)
            }

            override fun onError(e: Throwable) {
            }

            override fun onComplete() {
                sjUniWatch.wmLog.logE(TAG, "ten seconds back msg:$urn msg size:" + msgList.size)

                if (msgList.size > 0) {

                    var bufferSize = 0
                    msgList.forEachIndexed { index, it ->
                        if (it.divideType == DIVIDE_Y_F_2) {
                            bufferSize += it.payloadLen - 17
                        } else {
                            bufferSize += it.payloadLen
                        }
                    }

                    sjUniWatch.wmLog.logE(TAG, "urn：$urn buffer size:$bufferSize")

                    byteBufferSyncData =
                        ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)

                    msgList.forEachIndexed { index, it ->

                        if (it.divideType == DIVIDE_Y_F_2 && index == 0) {
                            sjUniWatch.wmLog.logE(
                                TAG,
                                "sport summary payload urn$urn f0:" + BtUtils.bytesToHexString(
                                    it.payload.copyOfRange(
                                        17,
                                        it.payload.size
                                    )
                                )
                            )
                            byteBufferSyncData.put(
                                it.payload.copyOfRange(
                                    17,
                                    it.payload.size
                                )
                            )
                        } else if (it.divideType == DIVIDE_Y_F_2 && index != 0) {
                            sjUniWatch.wmLog.logE(
                                TAG,
                                "sport summary payload urn$urn f1:" + BtUtils.bytesToHexString(
                                    it.payload.copyOfRange(
                                        28,
                                        it.payload.size
                                    )
                                )
                            )
                            byteBufferSyncData.put(
                                it.payload.copyOfRange(
                                    28,
                                    it.payload.size
                                )
                            )
                        } else {
                            sjUniWatch.wmLog.logE(
                                TAG,
                                "sport summary payload urn$urn fn:" + BtUtils.bytesToHexString(it.payload)
                            )
                            byteBufferSyncData.put(it.payload)
                        }
                    }

                    parseTenSecondsData(urn)

                    tenSecondsRequestIndex++

                    sjUniWatch.wmLog.logE(TAG, "tenSecondsRequestIndex：$tenSecondsRequestIndex")

                    if (tenSecondsRequestIndex < mTenUrnArray.size) {
                        syncTenSecondsData(mTenUrnArray[tenSecondsRequestIndex])
                    } else {
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


                        syncSportSummaryObserveEmitter?.onSuccess(wmSyncData)
                    }

                    sjUniWatch.wmLog.logD(TAG, "final data wmSyncData：$wmSyncData")

                }
            }
        })
    }

    private fun parseTenSecondsData(urn: Byte) {
        sjUniWatch.wmLog.logE(
            TAG,
            "ten seconds all payload len:" + byteBufferSyncData.limit() + " :data:" + BtUtils.bytesToHexString(
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

        //时间戳
        val timestamp = byteBufferSyncData.int
        val dataLen = byteBufferSyncData.short

        sjUniWatch.wmLog.logD(
            TAG,
            "timestampType:$timestampType --> baseDate:$baseYear$baseMon$baseDay  timestamp:$timestamp  dataLen:$dataLen"
        )

        val calendar = Calendar.getInstance()
        calendar.set(baseYear, baseMon, baseDay, 0, 0, 0)

        val realTimeStamp = calendar.timeInMillis + timestamp

        var dataIndex = 0
        while (byteBufferSyncData.hasRemaining()) {
            when (urn) {
                URN_SPORT_10S_RATE -> {
                    val wmHeartRateData =
                        WmRealtimeRateData(byteBufferSyncData.get().toInt() and 0XFF)

                    if (timestampType == 0) {//只有一个时间戳

                        wmHeartRateData.timestamp =
                            realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_TEN_SECONDS
                    }

                    sjUniWatch.wmLog.logD(
                        TAG,
                        "real time rate data: $dataIndex -> ${wmHeartRateData}"
                    )

                    val timeStampRateData =
                        TimestampedData(wmHeartRateData.timestamp, wmHeartRateData)

                    tenSecondsRealtimeRateMap.put(timeStampRateData)
                }

                URN_SPORT_10S_CALORIES -> {
                    val wmCalorieData =
                        WmCaloriesData(byteBufferSyncData.short.toInt())

                    if (timestampType == 0) {//只有一个时间戳

                        wmCalorieData.timestamp =
                            realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_TEN_SECONDS
                    }

                    sjUniWatch.wmLog.logD(
                        TAG,
                        "calorie data: $dataIndex -> $wmCalorieData"
                    )

                    val timeStampRateData =
                        TimestampedData(wmCalorieData.timestamp, wmCalorieData)

                    tenSecondsCaloriesMap.put(timeStampRateData)
                }

                URN_SPORT_10S_DISTANCE -> {
                    val wmDistanceData =
                        WmDistanceData(byteBufferSyncData.get().toInt())

                    if (timestampType == 0) {//只有一个时间戳

                        wmDistanceData.timestamp =
                            realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_TEN_SECONDS
                    }

                    sjUniWatch.wmLog.logD(
                        TAG,
                        "calorie data: $dataIndex -> $wmDistanceData"
                    )

                    val timeStampRateData =
                        TimestampedData(wmDistanceData.timestamp, wmDistanceData)

                    tenSecondsDistanceMap.put(timeStampRateData)
                }

                URN_SPORT_10S_STEP_FREQUENCY -> {
                    val wmStepFrequencyData =
                        WmStepFrequencyData(byteBufferSyncData.short.toInt())

                    if (timestampType == 0) {//只有一个时间戳
                        wmStepFrequencyData.timestamp =
                            realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_TEN_SECONDS
                    }

                    sjUniWatch.wmLog.logD(
                        TAG,
                        "step frequency: $dataIndex -> $wmStepFrequencyData"
                    )

                    val timeStampRateData =
                        TimestampedData(wmStepFrequencyData.timestamp, wmStepFrequencyData)

                    tenSecondsStepFrequencyMap.put(timeStampRateData)
                }
            }

            dataIndex++
        }

    }

    fun syncSportSummaryDataBusiness(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseSportSummaryData()
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {

            if (wmSyncData == null) {
                wmSyncData =
                    WmSyncData(
                        WmSyncDataType.STEP,
                        0,
                        WmIntervalType.ONE_HOUR,
                        mutableListOf<WmSportSummaryData>()
                    )
            }

            syncSportSummaryObserveEmitter?.onSuccess(wmSyncData)
        }
    }

    fun syncTenSecondsDistanceBusiness(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseTenSecondsData(URN_SPORT_10S_DISTANCE)
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            if (tenSecondsRequestIndex < mTenUrnArray.size) {
                tenSecondsRequestIndex++
                syncTenSecondsData(mTenUrnArray[tenSecondsRequestIndex])
            } else {
                if (wmSyncData == null) {
                    wmSyncData =
                        WmSyncData(
                            WmSyncDataType.STEP,
                            0,
                            WmIntervalType.ONE_HOUR,
                            mutableListOf<WmSportSummaryData>()
                        )
                }
                syncSportSummaryObserveEmitter?.onSuccess(wmSyncData)
            }
        }
    }

    fun syncTenSecondsCaloriesBusiness(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseTenSecondsData(URN_SPORT_10S_CALORIES)
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            if (tenSecondsRequestIndex < mTenUrnArray.size) {
                tenSecondsRequestIndex++
                syncTenSecondsData(mTenUrnArray[tenSecondsRequestIndex])
            } else {
                if (wmSyncData == null) {
                    wmSyncData =
                        WmSyncData(
                            WmSyncDataType.STEP,
                            0,
                            WmIntervalType.ONE_HOUR,
                            mutableListOf<WmSportSummaryData>()
                        )
                }
                syncSportSummaryObserveEmitter?.onSuccess(wmSyncData)
            }
        }
    }

    fun syncTenSecondsRateBusiness(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseTenSecondsData(URN_SPORT_10S_RATE)
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            if (tenSecondsRequestIndex < mTenUrnArray.size) {
                tenSecondsRequestIndex++
                syncTenSecondsData(mTenUrnArray[tenSecondsRequestIndex])
            } else {
                if (wmSyncData == null) {
                    wmSyncData =
                        WmSyncData(
                            WmSyncDataType.STEP,
                            0,
                            WmIntervalType.ONE_HOUR,
                            mutableListOf<WmSportSummaryData>()
                        )
                }
                syncSportSummaryObserveEmitter?.onSuccess(wmSyncData)
            }
        }
    }

    fun syncTenSecondsStepFrequencyBusiness(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseTenSecondsData(URN_SPORT_10S_STEP_FREQUENCY)
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            if (tenSecondsRequestIndex < mTenUrnArray.size) {
                tenSecondsRequestIndex++
                syncTenSecondsData(mTenUrnArray[tenSecondsRequestIndex])
            } else {
                if (wmSyncData == null) {
                    wmSyncData =
                        WmSyncData(
                            WmSyncDataType.STEP,
                            0,
                            WmIntervalType.ONE_HOUR,
                            mutableListOf<WmSportSummaryData>()
                        )
                }

                syncSportSummaryObserveEmitter?.onSuccess(wmSyncData)
            }
        }
    }

}