package com.ota.sdk.api

/**
 * 日志接口 - 可选实现
 * SDK 通过此接口将日志暴露给使用者，由使用者决定如何输出日志
 * 如果客户不提供此接口，SDK 将使用默认的空实现（不输出日志）
 */
interface ILogger {
    /**
     * 输出调试日志
     * 
     * @param tag 日志标签
     * @param message 日志内容
     * @param location 调用位置信息（格式：文件名:行号 方法名）
     */
    fun logD(tag: String, message: String, location: String? = null)
    
    /**
     * 输出信息日志
     * 
     * @param tag 日志标签
     * @param message 日志内容
     * @param location 调用位置信息（格式：文件名:行号 方法名）
     */
    fun logI(tag: String, message: String, location: String? = null)
    
    /**
     * 输出错误日志
     * 
     * @param tag 日志标签
     * @param message 日志内容
     * @param location 调用位置信息（格式：文件名:行号 方法名）
     * @param throwable 异常对象（可选）
     */
    fun logE(tag: String, message: String, location: String? = null, throwable: Throwable? = null)
    
    /**
     * 输出警告日志
     * 
     * @param tag 日志标签
     * @param message 日志内容
     * @param location 调用位置信息（格式：文件名:行号 方法名）
     */
    fun logW(tag: String, message: String, location: String? = null)
}
