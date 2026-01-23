package com.ota.sdk.model

/**
 * OTA 传输错误码枚举
 */
enum class OtaError(val code: Int) {
    /** 其他错误 */
    ERROR_OTHER(0),
    
    /** 设备忙碌 */
    ERROR_BUSY(1),
    
    /** 设备内存不足 */
    ERROR_LOW_MEMORY(2),
    
    /** 超过最大数量 */
    ERROR_OVER_MAX_COUNT(3),
    
    /** 设备电量不足 */
    ERROR_LOW_POWER(4),
    
    /** 传输超时 */
    ERROR_TIME_OUT(5),
    
    /** 文件异常 */
    ERROR_FILE_EXCEPTION(101),
    
    /** 设备断开连接 */
    ERROR_DISCONNECT(102);
    
    companion object {
        /**
         * 根据错误码获取枚举
         */
        fun fromCode(code: Int): OtaError {
            return values().find { it.code == code } ?: ERROR_OTHER
        }
    }
}

/**
 * OTA 传输异常
 * 
 * @param error 错误码
 * @param message 错误消息
 */
class OtaException(
    val error: OtaError,
    message: String
) : RuntimeException(message)
