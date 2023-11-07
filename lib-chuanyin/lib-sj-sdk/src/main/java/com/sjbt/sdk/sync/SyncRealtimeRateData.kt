package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.DataFormat
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.exception.SjException
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.TimeUtils
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SyncRealtimeRateData(val sjUniWatch: SJUniWatch) :
    AbSyncData<WmSyncData<WmRealtimeRateData>>(), ReadSubPkMsg {

    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var realTimeHeartRateObserveEmitter: SingleEmitter<WmSyncData<WmRealtimeRateData>>? =
        null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmRealtimeRateData>>? = null

    private val TAG = "SyncRealtimeRateData"
    private val msgList = mutableSetOf<MsgBean>()
    private var hasNext: Boolean = false
    private lateinit var byteBufferSyncData: ByteBuffer

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
    }

    override fun syncData(startTime: Long): Single<WmSyncData<WmRealtimeRateData>> {

        return Single.create { emitter ->
            realTimeHeartRateObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                this,
                CmdHelper.getReadSportSyncData(
                    startTime, lastSyncTime,
                    childUrn = URN_SPORT_RATE,
                    grandSon = URN_SPORT_RATE_REALTIME
                )
            ).subscribe(object :
                Observer<MsgBean> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: MsgBean) {
                    sjUniWatch.wmLog.logE(TAG, "real time rate back msg:$t")
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
                                "real time rate data:" + BtUtils.bytesToHexString(it.originData)
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

                        parseStepData()
                    }
                }
            })
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmRealtimeRateData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

    private fun parseStepData() {
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

        val realTimeRateList = mutableListOf<WmRealtimeRateData>()

        while (byteBufferSyncData.hasRemaining()) {

            val wmHeartRateData = WmRealtimeRateData(byteBufferSyncData.get().toInt() and 0XFF)

            if (timestampType == 0) {//只有一个时间戳
                sjUniWatch.wmLog.logD(
                    TAG,
                    "start base date:" + TimeUtils.date2String(Date(realTimeStamp + (byteBufferSyncData.position() - 12) * SYNC_DATA_INTERVAL))
                )

                wmHeartRateData.timestamp =
                    realTimeStamp + (byteBufferSyncData.position() - 12) * SYNC_DATA_INTERVAL
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "real time rate data: ${byteBufferSyncData.position()} -> ${wmHeartRateData}"
            )

            realTimeRateList.add(wmHeartRateData)
        }

        val wmSyncData =
            WmSyncData(
                WmSyncDataType.HEART_RATE_FIVE_MINUTES,
                realTimeStamp,
                WmIntervalType.FIVE_MINUTES,
                realTimeRateList
            )

        realTimeHeartRateObserveEmitter?.onSuccess(wmSyncData)
        lastSyncTime = System.currentTimeMillis()

        sjUniWatch.wmLog.logE(
            TAG,
            "${wmSyncData}"
        )
    }

    fun syncRealHeartRateBusiness(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseStepData()
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            val wmSyncData =
                WmSyncData(WmSyncDataType.STEP, 0, WmIntervalType.ONE_HOUR, mutableListOf<WmRealtimeRateData>())

            realTimeHeartRateObserveEmitter?.onSuccess(wmSyncData)
        }
    }

}