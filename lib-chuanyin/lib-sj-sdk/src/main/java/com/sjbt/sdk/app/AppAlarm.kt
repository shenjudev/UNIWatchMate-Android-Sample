package com.sjbt.sdk.app

import com.base.sdk.entity.apps.AlarmRepeatOption
import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.app.AbAppAlarm
import com.sjbt.sdk.ALARM_LEN
import com.sjbt.sdk.ALARM_NAME_LEN
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class AppAlarm(val sjUniWatch: SJUniWatch) : AbAppAlarm(),
    ExceptionStateListener {

    private var observeAlarmListEmitter: ObservableEmitter<List<WmAlarm>>? = null
    private var updateAlarmEmitter: SingleEmitter<Boolean>? = null
    private var getAlarmEmitter: SingleEmitter<List<WmAlarm>>? = null
    private val TAG = "AppAlarm"
    private var getData = false

    override fun updateAlarmList(alarms: List<WmAlarm>): Single<Boolean> {
        return Single.create {
            updateAlarmEmitter = it
            sjUniWatch.sendWriteNodeCmdList(getWriteUpdateAlarmCmd(alarms))
        }
    }

    override var observeAlarmList: Observable<List<WmAlarm>> = Observable.create {
        observeAlarmListEmitter = it
    }

    override fun observeDisconnectState() {

        updateAlarmEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }

        getAlarmEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }
    }

    override fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")
        when (nodeData.urn[2]) {
            URN_APP_ALARM_LIST -> {
                updateAlarmEmitter?.onError(WmTimeOutException("$TAG URN_APP_ALARM_LIST TIMEOUT"))
            }
        }
    }

    override var getAlarmList: Single<List<WmAlarm>> = Single.create {
        getData = true
        getAlarmEmitter = it
        sjUniWatch.sendReadNodeCmdList(getReadAlarmListCmd())
    }

    private fun syncAlarmListSuccess(alarmList: List<WmAlarm>) {
        alarmList?.let {
            if (getData) {
                getAlarmEmitter?.onSuccess(it)
                getData = false
            } else {
                observeAlarmListEmitter?.onNext(it)
            }
        }
    }

    fun appUpdateAlarm() {
        getData = false
        sjUniWatch.sendReadNodeCmdList(getReadAlarmListCmd())
    }

    fun alarmBusiness(nodeData: NodeData) {
        when (nodeData.urn[2]) {

            URN_APP_ALARM_LIST -> {

                if (nodeData.data.size == 1 && nodeData.dataFmt == DataFormat.FMT_ERRCODE) {
                    updateAlarmEmitter?.let {
                        it.onSuccess(nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
                    }
                } else {
                    val alarmList = mutableListOf<WmAlarm>()
                    val count = nodeData.dataLen / ALARM_LEN
                    sjUniWatch.wmLog.logD(TAG, "Alarm Count：$count")
                    val byteBuffer = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                    if (count > 0) {
                        while(byteBuffer.hasRemaining()){
                            val id = byteBuffer.get().toInt()
                            val nameArray = ByteArray(ALARM_NAME_LEN)
                            byteBuffer.get(nameArray)
                            val name = String(nameArray.takeWhile { it.toInt() != 0 }.toByteArray(), StandardCharsets.UTF_8)

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

                    syncAlarmListSuccess(alarmList)
                }
            }

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
            ByteBuffer.allocate(ALARM_LEN * totalAlarms.size)
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