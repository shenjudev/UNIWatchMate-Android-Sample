package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmWistRaise
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingWistRaise(val sjUniWatch: SJUniWatch) : AbWmSetting<WmWistRaise>() {
    private var observeEmitter: ObservableEmitter<WmWistRaise>? = null
    private var setEmitter: SingleEmitter<WmWistRaise>? = null
    private var getEmitter: SingleEmitter<WmWistRaise>? = null

    private var mWmWistRaise: WmWistRaise? = null
    private var backWmWistRaise = WmWistRaise();
    private var isGet = false

    private fun getWmWistRaise(wmWistRaise: WmWistRaise) {
        backWmWistRaise = wmWistRaise
        mWmWistRaise = WmWistRaise(wmWistRaise.isScreenWakeEnabled)
        getEmitter?.onSuccess(wmWistRaise)
    }

    private fun observeWmWistRaiseChange(wmWistRaise: WmWistRaise) {
        backWmWistRaise = wmWistRaise
        mWmWistRaise = WmWistRaise(wmWistRaise.isScreenWakeEnabled)
        observeEmitter?.onNext(wmWistRaise)
    }

    fun observeWmWistRaiseChange(type: Int, value: Int) {
        mWmWistRaise?.let {
            if (type == 4) {
                it.isScreenWakeEnabled = value == 1
                backWmWistRaise = it
                observeEmitter?.onNext(it)
            }
        }
    }

    fun setSuccess() {
        mWmWistRaise?.isScreenWakeEnabled = backWmWistRaise.isScreenWakeEnabled
        setEmitter?.onSuccess(backWmWistRaise)
        observeEmitter?.onNext(backWmWistRaise)
    }

    override fun observeChange(): Observable<WmWistRaise> {
        return Observable.create { emitter -> observeEmitter = emitter }
    }

    override fun set(obj: WmWistRaise): Single<WmWistRaise> {
        return Single.create(object : SingleOnSubscribe<WmWistRaise> {
            override fun subscribe(emitter: SingleEmitter<WmWistRaise>) {
                setEmitter = emitter
                backWmWistRaise.isScreenWakeEnabled = obj.isScreenWakeEnabled
                sjUniWatch.sendNormalMsg(
                    CmdHelper.getSetDeviceRingStateCmd(
                        4,
                        if (obj.isScreenWakeEnabled) 1 else 0
                    )
                )
            }
        })
    }

    override fun get(): Single<WmWistRaise> {
        return Single.create { emitter ->
            isGet = true
            getEmitter = emitter
            sjUniWatch.sendNormalMsg(CmdHelper.deviceRingStateCmd)
        }
    }

    fun backWistRaiseSettings(wmWistRaise: WmWistRaise) {
        if (isGet) {
            isGet = false
            getWmWistRaise(wmWistRaise)
        } else {
            observeWmWistRaiseChange(wmWistRaise)
        }
    }

    fun onTimeOut(nodeData: NodeData) {
    }

}