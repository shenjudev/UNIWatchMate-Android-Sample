package com.ota.sdk.command

import android.util.Log
import com.ota.sdk.constants.OtaProtocolConstants
import com.ota.sdk.constants.OtaProtocolConstants.CMD_ID_8001
import com.ota.sdk.constants.OtaProtocolConstants.HEAD_NODE_TYPE
import com.ota.sdk.constants.OtaProtocolConstants.URN_0
import com.ota.sdk.constants.OtaProtocolConstants.URN_1
import com.ota.sdk.constants.OtaProtocolConstants.URN_4
import com.ota.sdk.constants.OtaProtocolConstants.URN_6
import com.ota.sdk.model.DataFormat
import com.ota.sdk.model.OtaCmdInfo
import com.ota.sdk.model.OtaFileType
import com.ota.sdk.model.PayloadPackage
import com.ota.sdk.model.RequestType
import com.ota.sdk.utils.OtaCrcUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * OTA 命令构建器
 * 负责构建完整的协议包（包含协议头）
 */
object OtaCommandBuilder {
    private const val TAG = "OtaCommandBuilder"
    
    // 协议相关常量
    private const val TRANSFER_KEY: Short = 0x7FFF
    private const val MAX_ORDER_ID = 0xFD
    private const val TLOCP_LEN = 16  // 协议头长度
    private const val HEX_FFFF = 0xFFFF
    
    // 分包类型常量
    private const val DIVIDE_N_2: Byte = 0  // 不分片
    private const val DIVIDE_Y_F_2: Byte = 1  // 分片首包
    private const val DIVIDE_Y_M_2: Byte = 2  // 分片中包
    private const val DIVIDE_Y_E_2: Byte = 3  // 分片尾包
    
    // 命令索引（用于协议包中的 order 字段）
    @Volatile
    private var commandIndex = 0
    
    /**
     * 构建 OTA 传输请求命令（0x800A）
     * 用于 OTA 和 OTA_UPEX 文件类型
     * 
     * @param fileType 文件类型
     * @param fileLen 文件总长度
     * @param fileCount 文件数量
     * @param appendixSize 附加数据大小
     * @param crc 文件 CRC 校验值
     * @return 完整的协议包字节数组
     */
    fun buildTransferFile0ACmd(
        fileType: OtaFileType,
        fileLen: Int,
        fileCount: Int,
        appendixSize: Int,
        crc: Int
    ): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1 + 4 + 1 + 4 + 4)  // type(1) + fileLen(4) + fileCount(1) + appendixSize(4) + crc(4)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        
        // 文件类型
        byteBuffer.put(fileType.type.toByte())
        
        // 文件长度（OTA 文件使用 -1）
        if (fileType == OtaFileType.OTA || fileType == OtaFileType.OTA_UPEX) {
            byteBuffer.putInt(-1)
        } else {
            byteBuffer.putInt(fileLen)
        }
        
        // 文件数量
        byteBuffer.put(fileCount.toByte())
        
        // 附加数据大小
        byteBuffer.putInt(appendixSize)
        
        // CRC 校验值
        byteBuffer.putInt(crc)
        
        byteBuffer.flip()
        val payload = byteBuffer.array()
        
        // 构建完整协议包
        return buildFinalCmd(
            head = OtaProtocolConstants.HEAD_FILE_SPP_A_2_D,
            cmdId = OtaProtocolConstants.CMD_ID_800A,
            divideType = DIVIDE_N_2,
            dividePayloadLen = 0,
            offset = 0,
            crc = OtaCrcUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload = payload,
            newType = true
        )
    }


    /**
     * 构建文件信息命令（0x8002）
     *
     * @param len 文件长度
     * @param name 文件名称
     * @return 完整的协议包字节数组
     */
    fun buildTransferFile02Cmd(len: Int, name: String): ByteArray {
        val nameByte = name.toByteArray(Charset.defaultCharset())
        val byteBuffer = ByteBuffer.allocate(4 + nameByte.size)  // len(4) + name
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        byteBuffer.putInt(len)
        byteBuffer.put(nameByte)

        byteBuffer.flip()
        val payload = byteBuffer.array()

        return buildFinalCmd(
                head = OtaProtocolConstants.HEAD_FILE_SPP_A_2_D,
                cmdId = OtaProtocolConstants.CMD_ID_8002,
                divideType = DIVIDE_N_2,
                dividePayloadLen = 0,
                offset = 0,
                crc = OtaCrcUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload = payload,
                newType = true
        )
    }

    fun buildHighSpeed16Cmd(): ByteArray {

        val payload = ByteArray(0)

        return buildFinalCmd(
                head = OtaProtocolConstants.HEAD_COMMON_SPP_A_2_D,
                cmdId = OtaProtocolConstants.CMD_ID_8016,
                divideType = DIVIDE_N_2,
                dividePayloadLen = 0,
                offset = 0,
                crc = OtaCrcUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload = payload,
                newType = true
        )
    }

    /**
     * 构建节点协议 进入高速模式命令（urn：1016）
     */
//    private fun buildBleHighSpeedCmd(
//            payloadPackage: PayloadPackage,
//            timeSafe: Boolean = true,
//            divideType: Byte = DIVIDE_N_2,
//            requestType: RequestType = RequestType.REQ_TYPE_EXECUTE,
//            dataFormat: DataFormat = DataFormat.FMT_NODATA
//    ) : ByteArray{
//        val payloadPackage = PayloadPackage()
//        payloadPackage.putData(getUrnId(URN_1, URN_0, URN_1, URN_6), ByteArray(0))
//        val dataBytes =  payloadPackage.toByteArray(requestType.type, dataFormat).first()
//            val cmdArray = buildFinalCmd(
//                    HEAD_NODE_TYPE,
//                    CMD_ID_8001,
//                    divideType,
//                    0,
//                    0,
//                    OtaCrcUtils.getCrc(HEX_FFFF, dataBytes, dataBytes.size),
//                    dataBytes,
//                    true
//            )
//        return cmdArray;
//    }
    fun getUrnId(
            parentUrn: Byte,
            childUrn: Byte = URN_0,
            grandSon: Byte = URN_0,
            grandGrandSon: Byte = URN_0
    ): ByteArray {
        return byteArrayOf(
                parentUrn,
                childUrn,
                grandSon,
                grandGrandSon,
        )
    }
    /**
     * 构建高速命令（0x8002）
     *
     * @param len 文件长度
     * @param name 文件名称
     * @return 完整的协议包字节数组
     */
    fun buildEnterBleHighSpeedCmd(len: Int, name: String): ByteArray {
        val nameByte = name.toByteArray(Charset.defaultCharset())
        val byteBuffer = ByteBuffer.allocate(4 + nameByte.size)  // len(4) + name
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        byteBuffer.putInt(len)
        byteBuffer.put(nameByte)

        byteBuffer.flip()
        val payload = byteBuffer.array()

        return buildFinalCmd(
                head = OtaProtocolConstants.HEAD_FILE_SPP_A_2_D,
                cmdId = OtaProtocolConstants.CMD_ID_8002,
                divideType = DIVIDE_N_2,
                dividePayloadLen = 0,
                offset = 0,
                crc = OtaCrcUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload = payload,
                newType = true
        )
    }
    
    /**
     * 构建数据包传输命令（0x8003）
     * 
     * @param process 数据包序号
     * @param otaCmdInfo OTA 数据包信息
     * @param divideType 分包类型
     * @return 完整的协议包字节数组
     */
    fun buildTransfer03Cmd(
        process: Int,
        otaCmdInfo: OtaCmdInfo,
        divideType: Byte
    ): ByteArray {
        val byteBuffer = ByteBuffer.allocate(otaCmdInfo.payload.size + 4)  // process(4) + payload
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        
        byteBuffer.putInt(process)
        byteBuffer.put(otaCmdInfo.payload)
        
        byteBuffer.flip()
        val newPayload = byteBuffer.array()
        
        // 更新 otaCmdInfo 的 payload 和 crc
        otaCmdInfo.payload = newPayload
        Log.d(TAG, "发送数据包: buildTransfer03Cmd 序号=$process, 分包类型=$divideType, newPayload=${newPayload.size}")
        otaCmdInfo.crc = OtaCrcUtils.getCrc(HEX_FFFF, newPayload, newPayload.size)
        
        return buildFinalCmd(
            head = OtaProtocolConstants.HEAD_FILE_SPP_A_2_D,
            cmdId = OtaProtocolConstants.CMD_ID_8003,
            divideType = divideType,
            dividePayloadLen = 0,
            offset = otaCmdInfo.offSet,
            crc = otaCmdInfo.crc,
            payload = otaCmdInfo.payload,
            newType = false
        )
    }
    
    /**
     * 构建传输确认命令（0x8004）
     * 
     * @return 完整的协议包字节数组
     */
    fun buildTransfer04Cmd(): ByteArray {
        return buildFinalCmd(
            head = OtaProtocolConstants.HEAD_FILE_SPP_A_2_D,
            cmdId = OtaProtocolConstants.CMD_ID_8004,
            divideType = DIVIDE_N_2,
            dividePayloadLen = 0,
            offset = 0,
            crc = 0,
            payload = null,
            newType = true
        )
    }
    
    /**
     * 构建取消传输命令（0x8005）
     * 
     * @return 完整的协议包字节数组
     */
    fun buildTransferCancelCmd(): ByteArray {
        return buildFinalCmd(
            head = OtaProtocolConstants.HEAD_FILE_SPP_A_2_D,
            cmdId = OtaProtocolConstants.CMD_ID_8005,
            divideType = DIVIDE_N_2,
            dividePayloadLen = 0,
            offset = 0,
            crc = 0,
            payload = null,
            newType = true
        )
    }
    
    /**
     * 构建 OTA 超时处理命令（0x8008）
     * 
     * @return 完整的协议包字节数组
     */
    fun buildTransfer0AOtaOutTime08Cmd(): ByteArray {
        return buildFinalCmd(
            head = OtaProtocolConstants.HEAD_FILE_SPP_A_2_D,
            cmdId = OtaProtocolConstants.CMD_ID_8008,
            divideType = DIVIDE_N_2,
            dividePayloadLen = 0,
            offset = 0,
            crc = 0,
            payload = null,
            newType = true
        )
    }
    
    /**
     * 获取分包类型
     * 
     * @param otaProcess 当前包序号
     * @param packageCount 总包数
     * @return 分包类型
     */
    fun getDivideType(otaProcess: Int, packageCount: Int): Byte {
        return when {
            otaProcess == 0 && packageCount > 1 -> DIVIDE_Y_F_2  // 首包
            otaProcess == packageCount - 1 -> DIVIDE_Y_E_2  // 尾包
            else -> DIVIDE_Y_M_2  // 中间包
        }
    }
    
    /**
     * 构建完整协议包（TLOCP 格式）
     * 
     * 协议包结构：
     * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
     * |  Head  | Order  |  CmdId |Divide+ |Payload | Offset |  CRC   |  Payload Data   |
     * | (1byte)|(1byte) |(2bytes)| Length | Length |(4bytes)|(4bytes)|   (可变长度)    |
     * |        |        |        |(2bytes)|(2bytes)|        |        |                 |
     * +--------+--------+--------+--------+--------+--------+--------+--------+--------+
     * 
     * @param head 消息头
     * @param cmdId 命令ID
     * @param divideType 分包类型
     * @param dividePayloadLen 分包负载长度
     * @param offset 偏移量
     * @param crc CRC 校验值
     * @param payload 负载数据
     * @param newType 是否使用新的命令索引
     * @return 完整的协议包字节数组
     */
    private fun buildFinalCmd(
        head: Byte,
        cmdId: Short,
        divideType: Byte,
        dividePayloadLen: Short,
        offset: Int,
        crc: Int,
        payload: ByteArray?,
        newType: Boolean
    ): ByteArray {
        val payLoadLength = payload?.size ?: 0
        val byteBuffer = ByteBuffer.allocate(TLOCP_LEN + payLoadLength)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)  // 小端序，与原 SDK 一致
        
        // Head (1 byte)
        byteBuffer.put(head)
        
        // Order (1 byte) - 命令索引
        byteBuffer.put((commandIndex % MAX_ORDER_ID).toByte())
        
        // CmdId (2 bytes) - 使用 putShort 以小端序写入，与原 SDK 一致
        byteBuffer.putShort((cmdId.toInt() and TRANSFER_KEY.toInt()).toShort())
        
        // DivideType + Length (2 bytes)
        byteBuffer.put(writeShortToBytes(divideType, dividePayloadLen))
        
        // Payload Length (2 bytes)
        byteBuffer.putShort(payLoadLength.toShort())
        
        // Offset (4 bytes)
        byteBuffer.putInt(offset)
        
        // CRC (4 bytes)
        byteBuffer.putInt(crc)
        
        // Payload (可变长度)
        if (payload != null) {
            byteBuffer.put(payload)
        }
        
        byteBuffer.flip()
        
        // 如果使用新类型，增加命令索引
        if (newType) {
            commandIndex++
        }
        
        return byteBuffer.array()
    }
    
    /**
     * 写入分包类型和长度到字节数组
     * 
     * @param divideType 分包类型
     * @param totalLen 总长度
     * @return 2字节数组
     */
    private fun writeShortToBytes(divideType: Byte, totalLen: Short): ByteArray {
        val result = ByteArray(2)
        
        // 用第一个字节的高5位存储value的高5位 248 = 0b11111000
        result[0] = (totalLen.toInt() shr 8 shl 3 and 248).toByte()
        
        // 写入低三位分包类型和数据类型 7 = 0b00000111
        // 注意：Byte 类型需要转换为 Int 才能进行位运算
        result[0] = ((divideType.toInt() and 7) or result[0].toInt()).toByte()
        
        // 用第二个字节存储value的低8位
        result[1] = totalLen.toByte()
        
        return result
    }
}
