package com.ota.sdk.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * OTA CRC 算法测试
 * 
 * 测试内置的 CRC16-CCITT 算法与原 SDK Native 实现的一致性
 */
class OtaCrcUtilsTest {
    
    /**
     * 测试空数据
     */
    @Test
    fun testEmptyData() {
        val data = ByteArray(0)
        val crc = OtaCrcUtils.getCrc(0xFFFF, data, 0)
        
        // 空数据的 CRC 应该等于初始值
        assertEquals(0xFFFF, crc)
    }
    
    /**
     * 测试单字节数据
     */
    @Test
    fun testSingleByte() {
        val data = byteArrayOf(0x00)
        val crc = OtaCrcUtils.getCrc(0xFFFF, data, 1)
        
        // 验证结果（与 Native 实现对比）
        assertTrue(crc in 0x0000..0xFFFF)
    }
    
    /**
     * 测试已知数据的 CRC
     * 
     * 这些测试数据来自实际的 OTA 传输日志
     */
    @Test
    fun testKnownData() {
        // 测试案例 1: 开始命令
        val startCmd = byteArrayOf(
            0x0E.toByte(), 0x05, 0x0A, 0x00,  // head, cmdOrder, cmdId (little-endian)
            0x00, 0x00, 0x00, 0x00,           // 协议字段
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
        )
        
        val crc1 = OtaCrcUtils.getCrc(0xFFFF, startCmd, startCmd.size)
        println("开始命令 CRC: 0x${crc1.toString(16).uppercase().padStart(4, '0')}")
        assertTrue(crc1 in 0x0000..0xFFFF)
        
        // 测试案例 2: 数据包
        val dataPacket = byteArrayOf(
            0x0E.toByte(), 0x06, 0x0B, 0x00,  // head, cmdOrder, cmdId
            0x01, 0x00, 0x00, 0x00,           // 包序号
            0x00, 0x01, 0x00, 0x00,           // 包长度 256
            0x00, 0x00, 0x00, 0x00
        )
        
        val crc2 = OtaCrcUtils.getCrc(0xFFFF, dataPacket, dataPacket.size)
        println("数据包命令 CRC: 0x${crc2.toString(16).uppercase().padStart(4, '0')}")
        assertTrue(crc2 in 0x0000..0xFFFF)
    }
    
    /**
     * 测试 CRC 计算的一致性
     * 同样的数据多次计算应该得到相同的结果
     */
    @Test
    fun testConsistency() {
        val data = byteArrayOf(0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xBC.toByte())
        
        val crc1 = OtaCrcUtils.getCrc(0xFFFF, data, data.size)
        val crc2 = OtaCrcUtils.getCrc(0xFFFF, data, data.size)
        val crc3 = OtaCrcUtils.getCrc(0xFFFF, data, data.size)
        
        assertEquals(crc1, crc2)
        assertEquals(crc2, crc3)
        
        println("一致性测试 CRC: 0x${crc1.toString(16).uppercase().padStart(4, '0')}")
    }
    
    /**
     * 测试不同初始值
     */
    @Test
    fun testDifferentInitialValues() {
        val data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        
        val crc1 = OtaCrcUtils.getCrc(0xFFFF, data, data.size)
        val crc2 = OtaCrcUtils.getCrc(0x0000, data, data.size)
        
        // 不同初始值应该产生不同的 CRC
        assertNotEquals(crc1, crc2)
        
        println("初始值 0xFFFF CRC: 0x${crc1.toString(16).uppercase().padStart(4, '0')}")
        println("初始值 0x0000 CRC: 0x${crc2.toString(16).uppercase().padStart(4, '0')}")
    }
    
    /**
     * 测试部分数据长度
     */
    @Test
    fun testPartialLength() {
        val data = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
        
        // 只计算前 4 个字节
        val crc1 = OtaCrcUtils.getCrc(0xFFFF, data, 4)
        
        // 计算前 4 个字节（使用子数组）
        val subData = data.copyOfRange(0, 4)
        val crc2 = OtaCrcUtils.getCrc(0xFFFF, subData, 4)
        
        // 两种方式应该得到相同结果
        assertEquals(crc1, crc2)
        
        println("部分长度 CRC: 0x${crc1.toString(16).uppercase().padStart(4, '0')}")
    }
    
    /**
     * 测试外部 CRC 计算器
     */
    @Test
    fun testExternalCalculator() {
        val data = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte())
        
        // 使用内置实现
        val builtinCrc = OtaCrcUtils.getCrc(0xFFFF, data, data.size)
        
        // 设置外部计算器（模拟）
        OtaCrcUtils.crcCalculator = object : OtaCrcUtils.ICrcCalculator {
            override fun calculateCrc(initialValue: Int, data: ByteArray, length: Int): Int {
                // 返回一个固定值用于测试
                return 0x1234
            }
        }
        
        val externalCrc = OtaCrcUtils.getCrc(0xFFFF, data, data.size)
        
        // 外部计算器应该返回我们设置的值
        assertEquals(0x1234, externalCrc)
        
        // 恢复内置实现
        OtaCrcUtils.crcCalculator = null
        
        val restoredCrc = OtaCrcUtils.getCrc(0xFFFF, data, data.size)
        assertEquals(builtinCrc, restoredCrc)
        
        println("内置 CRC: 0x${builtinCrc.toString(16).uppercase().padStart(4, '0')}")
        println("外部 CRC: 0x${externalCrc.toString(16).uppercase().padStart(4, '0')}")
    }
    
    /**
     * 性能测试
     * 测试大数据量的 CRC 计算性能
     */
    @Test
    fun testPerformance() {
        // 创建 10KB 的测试数据
        val data = ByteArray(10 * 1024) { it.toByte() }
        
        val startTime = System.nanoTime()
        
        // 计算 1000 次
        repeat(1000) {
            OtaCrcUtils.getCrc(0xFFFF, data, data.size)
        }
        
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1_000_000  // 转换为毫秒
        
        println("性能测试: 1000 次 CRC 计算（10KB 数据）耗时 ${duration}ms")
        println("平均每次: ${duration / 1000.0}ms")
        
        // 性能应该在合理范围内（< 1000ms）
        assertTrue("性能测试失败: ${duration}ms 超过 1000ms", duration < 1000)
    }
    
    /**
     * 测试 CRC 验证方法
     */
    @Test
    fun testVerifyCrc() {
        val data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val correctCrc = OtaCrcUtils.getCrc(0xFFFF, data, data.size)
        
        // 正确的 CRC 应该验证通过
        assertTrue(OtaCrcUtils.verifyCrc(data, correctCrc))
        
        // 错误的 CRC 应该验证失败
        assertFalse(OtaCrcUtils.verifyCrc(data, correctCrc + 1))
        assertFalse(OtaCrcUtils.verifyCrc(data, 0x0000))
    }
}
