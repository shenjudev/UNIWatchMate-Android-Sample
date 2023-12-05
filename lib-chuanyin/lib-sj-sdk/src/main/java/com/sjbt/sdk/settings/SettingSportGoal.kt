package com.sjbt.sdk.settings

import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.DevFinal
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SettingSportGoal(val sjUniWatch: SJUniWatch) : AbWmSetting<WmSportGoal>(),
    ExceptionStateListener {
    private var observeEmitter: ObservableEmitter<WmSportGoal>? = null
    private var setEmitter: SingleEmitter<WmSportGoal>? = null
    private var getEmitter: SingleEmitter<WmSportGoal>? = null

    private var wmSportGoal: WmSportGoal? = null
    private val TAG = "SettingSportGoal"

    private var isGet = false

    override fun observeChange(): Observable<WmSportGoal> {
        return Observable.create { emitter -> observeEmitter = emitter }
    }

    override fun observeDisconnectState() {
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
        sjUniWatch.wmLog.logE(TAG, "onTimeOut:$msgBean")
    }

    override fun set(obj: WmSportGoal): Single<WmSportGoal> {
        wmSportGoal = obj
        return Single.create { emitter ->
            setEmitter = emitter
            val payloadPackage = getUpdateSportGoalAllCmd(obj)

            sjUniWatch.sendWriteNodeCmdList(payloadPackage)
        }
    }

    override fun get(): Single<WmSportGoal> {

        return Single.create { emitter ->
            isGet = true
            getEmitter = emitter
            sjUniWatch.sendReadNodeCmdList(getDeviceSportGoalCmd())
        }
    }


    fun sportInfoBusiness(it: NodeData) {
        when (it.urn[2]) {
            URN_0 -> {
                if (it.dataLen.toInt() == 1) {

                    if (it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal) {
                        setEmitter?.onSuccess(wmSportGoal)
                    } else {
                        setEmitter?.onError(RuntimeException("set fail"))
                    }

                } else {
                    val byteBuffer =
                        ByteBuffer.wrap(it.data).order(ByteOrder.LITTLE_ENDIAN)

                    val step = byteBuffer.getInt()
                    val distance = byteBuffer.getInt()
                    val calories = byteBuffer.getInt()
                    val activityDuration =
                        byteBuffer.getShort()

                    wmSportGoal = WmSportGoal(
                        step,
                        distance,
                        calories,
                        activityDuration
                    )

                    sjUniWatch.wmLog.logD(TAG, "sport goal：$wmSportGoal")

                    if (isGet) {
                        isGet = !isGet
                        getEmitter?.onSuccess(
                            wmSportGoal
                        )
                    }else{
                        observeEmitter?.onNext(wmSportGoal)
                    }

                }
            }
        }
    }

    /**
     * 获取设备上体育目标配置
     */
    private fun getDeviceSportGoalCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_1), ByteArray(0))
        return payloadPackage
    }

    /**
     * 获取设置体育目标的命令
     */
    private fun getUpdateSportGoalAllCmd(
        sportGoal: WmSportGoal
    ): PayloadPackage {

        val payloadPackage = PayloadPackage()

        val bbSport: ByteBuffer = ByteBuffer.allocate(4 + 4 + 4 + 2).order(ByteOrder.LITTLE_ENDIAN)
        bbSport.putInt(sportGoal.steps)
        bbSport.putInt(sportGoal.calories)
        bbSport.putInt(sportGoal.distance)
        bbSport.putShort(sportGoal.activityDuration)
        payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_1), bbSport.array())

        return payloadPackage
    }
}