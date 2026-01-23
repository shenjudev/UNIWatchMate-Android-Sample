package com.ota.sdk.constants

/**
 * OTA 协议常量（提供给客户，用于判断消息类型）
 * 
 * 客户在收到蓝牙消息后，可以使用这些常量判断是否是 OTA 相关消息
 */
object OtaProtocolConstants {
    /** OTA 文件传输协议头 */
    const val HEAD_FILE_SPP_A_2_D: Byte = 0x0E
    
    /** OTA 相关命令ID */
    const val CMD_ID_800A: Short = 0x0A  // OTA 传输请求/回复
    const val CMD_ID_8001: Short = 0x01  // 普通文件传输请求/回复
    const val CMD_ID_8002: Short = 0x02  // 文件信息/分片大小
    const val CMD_ID_8003: Short = 0x03  // 数据包传输
    const val CMD_ID_8004: Short = 0x04  // 传输确认
    const val CMD_ID_8005: Short = 0x05  // 取消传输
    const val CMD_ID_8006: Short = 0x06  // 设备主动取消
    const val CMD_ID_8008: Short = 0x08  // OTA 超时处理
    
    /**
     * 判断是否是 OTA 相关命令
     * 
     * @param head 消息头
     * @param cmdId 命令ID
     * @return true 如果是 OTA 相关命令
     */
    fun isOtaCommand(head: Byte, cmdId: Int): Boolean {
        return head == HEAD_FILE_SPP_A_2_D && 
               cmdId.toShort() in listOf(CMD_ID_800A, CMD_ID_8001, CMD_ID_8002,
                              CMD_ID_8003, CMD_ID_8004, CMD_ID_8005, 
                              CMD_ID_8006, CMD_ID_8008)
    }
}
