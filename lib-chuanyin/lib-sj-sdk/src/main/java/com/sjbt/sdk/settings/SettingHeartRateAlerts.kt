package com.sjbt.sdk.settings

import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer

class SettingHeartRateAlerts(val sjUniWatch: SJUniWatch) : AbWmSetting<WmHeartRateAlerts>() {
    var observeEmitter: ObservableEmitter<WmHeartRateAlerts>? = null
    var setEmitter: SingleEmitter<WmHeartRateAlerts>? = null
    var getEmitter: SingleEmitter<WmHeartRateAlerts>? = null
    private var isGet = false
    private var heartRateAlerts: WmHeartRateAlerts? = null
    private val TAG = "SettingHeartRateAlerts"

    override fun observeChange(): Observable<WmHeartRateAlerts> {
        return Observable.create { emitter ->
            observeEmitter = emitter
        }
    }

    fun observeConnectState() {
        sjUniWatch.observeConnectState.subscribe {
            if (it == WmConnectState.DISCONNECTED) {
                setEmitter?.onError(WmTimeOutException("time out exception"))
                getEmitter?.onError(WmTimeOutException("time out exception"))
            }
        }
    }

    override fun set(obj: WmHeartRateAlerts): Single<WmHeartRateAlerts> {
        return Single.create { emitter ->
            heartRateAlerts = obj
            heartRateAlerts?.refreshIntervals()
            setEmitter = emitter
            sjUniWatch.sendWriteNodeCmdList(getWriteRateSettingPayLoad(obj))
        }
    }

    override fun get(): Single<WmHeartRateAlerts> {
        return Single.create { emitter ->
            isGet = true
            getEmitter = emitter
            sjUniWatch.sendReadNodeCmdList(getReadRateSettingPayload())
        }
    }

    /**
     * 获取体育列表
     */
    private fun getReadRateSettingPayload(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(
            CmdHelper.getUrnId(
                URN_APP_SETTING, URN_APP_RATE
            ), ByteArray(0)
        )
        return payloadPackage
    }

    /**
     * 获取写入心率设置的命令
     */
    private fun getWriteRateSettingPayLoad(heartRateAlerts: WmHeartRateAlerts): PayloadPackage {
        val payloadPackage = PayloadPackage()

        val byteBuffer = ByteBuffer.allocate(6)
        byteBuffer.put(
            if (heartRateAlerts.isEnableHrAutoMeasure) {
                1
            } else {
                0
            }
        )
        byteBuffer.put(heartRateAlerts.maxHeartRate.toByte())

        byteBuffer.put(
            if (heartRateAlerts.exerciseHeartRateAlert.isEnable) {
                1
            } else {
                0
            }
        )
        byteBuffer.put(heartRateAlerts.exerciseHeartRateAlert.threshold.toByte())

        byteBuffer.put(
            if (heartRateAlerts.restingHeartRateAlert.isEnable) {
                1
            } else {
                0
            }
        )
        byteBuffer.put(heartRateAlerts.restingHeartRateAlert.threshold.toByte())

        payloadPackage.putData(
            CmdHelper.getUrnId(URN_APP_SETTING, URN_APP_RATE),
            byteBuffer.array()
        )

        return payloadPackage
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {

    }

    fun settingHeartRateBusiness(nodeData: NodeData) {

        if (nodeData.data.size == 1) {
            heartRateAlerts?.refreshIntervals()
            sjUniWatch.wmLog.logD(TAG, "heartRateAlerts:$heartRateAlerts")
            setEmitter?.onSuccess(heartRateAlerts)
        } else {
            val byteBuffer = ByteBuffer.wrap(nodeData.data)
            val isEnableHrAutoMeasure = byteBuffer.get().toInt() == 1
            val maxHeartRate = byteBuffer.get().toInt().and(0XFF)
            val isExerciseHeartEnabled = byteBuffer.get().toInt() == 1
            val exerciseThreshold = byteBuffer.get().toInt().and(0XFF)
            val isRestingHeartEnabled = byteBuffer.get().toInt() == 1
            val restingThreshold = byteBuffer.get().toInt().and(0XFF)

            heartRateAlerts = WmHeartRateAlerts(0)
            heartRateAlerts!!.isEnableHrAutoMeasure = isEnableHrAutoMeasure
            heartRateAlerts!!.maxHeartRate = maxHeartRate

            heartRateAlerts!!.exerciseHeartRateAlert.isEnable = isExerciseHeartEnabled
            heartRateAlerts!!.exerciseHeartRateAlert.threshold = exerciseThreshold

            heartRateAlerts!!.restingHeartRateAlert.threshold = restingThreshold
            heartRateAlerts!!.restingHeartRateAlert.isEnable = isRestingHeartEnabled
            heartRateAlerts!!.refreshIntervals()
            if (isGet) {
                isGet = false
                getEmitter?.onSuccess(heartRateAlerts)
            } else {
                observeEmitter?.onNext(heartRateAlerts)
            }
        }
    }
}