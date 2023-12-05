package com.sjbt.sdk.settings

import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmAppView
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingAppView(val sjUniWatch: SJUniWatch) : AbWmSetting<WmAppView>() {
    private var observeEmitter: ObservableEmitter<WmAppView>? = null
    var setEmitter: SingleEmitter<WmAppView>? = null
    private var getEmitter: SingleEmitter<WmAppView>? = null
    private var mAppView: WmAppView? = null
    private var isGet = false

    override fun observeChange(): Observable<WmAppView> {
        return Observable.create { emitter -> observeEmitter = emitter }
    }

    override fun set(appView: WmAppView): Single<WmAppView> {
        mAppView = appView
        return Single.create { emitter ->
            setEmitter = emitter
            appView.appViewList.forEach {
                if (it.status == 1) {
                    sjUniWatch.sendThreadTimeOutMsg(CmdHelper.setAppViewCmd(it.id.toByte()))
                }
            }
        }
    }

    fun observeConnectState() {
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

    override fun get(): Single<WmAppView> {
        return Single.create { emitter ->
            isGet = true
            getEmitter = emitter
            sjUniWatch.sendThreadTimeOutMsg(CmdHelper.appViewList)
        }
    }

    fun appViewsSetTimeOut() {
        setEmitter?.onError(WmTimeOutException("set app view time out"))
    }

    fun appViewsBackTimeOut() {
        getEmitter?.onError(WmTimeOutException("get app views time out"))
    }

    fun setAppViewResult(isSuccess: Boolean) {
        if (isSuccess) {
            setEmitter?.onSuccess(mAppView)
        } else {
            setEmitter?.onError(Throwable("set fail"))
        }
    }


    fun appViewsBack(appView: WmAppView) {
        mAppView = appView
        if (isGet) {
            isGet = false
            getEmitter?.onSuccess(appView)
        } else {
            observeEmitter?.onNext(appView)
        }
    }
}