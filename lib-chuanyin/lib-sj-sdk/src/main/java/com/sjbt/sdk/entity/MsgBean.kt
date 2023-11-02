package com.sjbt.sdk.entity

import android.util.Log
import com.sjbt.sdk.TAG_SJ
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.BtUtils

class MsgBean {
    @JvmField
    var head: Byte = 0
    var cmdOrder = 0
    var cmdIdStr: String? = null
    @JvmField
    var cmdId = 0
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
        return "BiuMsgBean{" +
                "head=" + head +
                ", cmdOrder=" + cmdOrder +
                ", cmdStr='" + cmdIdStr + '\'' +
                ", divideType=" + divideType +
                ", payloadLen=" + payloadLen +
                ", offset=" + offset +
                ", crc=" + crc +
                '}'
    }

    //绑定
    //我的表盘列表
    //绑定
    //传输文件的过程中，采用连续传输的方式
    //相机预览
    //                || (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8001)//节点消息
    //                || (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8002)//节点消息
    //通讯层节点消息
    val isNotTimeOut: Boolean
        get() = (head == HEAD_COMMON && cmdId == CMD_ID_800D.toInt() || head == HEAD_COMMON && cmdId == CMD_ID_800F.toInt() || head == HEAD_COMMON && cmdId == CMD_ID_802E.toInt() || head == HEAD_FILE_SPP_A_2_D && cmdId == CMD_ID_8003.toInt() || head == HEAD_CAMERA_PREVIEW) && cmdId == CMD_ID_8002.toInt() || head == HEAD_NODE_TYPE && cmdId == CMD_ID_8004.toInt() //通讯层节点消息

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
                Log.e(TAG_SJ, "node timeout code requestId:$requestId")
                Log.e(TAG_SJ, "node timeout code not timeout:" + isNotTimeOut)
                timeOutCode = "" + head + requestId //节点消息的
            } else {
                timeOutCode = "" + head + cmdOrder + cmdId
                Log.e(TAG_SJ, "old timeout code:$timeOutCode cmdId:$cmdId")
            }
            return timeOutCode
        }
}