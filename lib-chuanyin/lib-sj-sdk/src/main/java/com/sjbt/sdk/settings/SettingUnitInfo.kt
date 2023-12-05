package com.sjbt.sdk.settings

import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmUnitInfo
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_0
import com.sjbt.sdk.spp.cmd.URN_2
import com.sjbt.sdk.spp.cmd.URN_3
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SettingUnitInfo(val sjUniWatch: SJUniWatch) : AbWmSetting<WmUnitInfo>(),
    ExceptionStateListener {
    private var observeEmitter: ObservableEmitter<WmUnitInfo>? = null
    private var setEmitter: SingleEmitter<WmUnitInfo>? = null
    private var getEmitter: SingleEmitter<WmUnitInfo>? = null
    private var wmUnitInfo: WmUnitInfo? = null
    private var isGet = false

    override fun observeChange(): Observable<WmUnitInfo> {
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
    }

    override fun set(obj: WmUnitInfo): Single<WmUnitInfo> {
        wmUnitInfo = obj
        return Single.create { emitter ->
            setEmitter = emitter
            sjUniWatch.sendWriteNodeCmdList(getWriteUnitSettingCmd(obj))
        }
    }

    override fun get(): Single<WmUnitInfo> {
        return Single.create { emitter ->
            isGet = true
            getEmitter = emitter
            sjUniWatch.sendReadNodeCmdList(getReadUnitSettingCmd())
        }
    }


    fun unitInfoBusiness(it: NodeData) {
        when (it.urn[2]) {
            URN_0 -> {

                if (it.dataLen.toInt() == 1) {
                    setEmitter?.onSuccess(
                        if (it.data[0] == ErrorCode.ERR_CODE_OK.ordinal.toByte()) {
                            wmUnitInfo
                        } else {
                            null
                        }
                    )
                } else {
                    val byteBuffer =
                        ByteBuffer.wrap(it.data)

                    val timeUnit = byteBuffer.get()
                    val temperatureUnit = byteBuffer.get()
                    val distanceUnit = byteBuffer.get()
                    val weightUnit = byteBuffer.get()

                    wmUnitInfo = WmUnitInfo(
                        weightUnit = if (weightUnit.toInt() == 0) {
                            WmUnitInfo.WeightUnit.KG
                        } else {
                            WmUnitInfo.WeightUnit.LB
                        },
                        temperatureUnit = if (temperatureUnit.toInt() == 0) {
                            WmUnitInfo.TemperatureUnit.CELSIUS
                        } else {
                            WmUnitInfo.TemperatureUnit.FAHRENHEIT
                        },
                        timeFormat = if (timeUnit.toInt() == 0) {
                            WmUnitInfo.TimeFormat.TWELVE_HOUR
                        } else {
                            WmUnitInfo.TimeFormat.TWENTY_FOUR_HOUR
                        },
                        distanceUnit = if (distanceUnit.toInt() == 0) {
                            WmUnitInfo.DistanceUnit.KM
                        } else {
                            WmUnitInfo.DistanceUnit.MILE
                        }
                    )

                    if (isGet) {
                        isGet = false
                        getEmitter?.onSuccess(wmUnitInfo)
                    } else {
                        observeEmitter?.onNext(wmUnitInfo)
                    }
                }
            }
        }
    }

    /**
     * 获取设置单位信息的命令
     */
    private fun getWriteUnitSettingCmd(
        wmUnitInfo: WmUnitInfo
    ): PayloadPackage {

        val payloadPackage = PayloadPackage()
        val bbSport: ByteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
        bbSport.put(wmUnitInfo.timeFormat.ordinal.toByte())
        bbSport.put(wmUnitInfo.temperatureUnit.ordinal.toByte())
        bbSport.put(wmUnitInfo.distanceUnit.ordinal.toByte())
        bbSport.put(wmUnitInfo.weightUnit.ordinal.toByte())

        payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_3), bbSport.array())

        return payloadPackage
    }

    /**
     * 获取设置单位信息的命令
     */
    private fun getReadUnitSettingCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val bbSport: ByteBuffer = ByteBuffer.allocate(0)

        payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_3), bbSport.array())

        return payloadPackage
    }

}