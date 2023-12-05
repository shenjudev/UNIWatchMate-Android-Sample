package com.sjbt.sdk.settings

import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmSleepSettings
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CMD_ID_800C
import com.sjbt.sdk.spp.cmd.CMD_ID_800D
import com.sjbt.sdk.spp.cmd.CMD_ID_800E
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingSleepSet(val sjUniWatch: SJUniWatch) : AbWmSetting<WmSleepSettings>(),
    ExceptionStateListener {

    private var observeSleepSettingEmitter: ObservableEmitter<WmSleepSettings>? = null
    private var setEmitter: SingleEmitter<WmSleepSettings>? = null
    private var getEmitter: SingleEmitter<WmSleepSettings>? = null
    private var wmSleepSettings: WmSleepSettings? = null
    private var isGet = false

    override fun observeChange(): Observable<WmSleepSettings> {
        return Observable.create { emitter -> observeSleepSettingEmitter = emitter }
    }

    override fun observeConnectState() {
        setEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }

        getEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }
    }

    override fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {

    }

    private fun setSleepConfigSuccess(result: Boolean) {
        if (result) {
            setEmitter?.onSuccess(wmSleepSettings)
        } else {
            setEmitter?.onSuccess(null)
        }
    }

    override fun set(obj: WmSleepSettings): Single<WmSleepSettings> {
        return Single.create { emitter ->
            setEmitter = emitter

            val status = if (obj.open) {
                1
            } else {
                0
            }

            sjUniWatch.sendSyncSafeMsg(
                CmdHelper.setSleepSetCmd(
                    status.toByte(),
                    obj.startHour.toByte(),
                    obj.startMinute.toByte(),
                    obj.endHour.toByte(),
                    obj.endMinute.toByte()
                )
            )
        }
    }

    override fun get(): Single<WmSleepSettings> {
        return Single.create { emitter ->
            isGet = true
            getEmitter = emitter
            sjUniWatch.sendSyncSafeMsg(CmdHelper.getSleepSetCmd)
        }
    }

    fun sleepSetBusiness(msgBean: MsgBean) {
        when (msgBean.cmdId.toShort()) {
            CMD_ID_800C -> {
                val sleepOpen = msgBean.payload[0].toInt()
                val startHour = msgBean.payload[1].toInt()
                val startMin = msgBean.payload[2].toInt()
                val endHour = msgBean.payload[3].toInt()
                val endMin = msgBean.payload[4].toInt()

                this.wmSleepSettings = WmSleepSettings(
                    sleepOpen == 1,
                    startHour,
                    startMin,
                    endHour,
                    endMin
                )

                if (isGet) {
                    isGet = false
                    getEmitter?.onSuccess(
                        wmSleepSettings
                    )
                } else {

                    observeSleepSettingEmitter?.onNext(wmSleepSettings)
                }

            }

            CMD_ID_800D -> {
                sjUniWatch.sendSyncSafeMsg(CmdHelper.getRespondSuccessCmd(CMD_ID_800D))
            }

            CMD_ID_800E -> {
                val setSleepResult = msgBean.payload[0].toInt()
                setSleepConfigSuccess(setSleepResult == 1)
            }
        }
    }
}