package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.DataFormat
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.SYNC_DATA_INTERVAL_HOUR
import com.sjbt.sdk.spp.cmd.URN_SPORT_CALORIES
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.TimeUtils
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SyncCaloriesData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<WmCaloriesData>>(),
    ReadSubPkMsg {
    var lastSyncTime: Long = 0
    private var caloriesObserveEmitter: SingleEmitter<WmSyncData<WmCaloriesData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmCaloriesData>>? = null

    private val TAG = "SyncCaloriesData"
    private val msgList = mutableSetOf<MsgBean>()
    private var hasNext: Boolean = false
    private lateinit var byteBufferSyncData: ByteBuffer

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
    }

    override fun syncData(startTime: Long): Single<WmSyncData<WmCaloriesData>> {
        return Single.create { emitter ->
            caloriesObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                this,
                CmdHelper.getReadSportSyncData(
                    startTime, lastSyncTime,
                    childUrn = URN_SPORT_CALORIES
                )
            ).subscribe(object :
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
                                "step data:" + BtUtils.bytesToHexString(it.originData)
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
                }
            })
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmCaloriesData>> =
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

        val caloriesList = mutableListOf<WmCaloriesData>()

        var dataIndex = 0

        while (byteBufferSyncData.hasRemaining()) {

            val wmCaloriesData = WmCaloriesData(byteBufferSyncData.int)

            if (timestampType == 0) {//只有一个时间戳
                sjUniWatch.wmLog.logD(
                    TAG,
                    "start base date:" + TimeUtils.date2String(Date(realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR))
                )

                wmCaloriesData.timestamp =
                    realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "step data: ${byteBufferSyncData.position()} -> ${wmCaloriesData}"
            )

            caloriesList.add(wmCaloriesData)
            dataIndex ++
        }

        val wmSyncData =
            WmSyncData(WmSyncDataType.CALORIE, realTimeStamp, WmIntervalType.ONE_HOUR, caloriesList)

        caloriesObserveEmitter?.onSuccess(wmSyncData)
        lastSyncTime = System.currentTimeMillis()

        sjUniWatch.wmLog.logE(
            TAG,
            "${wmSyncData}"
        )
    }

    fun syncCaloriesBusiness(nodeData: NodeData) {

        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseStepData()
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            val wmSyncData =
                WmSyncData(WmSyncDataType.STEP, 0, WmIntervalType.ONE_HOUR, mutableListOf<WmCaloriesData>())

            caloriesObserveEmitter?.onSuccess(wmSyncData)
        }
    }

}