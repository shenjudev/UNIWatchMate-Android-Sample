package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmIntervalType
import com.base.sdk.entity.data.WmStepData
import com.base.sdk.entity.data.WmSyncData
import com.base.sdk.entity.data.WmSyncDataType
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.DataFormat
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper.getReadSportSyncData
import com.sjbt.sdk.spp.cmd.DIVIDE_N_2
import com.sjbt.sdk.spp.cmd.DIVIDE_Y_F_2
import com.sjbt.sdk.spp.cmd.SYNC_DATA_INTERVAL_HOUR
import com.sjbt.sdk.spp.cmd.URN_SPORT_STEP
import com.sjbt.sdk.utils.BtUtils
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SyncStepData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<WmStepData>>(),
    ReadSubPkMsg {

    var lastSyncTime: Long = 0
    private var stepObserveEmitter: ObservableEmitter<WmSyncData<WmStepData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmStepData>>? = null
    private val TAG = "SyncStepData"

    private val msgList = mutableListOf<MsgBean>()
    private lateinit var byteBufferSyncData: ByteBuffer

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    private var hasNext: Boolean = false
    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
        stepObserveEmitter?.onError(WmTimeOutException("$TAG time out exception"))
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msg")
    }

    override fun syncData(startTime: Long): Observable<WmSyncData<WmStepData>> {
        msgList.clear()

        return Observable.create { emitter ->
            stepObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                this,
                getReadSportSyncData(
                    startTime,
                    0,
                    childUrn = URN_SPORT_STEP
                )
            )
                .subscribe(object :
                    Observer<MsgBean> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: MsgBean) {
                        sjUniWatch.wmLog.logE(TAG, "step back msg:$t")
                        msgList.add(t)
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                        sjUniWatch.wmLog.logE(TAG, "step back msg:" + msgList.size)

                        if (msgList.size > 0) {
                            if (msgList.size == 1) {
                                msgList[0].payloadPackage?.itemList?.forEach {
                                    syncStepBusiness(it)
                                }

                            } else {
                                var bufferSize = 0
                                msgList.forEachIndexed() { index, it ->
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
                                        "step data:" + BtUtils.bytesToHexString(it.originData)
                                    )

                                    if (it.divideType == DIVIDE_N_2 || it.divideType == DIVIDE_Y_F_2) {
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
                                
                                parseStepData()
                            }

                        } else {
                            defaultBack()
                        }
                    }
                })
        }
    }

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

        val stepList = mutableListOf<WmStepData>()

        var dataIndex = 0

        while (byteBufferSyncData.hasRemaining()) {

            val wmStepData = WmStepData(byteBufferSyncData.int)

            if (timestampType == 0) {
                wmStepData.timestamp =
                    realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "step data: $dataIndex -> ${wmStepData}"
            )

            stepList.add(wmStepData)
            dataIndex++
        }

        val wmSyncData =
            WmSyncData(WmSyncDataType.STEP, realTimeStamp, WmIntervalType.ONE_HOUR, stepList)

        stepObserveEmitter?.onNext(wmSyncData)
        stepObserveEmitter?.onComplete()

        lastSyncTime = System.currentTimeMillis()

        sjUniWatch.wmLog.logE(
            TAG,
            "${wmSyncData}"
        )
    }

    override var observeSyncData: Observable<WmSyncData<WmStepData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

    fun syncStepBusiness(nodeData: NodeData) {
        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseStepData()
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            defaultBack()
        }
    }

    private fun defaultBack() {
        val wmSyncData =
            WmSyncData(
                WmSyncDataType.STEP,
                0,
                WmIntervalType.ONE_HOUR,
                mutableListOf<WmStepData>()
            )

        stepObserveEmitter?.onNext(wmSyncData)
        stepObserveEmitter?.onComplete()
    }

}