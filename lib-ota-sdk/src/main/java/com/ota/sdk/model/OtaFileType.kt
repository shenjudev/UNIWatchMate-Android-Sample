package com.ota.sdk.model

/**
 * OTA 文件类型枚举
 */
enum class OtaFileType(val type: Int) {
    /** 普通 OTA 文件 */
    OTA(2),
    
    /** OTA .upex 压缩文件（需要解压） */
    OTA_UPEX(5);
    
    companion object {
        /**
         * 根据类型值获取枚举
         */
        fun fromType(type: Int): OtaFileType? {
            return values().find { it.type == type }
        }
    }
}
