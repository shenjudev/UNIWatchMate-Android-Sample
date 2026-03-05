package com.ota.sdk.constants

/**
 * OTA 协议常量（提供给客户，用于判断消息类型）
 * 
 * 客户在收到蓝牙消息后，可以使用这些常量判断是否是 OTA 相关消息
 */
object OtaProtocolConstants {
    /** OTA 文件传输协议头 */
    const val HEAD_FILE_SPP_A_2_D: Byte = 0x0E
    const val HEAD_COMMON_SPP_A_2_D: Byte = 0x0B

    /** OTA 相关命令ID */
    const val CMD_ID_800A: Short = 0x0A  // OTA 传输请求/回复
    const val CMD_ID_8001: Short = 0x01  // 普通文件传输请求/回复
    const val CMD_ID_8002: Short = 0x02  // 文件信息/分片大小
    const val CMD_ID_8003: Short = 0x03  // 数据包传输
    const val CMD_ID_8004: Short = 0x04  // 传输确认
    const val CMD_ID_8005: Short = 0x05  // 取消传输
    const val CMD_ID_8006: Short = 0x06  // 设备主动取消
    const val CMD_ID_8008: Short = 0x08  // OTA 超时处理
    const val CMD_ID_8016: Short = 0x16  // OTA 超时处理
    const val CMD_ID_8030: Short = 0x30  // OTA 超时处理

    const val HEAD_NODE_TYPE = 0x30.toByte() //节点数据头


    /**
     * 节点类型数据配置
     **/
    const val URN_0: Byte = '0'.code.toByte() //48
    const val URN_1: Byte = '1'.code.toByte()
    const val URN_2: Byte = '2'.code.toByte()
    const val URN_3: Byte = '3'.code.toByte()
    const val URN_4: Byte = '4'.code.toByte()
    const val URN_5: Byte = '5'.code.toByte()
    const val URN_6: Byte = '6'.code.toByte()
    const val URN_7: Byte = '7'.code.toByte()
    const val URN_8: Byte = '8'.code.toByte()
    const val URN_9: Byte = '9'.code.toByte() //57
    const val URN_A: Byte = 'A'.code.toByte() //65
    const val URN_B: Byte = 'B'.code.toByte()
    const val URN_C: Byte = 'C'.code.toByte()
    const val URN_D: Byte = 'D'.code.toByte()
    const val URN_E: Byte = 'E'.code.toByte()
    const val URN_F: Byte = 'F'.code.toByte()
    const val URN_G: Byte = 'G'.code.toByte()
    const val URN_H: Byte = 'H'.code.toByte() //72
    /**
     * 判断是否是 OTA 相关命令
     * 
     * @param head 消息头
     * @param cmdId 命令ID
     * @return true 如果是 OTA 相关命令
     */
    fun isOtaCommand(head: Byte, cmdId: Int): Boolean {
        return head == HEAD_FILE_SPP_A_2_D || (head == HEAD_COMMON_SPP_A_2_D &&
               cmdId.toShort() in listOf(CMD_ID_8016))
    }
}
