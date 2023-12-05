package com.sjbt.sdk.app

import com.base.sdk.entity.settings.WmDateTime
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.app.AbAppDateTime
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_APP_CONTACT_EMERGENCY
import com.sjbt.sdk.spp.cmd.URN_APP_CONTACT_LIST
import com.sjbt.sdk.utils.DevFinal.STR.TAG
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class AppDateTime(val sjUniWatch: SJUniWatch) : AbAppDateTime() ,
    ExceptionStateListener {
    var setEmitter: SingleEmitter<Boolean>? = null

    override fun observeConnectState() {

        setEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }
    }

    override fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")

    }

    override fun setDateTime(dateTime: WmDateTime?): Single<Boolean> {
        return Single.create { emitter ->
            setEmitter = emitter
            sjUniWatch.sendSyncSafeMsg(CmdHelper.syncTimeCmd)
        }
    }
}