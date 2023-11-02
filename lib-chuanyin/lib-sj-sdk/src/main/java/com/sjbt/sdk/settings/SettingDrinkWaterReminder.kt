package com.sjbt.sdk.settings

import com.base.sdk.entity.common.WmNoDisturb
import com.base.sdk.entity.common.WmTimeFrequency
import com.base.sdk.entity.data.WmTimeRange
import com.base.sdk.entity.settings.WmSedentaryReminder
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_0
import com.sjbt.sdk.spp.cmd.URN_2
import com.sjbt.sdk.spp.cmd.URN_6
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SettingDrinkWaterReminder(val sjUniWatch: SJUniWatch) : AbWmSetting<WmSedentaryReminder>() {
    private var observeEmitter: ObservableEmitter<WmSedentaryReminder>? = null
    private var setEmitter: SingleEmitter<WmSedentaryReminder>? = null
    private var getEmitter: SingleEmitter<WmSedentaryReminder>? = null
    private var isGet = false
    private var mSedentaryReminder: WmSedentaryReminder? = null

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmSedentaryReminder> {
        return Observable.create { emitter -> observeEmitter = emitter }
    }

    override fun set(obj: WmSedentaryReminder): Single<WmSedentaryReminder> {
        return Single.create { emitter ->
            mSedentaryReminder = obj
            setEmitter = emitter
            sjUniWatch.sendWriteNodeCmdList(getWriteReadDrinkReminderCmd(obj))
        }
    }

    override fun get(): Single<WmSedentaryReminder> {
        return Single.create { emitter ->
            isGet = true
            getEmitter = emitter
            sjUniWatch.sendReadNodeCmdList(getReadDrinkReminderCmd())
        }
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
    }

    fun drinkWaterBusiness(it: NodeData) {

        when (it.urn[2]) {
            URN_0 -> {

                if (it.data.size == 1) {
                    setEmitter?.onSuccess(mSedentaryReminder)
                } else {
                    val byteBuffer =
                        ByteBuffer.wrap(it.data)

                    val enabled = byteBuffer.get().toInt() == 1
                    val startHour = byteBuffer.get()
                    val startMinute = byteBuffer.get()
                    val endHour = byteBuffer.get()
                    val endMinute = byteBuffer.get()
                    val frequency = byteBuffer.get()

                    val timeFrequency = when (frequency.toInt()) {
                        WmTimeFrequency.EVERY_30_MINUTES.value -> {
                            WmTimeFrequency.EVERY_30_MINUTES
                        }
                        WmTimeFrequency.EVERY_1_HOUR.value -> {
                            WmTimeFrequency.EVERY_1_HOUR
                        }
                        WmTimeFrequency.EVERY_1_HOUR_30_MINUTES.value -> {
                            WmTimeFrequency.EVERY_1_HOUR_30_MINUTES
                        }
                        else -> {
                            WmTimeFrequency.EVERY_30_MINUTES
                        }
                    }

                    val timeRange = WmTimeRange(
                        startHour.toInt(),
                        startMinute.toInt(),
                        endHour.toInt(),
                        endMinute.toInt()
                    )

                    val noDisturbEnable = byteBuffer.get().toInt() == 1
                    val noDisturbStartHour = byteBuffer.get()
                    val noDisturbStartMinute = byteBuffer.get()
                    val noDisturbEndHour = byteBuffer.get()
                    val noDisturbEndMinute = byteBuffer.get()

                    val noTimeRange = WmTimeRange(
                        noDisturbStartHour.toInt(),
                        noDisturbStartMinute.toInt(),
                        noDisturbEndHour.toInt(),
                        noDisturbEndMinute.toInt()
                    )

                    val noDisturb = WmNoDisturb(noDisturbEnable, noTimeRange)

                    mSedentaryReminder =
                        WmSedentaryReminder(enabled, timeRange, timeFrequency, noDisturb)

                    if (isGet) {
                        isGet = false
                        getEmitter?.let {
                            if (!it.isDisposed) {
                                it.onSuccess(mSedentaryReminder)
                            }
                        }
                    } else {
                        observeEmitter?.onNext(mSedentaryReminder)
                    }
                }
            }
        }
    }

    /**
     * 获取喝水提醒设置
     */
    private fun getReadDrinkReminderCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_6), ByteBuffer.allocate(0).array())
        return payloadPackage
    }

    /**
     * 设置喝水提醒
     */
    private fun getWriteReadDrinkReminderCmd(sedentaryReminder: WmSedentaryReminder): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(
            if (sedentaryReminder.isEnabled) {
                1.toByte()
            } else {
                0.toByte()
            }
        )
        byteBuffer.put(sedentaryReminder.timeRange.startHour.toByte())
        byteBuffer.put(sedentaryReminder.timeRange.startMinute.toByte())
        byteBuffer.put(sedentaryReminder.timeRange.endHour.toByte())
        byteBuffer.put(sedentaryReminder.timeRange.endMinute.toByte())
        byteBuffer.put(sedentaryReminder.frequency.value.toByte())

        byteBuffer.put(
            if (sedentaryReminder.noDisturbLunchBreak.isEnabled) {
                1.toByte()
            } else {
                0.toByte()
            }
        )
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.startHour.toByte())
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.startMinute.toByte())
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.endHour.toByte())
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.endMinute.toByte())

        payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_6), byteBuffer.array())
        return payloadPackage
    }
}