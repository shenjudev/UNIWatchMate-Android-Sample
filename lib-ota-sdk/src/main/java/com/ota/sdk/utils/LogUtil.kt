package com.ota.sdk.utils

import com.ota.sdk.api.ILogger

/**
 * SDK 内部日志工具类
 * 自动获取调用位置信息并通过 ILogger 接口暴露给使用者
 */
 internal object LogUtil {
    
    private var logger: ILogger? = null
    
    /**
     * 设置日志接口实现
     */
    fun setLogger(logger: ILogger?) {
        this.logger = logger
    }
    
    /**
     * 输出调试日志
     * 
     * @param tag 日志标签
     * @param message 日志内容
     */
    fun d(tag: String, message: String) {
        logger?.logD(tag, message, getCallerLocation())
    }
    
    /**
     * 输出信息日志
     * 
     * @param tag 日志标签
     * @param message 日志内容
     */
    fun i(tag: String, message: String) {
        logger?.logI(tag, message, getCallerLocation())
    }
    
    /**
     * 输出错误日志
     * 
     * @param tag 日志标签
     * @param message 日志内容
     * @param throwable 异常对象（可选）
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        logger?.logE(tag, message, getCallerLocation(), throwable)
    }
    
    /**
     * 输出警告日志
     * 
     * @param tag 日志标签
     * @param message 日志内容
     */
    fun w(tag: String, message: String) {
        logger?.logW(tag, message, getCallerLocation())
    }
    
    /**
     * 获取调用者的位置信息
     * 
     * @return 格式：文件名:行号 方法名
     */
    private fun getCallerLocation(): String {
        return try {
            // 获取当前线程的堆栈信息
            val stackTrace = Thread.currentThread().stackTrace
            
            // 找到第一个不是 LogUtil 和 Thread 类的调用位置
            // stackTrace[0] = VMStack.getThreadStackTrace
            // stackTrace[1] = Thread.getStackTrace
            // stackTrace[2] = LogUtil.getCallerLocation
            // stackTrace[3] = LogUtil.d/i/e/w
            // stackTrace[4] = 真正的调用者
            for (i in 4 until stackTrace.size) {
                val element = stackTrace[i]
                val className = element.className
                
                // 跳过 LogUtil 自身
                if (className.contains("LogUtil")) {
                    continue
                }
                
                // 获取简短的类名（不含包名）
                val simpleClassName = className.substringAfterLast('.')
                val fileName = element.fileName ?: "Unknown"
                val lineNumber = element.lineNumber
                val methodName = element.methodName
                
                return "$fileName:$lineNumber $methodName()"
            }
            
            "Unknown"
        } catch (e: Exception) {
            "Error:${e.message}"
        }
    }
}
