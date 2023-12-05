package com.sjbt.sdk.settings

import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmPersonalInfo
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_0
import com.sjbt.sdk.spp.cmd.URN_2
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SettingPersonalInfo(val sjUniWatch: SJUniWatch) : AbWmSetting<WmPersonalInfo>(),
    ExceptionStateListener {
    private var observeEmitter: ObservableEmitter<WmPersonalInfo>? = null
    private var setEmitter: SingleEmitter<WmPersonalInfo>? = null
    private var getEmitter: SingleEmitter<WmPersonalInfo>? = null

    private var personalInfo: WmPersonalInfo? = null

    override fun observeChange(): Observable<WmPersonalInfo> {
        return Observable.create { emitter -> observeEmitter = emitter }
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

    override fun set(obj: WmPersonalInfo): Single<WmPersonalInfo> {
        personalInfo = obj
        return Single.create { emitter ->
            setEmitter = emitter
            sjUniWatch.sendWriteNodeCmdList(getUpdatePersonalInfoAllCmd(obj))
        }
    }

    override fun get(): Single<WmPersonalInfo> {
        return Single.create { emitter ->
            getEmitter = emitter
            sjUniWatch.sendReadNodeCmdList(getDevicePersonalInfoCmd())
        }
    }

    fun personalInfoBusiness(it: NodeData) {
        when (it.urn[2]) {
            URN_0 -> {

                if (it.data.size <= 1) {
                    personalInfo?.let {
                        setEmitter?.onSuccess(it)
                    }

                } else {
                    val byteBuffer =
                        ByteBuffer.wrap(it.data).order(ByteOrder.LITTLE_ENDIAN)
                    val height = byteBuffer.getShort()
                    val weight = byteBuffer.getShort()
                    val gender = byteBuffer.get()

                    val year = byteBuffer.getShort()
                    val month = byteBuffer.get()
                    val day = byteBuffer.get()

                    personalInfo = WmPersonalInfo(
                        height,
                        weight,
                        if (gender.toInt() == 1) {
                            WmPersonalInfo.Gender.MALE
                        } else {
                            WmPersonalInfo.Gender.FEMALE
                        },
                        WmPersonalInfo.BirthDate(year, month, day)
                    )

                    getEmitter?.onSuccess(personalInfo)
                    observeEmitter?.onNext(personalInfo)
                }
            }
        }
    }

    /**
     * 获取设备上体育目标配置
     */
    private fun getDevicePersonalInfoCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_2), ByteArray(0))
        return payloadPackage
    }

    /**
     * 获取设置健康信息的命令
     */
    private fun getUpdatePersonalInfoAllCmd(
        personalInfo: WmPersonalInfo
    ): PayloadPackage {

        val payloadPackage = PayloadPackage()

        val bbSport: ByteBuffer = ByteBuffer.allocate(2 + 2 + 1 + 4).order(ByteOrder.LITTLE_ENDIAN)
        bbSport.putShort(personalInfo.height)
        bbSport.putShort(personalInfo.weight)
        bbSport.put(personalInfo.gender.ordinal.toByte())
        bbSport.putShort(personalInfo.birthDate.year)
        bbSport.put(personalInfo.birthDate.month)
        bbSport.put(personalInfo.birthDate.day)
        payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_2), bbSport.array())

        return payloadPackage
    }

}