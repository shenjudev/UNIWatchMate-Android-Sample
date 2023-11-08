package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.DataFormat
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_SPORT_SUMMARY
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
    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var syncSportSummaryObserveEmitter: SingleEmitter<WmSyncData<WmSportSummaryData>>? =
        null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmSportSummaryData>>? = null

    private val TAG = "SyncSportSummaryData"
    private val msgList = mutableSetOf<MsgBean>()
    private var hasNext: Boolean = false
    private lateinit var byteBufferSyncData: ByteBuffer

    override fun isSupport(): Boolean {
        return isActionSupport
    }

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

        return Single.create { emitter ->
            syncSportSummaryObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                this,
                CmdHelper.getReadSportSyncData(
                    startTime,
                    lastSyncTime,
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
                                        it.payload.lastIndex
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
            val mon = byteBufferSyncData.get().toInt()
            val day = byteBufferSyncData.get().toInt()

            val startTime = byteBufferSyncData.int
            val endTime = byteBufferSyncData.int

            val sportId = byteBufferSyncData.short.toInt()

            val sportType= byteBufferSyncData.get()
            val step = byteBufferSyncData.int
            val calories = byteBufferSyncData.int
            val distance = byteBufferSyncData.int
            val actTime= byteBufferSyncData.short
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
            val averageSpeed = byteBufferSyncData.short
//            val paces:ShortArray = byteBufferSyncData.short

            val calendar = Calendar.getInstance()
            calendar.set(year, mon, day, 0, 0, 0)

            val dateTime = calendar.timeInMillis

            val wmSportSummaryData = WmSportSummaryData(dateTime, startTime, endTime, sportId, sportType, step, calories, distance, actTime, maxRate, averageRate, minRate, rateLimitTime, rateUnAerobic, rateAerobic, rateFatBurning, rateWarmUp, maxStepSpeed, minStepSpeed, averageStepSpeed, fastPace, slowestPace, averageSpeed )

            activitySportSummaryList.add(wmSportSummaryData)
        }

        val wmSyncData =
            WmSyncData(
                WmSyncDataType.OXYGEN,
                realTimeStamp,
                WmIntervalType.FIVE_MINUTES,
                activitySportSummaryList
            )

        syncSportSummaryObserveEmitter?.onSuccess(wmSyncData)
        lastSyncTime = System.currentTimeMillis()

        sjUniWatch.wmLog.logE(
            TAG,
            "${wmSyncData}"
        )
    }

    fun syncSportSummaryDataBusiness(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseSportSummaryData()
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            val wmSyncData =
                WmSyncData(WmSyncDataType.STEP, 0, WmIntervalType.ONE_HOUR, mutableListOf<WmSportSummaryData>())
            syncSportSummaryObserveEmitter?.onSuccess(wmSyncData)
        }
    }
}