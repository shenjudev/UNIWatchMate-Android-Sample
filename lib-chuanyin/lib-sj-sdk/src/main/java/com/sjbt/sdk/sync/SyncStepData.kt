package com.sjbt.sdk.sync

import android.text.format.DateUtils
import com.base.sdk.entity.data.WmIntervalType
import com.base.sdk.entity.data.WmStepData
import com.base.sdk.entity.data.WmSyncData
import com.base.sdk.entity.data.WmSyncDataType
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper.getReadSportSyncData
import com.sjbt.sdk.spp.cmd.URN_SPORT_STEP
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.TimeUtils
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SyncStepData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<WmStepData>>(),
    ReadSubPkMsg {

    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var stepObserveEmitter: SingleEmitter<WmSyncData<WmStepData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmStepData>>? = null
    private val TAG = "SyncStepData"

    private val msgList = mutableSetOf<MsgBean>()
    private lateinit var byteBufferSyncData: ByteBuffer

    override fun isSupport(): Boolean {
        return isActionSupport
    }

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
    }

    override fun syncData(startTime: Long): Single<WmSyncData<WmStepData>> {
        msgList.clear()

        return Single.create { emitter ->
            stepObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                this,
                getReadSportSyncData(
                    startTime,
                    lastSyncTime,
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
                        sjUniWatch.wmLog.logE(TAG, "back msg:" + msgList.size)

                        if (msgList.size > 0) {

                            var bufferSize = 0
                            msgList.forEach {
                                bufferSize += it.payloadLen
                            }

                            byteBufferSyncData = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)

                            msgList.forEachIndexed { index, it ->
                                sjUniWatch.wmLog.logE(
                                    TAG,
                                    "step data:" + BtUtils.bytesToHexString(it.originData)
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

        val baseYear = byteBufferSyncData.short
        val baseMon = byteBufferSyncData.get()
        val baseDay = byteBufferSyncData.get()

        //时间戳
        val timestamp = byteBufferSyncData.int
        val dataLen = byteBufferSyncData.short

        sjUniWatch.wmLog.logD(
            TAG,
            "timestampType:$timestampType --> baseDate:$baseYear$baseMon$baseDay  timestamp:$timestamp  dataLen:$dataLen"
        )

        //        byteArray.copyOfRange(10, byteArray.lastIndex).forEachIndexed { index, it ->
        //            sjUniWatch.wmLog.logD(TAG, "step data: $index -> ${it.toInt() and 0xFF}")
        //        }

        val calendar = Calendar.getInstance()
        calendar.set(baseYear.toInt(), baseMon.toInt()-1, baseDay.toInt(), 0, 0, 0)

        val realTimeStamp = calendar.timeInMillis + timestamp

        val stepList = mutableListOf<WmStepData>()

        while (byteBufferSyncData.hasRemaining()) {

            val wmStepData = WmStepData(byteBufferSyncData.get().toInt() and 0XFF)

            if (timestampType == 0) {//只有一个时间戳
                sjUniWatch.wmLog.logD(
                    TAG,
                    "start base date:" + TimeUtils.date2String(Date(realTimeStamp + (byteBufferSyncData.position() - 12) * 60 * 60 * 1000))
                )

                wmStepData.timestamp =
                    realTimeStamp + (byteBufferSyncData.position() - 12) * 60 * 60 * 1000
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "step data: ${byteBufferSyncData.position()} -> ${wmStepData}"
            )

            stepList.add(wmStepData)
        }

        val wmSyncData =
            WmSyncData(WmSyncDataType.STEP, realTimeStamp, WmIntervalType.ONE_HOUR, stepList)

        stepObserveEmitter?.onSuccess(wmSyncData)


        sjUniWatch.wmLog.logE(
            TAG,
            "${wmSyncData}"
        )
    }

    override var observeSyncData: Observable<WmSyncData<WmStepData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

    fun syncStepBusiness(byteArray: ByteArray) {

        byteBufferSyncData = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
        parseStepData()

//        //0: 只有一个时间戳
//        //1：每天一个时间戳
//        //2：每小时一个时间戳
//        val timestampType = byteBufferSyncData.get().toInt()
//
//        val baseYear = byteBufferSyncData.short
//        val baseMon = byteBufferSyncData.get()
//        val baseDay = byteBufferSyncData.get()
//
//        //时间戳
//        val timestamp = byteBufferSyncData.int
//        val dataLen = byteBufferSyncData.short
//
//        sjUniWatch.wmLog.logD(
//            TAG,
//            "timestampType:$timestampType --> baseDate:$baseYear$baseMon$baseDay  timestamp:$timestamp  dataLen:$dataLen"
//        )
//
////        byteArray.copyOfRange(10, byteArray.lastIndex).forEachIndexed { index, it ->
////            sjUniWatch.wmLog.logD(TAG, "step data: $index -> ${it.toInt() and 0xFF}")
////        }
//
//        val calendar = Calendar.getInstance()
//        calendar.set(baseYear.toInt(), baseMon.toInt(), baseDay.toInt(), 0, 0, 0)
//
//        val realTimeStamp = calendar.timeInMillis + timestamp
//
//        sjUniWatch.wmLog.logD(TAG, "start base date:" + TimeUtils.date2String(Date(realTimeStamp)))
//
//        val stepList = mutableListOf<WmStepData>()
//
//        while (byteBufferSyncData.hasRemaining()) {
//
//            val wmStepData = WmStepData(byteBufferSyncData.get().toInt() and 0XFF)
//
//            if (timestampType == 0) {
//                sjUniWatch.wmLog.logD(
//                    TAG,
//                    "start base date:" + TimeUtils.date2String(Date(realTimeStamp + (byteBufferSyncData.position() - 12) * 60 * 60 * 1000))
//                )
//
//                wmStepData.timestamp =
//                    realTimeStamp + (byteBufferSyncData.position() - 12) * 60 * 60 * 1000
//            }
//
//            sjUniWatch.wmLog.logD(
//                TAG,
//                "step data: ${byteBufferSyncData.position()} -> ${wmStepData}"
//            )
//
//            stepList.add(wmStepData)
//        }
//
//        val wmSyncData =
//            WmSyncData(WmSyncDataType.STEP, realTimeStamp, WmIntervalType.ONE_HOUR, stepList)
//
//        stepObserveEmitter?.onSuccess(wmSyncData)
//
//
//        sjUniWatch.wmLog.logE(
//            TAG,
//            "${wmSyncData}"
//        )

    }

}