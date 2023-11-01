package com.sjbt.sdk.spp.cmd

import android.util.Log
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.apps.*
import com.base.sdk.entity.apps.WmContact.Companion.NAME_BYTES_LIMIT
import com.base.sdk.entity.apps.WmContact.Companion.NUMBER_BYTES_LIMIT
import com.base.sdk.entity.settings.*
import com.base.sdk.port.FileType
import com.google.gson.Gson
import com.sjbt.sdk.ALARM_NAME_LEN
import com.sjbt.sdk.entity.DivideInfo
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.OtaCmdInfo
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.entity.old.TimeSyncBean
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.ByteUtil
import com.sjbt.sdk.utils.TimeUtils
import org.json.JSONException
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

object CmdHelper {
    //对CMD_ORDER.length 取余后作为 CMD_ORDER 的索引下标
    var command_index = 0
    private var key1 = 0
    private var mKey1: String? = null
    private var mKey2: String? = null
    private var mKeyData1: String? = null
    private var mKeyData2: String? = null
    private val gson = Gson()

    const val MAX_ORDER_ID = 127

    /**
     * 写入分包类型和总长度
     */
    private fun writeShortToBytes(divideType: Byte, totalLen: Short): ByteArray {
        val result = ByteArray(2)

        // 用第一个字节的高5位存储value的高5位 248 = 0b11111000
        result[0] = (totalLen.toInt() shr 8 shl 3 and 248).toByte()

        // 写入低三位分包类型和数据类型 7 = 0b00000111
        result[0] = (divideType and 7) or result[0]

        // 用第二个字节存储value的低8位
        result[1] = totalLen.toByte()


        return result
    }

    /**
     * 获取divideType和业务单元总包长度
     */
    fun readDivideInfoFromBytes(data: ByteArray): DivideInfo {
        require(data.size == 2) { "Invalid byte array length" }

        // 使用第二个字节的低8位和第一个字节的高5位构造short值 248 = 0b11111000
        val upperByte = data[0].toInt() and 248 shl 8 shr 3
        val lowerByte = data[1].toInt() and 0xFF
        val value = (lowerByte or upperByte).toShort()

        // 低三位分包类型和数据类型  7 = 0b00000111
        val divideType = (data[1].toInt() and 7).toByte()
        return DivideInfo(divideType, value)
    }


    /**
     * 组包公用方法
     */
    fun constructCmd(
        head: Byte,
        cmd_id: Short,
        divideType: Byte,
        dividePayloadTotalLen: Short = 0,
        offset: Int,
        crc: Int,
        payload: ByteArray?
    ): ByteArray {
        val payLoadLength = payload?.size ?: 0
        val byteBuffer = ByteBuffer.allocate(16 + payLoadLength)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN) //采用小端

        //TYPE
        byteBuffer.put(head)
        byteBuffer.put((command_index % MAX_ORDER_ID).toByte())
        Log.e("SJ_SDK>>>>>", "ORIGIN_ORDER_ID:" + (command_index % MAX_ORDER_ID).toByte())
        byteBuffer.putShort((cmd_id.toInt() and TRANSFER_KEY.toInt()).toShort()) //携带方向

        //Length
//        val divideLenArray = writeShortToBytes(divideType, dividePayloadTotalLen)
//        Log.e("SJ_SDK>>>>>", "DivideType INFO:" + readShortFromBytes(divideLenArray))

        byteBuffer.put(writeShortToBytes(divideType, dividePayloadTotalLen))
        byteBuffer.putShort(payLoadLength.toShort())

        //Offset
        byteBuffer.putInt(offset)

        //CRC
        byteBuffer.putInt(crc)

        //Payload
        if (payload != null) {
            byteBuffer.put(payload)
        }
        byteBuffer.flip()
        command_index++
        return byteBuffer.array()
    }

    fun getRandomNumber(length: Int): String? {
        val chars = RANDOM.toCharArray()
        val l = System.currentTimeMillis()
        val random = Random(l)
        val stringBuffer = StringBuffer()
        for (i in 0 until length) {
            stringBuffer.append(chars[random.nextInt(chars.size)])
        }
        return BtUtils.stringToHexString(stringBuffer.toString())
    }

    /**
     * 获取JSON格式的 Payload数据
     *
     * @param msg
     * @return
     */
    @JvmStatic
    fun getPayLoadJson(response: Boolean, msg: ByteArray): MsgBean {
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

            val divideInfo = readDivideInfoFromBytes(divideArray.reversedArray())
            val divideType = divideInfo.divideType

            msgBean.divideType = divideType
            msgBean.payloadPackTotalLen = divideInfo.payloadPackTotalLen

            val payLoadLength = msg.size - 16

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
                    System.arraycopy(msg, 16, payload, 0, payLoadLength)
                    msgBean.payload = payload
                    if (divideType == DIVIDE_N_JSON) {
                        val payloadJson = String(payload, StandardCharsets.UTF_8)
                        msgBean.payloadJson = payloadJson
                    }
                }
            } else {
                if (msgBean.head != HEAD_NODE_TYPE) {//如果不是节点数据，分包前四个是序号
                    val divideIndexArray = ByteArray(4)
                    System.arraycopy(msg, 16, divideIndexArray, 0, divideIndexArray.size)
                    msgBean.divideIndex = ByteUtil.bytesToInt(divideIndexArray)

                    val payload = ByteArray(payLoadLength - 4)
                    System.arraycopy(msg, 20, payload, 0, payload.size)
                    msgBean.payload = payload
                } else {
                    val payload = ByteArray(payLoadLength)
                    System.arraycopy(msg, 16, payload, 0, payload.size)
                    msgBean.payload = payload
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return msgBean
    }

    /**
     * 新协议 获取校验命令
     *
     * @return
     */
    val verifyPayload: ByteArray
        get() {
            val verificationArray = arrayOfNulls<String>(5)
            verificationArray[0] = getRandomNumber(14)
            verificationArray[1] = getRandomNumber(2) //密钥
            verificationArray[2] = getRandomNumber(32)
            verificationArray[3] = getRandomNumber(16) //异或原参

            val bytes = BtUtils.hexStringToByteArray(verificationArray[2])
            key1 = verificationArray[1]!!.toInt(16)
            mKey1 = BtUtils.intToHex(key1)
            //        LogUtils.logBlueTooth("APP加密的Key1:" + mKey1);
            verificationArray[4] = BtUtils.bytesToHexString(bytes) //未加密数据

//        LogUtils.logBlueTooth("APP未加密的数据:" + verificationArray[4]);
            verificationArray[2] =
                BtUtils.bytesToHexString(BtUtils.encryptData(key1, bytes, bytes.size))

//        LogUtils.logBlueTooth("APP加密后的数据:" + verificationArray[2]);
            mKeyData1 = verificationArray[3]
            mKeyData2 = verificationArray[4]
            mKey2 = BtUtils.getTheAccumulatedValueAnd(verificationArray[2]) //加密后累加

//        LogUtils.logBlueTooth("APP未加密的数据_mKeyData2 :" + mKeyData2);
//        LogUtils.logBlueTooth("APP解密累加后 KEY2:" + mKey2);
            val sbVerify = StringBuilder()
            sbVerify.append(verificationArray[0])
            sbVerify.append(verificationArray[1])
            sbVerify.append(verificationArray[2])
            sbVerify.append(verificationArray[3])
            return BtUtils.hexStringToByteArray(sbVerify.toString())
        }

    /**
     * 新协议 握手命令
     *
     * @return 组装好的握手命令
     */
    val biuShakeHandsCmd: ByteArray
        get() {
            val payload = BtUtils.hexStringToByteArray(getRandomNumber(61))
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            //LogUtils.logBlueTooth("发送握手消息:")
            return constructCmd(
                HEAD_VERIFY,
                CMD_ID_8001.toShort(),
                DIVIDE_N_2,
                0,
                0,
                crc,
                payload
            )
        }

    /**
     * 新协议 获取通讯层协议
     *
     * @return 组装好的握手命令
     */
    val communityMsg: ByteArray
        get() {
            val payload = ByteArray(1)
            payload[0] = 1
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            return constructCmd(
                HEAD_NODE_TYPE,
                CMD_ID_8004,
                DIVIDE_N_2,
                0,
                0,
                crc,
                payload
            )
        }

    /**
     * 获取绑定命令
     * @return
     */
    fun getBindCmd(bindInfo: WmBindInfo): ByteArray {
        //LogUtils.logBlueTooth("绑定命令")
        val byteBuffer = ByteBuffer.allocate(17)
        byteBuffer.put(bindInfo.bindType.ordinal.toByte())

        bindInfo.randomCode?.let {
            //LogUtils.logBlueTooth("随机码:$it")
            byteBuffer.put(it.toByteArray())
        }

        val payload = byteBuffer.array()
        val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)

        return constructCmd(
            HEAD_COMMON,
            CMD_ID_802E,
            DIVIDE_N_2,
            0,
            0,
            crc,
            payload
        )
    }

    /**
     * 获取绑定命令
     * @return
     */
    fun getUnBindCmd(): ByteArray {
        //LogUtils.logBlueTooth("解绑命令")

        return constructCmd(
            HEAD_COMMON,
            CMD_ID_802F,
            DIVIDE_N_2,
            0,
            0,
            0,
            null
        )
    }

    /**
     * 新协议 校验命令
     *
     * @return 组装好的校验命令
     */
    val biuVerifyCmd: ByteArray
        get() {
            val payload = verifyPayload
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            //LogUtils.logBlueTooth("2.发送校验信息:")
            return constructCmd(
                HEAD_VERIFY,
                CMD_ID_8002,
                DIVIDE_N_2,
                0,
                0,
                crc,
                payload
            )
        }

    /**
     * 获取同步时间命令
     *
     * @return
     */
    /**
     * 获取同步时间命令
     *
     * @return
     */
    val syncTimeCmd: ByteArray
        get() {
            //LogUtils.logBlueTooth("4.5 同步时间信息:")
            val timeSyncBean = TimeSyncBean()
            timeSyncBean.setCurrDate(
                TimeUtils.getNowString(
                    TimeUtils.getSafeDateFormat(
                        "yyyy-MM-dd"
                    )
                )
            )
            timeSyncBean.setTimeZoo(
                SimpleTimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)
            )
            timeSyncBean.setCurrTime(
                TimeUtils.getNowString(
                    TimeUtils.getSafeDateFormat(
                        "yyyy-MM-dd HH:mm:ss"
                    )
                )
            )
            timeSyncBean.setTimestamp(System.currentTimeMillis() / 1000)
            timeSyncBean.setTimeformat(1)
            //LogUtils.logCommon("时间同步：$timeSyncBean")
            val payload = gson.toJson(timeSyncBean).toByteArray(StandardCharsets.UTF_8)
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            return constructCmd(HEAD_COMMON, CMD_ID_8007, DIVIDE_N_JSON, 0, 0, crc, payload)
        }

    /**
     * 获取基本信息
     *
     * @return
     */
    val baseInfoCmd: ByteArray
        get() {
            //logSendMsg("3.发送基本信息:")
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8001.toShort(),
                DIVIDE_N_JSON,
                0, 0,
                0,
                null
            )
        }

    /**
     * 获取APPView list
     *
     * @return
     */
    val appViewList: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_8008.toShort(),
            DIVIDE_N_2,
            0, 0,
            0,
            null
        )//1抬腕数据

    /**
     * 预备收集抬腕数据
     *
     * @return
     */
    val debugDataPreState: ByteArray
        get() {
            val payload = ByteArray(1)
            payload[0] = 1 //1抬腕数据
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            return constructCmd(
                HEAD_COLLECT_DEBUG_DATA,
                CMD_ID_8001.toShort(),
                DIVIDE_N_2,
                0, 0,
                crc,
                payload
            )
        }

    /**
     * 预备收集抬腕数据
     *
     * @return
     */
    fun getCollectDebugData(page: Byte): ByteArray {
        val payload = ByteArray(1)
        payload[0] = page //1抬腕数据
        val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
        return constructCmd(
            HEAD_COLLECT_DEBUG_DATA,
            CMD_ID_8002.toShort(),
            DIVIDE_N_2,
            0,
            0,
            crc,
            payload
        )
    }

    /**
     * 生成设置Appview的命令
     *
     * @param id
     * @return
     */
    fun setAppViewCmd(id: Byte): ByteArray {
        val payload = ByteArray(1)
        payload[0] = id
        val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8009.toShort(),
            DIVIDE_N_2,
            0,
            0,
            crc,
            payload
        )
    }

    /**
     * 获取电池信息
     *
     * @return
     */
    val batteryInfo: ByteArray
        get() {
            //logSendMsg("发送电量信息:")
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8003.toShort(),
                DIVIDE_N_JSON,
                0,
                0,
                0,
                null
            )
        }

    /**
     * 获取状态信息CMD
     *
     * @return
     */
    val statusCmd: ByteArray
        get() {
            //logSendMsg("2.发送状态信息:")
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8002.toShort(),
                DIVIDE_N_2,
                0,
                0,
                0,
                null
            )
        }

    /**
     * 获取同步通知消息
     *
     * @param notifyMsgBean
     * @return
     */
    fun getNotificationCmd(notifyMsgBean: WmNotification?): ByteArray {
        val payload = gson.toJson(notifyMsgBean).toByteArray(StandardCharsets.UTF_8)
        //logSendMsg("发送通知消息:" + gson.toJson(notifyMsgBean))
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8004,
            DIVIDE_N_JSON,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取时间同步设置状态
     *
     * @return
     */
    fun getTimeSetCmd(mode: Int, open: Int): ByteArray? {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("mode", mode)
            jsonObject.put("open", open)
            val json = jsonObject.toString()
            //logSendMsg("4.5发送查询时间同步设置：$json")
            val payload =
                json.toByteArray(StandardCharsets.UTF_8)
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_800C.toShort(),
                DIVIDE_N_JSON,
                0,
                0,
                crc,
                payload
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取一次设置多个初始化信息的命令
     *
     * @return
     */
    fun getSetAllSportInitCmd(initInfo: WmSportGoal): ByteArray {
//        //logSendMsg("一次设置多个初始化信息:")
        val jsonObject = JSONObject()
        return try {
            jsonObject.put("heat_goal", initInfo.calories)
            jsonObject.put("step_goal", initInfo.steps)
            jsonObject.put("dis_goal", initInfo.distance)
            val json = jsonObject.toString()
            val payload =
                json.toByteArray(StandardCharsets.UTF_8)
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            constructCmd(
                HEAD_SPORT_HEALTH,
                CMD_ID_8005.toShort(),
                DIVIDE_N_JSON,
                0,
                0,
                crc,
                payload
            )
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 获取设置密码
     *
     * @param msgSetType
     * @param password
     * @return
     */
    fun getSetPwdCmd(password: String?, msgSetType: Int): ByteArray {
        return try {
            val jsonObject = JSONObject()
            jsonObject.put("type", msgSetType)
            jsonObject.put("pwd", password)
            val json = jsonObject.toString()
            //LogUtils.logCommon("设置密码：$json")
            val payload =
                json.toByteArray(StandardCharsets.UTF_8)
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            constructCmd(
                HEAD_COMMON,
                CMD_ID_800B.toShort(),
                DIVIDE_N_JSON,
                0,
                0,
                crc,
                payload
            )
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 同步绑定状态
     *
     * @return
     */
    fun getBindStateCmd(state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_800D.toShort(),
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    val createBondCmd: ByteArray
        get() {
            val byteBuffer = ByteBuffer.allocate(1)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            byteBuffer.put(1.toByte())
            byteBuffer.flip()
            val payload = byteBuffer.array()
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8012.toShort(),
                DIVIDE_N_2,
                0,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )
        }

    /**
     * 获取取消绑定协议CMD
     *
     * @return
     */
    val cancelBindRequestCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_8011.toShort(),
            DIVIDE_N_2,
            0,
            0,
            0,
            null
        )

    /**
     * 获取解绑协议CMD
     *
     * @return
     */
    val unBindRequestCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_800E.toShort(),
            DIVIDE_N_2,
            0,
            0,
            0,
            null
        )

    /**
     * 获取DialList CMD
     *
     * @return
     */
    fun getDialListCmd(order: Byte): ByteArray {
//        logSendMsg("9.从设备端获取我的表盘数据")
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(order)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_800F.toShort(),
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取DialList CMD
     *
     * @return
     */
    fun getDialStateCmd(id: String): ByteArray {
        val idArray = id.toByteArray()
        val byteBuffer = ByteBuffer.allocate(idArray.size)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(idArray)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8014.toShort(),
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }


    /**
     * 操作表盘 0x0 type:1设定 2删除
     *
     * @param type
     * @param dialId
     * @return
     */
    fun getDialActionCmd(type: Byte, dialId: String): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1 + dialId.toByteArray().size)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type)
        byteBuffer.put(dialId.toByteArray())
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8010.toShort(),
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取81命令
     *
     * @param type 1:音频文件 2:OTA（根据实际需求定义） 3:BIN
     * @return
     */
    fun getTransferFile01Cmd(type: Byte, fileLen: Int, fileCount: Int): ByteArray {
//        logSendMsg("文件长度:$fileLen")
        val byteBuffer = ByteBuffer.allocate(1 + 4 + 1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type)
        if (type == FileType.OTA.type.toByte() || type == FileType.OTA_UPEX.type.toByte()) {
            byteBuffer.putInt(-1)
        } else {
            byteBuffer.putInt(fileLen)
        }

        byteBuffer.put(fileCount.toByte())
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_SPP_A_2_D,
            CMD_ID_8001.toShort(),
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 文件长度和名称上报
     *
     * @param len  文件长度
     * @param name 文件名称
     * @return
     */
    fun getTransferFile02Cmd(len: Int, name: String): ByteArray {
        val nameByte = name.toByteArray(Charset.defaultCharset())
        val byteBuffer = ByteBuffer.allocate(4 + nameByte.size)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.putInt(len)
        byteBuffer.put(nameByte)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_SPP_A_2_D,
            CMD_ID_8002,
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取83命令
     *
     * @param otaCmdInfo
     * @return
     */
    fun getTransfer03Cmd(
        process: Int,
        otaCmdInfo: OtaCmdInfo,
        divideType: Byte
    ): ByteArray {
        val byteBuffer =
            ByteBuffer.allocate(otaCmdInfo.payload.size + 4)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.putInt(process)
        byteBuffer.put(otaCmdInfo.payload)
        byteBuffer.flip()
        otaCmdInfo.payload = byteBuffer.array()
        otaCmdInfo.crc =
            BtUtils.getCrc(HEX_FFFF, otaCmdInfo.payload, otaCmdInfo.payload.size)
//        logSendMsg("发送消息序号：" + process + " 包长:" + otaCmdInfo.payload.size)
        return constructCmd(
            HEAD_FILE_SPP_A_2_D, CMD_ID_8003,
            divideType, 0, otaCmdInfo.offSet, otaCmdInfo.crc, otaCmdInfo.payload
        )
    }

    /**
     * 获取83命令 确认是否成功
     *
     * @return
     */
    val transfer04Cmd: ByteArray
        get() = constructCmd(
            HEAD_FILE_SPP_A_2_D, CMD_ID_8004.toShort(),
            DIVIDE_N_2, 0, 0, 0, null
        )

    /**
     * App取消文件传输
     *
     * @return
     */
    val transferCancelCmd: ByteArray
        get() = constructCmd(
            HEAD_FILE_SPP_A_2_D, CMD_ID_8005.toShort(),
            DIVIDE_N_2, 0, 0, 0, null
        )

    /**
     * 获取睡眠记录
     *
     * @return
     */
    val getSleepSetCmd: ByteArray
        get() {
//            logSendMsg("获取睡眠区间设置")
            return constructCmd(
                HEAD_SPORT_HEALTH,
                CMD_ID_800C,
                DIVIDE_N_2,
                0,
                0,
                0,
                null
            )
        }

    /**
     * 获取睡眠记录
     *
     * @return
     */
    fun setSleepSetCmd(open: Byte, startH: Byte, startM: Byte, endH: Byte, endM: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(5)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(open)
        byteBuffer.put(startH)
        byteBuffer.put(startM)
        byteBuffer.put(endH)
        byteBuffer.put(endM)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_SPORT_HEALTH,
            CMD_ID_800E.toShort(),
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * App修改来电响铃
     *
     *
     * 0: 响铃
     * 1: 震动反馈
     * 2: 抬腕亮屏
     *
     * @param state
     * @return
     */
    fun getSetDeviceRingStateCmd(type: Byte, state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8018.toShort(),
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 设备端修改来电响铃
     *
     * @return
     */
    val deviceRingStateCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_8017.toShort(),
            DIVIDE_N_2,
            0,
            0,
            0,
            null
        )

    /**
     * 设备端修改来电响铃
     *
     * @return
     */
    val deviceRingStateRespondCmd: ByteArray
        get() {
            val byteBuffer = ByteBuffer.allocate(1)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            byteBuffer.put(1.toByte())
            byteBuffer.flip()
            val payload = byteBuffer.array()
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8019.toShort(),
                DIVIDE_N_2,
                0,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )
        }

    /**
     * 当设备添加通讯录的时候需要给回应，固定是成功
     * CMD_ID_8024/CMD_ID_8026/CMD_ID_8020
     *
     * @return
     */
    fun getRespondSuccessCmd(cmdId: Short): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(1.toByte())
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            cmdId,
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 回应相机
     *
     * @param state
     * @return
     */
    fun getCameraRespondCmd(cmdId: Short, state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            cmdId,
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 相机设置
     *
     * @param state
     * @return
     */
    fun getCameraStateActionCmd(action: Byte, state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(action)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_802C,
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * App拉起设备相机
     *
     * @param state
     * @return
     */
    fun getAppCallDeviceCmd(state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_802A,
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    private fun getFormatTime(timeMillions: Long): Int {
        val simpleDateFormat = SimpleDateFormat("yyMMdd")
        val date = Date(timeMillions)
        val res = simpleDateFormat.format(date)
//        logSendMsg("转换后的时间：$res")
        return res.toInt()
    }

    private val weatherCodeClear = intArrayOf(0, 1, 2, 3, 38)
    private val weatherCodeCloud = intArrayOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19)
    private val weatherCodeRain = intArrayOf(20, 21, 22, 23, 24, 25, 37)
    private val weatherCodeSnow = intArrayOf(26, 27, 28, 29, 30, 31)
    private val weatherCodeSand = intArrayOf(4, 5, 6, 7, 8, 9, 32, 33, 34, 35, 36)

    /**
     * 1：晴； 2：多云；3：雨；4：雪；5：沙尘  6未知
     *
     * @param weatherCode
     * @return
     */
    private fun getWeatherBigCode(weatherCode: Int): Byte {
        var weatherBigCode: Byte = 99
        for (code in weatherCodeClear) {
            if (code == weatherCode) {
                weatherBigCode = 1
                return weatherBigCode
            }
        }
        for (code in weatherCodeCloud) {
            if (code == weatherCode) {
                weatherBigCode = 2
                return weatherBigCode
            }
        }
        for (code in weatherCodeRain) {
            if (code == weatherCode) {
                weatherBigCode = 3
                return weatherBigCode
            }
        }
        for (code in weatherCodeSnow) {
            if (code == weatherCode) {
                weatherBigCode = 4
                return weatherBigCode
            }
        }
        for (code in weatherCodeSand) {
            if (code == weatherCode) {
                weatherBigCode = 5
                return weatherBigCode
            }
        }
        return weatherBigCode
    }

    private fun getWeekByTimeStamp(weekTime: Long): Byte {
        val week = TimeUtils.getUSWeek(weekTime)

//        SJLog.INSTANCE.logSendMsg("星期：" + week);
        if (week.contains("Sun")) {
            return 7
        } else if (week.contains("Mon")) {
            return 1
        } else if (week.contains("Tue")) {
            return 2
        } else if (week.contains("Wed")) {
            return 3
        } else if (week.contains("Thu")) {
            return 4
        } else if (week.contains("Fri")) {
            return 5
        } else if (week.contains("Sat")) {
            return 6
        } else if (week.contains("Sun")) {
            return 7
        }
        return 0
    }

    /**
     * 相机预览界面开始
     *
     * @return
     */
    fun getCameraPreviewCmd01(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(1.toByte())
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_CAMERA_PREVIEW,
            CMD_ID_8001,
            DIVIDE_N_2,
            0,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 相机预览界面数据发送
     *
     * @return
     */
    fun getCameraPreviewDataCmd02(data: ByteArray, divideType: Byte): ByteArray {
        return constructCmd(
            HEAD_CAMERA_PREVIEW,
            CMD_ID_8002,
            divideType,
            0,
            0,
            BtUtils.getCrc(
                HEX_FFFF,
                data,
                data.size
            ),
            data
        )
    }

    fun sizeOfNumber(variable: Any): Int {
        // Long.SIZE_BYTES = 8
        return ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN)
            .let { buffer ->
                when (variable) {
                    is Byte -> buffer.put(variable)
                    is Short -> buffer.putShort(variable)
                    is Int -> buffer.putInt(variable)
                    is Long -> buffer.putLong(variable)
                    is Float -> buffer.putFloat(variable)
                    is Double -> buffer.putDouble(variable)
                    else -> throw IllegalArgumentException("unsupported data type: ${variable::class.simpleName}")
                }
                buffer.position(0).limit()
            }
    }

    fun getUrnId(
        parentUrn: Byte,
        childUrn: Byte = URN_0,
        grandSon: Byte = URN_0,
        grandgrandSon: Byte = URN_0
    ): ByteArray {
        return byteArrayOf(
            parentUrn,
            childUrn,
            grandSon,
            grandgrandSon,
        )
    }

    /**
     * 获取设备上体育目标配置
     */
    fun getDeviceSportGoalCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(getUrnId(URN_2, URN_1), ByteArray(0))
        return payloadPackage
    }

    /**
     * 获取设置体育目标的命令
     */
    fun getUpdateSportGoalAllCmd(
        sportGoal: WmSportGoal
    ): PayloadPackage {

        val payloadPackage = PayloadPackage()

//        if (sportGoal.steps != 0 || sportGoal.calories != 0 || sportGoal.distance != 0 || sportGoal.activityDuration.toInt() != 0) {
        val bbSport: ByteBuffer = ByteBuffer.allocate(4 + 4 + 4 + 2).order(ByteOrder.LITTLE_ENDIAN)
        bbSport.putInt(sportGoal.steps)
        bbSport.putInt(sportGoal.calories)
        bbSport.putInt(sportGoal.distance)
        bbSport.putShort(sportGoal.activityDuration)
        payloadPackage.putData(getUrnId(URN_2, URN_1), bbSport.array())
//        } else {
//            if (sportGoal.steps != 0) {
//                payloadPackage.putData(
//                    getUrnId(URN_2, URN_1, URN_1),
//                    ByteBuffer.allocate(sizeOfNumber(sportGoal.steps)).putInt(sportGoal.steps)
//                        .array()
//                )
//            } else if (sportGoal.calories != 0) {
//                payloadPackage.putData(
//                    getUrnId(URN_2, URN_1, URN_2),
//                    ByteBuffer.allocate(sizeOfNumber(sportGoal.calories)).putInt(sportGoal.calories)
//                        .array()
//                )
//            } else if (sportGoal.distance != 0) {
//                payloadPackage.putData(
//                    getUrnId(URN_2, URN_1, URN_3),
//                    ByteBuffer.allocate(sizeOfNumber(sportGoal.distance)).putInt(sportGoal.distance)
//                        .array()
//                )
//            } else if (sportGoal.activityDuration.toInt() != 0) {
//                payloadPackage.putData(
//                    getUrnId(URN_2, URN_1, URN_4),
//                    ByteBuffer.allocate(sizeOfNumber(sportGoal.activityDuration))
//                        .putShort(sportGoal.activityDuration).array()
//                )
//            }
//        }

        return payloadPackage
    }

    /**
     * 获取设备上体育目标配置
     */
    fun getDevicePersonalInfoCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(getUrnId(URN_2, URN_2), ByteArray(0))
        return payloadPackage
    }

    /**
     * 获取设置健康信息的命令
     */
    fun getUpdatePersonalInfoAllCmd(
        personalInfo: WmPersonalInfo
    ): PayloadPackage {

        val payloadPackage = PayloadPackage()

//        if (personalInfo.height != 0 || personalInfo.weight != 0 || personalInfo.gender != 0 || personalInfo.activityDuration.toInt() != 0) {
        val bbSport: ByteBuffer = ByteBuffer.allocate(2 + 2 + 1 + 4).order(ByteOrder.LITTLE_ENDIAN)
        bbSport.putShort(personalInfo.height)
        bbSport.putShort(personalInfo.weight)
        bbSport.put(personalInfo.gender.ordinal.toByte())
        bbSport.putShort(personalInfo.birthDate.year)
        bbSport.put(personalInfo.birthDate.month)
        bbSport.put(personalInfo.birthDate.day)
        payloadPackage.putData(getUrnId(URN_2, URN_2), bbSport.array())
//        } else {
//            if (personalInfo.steps != 0) {
//                payloadPackage.putData(
//                    getUrnId(URN_2, URN_1, URN_1),
//                    ByteBuffer.allocate(sizeOfNumber(personalInfo.steps)).putInt(personalInfo.steps)
//                        .array()
//                )
//            } else if (personalInfo.calories != 0) {
//                payloadPackage.putData(
//                    getUrnId(URN_2, URN_1, URN_2),
//                    ByteBuffer.allocate(sizeOfNumber(personalInfo.calories)).putInt(personalInfo.calories)
//                        .array()
//                )
//            } else if (personalInfo.distance != 0) {
//                payloadPackage.putData(
//                    getUrnId(URN_2, URN_1, URN_3),
//                    ByteBuffer.allocate(sizeOfNumber(personalInfo.distance)).putInt(personalInfo.distance)
//                        .array()
//                )
//            } else if (personalInfo.activityDuration.toInt() != 0) {
//                payloadPackage.putData(
//                    getUrnId(URN_2, URN_1, URN_4),
//                    ByteBuffer.allocate(sizeOfNumber(personalInfo.activityDuration))
//                        .putShort(personalInfo.activityDuration).array()
//                )
//            }
//        }

        return payloadPackage
    }

    /**
     * 获取设置单位信息的命令
     */
    fun getWriteUnitSettingCmd(
        wmUnitInfo: WmUnitInfo
    ): PayloadPackage {

        val payloadPackage = PayloadPackage()
        val bbSport: ByteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
        bbSport.put(wmUnitInfo.timeFormat.ordinal.toByte())
        bbSport.put(wmUnitInfo.distanceUnit.ordinal.toByte())
        bbSport.put(wmUnitInfo.temperatureUnit.ordinal.toByte())
        bbSport.put(wmUnitInfo.weightUnit.ordinal.toByte())

        payloadPackage.putData(getUrnId(URN_2, URN_3), bbSport.array())

        return payloadPackage
    }

    /**
     * 获取设置单位信息的命令
     */
    fun getReadUnitSettingCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val bbSport: ByteBuffer = ByteBuffer.allocate(0)

        payloadPackage.putData(getUrnId(URN_2, URN_3), bbSport.array())

        return payloadPackage
    }

    /**
     * 获取语言列表的命令
     */
    fun getReadLanguageListCmd(): PayloadPackage {

        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(getUrnId(URN_2, URN_4, URN_1), byteBuffer.array())

        return payloadPackage
    }

    /**
     * 获取设置语言命令
     */
    fun getWriteLanguageCmd(bcp: String): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val bbSport: ByteBuffer = ByteBuffer.allocate(6)
        if (bcp.length <= 6) {
            bbSport.put(bcp.toByteArray(StandardCharsets.UTF_8))
            payloadPackage.putData(getUrnId(URN_2, URN_4, URN_2), bbSport.array())
        }

        return payloadPackage
    }

    /**
     * 获取久坐提醒设置
     */
    fun getReadSedentaryReminderCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(getUrnId(URN_2, URN_5), ByteBuffer.allocate(0).array())
        return payloadPackage
    }

    /**
     * 设置久坐提醒
     */
    fun getWriteSedentaryReminderCmd(sedentaryReminder: WmSedentaryReminder): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(
            if (sedentaryReminder.isEnabled) {
                1.toByte()
            } else {
                0.toByte()
            }
        )
        byteBuffer.put(sedentaryReminder.timeRange.startHour.toByte())
        byteBuffer.put(sedentaryReminder.timeRange.startMinute.toByte())
        byteBuffer.put(sedentaryReminder.timeRange.endHour.toByte())
        byteBuffer.put(sedentaryReminder.timeRange.endMinute.toByte())
        byteBuffer.put(sedentaryReminder.frequency.value.toByte())

        byteBuffer.put(
            if (sedentaryReminder.noDisturbLunchBreak.isEnabled) {
                1.toByte()
            } else {
                0.toByte()
            }
        )
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.startHour.toByte())
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.startMinute.toByte())
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.endHour.toByte())
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.endMinute.toByte())

        payloadPackage.putData(getUrnId(URN_2, URN_5), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 获取喝水提醒设置
     */
    fun getReadDrinkReminderCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(getUrnId(URN_2, URN_6), ByteBuffer.allocate(0).array())
        return payloadPackage
    }

    /**
     * 设置喝水提醒
     */
    fun getWriteReadDrinkReminderCmd(sedentaryReminder: WmSedentaryReminder): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(
            if (sedentaryReminder.isEnabled) {
                1.toByte()
            } else {
                0.toByte()
            }
        )
        byteBuffer.put(sedentaryReminder.timeRange.startHour.toByte())
        byteBuffer.put(sedentaryReminder.timeRange.startMinute.toByte())
        byteBuffer.put(sedentaryReminder.timeRange.endHour.toByte())
        byteBuffer.put(sedentaryReminder.timeRange.endMinute.toByte())
        byteBuffer.put(sedentaryReminder.frequency.value.toByte())

        byteBuffer.put(
            if (sedentaryReminder.noDisturbLunchBreak.isEnabled) {
                1.toByte()
            } else {
                0.toByte()
            }
        )
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.startHour.toByte())
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.startMinute.toByte())
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.endHour.toByte())
        byteBuffer.put(sedentaryReminder.noDisturbLunchBreak.timeRange.endMinute.toByte())

        payloadPackage.putData(getUrnId(URN_2, URN_6), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 添加闹钟
     */
    fun getWriteUpdateAlarmCmd(alarms: List<WmAlarm>): PayloadPackage {
        val payloadPackage = PayloadPackage()

        val totalAlarms = mutableListOf<WmAlarm>()
        totalAlarms.addAll(alarms)

//        if (alarms.size < 10) {
//            for (i in 0 until 10 - alarms.size) {
//                totalAlarms.add(WmAlarm("", 0, 0, AlarmRepeatOption.fromValue(0)))
//            }
//        }

        val byteBuffer: ByteBuffer =
            ByteBuffer.allocate(25 * totalAlarms.size).order(ByteOrder.LITTLE_ENDIAN)

        totalAlarms.forEach { alarm ->
            byteBuffer.put(0)
            val originNameArray = alarm.alarmName.toByteArray(StandardCharsets.UTF_8)
            byteBuffer.put(originNameArray.copyOf(ALARM_NAME_LEN))
            byteBuffer.put(alarm.hour.toByte())
            byteBuffer.put(alarm.minute.toByte())
            byteBuffer.put(AlarmRepeatOption.toValue(alarm.repeatOptions).toByte())
            byteBuffer.put(
                if (alarm.isOn) {
                    1.toByte()
                } else {
                    0.toByte()
                }
            )
        }

        payloadPackage.putData(getUrnId(URN_4, URN_1, URN_1), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 添加闹钟
     */
    fun getWriteAddAlarmCmd(alarm: WmAlarm): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer =
            ByteBuffer.allocate(ALARM_NAME_LEN + 5).order(ByteOrder.LITTLE_ENDIAN)
//        byteBuffer.put(alarm.alarmId.toByte())
        val originNameArray = alarm.alarmName.toByteArray(StandardCharsets.UTF_8)
        byteBuffer.put(originNameArray.copyOf(ALARM_NAME_LEN))
        byteBuffer.put(alarm.hour.toByte())
        byteBuffer.put(alarm.minute.toByte())
        byteBuffer.put(AlarmRepeatOption.toValue(alarm.repeatOptions).toByte())
        byteBuffer.put(
            if (alarm.isOn) {
                1.toByte()
            } else {
                0.toByte()
            }
        )
        payloadPackage.putData(getUrnId(URN_4, URN_1, URN_2), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 获取闹钟列表
     */
    fun getReadAlarmListCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(getUrnId(URN_4, URN_1, URN_1), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 更新闹钟
     */
    fun getWriteModifyAlarmCmd(alarm: WmAlarm): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer =
            ByteBuffer.allocate(ALARM_NAME_LEN + 5).order(ByteOrder.LITTLE_ENDIAN)
//        byteBuffer.put(alarm.alarmId.toByte())
        val originNameArray = alarm.alarmName.toByteArray(StandardCharsets.UTF_8)

//        Log.e(">>>>>>>>","alarm name："+String(originNameArray))

        byteBuffer.put(originNameArray.copyOf(ALARM_NAME_LEN))
        byteBuffer.put(alarm.hour.toByte())
        byteBuffer.put(alarm.minute.toByte())
        byteBuffer.put(AlarmRepeatOption.toValue(alarm.repeatOptions).toByte())
        byteBuffer.put(
            if (alarm.isOn) {
                1.toByte()
            } else {
                0.toByte()
            }
        )
        payloadPackage.putData(getUrnId(URN_4, URN_1, URN_3), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 删除闹钟
     */
    fun getExecuteDeleteAlarmCmd(alarmIds: List<Byte>): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(getUrnId(URN_4, URN_1, URN_4), alarmIds.toByteArray())
        return payloadPackage
    }

    /**
     * 获取通讯录总包个数
     */
    fun getReadContactCountCmd(contactCount: Byte): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(contactCount)
        payloadPackage.putData(getUrnId(URN_4, URN_3, URN_1), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 获取通讯录
     */
    fun getReadContactListCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(getUrnId(URN_4, URN_3, URN_2), ByteArray(0))
        return payloadPackage
    }

    /**
     * 更新通讯录
     */
    fun getWriteContactListCmd(contacts: List<WmContact>): PayloadPackage {
        val payloadPackage = PayloadPackage()

        val count = MAX_BUSINESS_BUFFER_SIZE / (NAME_BYTES_LIMIT + NUMBER_BYTES_LIMIT)

        val contactGroup = contacts.chunked(count)

        for (i in 0 until contactGroup.size) {

            val byteBuffer: ByteBuffer =
                ByteBuffer.allocate(contactGroup[i].size * (NAME_BYTES_LIMIT + NUMBER_BYTES_LIMIT))

            contactGroup[i].forEach {
                byteBuffer.put(
                    it.name.toByteArray().copyOf(NAME_BYTES_LIMIT)
                )

                byteBuffer.put(
                    it.number.toByteArray().copyOf(NUMBER_BYTES_LIMIT)
                )
            }

            payloadPackage.putData(getUrnId(URN_4, URN_3, URN_2), byteBuffer.array())
        }

        return payloadPackage
    }

    /**
     * 更新紧急联系人
     */
    fun getWriteEmergencyNumberCmd(number: WmEmergencyCall): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer =
            ByteBuffer.allocate(1 + (NAME_BYTES_LIMIT + NUMBER_BYTES_LIMIT) * number.emergencyContacts.size)
        byteBuffer.put(
            if (number.isEnabled) {
                1
            } else {
                0
            }.toByte()
        )

        number.emergencyContacts.forEach {
            byteBuffer.put(it.name.toByteArray().copyOf(NAME_BYTES_LIMIT))
            byteBuffer.put(it.number.toByteArray().copyOf(NUMBER_BYTES_LIMIT))
        }

        payloadPackage.putData(getUrnId(URN_4, URN_3, URN_3), byteBuffer.array())

        return payloadPackage
    }

    /**
     * 获取紧急联系人
     */
    fun getReadEmergencyNumberCmd(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(getUrnId(URN_4, URN_3, URN_3), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 查找手表
     */
    fun getExecuteStartFindDevice(wmFind: WmFind): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(wmFind.count.toByte())
        byteBuffer.putShort(wmFind.timeSeconds.toShort())
        payloadPackage.putData(getUrnId(URN_5, URN_2, URN_1), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 停止查找手机
     */
    fun getExecuteStopFindMobile(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(getUrnId(URN_5, URN_1, URN_2), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 停止查找手表
     */
    fun getExecuteStopFindDevice(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(getUrnId(URN_5, URN_2, URN_2), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 回复查找手机
     */
    fun getResponseStartFindPhone(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(getUrnId(URN_5, URN_1, URN_1), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 回复停止查找手机
     */
    fun getResponseStopFindPhone(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(0)
        payloadPackage.putData(getUrnId(URN_5, URN_1, URN_2), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 音乐控制 监听设备端
     */
    fun getExecuteMusicControlCmd(wmMusicControl: WmMusicControlType): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(1)
        byteBuffer.put(wmMusicControl.type)
        payloadPackage.putData(getUrnId(URN_4, URN_B), byteBuffer.array())
        return payloadPackage
    }

    /**
     * 获取当天天气命令
     * 摄氏度 = (华氏度 - 32°F) ÷ 1.8；华氏度 = 32°F+ 摄氏度 × 1.8
     */
    fun getWriteTodayWeatherCmd(
        totalLen: Int,
        temperatureUnit: WmUnitInfo.TemperatureUnit,
        wmWeather: WmWeather
    ): PayloadPackage {
        val payloadPackage = PayloadPackage()

        val byteBuffer: ByteBuffer = ByteBuffer.allocate(totalLen).order(ByteOrder.LITTLE_ENDIAN)

        //时间
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = wmWeather.pubDate
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要加1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        byteBuffer.putShort(year.toShort())
        byteBuffer.put(month.toByte())
        byteBuffer.put(day.toByte())
        byteBuffer.put(hour.toByte())
        byteBuffer.put(minute.toByte())
        byteBuffer.put(second.toByte())

        //位置
        byteBuffer.put((wmWeather.location.country.toByteArray().size + 1).toByte())
        byteBuffer.put((wmWeather.location.city.toByteArray().size + 1).toByte())

        byteBuffer.put(wmWeather.location.country.toByteArray())
        byteBuffer.put(0)//字符串结束符
        byteBuffer.put(wmWeather.location.city.toByteArray())
        byteBuffer.put(0)//字符串结束符


        //当天
        wmWeather.todayWeather.forEach {

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要加1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)

            byteBuffer.putShort(year.toShort())
            byteBuffer.put(month.toByte())
            byteBuffer.put(day.toByte())
            byteBuffer.put(hour.toByte())
            byteBuffer.put(minute.toByte())
            byteBuffer.put(second.toByte())

            val currTemp = when (temperatureUnit) {
                WmUnitInfo.TemperatureUnit.CELSIUS -> {//摄氏度
                    it.curTemp
                }

                WmUnitInfo.TemperatureUnit.FAHRENHEIT -> {//华氏度
//                 摄氏度=(华氏度 - 32°F) ÷ 1.8；
                    (it.curTemp - 32) / 1.8
                }
            }

            byteBuffer.put(currTemp.toByte())
            byteBuffer.putShort(it.humidity.toShort())
            byteBuffer.put(it.uvIndex.toByte())
            byteBuffer.put(it.weatherCode.toByte())
            byteBuffer.put((it.weatherDesc.toByteArray().size + 1).toByte())
            byteBuffer.put(it.weatherDesc.toByteArray())
            byteBuffer.put(0)//字符串结束符
        }

        payloadPackage.putData(
            getUrnId(URN_APP_SETTING, URN_APP_WEATHER, URN_APP_WEATHER_PUSH_TODAY),
            byteBuffer.array()
        )

        return payloadPackage
    }

    /**
     * 获取当天天气命令
     * 摄氏度 = (华氏度 - 32°F) ÷ 1.8；华氏度 = 32°F+ 摄氏度 × 1.8
     */
    fun getWriteSevenDaysWeatherCmd(
        totalLen: Int,
        temperatureUnit: WmUnitInfo.TemperatureUnit,
        wmWeather: WmWeather
    ): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(totalLen).order(ByteOrder.LITTLE_ENDIAN)

        //时间
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = wmWeather.pubDate
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要加1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        byteBuffer.putShort(year.toShort())
        byteBuffer.put(month.toByte())
        byteBuffer.put(day.toByte())
        byteBuffer.put(hour.toByte())
        byteBuffer.put(minute.toByte())
        byteBuffer.put(second.toByte())

        //位置
        byteBuffer.put((wmWeather.location.country.toByteArray().size + 1).toByte())
        byteBuffer.put((wmWeather.location.city.toByteArray().size + 1).toByte())
        byteBuffer.put(wmWeather.location.country.toByteArray())
        byteBuffer.put(0)//字符串结束符
        byteBuffer.put(wmWeather.location.city.toByteArray())
        byteBuffer.put(0)//字符串结束符

        wmWeather.weatherForecast.forEach {

            //时间
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要加1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val week = calendar.get(Calendar.WEEK_OF_MONTH)

            byteBuffer.putShort(year.toShort())
            byteBuffer.put(month.toByte())
            byteBuffer.put(day.toByte())
            byteBuffer.put(hour.toByte())
            byteBuffer.put(minute.toByte())
            byteBuffer.put(second.toByte())
//            byteBuffer.put(week.toByte())
            byteBuffer.put(it.week.ordinal.toByte())

            when (temperatureUnit) {
                WmUnitInfo.TemperatureUnit.CELSIUS -> {//摄氏度
                    byteBuffer.put(it.lowTemp.toByte())
                    byteBuffer.put(it.highTemp.toByte())
                    byteBuffer.put(it.curTemp.toByte())
                }

                WmUnitInfo.TemperatureUnit.FAHRENHEIT -> {//华氏度
//                 摄氏度=(华氏度 - 32°F) ÷ 1.8；
                    byteBuffer.put(((it.lowTemp - 32) / 1.8).toInt().toByte())
                    byteBuffer.put(((it.highTemp - 32) / 1.8).toInt().toByte())
                    byteBuffer.put(((it.highTemp - 32) / 1.8).toInt().toByte())
                }
            }

            byteBuffer.putShort(it.humidity.toShort())
            byteBuffer.put(it.uvIndex.toByte())
            byteBuffer.put(it.dayCode.toByte())
            byteBuffer.put(it.nightCode.toByte())

            byteBuffer.put((it.dayDesc.toByteArray().size + 1).toByte())
            byteBuffer.put((it.nightDesc.toByteArray().size + 1).toByte())

            byteBuffer.put(it.dayDesc.toByteArray())
            byteBuffer.put(0)//字符串结束符
            byteBuffer.put(it.nightDesc.toByteArray())
            byteBuffer.put(0)//字符串结束符

        }

        payloadPackage.putData(
            getUrnId(URN_APP_SETTING, URN_APP_WEATHER, URN_APP_WEATHER_PUSH_SEVEN_DAYS),
            byteBuffer.array()
        )

        return payloadPackage
    }

    /**
     * 获取运动数据
     */
    fun getReadSportSyncData(
        startTime: Long,
        endTime: Long,
        childUrn: Byte,
        grandSon: Byte = URN_0
    ): PayloadPackage {
        val payloadPackage = PayloadPackage()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        val sYear = calendar.get(Calendar.YEAR)
        val sMonth = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要加1
        val sDay = calendar.get(Calendar.DAY_OF_MONTH)
        val sHour = calendar.get(Calendar.HOUR_OF_DAY)
        val sMinute = calendar.get(Calendar.MINUTE)
        val sSecond = calendar.get(Calendar.SECOND)

        calendar.timeInMillis = endTime
        val eYear = calendar.get(Calendar.YEAR)
        val eMonth = calendar.get(Calendar.MONTH) + 1
        val eDay = calendar.get(Calendar.DAY_OF_MONTH)
        val eHour = calendar.get(Calendar.HOUR_OF_DAY)
        val eMinute = calendar.get(Calendar.MINUTE)
        val eSecond = calendar.get(Calendar.SECOND)

        val byteBuffer = ByteBuffer.allocate(14)
        if (startTime != 0L) {
            byteBuffer.putShort(sYear.toShort())
            byteBuffer.put(sMonth.toByte())
            byteBuffer.put(sDay.toByte())
            byteBuffer.put(sHour.toByte())
            byteBuffer.put(sMinute.toByte())
            byteBuffer.put(sSecond.toByte())
        }

        if (endTime != 0L) {
            byteBuffer.putShort(eYear.toShort())
            byteBuffer.put(eMonth.toByte())
            byteBuffer.put(eDay.toByte())
            byteBuffer.put(eHour.toByte())
            byteBuffer.put(eMinute.toByte())
            byteBuffer.put(eSecond.toByte())
        }


        payloadPackage.putData(
            getUrnId(URN_SPORT_DATA, childUrn = childUrn, grandSon = grandSon),
            byteBuffer.array()
        )

        return payloadPackage
    }

    /**
     * 获取MTU
     *
     * @return
     */
    val getMTUCmd: ByteArray
        get() = constructCmd(
            HEAD_NODE_TYPE,
            CMD_ID_8003,
            DIVIDE_N_2,
            0,
            0,
            0,
            null
        )

}