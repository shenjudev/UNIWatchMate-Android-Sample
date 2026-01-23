package com.ota.sdk.model

/**
 * OTA 命令数据包信息
 */
data class OtaCmdInfo(
    /** 数据偏移量 */
    var offSet: Int = 0,
    /** 数据负载 */
    var payload: ByteArray = ByteArray(0),
    /** CRC 校验值 */
    var crc: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as OtaCmdInfo
        
        if (offSet != other.offSet) return false
        if (!payload.contentEquals(other.payload)) return false
        if (crc != other.crc) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = offSet
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + crc
        return result
    }
    
    override fun toString(): String {
        return "OtaCmdInfo(offSet=$offSet, payload.size=${payload.size}, crc=$crc)"
    }
}
