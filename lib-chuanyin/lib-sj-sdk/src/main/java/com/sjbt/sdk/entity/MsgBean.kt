package com.sjbt.sdk.entity;

import static com.sjbt.sdk.SJConfigKt.TAG_SJ;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8001;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8002;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8003;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8004;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_800D;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_800F;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_802E;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_CAMERA_PREVIEW;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_COMMON;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_FILE_SPP_A_2_D;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_NODE_TYPE;
import android.util.Log;
import com.sjbt.sdk.utils.BtUtils;

public class MsgBean {
    //    public String head;
    public byte head;
    public int cmdOrder;
    public String cmdIdStr;
    public int cmdId;

    public byte divideType;
    public short payloadPackTotalLen;
    public int payloadLen;

    public int offset;
    public int crc;
    public int divideIndex;

    public byte[] payload;
    public String payloadJson;

    public byte[]  originData;

    @Override
    public String toString() {
        return "BiuMsgBean{" +
                "head=" + head +
                ", cmdOrder=" + cmdOrder +
                ", cmdStr='" + cmdIdStr + '\'' +
                ", divideType=" + divideType +
                ", payloadLen=" + payloadLen +
                ", offset=" + offset +
                ", crc=" + crc +
                '}';
    }

    public Boolean isNotTimeOut() {
        return (head == HEAD_COMMON && cmdId == CMD_ID_800D)//绑定
                || (head == HEAD_COMMON && cmdId == CMD_ID_800F)//我的表盘列表
                || (head == HEAD_COMMON && cmdId == CMD_ID_802E)//绑定

                || (head == HEAD_FILE_SPP_A_2_D && cmdId == CMD_ID_8003)//传输文件的过程中，采用连续传输的方式

                || (head == HEAD_CAMERA_PREVIEW && cmdId == CMD_ID_8002)//相机预览

//                || (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8001)//节点消息
//                || (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8002)//节点消息
                || (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8004)//通讯层节点消息
                ;
    }

    public Boolean isNodeMsg() {
        return head == HEAD_NODE_TYPE;
    }

    public String getTimeOutCode() {
        String timeOutCode;

        if (isNodeMsg()) {
            short requestId = 0;

            byte[] requestArray = new byte[2];

            if (payload != null && payload.length > 2) {
                requestArray[0] = payload[0];
                requestArray[1] = payload[1];
                requestId = BtUtils.byte2short(requestArray);
            }

            Log.e(TAG_SJ, "node timeout code requestId:" + requestId);
            Log.e(TAG_SJ, "node timeout code not timeout:" + isNotTimeOut());

            timeOutCode = "" + head + requestId;//节点消息的
        } else {
            timeOutCode = "" + head + cmdOrder + cmdId;
            Log.e(TAG_SJ, "old timeout code:" + timeOutCode+" cmdId:"+cmdId);
        }

        return timeOutCode;
    }

}
