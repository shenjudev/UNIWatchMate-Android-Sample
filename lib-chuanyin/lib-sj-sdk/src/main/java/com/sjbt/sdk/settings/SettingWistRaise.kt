package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmWristRaise
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingWistRaise(val sjUniWatch: SJUniWatch) : AbWmSetting<WmWristRaise>() {
    private var observeEmitter: ObservableEmitter<WmWristRaise>? = null
    private var setEmitter: SingleEmitter<WmWristRaise>? = null
    private var getEmitter: SingleEmitter<WmWristRaise>? = null

    private var mWmWristRaise: WmWristRaise? = null
    private var backWmWristRaise = WmWristRaise();
    private var isGet = false

    private fun getWmWistRaise(wmWristRaise: WmWristRaise) {
        backWmWristRaise = wmWristRaise
        mWmWristRaise = WmWristRaise(wmWristRaise.isScreenWakeEnabled)
        getEmitter?.onSuccess(wmWristRaise)
    }

    private fun observeWmWistRaiseChange(wmWristRaise: WmWristRaise) {
        backWmWristRaise = wmWristRaise
        mWmWristRaise = WmWristRaise(wmWristRaise.isScreenWakeEnabled)
        observeEmitter?.onNext(wmWristRaise)
    }

    fun observeWmWistRaiseChange(type: Int, value: Int) {
        mWmWristRaise?.let {
            if (type == 4) {
                it.isScreenWakeEnabled = value == 1
                backWmWristRaise = it
                observeEmitter?.onNext(it)
            }
        }
    }

    fun setSuccess() {
        mWmWristRaise?.isScreenWakeEnabled = backWmWristRaise.isScreenWakeEnabled
        setEmitter?.onSuccess(backWmWristRaise)
        observeEmitter?.onNext(backWmWristRaise)
    }

    override fun observeChange(): Observable<WmWristRaise> {
        return Observable.create { emitter -> observeEmitter = emitter }
    }

    override fun set(obj: WmWristRaise): Single<WmWristRaise> {
        return Single.create(object : SingleOnSubscribe<WmWristRaise> {
            override fun subscribe(emitter: SingleEmitter<WmWristRaise>) {
                setEmitter = emitter
                backWmWristRaise.isScreenWakeEnabled = obj.isScreenWakeEnabled
                sjUniWatch.sendNormalMsg(
                    CmdHelper.getSetDeviceRingStateCmd(
                        4,
                        if (obj.isScreenWakeEnabled) 1 else 0
                    )
                )
            }
        })
    }

    override fun get(): Single<WmWristRaise> {
        return Single.create { emitter ->
            isGet = true
            getEmitter = emitter
            sjUniWatch.sendNormalMsg(CmdHelper.deviceRingStateCmd)
        }
    }

    fun backWistRaiseSettings(wmWristRaise: WmWristRaise) {
        if (isGet) {
            isGet = false
            getWmWistRaise(wmWristRaise)
        } else {
            observeWmWistRaiseChange(wmWristRaise)
        }
    }

    fun onTimeOut(nodeData: NodeData) {
    }

}