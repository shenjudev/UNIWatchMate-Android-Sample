package com.sjbt.sdk.app

import com.base.sdk.entity.apps.AlarmRepeatOption
import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.app.AbAppAlarm
import com.sjbt.sdk.*
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class AppAlarm(val sjUniWatch: SJUniWatch) : AbAppAlarm(), ReadSubPkMsg,
    ExceptionStateListener {

    private var observeAlarmListEmitter: ObservableEmitter<List<WmAlarm>>? = null
    private var updateAlarmListEmitter: SingleEmitter<Boolean>? = null
    private var getAlarmListEmitter: SingleEmitter<List<WmAlarm>>? = null
    private val TAG = "AppAlarm"
    private var getData = false
    private var hasNext = false
    private val msgList = mutableSetOf<MsgBean>()

    override fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    override fun getHasNext(): Boolean {
        return hasNext
    }

    override fun updateAlarmList(alarms: List<WmAlarm>): Single<Boolean> {
        return Single.create {
            updateAlarmListEmitter = it
            if (alarms.size < 8) {
                sjUniWatch.sendWriteNodeCmdList(getWriteUpdateAlarmCmd(alarms))
            } else {
                sjUniWatch.observableMtu.subscribe { mtu ->
                    val payloadPackage = getWriteUpdateAlarmCmd(alarms)

                   sjUniWatch.sendWriteSubpackageNodeCmdList(
                        mtu,
                        payloadPackage
                    )
                }
            }
        }
    }

    override var observeAlarmList: Observable<List<WmAlarm>> = Observable.create {
        observeAlarmListEmitter = it
    }

    override fun observeDisconnectState() {

        updateAlarmListEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }

        getAlarmListEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }
    }

    override fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")
        when (nodeData.urn[2]) {
            URN_APP_ALARM_LIST -> {
                updateAlarmListEmitter?.onError(WmTimeOutException("$TAG URN_APP_ALARM_LIST TIMEOUT"))
            }
        }
    }

    override var getAlarmList: Single<List<WmAlarm>> = Single.create {
        getAlarmListEmitter = it
        getData = true
        getAlarmSubPkList()
    }

    fun appUpdateAlarm() {
        getData = false
        getAlarmSubPkList()
    }

    private fun getAlarmSubPkList() {
        msgList.clear()

        sjUniWatch.sendReadSubPkObserveNode(this, getReadAlarmListCmd()).subscribe(object :
            Observer<MsgBean> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: MsgBean) {
                msgList.add(t)
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {
                try {
                    if (msgList.isNotEmpty()) {
                        var byteBuffer =
                            ByteBuffer.allocate(ALARM_TOTAL_LEN * 10).order(ByteOrder.LITTLE_ENDIAN)

                        msgList.forEachIndexed { index, msgBean ->

                            if ((msgBean.isNodeMsg) && (msgBean.divideType == DIVIDE_N_2 || msgBean.divideType == DIVIDE_Y_F_2)) {
                                msgBean.payloadPackage?.let {
                                    it.itemList.forEach { node ->
                                        byteBuffer.put(node.data)
                                    }
                                }
                            } else {
                                byteBuffer.put(msgBean.payload)
                            }
                        }

                        syncAlarmListSuccess(parseAlarmList(byteBuffer))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    getAlarmListEmitter?.onError(WmTimeOutException("get alarm list time out exception"))
                }
            }
        })
    }

    private fun parseAlarmList(byteBuffer: ByteBuffer): MutableList<WmAlarm> {
        val alarmList = mutableListOf<WmAlarm>()
        val count = byteBuffer.limit() / ALARM_TOTAL_LEN
        byteBuffer.rewind()
        sjUniWatch.wmLog.logD(TAG, "Alarm Count：$count")

        if (count > 0) {
            while (byteBuffer.hasRemaining()) {
                val id = byteBuffer.get().toInt()
                val nameArray = ByteArray(ALARM_NAME_LEN)
                byteBuffer.get(nameArray)
                val name = String(
                    nameArray.filter { it != 0.toByte() }.toByteArray(),
                    StandardCharsets.UTF_8
                )

                val hour = byteBuffer.get().toInt()
                val minute = byteBuffer.get().toInt()
                val repeatOptions = byteBuffer.get().toInt()
                val isEnable = byteBuffer.get().toInt()

                val wmAlarm =
                    WmAlarm(
                        name,
                        hour,
                        minute,
                        AlarmRepeatOption.fromValue(repeatOptions)
                    )

                wmAlarm.isOn = isEnable == 1

                sjUniWatch.wmLog.logD(TAG, "Alarm INFO:$wmAlarm ")

                if (id != 0) {
                    alarmList.add(wmAlarm)
                }
            }
        }
        return alarmList
    }

    private fun syncAlarmListSuccess(alarmList: List<WmAlarm>) {
        alarmList?.let {
            if (getData) {
                getAlarmListEmitter?.onSuccess(it)
                getData = false
            } else {
                observeAlarmListEmitter?.onNext(it)
            }
        }
    }

    fun alarmBusiness(nodeData: NodeData) {

        try {
            when (nodeData.urn[2]) {
                URN_APP_ALARM_LIST -> {
                    if (nodeData.data.size == 1 && nodeData.dataFmt == DataFormat.FMT_ERRCODE) {
                        updateAlarmListEmitter?.let {
                            it.onSuccess(nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
                        }
                    } else {
                        val alarmList = mutableListOf<WmAlarm>()
                        val count = nodeData.dataLen / ALARM_TOTAL_LEN
                        sjUniWatch.wmLog.logD(TAG, "Alarm Count：$count")
                        val byteBuffer =
                            ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                        parseAlarmList(byteBuffer)
                        syncAlarmListSuccess(alarmList)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getAlarmListEmitter?.onError(WmTimeOutException("time out exception"))
        }

    }

    /**
     * 更新闹钟列表
     */
    private fun getWriteUpdateAlarmCmd(alarms: List<WmAlarm>): PayloadPackage {
        val payloadPackage = PayloadPackage()

        sjUniWatch.wmLog.logD(TAG, "alarms:$alarms")
        val totalAlarms = mutableListOf<WmAlarm>()
        totalAlarms.addAll(alarms)

        val byteBuffer: ByteBuffer =
            ByteBuffer.allocate(ALARM_TOTAL_LEN * totalAlarms.size)
                .order(ByteOrder.LITTLE_ENDIAN)

        totalAlarms.forEach { alarm ->
            byteBuffer.put(0)
            val originNameArray = alarm.alarmName.toByteArray(StandardCharsets.UTF_8)
            byteBuffer.put(originNameArray.copyOf(ALARM_NAME_LEN))

            byteBuffer.put(alarm.hour.toByte())
            byteBuffer.put(alarm.minute.toByte())
            byteBuffer.put(AlarmRepeatOption.toValue(alarm.repeatOptions).toByte())
            byteBuffer.put(
                if (alarm.isOn) {
                    1.toByte()
                } else {
                    0.toByte()
                }
            )
        }

        payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_1, URN_1), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 获取闹钟列表
     */
    private fun getReadAlarmListCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_1, URN_1), byteBuffer.array())
        return payloadPackage
    }

}