package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.entity.settings.WmSleepSettings
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.DataFormat
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.SYNC_DATA_INTERVAL_HOUR
import com.sjbt.sdk.spp.cmd.URN_SPORT_SLEEP
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.TimeUtils
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SyncSleepData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<WmSleepData>>(),
    ReadSubPkMsg {
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<WmSyncData<WmSleepData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmSleepData>>? = null

    private val TAG = "SyncSleepData"
    private val msgList = mutableSetOf<MsgBean>()
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
    }

    override fun syncData(startTime: Long): Single<WmSyncData<WmSleepData>> {

        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                this,
                CmdHelper.getReadSportSyncData(
                    startTime,
                    lastSyncTime,
                    childUrn = URN_SPORT_SLEEP
                )
            ).subscribe(object :
                Observer<MsgBean> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: MsgBean) {
                    sjUniWatch.wmLog.logE(TAG, "sleep record back msg:$t")
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
                                "sleep record data:" + BtUtils.bytesToHexString(it.originData)
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

    override var observeSyncData: Observable<WmSyncData<WmSleepData>> =
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

        val realTimeStamp = calendar.timeInMillis / 1000 + timestamp

        val sleepDataList = mutableListOf<WmSleepData>()

        var dataIndex: Int = 0

        while (byteBufferSyncData.hasRemaining()) {

            val isEnable = byteBufferSyncData.get().toInt() == 1

            val startHour = byteBufferSyncData.get().toInt()
            val startMin = byteBufferSyncData.get().toInt()
            val endHour = byteBufferSyncData.get().toInt()
            val endMin = byteBufferSyncData.get().toInt()

            val wmSleepSettings = WmSleepSettings(
                isEnable, startHour,
                startMin,
                endHour,
                endMin
            )

            val sleepYear = byteBufferSyncData.short.toInt()
            val sleepMon = byteBufferSyncData.get().toInt() - 1
            val sleepDay = byteBufferSyncData.get().toInt()

            val calendar = Calendar.getInstance()
            calendar.set(sleepYear, sleepMon, sleepDay, 0, 0, 0)

            var dateStamp = (calendar.timeInMillis / 1000).toInt()
            var bedTime = byteBufferSyncData.int + dateStamp
            var getUpTime = byteBufferSyncData.int + dateStamp

            var totalSleepMinutes: Int = byteBufferSyncData.int

            var sleepType: Int = byteBufferSyncData.get().toInt()

            var awakeSleepMinutes: Int = byteBufferSyncData.short.toInt()
            var lightSleepMinutes: Int = byteBufferSyncData.short.toInt()
            var deepSleepMinutes: Int = byteBufferSyncData.short.toInt()
            var remSleepMinutes: Int = byteBufferSyncData.short.toInt()

            var awakeSleepCount: Int = byteBufferSyncData.short.toInt()
            var lightSleepCount: Int = byteBufferSyncData.short.toInt()
            var deepSleepCount: Int = byteBufferSyncData.short.toInt()
            var remSleepCount: Int = byteBufferSyncData.short.toInt()

            var awakePercentage: Int = byteBufferSyncData.short.toInt()
            var lightSleepPercentage: Int = byteBufferSyncData.short.toInt()
            var deepSleepPercentage: Int = byteBufferSyncData.short.toInt()
            var remSleepPercentage: Int = byteBufferSyncData.short.toInt()

            var sleepScore: Int = byteBufferSyncData.short.toInt()

            val wmSleepSummary = WmSleepSummary(
                dateStamp,
                bedTime,
                getUpTime,
                totalSleepMinutes,
                sleepType,
                awakeSleepMinutes,
                lightSleepMinutes,
                deepSleepMinutes,
                remSleepMinutes,
                awakeSleepCount,
                lightSleepCount,
                deepSleepCount,
                remSleepCount,
                awakePercentage,
                lightSleepPercentage,
                deepSleepPercentage,
                remSleepPercentage,
                sleepScore
            )

            val sleepItems = mutableListOf<WmSleepItem>()

            while (byteBufferSyncData.hasRemaining()) {
                val status = byteBufferSyncData.get().toInt()
                val duration = byteBufferSyncData.short.toInt()
                val sleepItem = WmSleepItem(status, duration)
                sleepItems.add(sleepItem)
            }

            val wmSleepData = WmSleepData(wmSleepSettings, wmSleepSummary, sleepItems)

            if (timestampType == 0) {//只有一个时间戳
                sjUniWatch.wmLog.logD(
                    TAG,
                    "start base date:" + TimeUtils.date2String(Date(realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR))
                )

                wmSleepData.timestamp =
                    realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "sleep record data: $dataIndex -> $wmSleepData"
            )

            sleepDataList.add(wmSleepData)
            dataIndex++
        }

        val wmSyncData =
            WmSyncData(
                WmSyncDataType.HEART_RATE_FIVE_MINUTES,
                realTimeStamp,
                WmIntervalType.FIVE_MINUTES,
                sleepDataList
            )

        activityObserveEmitter?.onSuccess(wmSyncData)
        lastSyncTime = System.currentTimeMillis()
    }

    fun syncRealHeartRateBusiness(nodeData: NodeData) {

        if (nodeData.dataFmt == DataFormat.FMT_BIN) {
            byteBufferSyncData = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)
            parseStepData()
        } else if (nodeData.dataFmt == DataFormat.FMT_ERRCODE || nodeData.dataFmt == DataFormat.FMT_NODATA) {
            val wmSyncData =
                WmSyncData(
                    WmSyncDataType.STEP,
                    0,
                    WmIntervalType.ONE_HOUR,
                    mutableListOf<WmSleepData>()
                )

            activityObserveEmitter?.onSuccess(wmSyncData)
        }
    }

}