package com.ota.sdk.utils

import com.ota.sdk.constants.OtaProtocolConstants
import java.nio.ByteBuffer

/**
 * 协议消息解析器
 * 用于解析蓝牙接收到的原始数据
 */
internal object ProtocolParser {

    private const val TLOCP_LEN = 16  // 协议头长度

    /**
     * 解析后的消息
     */
    data class ParsedMessage(
        val head: Byte,
        val cmdId: Int,
        val payload: ByteArray
    ) {
        /**
         * 判断是否是 OTA 相关消息
         */
        fun isOtaMessage(): Boolean {
            return OtaProtocolConstants.isOtaCommand(head, cmdId)
        }

        override fun toString(): String {
            return "ParsedMessage(head=$head, cmdId=$cmdId, payload=${payload.contentToString()})"
        }


    }

    /**
     * 从原始数据解析消息
     *
     * @param rawData 接收到的原始数据
     * @return 解析后的消息，如果解析失败返回 null
     */
    fun parse(rawData: ByteArray): ParsedMessage? {
        return try {
            // 检查数据长度
            if (rawData.size < TLOCP_LEN) {
                return null
            }

            val byteBuffer = ByteBuffer.wrap(rawData)

            // 1. 读取 head (1 byte)
            val head = byteBuffer.get()

            // 2. 跳过 cmdOrder (1 byte)
            byteBuffer.get()

            // 3. 读取 cmdId (2 bytes, little-endian)

            val cmdId = ByteArray(2)
            System.arraycopy(rawData, 2, cmdId, 0, cmdId.size)

            val temp = cmdId[0]
            cmdId[0] = cmdId[1]
            cmdId[1] = temp
            cmdId[0] = 0x00
            val cmdIdFinal = byte2short(cmdId).toInt()

            // 4. 提取 payload (跳过协议头的剩余部分)
            val payloadLength = rawData.size - TLOCP_LEN
            val payload = if (payloadLength > 0) {
                ByteArray(payloadLength).also {
                    System.arraycopy(rawData, TLOCP_LEN, it, 0, payloadLength)
                }
            } else {
                byteArrayOf()
            }

            ParsedMessage(head, cmdIdFinal, payload)

        } catch (e: Exception) {
            LogUtil.e("ProtocolParser", "解析协议失败: ${e.message}", e)
            null
        }
    }

    fun byte2short(b: ByteArray): Short {
        var l: Short = 0
        for (i in 0 until 2) {
            l = (l.toInt() shl 8).toShort() // 左移8位，先转Int避免溢出再转回Short
            l = (l.toInt() or (b[i].toInt() and 0xff)).toShort() // 按位或运算，确保byte无符号处理
        }
        return l
    }
}