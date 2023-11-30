package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.sync.AbSyncData
import com.google.gson.Gson
import com.sjbt.sdk.ReadSubPkMsg
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.DataFormat
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.SportTypeData
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.TimeUtils
import com.sjbt.sdk.utils.readSportTypeJsonFromAssets
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * 每日活动时长
 */
class SyncDailyActivityDurationData(val sjUniWatch: SJUniWatch) :
    AbSyncData<WmSyncData<WmDailyActivityDurationData>>(), ReadSubPkMsg {

    var lastSyncTime: Long = 0
    private var activityDurationObserveEmitter: ObservableEmitter<WmSyncData<WmDailyActivityDurationData>>? =
        null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmDailyActivityDurationData>>? =
        null

    private val TAG = "SyncDailyActivityDurationData"
    private val msgList = mutableListOf<MsgBean>()
    private var hasNext: Boolean = false
    private lateinit var byteBufferSyncData: ByteBuffer

    private val sportTypeMap = mutableMapOf<Int, Int>()

    private fun getSportTypeById(sportId: Int): Int {
        sportTypeMap.get(sportId)?.let {
            return it
        } ?: kotlin.run {
            return -1
        }
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

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        activityDurationObserveEmitter?.onError(WmTimeOutException("$TAG time out exception"))
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")

    }

    override fun syncData(startTime: Long): Observable<WmSyncData<WmDailyActivityDurationData>> {
        msgList.clear()
        return Observable.create { emitter ->
            activityDurationObserveEmitter = emitter

            readSportTypeJsonFromAssets(sjUniWatch.mContext)?.let {
                sjUniWatch.wmLog.logE(TAG, "readJsonFromAssets:$it")
                val sportTypeData = Gson().fromJson(it, SportTypeData::class.java)
                sportTypeData.sports.forEach { sportType ->
                    sportTypeMap[sportType.id] = sportType.sport_type
                }
            }

            sjUniWatch.sendReadSubPkObserveNode(
                this,
                CmdHelper.getReadSportSyncData(
                    startTime, 0,
                    childUrn = URN_SPORT_DAILY_ACTIVITY_LEN
                )
            ).subscribe(object :
                Observer<MsgBean> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: MsgBean) {
//                    sjUniWatch.wmLog.logE(TAG, "activity duration back msg:$t")
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
//                                sjUniWatch.wmLog.logE(
//                                    TAG,
//                                    "activity duration data:" + BtUtils.bytesToHexString(it.originData)
//                                )

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

    override var observeSyncData: Observable<WmSyncData<WmDailyActivityDurationData>> =
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
        val baseMon = byteBufferSyncData.get().toInt()
        val baseDay = byteBufferSyncData.get().toInt()

        //相对时间戳
        val timestamp = byteBufferSyncData.int
        val dataLen = byteBufferSyncData.short

        sjUniWatch.wmLog.logD(
            TAG,
            "timestampType:$timestampType --> baseDate:$baseYear$baseMon$baseDay  timestamp:$timestamp  dataLen:$dataLen"
        )

        val calendar = Calendar.getInstance()
        calendar.set(baseYear, baseMon, baseDay, 0, 0, 0)

        val realTimeStamp = (calendar.timeInMillis + timestamp) / 1000 * 1000

        val activityDurationDataList = mutableListOf<WmDailyActivityDurationData>()

        var dataIndex = 0
        while (byteBufferSyncData.hasRemaining()) {

            val sportId = byteBufferSyncData.short.toInt()

            val wmDailyActivityDurationData =
                WmDailyActivityDurationData(getSportTypeById(sportId), byteBufferSyncData.int)

            if (timestampType == 0) {//只有一个时间戳
                sjUniWatch.wmLog.logD(
                    TAG,
                    "start base date:" + TimeUtils.date2String(Date(realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR))
                )

                wmDailyActivityDurationData.timestamp =
                    realTimeStamp + dataIndex * SYNC_DATA_INTERVAL_HOUR
            }

            sjUniWatch.wmLog.logD(
                TAG,
                "daily activity duration data sportId: $sportId -> ${wmDailyActivityDurationData}"
            )

            activityDurationDataList.add(wmDailyActivityDurationData)

            dataIndex++
        }

        val result = activityDurationDataList.groupBy { it.sportType }
            .mapValues { it.value.sumOf { data -> data.duration } }
            .map { WmDailyActivityDurationData(it.key, it.value) }

        val wmSyncData =
            WmSyncData(
                WmSyncDataType.ACTIVITY_DURATION,
                realTimeStamp,
                WmIntervalType.ONE_HOUR,
                result
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
                mutableListOf<WmDailyActivityDurationData>()
            )

        activityDurationObserveEmitter?.onNext(wmSyncData)
        activityDurationObserveEmitter?.onComplete()
    }

}