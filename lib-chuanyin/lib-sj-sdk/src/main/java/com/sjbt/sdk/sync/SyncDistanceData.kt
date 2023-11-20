package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.DataFormat
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.TimeUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SyncDistanceData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<WmDistanceData>>(),
    ReadSubPkMsg {

    var lastSyncTime: Long = 0
    private var activityObserveEmitter: ObservableEmitter<WmSyncData<WmDistanceData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmDistanceData>>? = null

    private val TAG = "SyncDistanceData"
    private val msgList = mutableListOf<MsgBean>()
    private var hasNext: Boolean = false
    private lateinit var byteBufferSyncData: ByteBuffer

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
//        activityObserveEmitter?.onError(WmTimeOutException())
    }

    override fun syncData(startTime: Long): Observable<WmSyncData<WmDistanceData>> {
        msgList.clear()
        return Observable.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                this,
                CmdHelper.getReadSportSyncData(
                    startTime, lastSyncTime,
                    childUrn = URN_SPORT_DISTANCE
                )
            ).subscribe(object :
                Observer<MsgBean> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: MsgBean) {
                    sjUniWatch.wmLog.logE(TAG, "distance back msg:$t")
                    msgList.add(t)
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                    sjUniWatch.wmLog.logE(TAG, "back msg:" + msgList.size)

                    if (msgList.size > 0) {

                        if (msgList.size == 1) {
                            msgList[0].payloadPackage?.itemList?.forEach {
                                syncDistanceBusiness(it)
                            }

                        } else {
                            var bufferSize = 0
                            msgList.forEach {
                                if (it.divideType == DIVIDE_N_2 || it.divideType == DIVIDE_Y_F_2) {
                                    bufferSize += it.payloadLen - 17
                                } else {
                                    bufferSize += it.payloadLen
                                }
                            }

                            byteBufferSyncData =
                                ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)

                            msgList.forEachIndexed { index, it ->
                                sjUniWatch.wmLog.logE(
                                    TAG,
                                    "distance data:" + BtUtils.bytesToHexString(it.originData)
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
                        }

                        parseStepData()
                    }
                }
            })
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmDistanceData>> =
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

        val distanceList = mutableListOf<WmDistanceData>()

        var dataIndex = 0

        while (byteBufferSyncData.hasRemaining()) {

            val wmDistanceData = WmDistanceData(byteBufferSyncData.get().toInt() and 0XFF)

            if (timestampType == 0) {//只有一个时间戳
                sjUniWatch.wmLog.logD(
                    TAG,
                    "start base date:" + TimeUtils.date2String(Date(realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR))
                )

                wmDistanceData.timestamp =
                    realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "distance data: ${byteBufferSyncData.position()} -> ${wmDistanceData}"
            )

            distanceList.add(wmDistanceData)
        }

        val wmSyncData =
            WmSyncData(
                WmSyncDataType.DISTANCE,
                realTimeStamp,
                WmIntervalType.FIVE_MINUTES,
                distanceList
            )

        activityObserveEmitter?.onNext(wmSyncData)
        activityObserveEmitter?.onComplete()
        lastSyncTime = System.currentTimeMillis()

        sjUniWatch.wmLog.logE(
            TAG,
            "${wmSyncData}"
        )
    }

    fun syncDistanceBusiness(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            val wmSyncData =
                WmSyncData(
                    WmSyncDataType.DISTANCE,
                    0,
                    WmIntervalType.ONE_HOUR,
                    mutableListOf<WmDistanceData>()
                )

            activityObserveEmitter?.onNext(wmSyncData)
            activityObserveEmitter?.onComplete()
        }
    }
}