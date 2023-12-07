package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmLanguage
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.app.AbAppLanguage
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.DevFinal
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class AppLanguage(val sjUniWatch: SJUniWatch) : AbAppLanguage(),
    ExceptionStateListener {
    private var languageListEmitter: SingleEmitter<List<WmLanguage>>? = null
    private var languageSetEmitter: SingleEmitter<WmLanguage>? = null
    private val languageList = mutableListOf<WmLanguage>()

    private val TAG = "AppLanguage"

    private var wmLanguage: WmLanguage? = null

    override val syncLanguageList: Single<List<WmLanguage>> = Single.create {
        languageListEmitter = it
        sjUniWatch.sendReadNodeCmdList(getReadLanguageListCmd())
    }

    override fun setLanguage(language: WmLanguage): Single<WmLanguage> {
        wmLanguage = language
        return Single.create {
            languageSetEmitter = it
            sjUniWatch.sendWriteNodeCmdList(getWriteLanguageCmd(language.bcp))
        }
    }

    override fun observeDisconnectState() {

        languageListEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }

        languageSetEmitter?.let { emitter ->
            if (!emitter.isDisposed) {
                emitter.onError(WmTimeOutException("time out exception"))
            }
        }
    }

    override fun onTimeOut(msgBean: MsgBean,nodeData: NodeData) {
        sjUniWatch.wmLog.logE(DevFinal.STR.TAG, "onTimeOut:$msgBean")
    }

    fun languageBusiness(
        nodeData: NodeData,
        msgBean: MsgBean?
    ) {
        when (nodeData.urn[2]) {
            URN_SETTING_LANGUAGE_LIST -> {

                msgBean?.let {
                    if (it.divideType == DIVIDE_N_2 || it.divideType == DIVIDE_Y_F_2) {
                        languageList.clear()
                    }
                }

                val languageCount = nodeData.data.size / 6
                var currLanguageBcp = ""

                for (i in 0 until languageCount) {
                    val bcpArray =
                        nodeData.data.copyOfRange(6 * i, 6 * i + 6).filter { it != 0.toByte() }.toByteArray()

//                    sjUniWatch.wmLog.logE(TAG, "language bcpArray:" + bcpArray.size)

                    val bcp = String(bcpArray, StandardCharsets.UTF_8)

//                    sjUniWatch.wmLog.logE(TAG, "language bcp:" + bcp)

                    if (i != 0) {
                        val language = WmLanguage(bcp, "", bcp == currLanguageBcp)
                        languageList.add(language)
                    } else {
                        currLanguageBcp = bcp
                    }
                }

                languageListEmitter?.onSuccess(languageList)
            }

            URN_SETTING_LANGUAGE_SET -> {
                wmLanguage?.let {
                    val result = nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                    if (result) {
                        languageSetEmitter?.onSuccess(it)
                    } else {
                        languageSetEmitter?.onError(RuntimeException("set fail"))
                    }
                }
            }
        }
    }

    /**
     * 获取语言列表的命令
     */
    private fun getReadLanguageListCmd(): PayloadPackage {

        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_4, URN_1), byteBuffer.array())

        return payloadPackage
    }

    /**
     * 获取设置语言命令
     */
    private fun getWriteLanguageCmd(bcp: String): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val bbSport: ByteBuffer = ByteBuffer.allocate(6)
        if (bcp.length <= 6) {
            bbSport.put(bcp.toByteArray(StandardCharsets.UTF_8))
            payloadPackage.putData(CmdHelper.getUrnId(URN_2, URN_4, URN_2), bbSport.array())
        }

        return payloadPackage
    }
}