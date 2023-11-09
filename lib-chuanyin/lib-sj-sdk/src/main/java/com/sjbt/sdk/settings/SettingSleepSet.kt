package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSleepSettings
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CMD_ID_800C
import com.sjbt.sdk.spp.cmd.CMD_ID_800D
import com.sjbt.sdk.spp.cmd.CMD_ID_800E
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingSleepSet(val sjUniWatch: SJUniWatch) : AbWmSetting<WmSleepSettings>() {

    private var observeSleepSettingEmitter: ObservableEmitter<WmSleepSettings>? = null
    private var setSleepSettingEmitter: SingleEmitter<WmSleepSettings>? = null
    private var getSleepSettingEmitter: SingleEmitter<WmSleepSettings>? = null
    private var wmSleepSettings: WmSleepSettings? = null
    private var isGet = false

    override fun observeChange(): Observable<WmSleepSettings> {
        return Observable.create { emitter -> observeSleepSettingEmitter = emitter }
    }

    private fun setSleepConfigSuccess(result: Boolean) {
        if (result) {
            setSleepSettingEmitter?.onSuccess(wmSleepSettings)
        } else {
            setSleepSettingEmitter?.onSuccess(null)
        }
    }

    override fun set(obj: WmSleepSettings): Single<WmSleepSettings> {
        return Single.create { emitter ->
            setSleepSettingEmitter = emitter

            val status = if (obj.open) {
                1
            } else {
                0
            }

            sjUniWatch.sendNormalMsg(
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
            getSleepSettingEmitter = emitter
            sjUniWatch.sendNormalMsg(CmdHelper.getSleepSetCmd)
        }
    }

    fun onTimeOut(nodeData: NodeData) {

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
                    getSleepSettingEmitter?.onSuccess(
                        wmSleepSettings
                    )
                } else {

                    observeSleepSettingEmitter?.onNext(wmSleepSettings)
                }

            }

            CMD_ID_800D -> {
                sjUniWatch.sendNormalMsg(CmdHelper.getRespondSuccessCmd(CMD_ID_800D))
            }

            CMD_ID_800E -> {
                val setSleepResult = msgBean.payload[0].toInt()
                setSleepConfigSuccess(setSleepResult == 1)
            }
        }
    }
}