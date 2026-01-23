package com.ota.sdk.api

/**
 * 蓝牙通信接口 - 客户需要实现此接口
 * 此接口完全独立，不依赖任何现有 SDK 或类
 * 
 * 客户需要用自己的蓝牙通信库实现此接口，SDK 会调用这些方法发送协议包
 */
interface IBluetoothCommunicator {
    /**
     * 发送消息
     * SDK 会调用此方法发送构建好的完整协议包（字节数组）
     * 
     * @param data 完整的协议包数据（包含协议头、命令ID、payload等）
     */
    fun sendMessage(data: ByteArray)
}
