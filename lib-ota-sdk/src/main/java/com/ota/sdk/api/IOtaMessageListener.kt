package com.ota.sdk.api

/**
 * 消息监听接口 - SDK 提供，客户需要调用
 * 客户在收到蓝牙消息后，直接将原始数据传给 SDK，由 SDK 内部判断和解析
 * 
 * 使用方式：
 * 1. 通过 OtaTransferManager.getMessageListener() 获取此接口实例
 * 2. 在客户的蓝牙接收回调中，将接收到的原始数据直接传给 SDK
 * 3. SDK 内部会自动判断是否是 OTA 相关消息并处理
 * 
 * 示例：
 * ```kotlin
 * // 在蓝牙接收回调中
 * fun onDataReceived(data: ByteArray) {
 *     // 直接传给 OTA SDK，无需解析
 *     otaMessageListener.onMessageReceived(data)
 * }
 * ```
 */
interface IOtaMessageListener {
    /**
     * 接收蓝牙消息
     * 客户在蓝牙接收回调中，将接收到的原始数据直接传给此方法
     * SDK 会自动判断是否是 OTA 相关消息，并进行相应处理
     * 
     * @param rawData 接收到的原始数据（完整的协议包）
     * @return true 如果是 OTA 消息并已处理，false 如果不是 OTA 消息
     */
    fun onMessageReceived(rawData: ByteArray): Boolean
}
