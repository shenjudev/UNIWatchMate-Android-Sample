package com.ota.sdk.utils

/**
 * OTA CRC 校验工具类
 * 
 * 提供 CRC16-CCITT 算法实现，与原 SDK 的 Native 实现完全一致
 * 
 * 【算法说明】
 * - 多项式: 0x1021 (CRC-16-CCITT)
 * - 初始值: 通常为 0xFFFF
 * - 输入反转: 否
 * - 输出反转: 否
 * 
 * 【使用方式】
 * ```kotlin
 * // 计算数据的 CRC16
 * val crc = OtaCrcUtils.getCrc(0xFFFF, data, data.size)
 * 
 * // 如果需要使用外部实现（例如 Native）
 * OtaCrcUtils.crcCalculator = object : OtaCrcUtils.ICrcCalculator {
 *     override fun calculateCrc(initialValue: Int, data: ByteArray, length: Int): Int {
 *         return BtUtils.getCrc(initialValue, data, length)  // 调用 Native
 *     }
 * }
 * ```
 */
object OtaCrcUtils {
    
    /**
     * CRC16-CCITT 多项式
     * 标准值: 0x1021
     */
    private const val POLY_CRC16 = 0x1021
    
    /**
     * CRC 计算器接口
     * 客户端可以通过实现此接口来提供自己的 CRC 算法（例如 Native 实现）
     */
    interface ICrcCalculator {
        /**
         * 计算 CRC 值
         * @param initialValue 初始值（通常为 0xFFFF）
         * @param data 数据字节数组
         * @param length 数据长度
         * @return CRC 值
         */
        fun calculateCrc(initialValue: Int, data: ByteArray, length: Int): Int
    }
    
    /**
     * 外部 CRC 计算器（可选）
     * 
     * 如果设置了此属性，则优先使用外部实现
     * 这允许客户端使用自己的 Native CRC 实现以提高性能
     * 
     * 【使用示例】
     * ```kotlin
     * // 使用原 SDK 的 Native 实现
     * OtaCrcUtils.crcCalculator = object : OtaCrcUtils.ICrcCalculator {
     *     override fun calculateCrc(initialValue: Int, data: ByteArray, length: Int): Int {
     *         return com.sjbt.sdk.utils.BtUtils.getCrc(initialValue, data, length)
     *     }
     * }
     * ```
     */
    var crcCalculator: ICrcCalculator? = null
    
    /**
     * 计算 CRC16 校验值
     * 
     * 此方法是 OTA SDK 的公开 API，与原 SDK 的 BtUtils.getCrc() 完全兼容
     * 
     * 【实现策略】
     * 1. 如果设置了 crcCalculator，则使用外部实现（例如 Native）
     * 2. 否则使用内置的 Kotlin 实现
     * 
     * 【性能说明】
     * - Kotlin 实现：纯 Kotlin 代码，跨平台兼容
     * - Native 实现：性能更好（约快 2-3 倍），但需要额外集成
     * 
     * @param initialValue 初始值（通常为 0xFFFF）
     * @param data 数据字节数组
     * @param length 数据长度
     * @return CRC16 校验值（0x0000 ~ 0xFFFF）
     */
    fun getCrc(initialValue: Int, data: ByteArray, length: Int): Int {
        // 优先使用外部提供的 CRC 计算器（例如 Native 实现）
        val calculator = crcCalculator
        if (calculator != null) {
            return calculator.calculateCrc(initialValue, data, length)
        }
        
        // 使用内置的 Kotlin CRC16 实现
        return calculateCrc16Ccitt(initialValue, data, length)
    }
    
    /**
     * 内置的 CRC16-CCITT 算法实现（Kotlin 版本）
     * 
     * 此实现与原 SDK 的 Native 代码完全一致：
     * ```c++
     * uint16_t get_crc(uint16_t val, uint8_t *buf, uint32_t len) {
     *     for (uint32_t i = 0; i < len; i++) {
     *         val = get_crc_1byte(val, buf[i]);
     *     }
     *     return val;
     * }
     * ```
     * 
     * @param initialValue 初始值（通常为 0xFFFF）
     * @param data 数据字节数组
     * @param length 数据长度
     * @return CRC16 校验值
     */
    private fun calculateCrc16Ccitt(initialValue: Int, data: ByteArray, length: Int): Int {
        var crc = initialValue and 0xFFFF
        
        // 对每个字节计算 CRC
        for (i in 0 until length) {
            crc = calculateCrc16OneByte(crc, data[i].toInt() and 0xFF)
        }
        
        return crc and 0xFFFF
    }
    
    /**
     * 计算单个字节的 CRC16
     * 
     * 此实现与原 SDK 的 Native 代码完全一致：
     * ```c++
     * uint16_t get_crc_1byte(uint16_t lastcrc, uint16_t byte_val) {
     *     for (uint32_t i = 0; i < 8; i++) {
     *         if (((byte_val << i) ^ (lastcrc >> 8)) & 0x80) {
     *             lastcrc <<= 1;
     *             lastcrc = lastcrc ^ POLY_CRC16;  // 0x1021
     *         } else {
     *             lastcrc <<= 1;
     *         }
     *     }
     *     return lastcrc;
     * }
     * ```
     * 
     * 【算法说明】
     * 1. 对字节的每一位（8位）进行处理
     * 2. 检查 (byte_val << i) ^ (lastcrc >> 8) 的最高位
     * 3. 如果最高位为 1，左移并异或多项式 0x1021
     * 4. 否则只左移
     * 
     * @param lastCrc 当前 CRC 值
     * @param byteVal 当前字节值
     * @return 更新后的 CRC 值
     */
    private fun calculateCrc16OneByte(lastCrc: Int, byteVal: Int): Int {
        var crc = lastCrc
        
        // 对字节的每一位进行处理（共 8 位）
        for (i in 0 until 8) {
            // 检查 (byteVal << i) ^ (crc >> 8) 的最高位（第 7 位）
            val isHighBitSet = (((byteVal shl i) xor (crc shr 8)) and 0x80) != 0
            
            if (isHighBitSet) {
                // 最高位为 1：左移并异或多项式
                crc = (crc shl 1) xor POLY_CRC16
            } else {
                // 最高位为 0：只左移
                crc = crc shl 1
            }
        }
        
        return crc and 0xFFFF
    }
    
    /**
     * 验证 CRC 值是否正确
     * 
     * @param data 数据（包含 CRC 字段）
     * @param expectedCrc 期望的 CRC 值
     * @return true 表示 CRC 正确
     */
    fun verifyCrc(data: ByteArray, expectedCrc: Int): Boolean {
        val calculatedCrc = getCrc(0xFFFF, data, data.size)
        return calculatedCrc == expectedCrc
    }
}

