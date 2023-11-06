package com.sjbt.sdk.entity

import android.util.Log
import com.sjbt.sdk.TAG_SJ
import com.sjbt.sdk.log.SJLog
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
    lateinit var payload: ByteArray

    var payloadJson: String? = null

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
                '}'
    }

    val isNotTimeOut: Boolean
        get() = ((head == HEAD_COMMON && cmdId == CMD_ID_800D.toInt()) ||//绑定
                (head == HEAD_COMMON && cmdId == CMD_ID_800F.toInt()) ||//我的表盘列表
                (head == HEAD_COMMON && cmdId == CMD_ID_802E.toInt()) ||//绑定
                (head == HEAD_FILE_SPP_A_2_D && cmdId == CMD_ID_8003.toInt()) ||//传输文件的过程中，采用连续传输的方式
                (head == HEAD_CAMERA_PREVIEW && cmdId == CMD_ID_8002.toInt()) ||//相机预览
                (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8004.toInt()) || //通讯层节点消息
                (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8004.toInt())) //通讯层节点消息

    private val isNodeMsg: Boolean = head == HEAD_NODE_TYPE

    //节点消息的
    val timeOutCode: String
        get() {
            val timeOutCode: String
            if (isNodeMsg) {
                var requestId: Short = 0
                val requestArray = ByteArray(2)
                if (payload != null && payload!!.size > 2) {
                    requestArray[0] = payload!![0]
                    requestArray[1] = payload!![1]
                    requestId = BtUtils.byte2short(requestArray)
                }
                Log.d(TAG_SJ, "node timeout code requestId:$requestId")
                Log.d(TAG_SJ, "node timeout code not timeout:$isNotTimeOut")
                timeOutCode = "" + head + requestId //节点消息的
            } else {
                timeOutCode = "" + head + cmdOrder + cmdId
                Log.d(TAG_SJ, "old timeout code:$timeOutCode cmdId:$cmdId")
            }
            return timeOutCode
        }

    companion object {
        fun fromByteArrayToMsgBean(msg: ByteArray): MsgBean {
            val msgBean = MsgBean()
            try {
                val byteBuffer = ByteBuffer.wrap(msg)
                msgBean.originData = msg
                msgBean.head = byteBuffer.get()
                msgBean.cmdOrder = byteBuffer.get().toInt() and 0Xfffffff

                val cmdId = ByteArray(2)

                System.arraycopy(msg, 2, cmdId, 0, cmdId.size)

                val temp = cmdId[0]
                cmdId[0] = cmdId[1]
                cmdId[1] = temp
                cmdId[0] = 0x00

                msgBean.cmdIdStr = BtUtils.bytesToHexString(cmdId)
                msgBean.cmdId = BtUtils.byte2short(cmdId).toInt()

//            Log.e("SJ_SDK>>>>>", "response:" + response + "  cmdIdStr:" + msgBean.cmdIdStr)

                //            LogUtils.logBlueTooth("返回命令cmdId:" + msgBean.cmdId);
                val divideArray = ByteArray(2)
                divideArray[0] = byteBuffer[4]
                divideArray[1] = byteBuffer[5]

                val divideInfo = CmdHelper.readDivideInfoFromBytes(divideArray.reversedArray())
                val divideType = divideInfo.divideType

                msgBean.divideType = divideType
                msgBean.payloadPackTotalLen = divideInfo.payloadPackTotalLen

                val payLoadLength = msg.size - BT_MSG_BASE_LEN

                msgBean.payloadLen = payLoadLength
                val offsetArray = ByteArray(4)
                System.arraycopy(msg, 8, offsetArray, 0, offsetArray.size)
                msgBean.offset = ByteUtil.bytesToInt(offsetArray, ByteOrder.LITTLE_ENDIAN)
                val crcArray = ByteArray(4)
                System.arraycopy(msg, 12, crcArray, 0, crcArray.size)
                msgBean.crc = ByteUtil.bytesToInt(crcArray, ByteOrder.LITTLE_ENDIAN)

                if (msgBean.divideType == DIVIDE_N_2 || msgBean.divideType == DIVIDE_N_JSON) {
                    if (payLoadLength > 0) {

                        val payload = ByteArray(payLoadLength)
                        System.arraycopy(msg, BT_MSG_BASE_LEN, payload, 0, payLoadLength)
                        msgBean.payload = payload
                        if (divideType == DIVIDE_N_JSON) {
                            val payloadJson = String(payload, StandardCharsets.UTF_8)
                            msgBean.payloadJson = payloadJson
                        }

                        if (msgBean.head == HEAD_NODE_TYPE && msgBean.cmdId != CMD_ID_8004.toInt()) {
                            msgBean.requestId = ByteBuffer.wrap(msgBean.payload).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

                            msgBean.nodeId =
                                ByteBuffer.wrap(msgBean.payload.copyOfRange(10, 14)).order(ByteOrder.LITTLE_ENDIAN).int

                            Log.e(TAG_SJ, "requestId:" + msgBean.requestId)
                            Log.e(TAG_SJ, "nodeId:" + msgBean.nodeId)

                        }
                    }
                } else {//分包 subpackage
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

                        if (msgBean.head == HEAD_NODE_TYPE && msgBean.cmdId != CMD_ID_8004.toInt()) {
                            msgBean.requestId = ByteBuffer.wrap(msgBean.payload).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

                            msgBean.nodeId =
                                ByteBuffer.wrap(msgBean.payload.copyOfRange(10, 14)).order(ByteOrder.LITTLE_ENDIAN).int

                            Log.e(TAG_SJ, "requestId:" + msgBean.requestId)
                            Log.e(TAG_SJ, "nodeId:" + msgBean.nodeId)

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