package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmStepData
import com.base.sdk.entity.data.WmSyncData
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper.getReadSportSyncData
import com.sjbt.sdk.spp.cmd.URN_SPORT_STEP
import com.sjbt.sdk.utils.BtUtils
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SyncStepData(val sjUniWatch: SJUniWatch) : AbSyncData<WmSyncData<WmStepData>>() {

    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<WmSyncData<WmStepData>>? = null
    private var observeChangeEmitter: ObservableEmitter<WmSyncData<WmStepData>>? = null
    private val TAG = "SyncStepData"

    private val msgList = mutableSetOf<MsgBean>()
    private val byteBufferSyncData = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)

    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
    }

    override fun syncData(startTime: Long): Single<WmSyncData<WmStepData>> {
        msgList.clear()
        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
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
                        sjUniWatch.wmLog.logE(TAG, "step back msg:" + t)
                        msgList.add(t)
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                        sjUniWatch.wmLog.logE(TAG, "back msg:" + msgList.size)

                        msgList.forEach {
                            sjUniWatch.wmLog.logE(
                                TAG,
                                "step data:" + BtUtils.bytesToHexString(it.originData)
                            )
                            byteBufferSyncData.put(it.payload)
                        }

                        //0: 只有一个时间戳
                        //1：每天一个时间戳
                        //2：每小时一个时间戳
                        val timestampType = byteBufferSyncData.get().toInt()
                        val baseDate = byteBufferSyncData.int
                        //时间戳
                        val timestamp = byteBufferSyncData.int
                        val dataLen = byteBufferSyncData.short

                        sjUniWatch.wmLog.logD(
                            TAG,
                            "timestampType:$timestampType --> baseDate:$baseDate"
                        )

                    }
                })
        }
    }

    override var observeSyncData: Observable<WmSyncData<WmStepData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}