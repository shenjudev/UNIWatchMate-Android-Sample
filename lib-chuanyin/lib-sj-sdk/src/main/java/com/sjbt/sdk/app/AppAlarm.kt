package com.sjbt.sdk.app

import com.base.sdk.entity.apps.AlarmRepeatOption
import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.port.app.AbAppAlarm
import com.sjbt.sdk.ALARM_NAME_LEN
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class AppAlarm(val sjUniWatch: SJUniWatch) : AbAppAlarm() {

    private var _isSupport: Boolean = true
    private var observeAlarmListEmitter: ObservableEmitter<List<WmAlarm>>? = null
    private var updateAlarmEmitter: SingleEmitter<Boolean>? = null

    private val TAG = "AppAlarm"

    override fun isSupport(): Boolean {
        return _isSupport
    }

    override fun updateAlarmList(alarms: List<WmAlarm>): Single<Boolean> {
        return Single.create {
            updateAlarmEmitter = it
            sjUniWatch.sendWriteNodeCmdList(getWriteUpdateAlarmCmd(alarms))
        }
    }

    override var observeAlarmList: Observable<List<WmAlarm>> = Observable.create {
        observeAlarmListEmitter = it
        sjUniWatch.sendReadNodeCmdList(getReadAlarmListCmd())
    }

    private fun syncAlarmListSuccess(alarmList: List<WmAlarm>) {
        alarmList?.let {
            observeAlarmListEmitter?.onNext(it)
        }
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
    }

    fun alarmBusiness(nodeData: NodeData) {
        when (nodeData.urn[2]) {

            URN_APP_ALARM_LIST -> {

                if (nodeData.data.size == 1) {
                    updateAlarmEmitter?.let {
                        it.onSuccess(nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
                    }
                } else {
                    val alarmList = mutableListOf<WmAlarm>()
                    val count = nodeData.dataLen / 25
                    sjUniWatch.wmLog.logD(TAG, "Alarm Count：$count")

                    if (count > 0) {
                        for (i in 0 until count) {

                            val alarmArray = nodeData.data.copyOfRange(i * 25, i * 25 + 25)
                            val id = alarmArray[0].toInt()
                            val nameArray =
                                alarmArray.copyOfRange(1, 21).takeWhile { it.toInt() != 0 }
                                    .toByteArray()
                            val name = String(nameArray, StandardCharsets.UTF_8)
                            sjUniWatch.wmLog.logD(TAG, "id$id name:$name")

                            val hour = alarmArray[21].toInt()
                            val minute = alarmArray[22].toInt()
                            val repeatOptions = alarmArray[23].toInt()
                            val isEnable = alarmArray[24].toInt()

                            val wmAlarm =
                                WmAlarm(
                                    name,
                                    hour,
                                    minute,
                                    AlarmRepeatOption.fromValue(repeatOptions)
                                )

                            wmAlarm.isOn = isEnable == 1

//                            alarmIdStates.forEach {
//                                it.used = it.value== id
//                            }

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

        val totalAlarms = mutableListOf<WmAlarm>()
        totalAlarms.addAll(alarms)

//        if (alarms.size < 10) {
//            for (i in 0 until 10 - alarms.size) {
//                totalAlarms.add(WmAlarm("", 0, 0, AlarmRepeatOption.fromValue(0)))
//            }
//        }

        val byteBuffer: ByteBuffer =
            ByteBuffer.allocate(25 * totalAlarms.size).order(ByteOrder.LITTLE_ENDIAN)

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
     * 添加闹钟
     */
    fun getWriteAddAlarmCmd(alarm: WmAlarm): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer =
            ByteBuffer.allocate(ALARM_NAME_LEN + 5).order(ByteOrder.LITTLE_ENDIAN)
//        byteBuffer.put(alarm.alarmId.toByte())
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
        payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_1, URN_2), byteBuffer.array())
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

    /**
     * 更新闹钟
     */
    fun getWriteModifyAlarmCmd(alarm: WmAlarm): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer =
            ByteBuffer.allocate(ALARM_NAME_LEN + 5).order(ByteOrder.LITTLE_ENDIAN)
//        byteBuffer.put(alarm.alarmId.toByte())
        val originNameArray = alarm.alarmName.toByteArray(StandardCharsets.UTF_8)

//        Log.e(">>>>>>>>","alarm name："+String(originNameArray))

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
        payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_1, URN_3), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 删除闹钟
     */
    fun getExecuteDeleteAlarmCmd(alarmIds: List<Byte>): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(CmdHelper.getUrnId(URN_4, URN_1, URN_4), alarmIds.toByteArray())
        return payloadPackage
    }

}