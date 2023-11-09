package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmSport
import com.base.sdk.port.app.AbAppSport
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 应用 - 运动 列表获取和更新
 */
class AppSport(val sjUniWatch: SJUniWatch) : AbAppSport() {
    private var getSportListEmitter: SingleEmitter<List<WmSport>>? = null
    private var updateSportListEmitter: SingleEmitter<Boolean>? = null
    private val mSportList = mutableListOf<WmSport>()
    private val TAG = "AppSport"

    override val getSportList: Single<List<WmSport>> = Single.create {
        mSportList.clear()
        getSportListEmitter = it
        sjUniWatch.sendReadNodeCmdList(getReadSportListPayloadPackage())
    }

    /**
     * 获取体育列表命令
     */
    private fun getReadSportListPayloadPackage(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(
            CmdHelper.getUrnId(
                URN_APP_SETTING,
                URN_APP_SPORT,
                URN_APP_SPORT_LIST
            ), ByteArray(0)
        )
        return payloadPackage
    }

    /**
     * 更新体育列表命令
     */
    private fun getWriteUpdateSportListPayloadPackage(sportList: List<WmSport>): PayloadPackage {
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
                URN_APP_SPORT_LIST
            ), byteBuffer.array()
        )

        return payloadPackage
    }

    override fun updateSportList(list: List<WmSport>): Single<Boolean> {
        return Single.create {
            updateSportListEmitter = it
            sjUniWatch.sendWriteNodeCmdList(getWriteUpdateSportListPayloadPackage(list))
        }
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {

    }

    fun appSportBusiness(nodeData: NodeData) {

        when (nodeData.urn[2]) {
            URN_APP_SPORT_LIST -> {

                if (nodeData.data.size == 1) {
                    updateSportListEmitter?.onSuccess(nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
                } else {

                    val byteBuffer = ByteBuffer.wrap(nodeData.data).order(ByteOrder.LITTLE_ENDIAN)

                    for (i in 0 until nodeData.data.size / 2) {
                        val sportId = byteBuffer.short.toInt()
                        val wmSport = WmSport(sportId, 0, false)
                        sjUniWatch.wmLog.logD(TAG, "sport id:" + sportId);
                        if (sportId != 0) {
                            mSportList.add(wmSport)
                        }
                    }

                    getSportListEmitter?.onSuccess(mSportList)

                }
            }
        }
    }


}