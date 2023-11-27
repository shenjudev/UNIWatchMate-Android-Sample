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
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * 每小时持续活动的时长
 */
class SyncActivityDurationData(val sjUniWatch: SJUniWatch) :
    AbSyncData<WmSyncData<WmActivityDurationData>>(), ReadSubPkMsg {

    var lastSyncTime: Long = 0
    private var activityDurationObserveEmitter: ObservableEmitter<WmSyncData<WmActivityDurationData>>? =
        null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmActivityDurationData>>? = null

    private val TAG = "SyncActivityDurationData"
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

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        activityDurationObserveEmitter?.onError(WmTimeOutException())
//        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")

    }

    override fun syncData(startTime: Long): Observable<WmSyncData<WmActivityDurationData>> {
        msgList.clear()
        return Observable.create { emitter ->
            activityDurationObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                this,
                CmdHelper.getReadSportSyncData(
                    startTime, 0,
                    childUrn = URN_SPORT_ACTIVITY_LEN
                )
            ).subscribe(object :
                Observer<MsgBean> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: MsgBean) {
                    sjUniWatch.wmLog.logE(TAG, "activity duration back msg:$t")
                    msgList.add(t)
                }

                override fun onError(e: Throwable) {
                }

                override fun onComplete() {
                    sjUniWatch.wmLog.logE(TAG, "back msg:" + msgList.size)

                    if (msgList.size > 0) {

                        if (msgList.size == 1) {
                            msgList[0].payloadPackage?.itemList?.forEach {
                                syncActivityDurationDataBusiness(it)
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
                                    "activity duration data:" + BtUtils.bytesToHexString(it.originData)
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

                            parseStepData()
                        }
                    } else {
                        defaultBack()
                    }
                }
            })
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmActivityDurationData>> =
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

        val activityDurationDataList = mutableListOf<WmActivityDurationData>()

        var dataIndex = 0
        while (byteBufferSyncData.hasRemaining()) {

            val wmActivityDurationData = WmActivityDurationData(byteBufferSyncData.int)

            if (timestampType == 0) {//只有一个时间戳
                sjUniWatch.wmLog.logD(
                    TAG,
                    "start base date:" + TimeUtils.date2String(Date(realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR))
                )

                wmActivityDurationData.timestamp =
                    realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "activity duration data: $dataIndex -> ${wmActivityDurationData}"
            )

            activityDurationDataList.add(wmActivityDurationData)

            dataIndex++
        }

        val wmSyncData =
            WmSyncData(
                WmSyncDataType.ACTIVITY_DURATION,
                realTimeStamp,
                WmIntervalType.ONE_HOUR,
                activityDurationDataList
            )

        activityDurationObserveEmitter?.onNext(wmSyncData)
        activityDurationObserveEmitter?.onComplete()

        lastSyncTime = System.currentTimeMillis()

        sjUniWatch.wmLog.logE(
            TAG,
            "${wmSyncData}"
        )
    }

    fun syncActivityDurationDataBusiness(nodeData: NodeData) {
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
                WmSyncDataType.ACTIVITY_DURATION,
                0,
                WmIntervalType.ONE_HOUR,
                mutableListOf<WmActivityDurationData>()
            )

        activityDurationObserveEmitter?.onNext(wmSyncData)
        activityDurationObserveEmitter?.onComplete()
    }

}