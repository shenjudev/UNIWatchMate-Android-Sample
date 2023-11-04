package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.SYNC_DATA_INTERVAL
import com.sjbt.sdk.spp.cmd.URN_SPORT_ACTIVITY_LEN
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
class SyncActivityDurationData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<WmActivityDurationData>>(),ReadSubPkMsg {

    private var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityDurateObserveEmitter: SingleEmitter<WmSyncData<WmActivityDurationData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmActivityDurationData>>? = null

    private val TAG = "SyncActivityDurationData"
    private val msgList = mutableSetOf<MsgBean>()
    private var hasNext: Boolean = false
    private lateinit var byteBufferSyncData: ByteBuffer

    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
       return hasNext
    }

    fun onTimeOut(nodeData: NodeData) {

    }

    override fun syncData(startTime: Long): Single<WmSyncData<WmActivityDurationData>> {

        return Single.create { emitter ->
            activityDurateObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(this,
                CmdHelper.getReadSportSyncData(
                    startTime, lastSyncTime,
                    childUrn = URN_SPORT_ACTIVITY_LEN
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

        while (byteBufferSyncData.hasRemaining()) {

            val wmActivityDurationData = WmActivityDurationData(byteBufferSyncData.get().toInt() and 0XFF)

            if (timestampType == 0) {//只有一个时间戳
                sjUniWatch.wmLog.logD(
                    TAG,
                    "start base date:" + TimeUtils.date2String(Date(realTimeStamp + (byteBufferSyncData.position() - 12) * SYNC_DATA_INTERVAL))
                )

                wmActivityDurationData.timestamp =
                    realTimeStamp + (byteBufferSyncData.position() - 12) * SYNC_DATA_INTERVAL
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "real time rate data: ${byteBufferSyncData.position()} -> ${wmActivityDurationData}"
            )

            activityDurationDataList.add(wmActivityDurationData)
        }

        val wmSyncData =
            WmSyncData(
                WmSyncDataType.OXYGEN,
                realTimeStamp,
                WmIntervalType.FIVE_MINUTES,
                activityDurationDataList
            )

        activityDurateObserveEmitter?.onSuccess(wmSyncData)
        lastSyncTime = System.currentTimeMillis()

        sjUniWatch.wmLog.logE(
            TAG,
            "${wmSyncData}"
        )
    }

    fun syncActivityDurationDataBusiness(byteArray: ByteArray) {
        byteBufferSyncData = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
        parseStepData()
    }

}