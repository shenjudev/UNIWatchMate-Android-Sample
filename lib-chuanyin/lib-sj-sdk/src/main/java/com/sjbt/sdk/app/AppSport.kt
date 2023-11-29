package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmSport
import com.base.sdk.exception.WmTimeOutException
import com.base.sdk.port.app.AbAppSport
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.DevFinal
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 应用 - 运动 列表获取和更新
 */
class AppSport(val sjUniWatch: SJUniWatch) : AbAppSport() {
    private var getFixedSportListEmitter: SingleEmitter<List<WmSport>>? = null
    private var getDynamicSportListEmitter: SingleEmitter<List<WmSport>>? = null
    private var getSupportSportListEmitter: SingleEmitter<List<WmSport>>? = null
    private var updateFixedSportListEmitter: SingleEmitter<Boolean>? = null
    private var updateDynamicSportListEmitter: SingleEmitter<Boolean>? = null

    private val mFixedSportList = mutableListOf<WmSport>()
    private val mDynamicSportList = mutableListOf<WmSport>()
    private val mSupportSportList = mutableListOf<WmSport>()
    private val TAG = "AppSport"

    override val getFixedSportList: Single<List<WmSport>> = Single.create {
        mFixedSportList.clear()
        getFixedSportListEmitter = it
        sjUniWatch.sendReadNodeCmdList(getReadSportListPayloadPackage(URN_APP_FIXED_SPORT_LIST))
    }

    override fun updateFixedSportList(list: List<WmSport>): Single<Boolean> {

        return Single.create {
            updateFixedSportListEmitter = it
            sjUniWatch.sendWriteNodeCmdList(
                getWriteSportListPayloadPackage(
                    URN_APP_FIXED_SPORT_LIST,
                    list
                )
            )
        }
    }

    override fun updateDynamicSportList(list: List<WmSport>): Single<Boolean> {
        return Single.create {
            updateDynamicSportListEmitter = it
            sjUniWatch.sendWriteNodeCmdList(
                getWriteSportListPayloadPackage(
                    URN_APP_DYNAMIC_SPORT_LIST, list
                )
            )
        }
    }

    override val getDynamicSportList: Single<List<WmSport>> = Single.create {
        mDynamicSportList.clear()
        getDynamicSportListEmitter = it
        sjUniWatch.sendReadNodeCmdList(getReadSportListPayloadPackage(URN_APP_DYNAMIC_SPORT_LIST))
    }

    override val getSupportSportList: Single<List<WmSport>> = Single.create {
        mSupportSportList.clear()
        getSupportSportListEmitter = it
        sjUniWatch.sendReadNodeCmdList(getReadSupportSportListPayloadPackage())
    }

    /**
     * 获取支持的体育列表命令
     */
    private fun getReadSupportSportListPayloadPackage(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(
            CmdHelper.getUrnId(
                URN_APP_SETTING,
                URN_APP_SPORT,
                URN_APP_SUPPORT_SPORT_LIST
            ), ByteArray(0)
        )
        return payloadPackage
    }

    /**
     * 获取体育列表命令
     */
    private fun getReadSportListPayloadPackage(urn: Byte): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(
            CmdHelper.getUrnId(
                URN_APP_SETTING,
                URN_APP_SPORT,
                urn
            ), ByteArray(0)
        )
        return payloadPackage
    }

    /**
     * 更新体育列表命令
     */
    private fun getWriteSportListPayloadPackage(
        urn: Byte,
        sportList: List<WmSport>
    ): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer = ByteBuffer.allocate(sportList.size * 2).order(ByteOrder.LITTLE_ENDIAN)

        sportList.forEach {
            sjUniWatch.wmLog.logD(TAG, "update sport id:" + it.id)
            byteBuffer.putShort(it.id.toShort())
        }

        payloadPackage.putData(
            CmdHelper.getUrnId(
                URN_APP_SETTING,
                URN_APP_SPORT,
                urn
            ), byteBuffer.array()
        )

        return payloadPackage
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        sjUniWatch.wmLog.logE(DevFinal.STR.TAG, "onTimeOut:$msgBean")
        when (nodeData.urn[2]) {
            URN_APP_FIXED_SPORT_LIST -> getFixedSportListEmitter?.onError(WmTimeOutException("$TAG get sport list time out"))
            URN_APP_SUPPORT_SPORT_LIST -> getSupportSportListEmitter?.onError(WmTimeOutException("$TAG get support sport list time out"))
        }
    }

    fun appSportBusiness(payloadPackage: PayloadPackage, nodeData: NodeData) {

        when (nodeData.urn[2]) {
            URN_APP_FIXED_SPORT_LIST -> {

                if (nodeData.data.size == 1) {

//                    if (nodeData.dataFmt == DataFormat.FMT_ERRCODE) {
//                        getFixedSportListEmitter?.onSuccess(mFixedSportList)
//                    }else{
                        updateFixedSportListEmitter?.onSuccess(nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
//                    }

                } else {

                    val byteBuffer = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                    for (i in 0 until nodeData.data.size / 2) {
                        val sportId = byteBuffer.short.toInt()
                        val wmSport = WmSport(sportId, 0, false)
                        sjUniWatch.wmLog.logD(TAG, "fixed sport id:$sportId");
                        if (sportId != 0) {
                            mFixedSportList.add(wmSport)
                        }
                    }

                    getFixedSportListEmitter?.onSuccess(mFixedSportList)
                }
            }

            URN_APP_DYNAMIC_SPORT_LIST -> {
                if (nodeData.data.size == 1) {

//                    if (nodeData.dataFmt == DataFormat.FMT_ERRCODE) {
//                        getDynamicSportListEmitter?.onSuccess(mDynamicSportList)
//                    }else{
                        updateDynamicSportListEmitter?.onSuccess(nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
//                    }

                } else {

                    val byteBuffer = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                    for (i in 0 until nodeData.data.size / 2) {
                        val sportId = byteBuffer.short.toInt()
                        val wmSport = WmSport(sportId, 0, false)
                        sjUniWatch.wmLog.logD(TAG, "dynamic sport id:$sportId");
                        if (sportId != 0) {
                            mDynamicSportList.add(wmSport)
                        }
                    }

                    getDynamicSportListEmitter?.onSuccess(mDynamicSportList)
                }
            }

            URN_APP_SUPPORT_SPORT_LIST -> {

                if (nodeData.data.size == 1) {
                    getSupportSportListEmitter?.onSuccess(mSupportSportList)
                } else {
                    val byteBuffer = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                    for (i in 0 until nodeData.data.size / 2) {
                        val sportId = byteBuffer.short.toInt()
                        val wmSport = WmSport(sportId, 0, false)
                        sjUniWatch.wmLog.logD(TAG, "support sport id:$sportId");
                        if (sportId != 0) {
                            mSupportSportList.add(wmSport)
                        }
                    }
                    getSupportSportListEmitter?.onSuccess(mSupportSportList)

                }
            }
        }
    }

}