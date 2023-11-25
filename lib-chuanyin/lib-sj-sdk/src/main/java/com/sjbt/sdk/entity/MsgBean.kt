package com.sjbt.sdk.entity

import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.ByteUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class MsgBean {

    @JvmField
    var head: Byte = 0
    var cmdOrder = 0
    var cmdIdStr: String? = null

    @JvmField
    var cmdId = 0
    var nodeId = 0
    var requestId = 0
    var divideType: Byte = 0
    var payloadPackTotalLen: Short = 0
    var payloadLen = 0
    var offset = 0
    var crc = 0
    var divideIndex = 0
    lateinit var originData: ByteArray
    var payload: ByteArray = byteArrayOf()
    var payloadJson: String? = null
    var payloadPackage: PayloadPackage? = null

    override fun toString(): String {
        return "MsgBean{" +
                "head=" + Integer.toHexString(head.toInt() and 0XFF) +
                ", cmdOrder=" + cmdOrder +
                ", cmdStr='" + cmdIdStr + '\'' +
                ", divideType=" + divideType +
                ", payloadLen=" + payloadLen +
                ", payloadPackTotalLen=" + payloadPackTotalLen +
                ", requestId=" + requestId +
                ", nodeId=" + nodeId +
                ", timeOutCode=" + timeOutCode +
                ", isNeedTimeOut=" + isNeedTimeOut +
                '}'
    }

    var isNeedTimeOut: Boolean = true
    var isNodeMsg: Boolean = head == HEAD_NODE_TYPE

    //节点消息的
    var timeOutCode: String? = null

    companion object {

        private fun isNeedTimeOut(head: Byte, divideType: Byte, cmdId: Int): Boolean {
            return !((head == HEAD_COMMON && cmdId == CMD_ID_800D.toInt()) ||//云端绑定同步
                    (head == HEAD_COMMON && cmdId == CMD_ID_800F.toInt()) ||//我的表盘列表
                    (head == HEAD_COMMON && cmdId == CMD_ID_802E.toInt()) ||//与设备绑定
                    (head == HEAD_FILE_SPP_A_2_D && cmdId == CMD_ID_8003.toInt()) ||//传输文件的过程中，采用连续传输的方式
                    (head == HEAD_CAMERA_PREVIEW && cmdId == CMD_ID_8002.toInt()) ||//相机预览
                    (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8004.toInt()) || //通讯层节点消息
                    (divideType == DIVIDE_Y_M_2 || divideType == DIVIDE_Y_M_JSON || divideType == DIVIDE_Y_E_2|| divideType == DIVIDE_Y_E_JSON)) //中间包尾包没有超时设置

        }

        fun fromByteArrayToMsgBean(msg: ByteArray): MsgBean {
            val msgBean = MsgBean()
            try {
                val byteBuffer = ByteBuffer.wrap(msg)
                msgBean.originData = msg
                msgBean.head = byteBuffer.get()
                msgBean.cmdOrder = byteBuffer.get().toUByte().toInt()
                msgBean.isNodeMsg = msgBean.head == HEAD_NODE_TYPE

                val cmdId = ByteArray(2)

                System.arraycopy(msg, 2, cmdId, 0, cmdId.size)

                val temp = cmdId[0]
                cmdId[0] = cmdId[1]
                cmdId[1] = temp
                cmdId[0] = 0x00

                msgBean.cmdIdStr = BtUtils.bytesToHexString(cmdId)
                msgBean.cmdId = BtUtils.byte2short(cmdId).toInt()

                val divideArray = ByteArray(2)
                divideArray[0] = byteBuffer[4]
                divideArray[1] = byteBuffer[5]

                val divideInfo = CmdHelper.readDivideInfoFromBytes(divideArray.reversedArray())
                val divideType = divideInfo.divideType

                msgBean.divideType = divideType
                msgBean.payloadPackTotalLen = divideInfo.payloadPackTotalLen

                msgBean.isNeedTimeOut = isNeedTimeOut(msgBean.head, divideType, msgBean.cmdId)

                val payLoadLength = msg.size - BT_MSG_BASE_LEN

                msgBean.payloadLen = payLoadLength
                val offsetArray = ByteArray(4)
                System.arraycopy(msg, 8, offsetArray, 0, offsetArray.size)
                msgBean.offset = ByteUtil.bytesToInt(offsetArray, ByteOrder.LITTLE_ENDIAN)
                val crcArray = ByteArray(4)
                System.arraycopy(msg, 12, crcArray, 0, crcArray.size)
                msgBean.crc = ByteUtil.bytesToInt(crcArray, ByteOrder.LITTLE_ENDIAN)

                if (msgBean.divideType == DIVIDE_N_2 || msgBean.divideType == DIVIDE_N_JSON) {

                    msgBean.timeOutCode = (msgBean.head + msgBean.cmdOrder).toString()

                    if (payLoadLength > 0) {
                        val payload = ByteArray(payLoadLength)
                        System.arraycopy(msg, BT_MSG_BASE_LEN, payload, 0, payLoadLength)
                        msgBean.payload = payload
                        if (divideType == DIVIDE_N_JSON) {
                            val payloadJson = String(payload, StandardCharsets.UTF_8)
                            msgBean.payloadJson = payloadJson
                        }

                        if (msgBean.head == HEAD_NODE_TYPE && msgBean.cmdId != CMD_ID_8004.toInt() && ((msgBean.divideType == DIVIDE_Y_F_2 || msgBean.divideType == DIVIDE_N_2))) {

                            msgBean.requestId = ByteBuffer.wrap(msgBean.payload)
                                .order(ByteOrder.LITTLE_ENDIAN).short.toUShort().toInt()

                            msgBean.nodeId =
                                ByteBuffer.wrap(msgBean.payload.copyOfRange(10, 14))
                                    .order(ByteOrder.LITTLE_ENDIAN).int

                            msgBean.payloadPackage = PayloadPackage.fromByteArray(payload)

                            msgBean.timeOutCode =
                                msgBean.requestId.toString() + msgBean.nodeId.toString()

                        }
                    }
                } else {//分包 subpackage
                    msgBean.timeOutCode = (msgBean.head + msgBean.cmdOrder).toString()

                    if (msgBean.head != HEAD_NODE_TYPE) {//如果不是节点数据，分包前四个是序号
                        val divideIndexArray = ByteArray(4)
                        System.arraycopy(
                            msg,
                            BT_MSG_BASE_LEN,
                            divideIndexArray,
                            0,
                            divideIndexArray.size
                        )
                        msgBean.divideIndex = ByteUtil.bytesToInt(divideIndexArray)

                        val payload = ByteArray(payLoadLength - 4)
                        System.arraycopy(msg, 20, payload, 0, payload.size)
                        msgBean.payload = payload

                    } else {
                        val payload = ByteArray(payLoadLength)
                        System.arraycopy(msg, BT_MSG_BASE_LEN, payload, 0, payload.size)
                        msgBean.payload = payload

                        if ((msgBean.divideType == DIVIDE_Y_F_2 || msgBean.divideType == DIVIDE_N_2) && msgBean.payload.size > 10) {
                            msgBean.payloadPackage = PayloadPackage.fromByteArray(payload)

                            msgBean.requestId = ByteBuffer.wrap(msgBean.payload)
                                .order(ByteOrder.LITTLE_ENDIAN).short.toUShort().toInt()

                            msgBean.nodeId =
                                ByteBuffer.wrap(msgBean.payload.copyOfRange(10, 14))
                                    .order(ByteOrder.LITTLE_ENDIAN).int

                            msgBean.timeOutCode = msgBean.requestId.toString()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return msgBean
        }
    }
}