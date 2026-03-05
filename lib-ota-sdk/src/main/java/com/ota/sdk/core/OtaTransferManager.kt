package com.ota.sdk.core

import android.content.Context
import com.ota.sdk.command.OtaCommandBuilder
import com.ota.sdk.api.IBluetoothCommunicator
import com.ota.sdk.api.ILogger
import com.ota.sdk.api.IOtaMessageListener
import com.ota.sdk.model.*
import com.ota.sdk.utils.OtaCrcUtils
import com.ota.sdk.utils.OtaFileProcessor
import com.ota.sdk.utils.LogUtil
import com.ota.sdk.utils.ProtocolParser
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

/**
 * OTA 传输管理器
 * SDK 的核心入口类，客户通过此类进行 OTA 传输
 * 
 * 使用示例：
 * ```kotlin
 * val manager = OtaTransferManager(context, communicator, logger)
 * manager.startTransfer(OtaFileType.OTA, listOf(file))
 *     .subscribe(
 *         { state -> /* 处理状态 */ },
 *         { error -> /* 处理错误 */ }
 *     )
 * ```
 */
class OtaTransferManager(
    private val context: Context,
    private val communicator: IBluetoothCommunicator,
    logger: ILogger? = null
) {
    private val TAG = "OtaTransferManager"
    
    init {
        // 设置日志接口实现，SDK 内部通过 LogUtil 统一输出日志
        LogUtil.setLogger(logger)
    }
    
    private val fileProcessor: OtaFileProcessor = OtaFileProcessor()
    private val protocolHandler: OtaProtocolHandler = OtaProtocolHandler(
        communicator,
        fileProcessor
    )
    
    // 消息监听器实现
    private val messageListener = object : IOtaMessageListener {
        override fun onMessageReceived(rawData: ByteArray): Boolean {
            // 解析协议
            val parsedMessage = ProtocolParser.parse(rawData)
            
            if (parsedMessage == null) {
                LogUtil.w(TAG, "协议解析失败，忽略此消息")
                return false
            }
            LogUtil.w(TAG, "协议解析成功 $parsedMessage")

            // 判断是否是 OTA 消息
            if (!parsedMessage.isOtaMessage()) {
                // 不是 OTA 消息，返回 false 让使用者自行处理
                return false
            }
            
            // 是 OTA 消息，交给协议处理器处理
            LogUtil.d(TAG, "收到 OTA 消息: head=0x${parsedMessage.head.toString(16)}, cmdId=0x${parsedMessage.cmdId.toString(16)}")
            protocolHandler.handleMessage(
                parsedMessage.head,
                parsedMessage.cmdId.toShort(),
                parsedMessage.payload
            )
            
            return true  // 已处理
        }
    }
    
    /**
     * 开始 OTA 传输
     * 
     * @param fileType 文件类型（OTA 或 OTA_UPEX）
     * @param files 文件列表
     * @param appendixSize 附加数据大小（默认为 0）
     * @param bleTransmission 是否是ble传输数据、ble传输数据需要进入高速模式
     * @return Observable<OtaTransferState> 传输状态流
     */
    fun startTransfer(
        fileType: OtaFileType,
        files: List<File>,
        appendixSize: Int = 0,
        bleTransmission: Boolean = false
    ): Observable<OtaTransferState> {
        LogUtil.d(TAG, "开始 OTA 传输: fileType=$fileType, files=${files.size}, appendixSize=$appendixSize")
        
        // 验证文件
        if (files.isEmpty()) {
            return Observable.error(OtaException(OtaError.ERROR_FILE_EXCEPTION, "文件列表为空"))
        }
        
        var fileLen: Long = 0
        files.forEach { file ->
            if (!file.exists() || !file.isFile) {
                return Observable.error(
                    OtaException(OtaError.ERROR_FILE_EXCEPTION, "文件不存在: ${file.absolutePath}")
                )
            }
            fileLen += file.length()
        }
        
        if (fileLen == 0L) {
            return Observable.error(OtaException(OtaError.ERROR_FILE_EXCEPTION, "文件总长度为 0"))
        }
        
        // 处理 .upex 文件（需要解压）
        if (fileType == OtaFileType.OTA_UPEX) {
            val externalFilesDir: File? = context.getExternalFilesDir(null)
            val rootPath = if (externalFilesDir == null) {
                context.filesDir.absolutePath
            } else {
                externalFilesDir.absolutePath
            }
            
            val defaultPath = rootPath + File.separator + "ota_unzip"
            val unZipResult = fileProcessor.untarFile(files[0].path, defaultPath)
            
            LogUtil.d(TAG, "解压 .upex 文件结果: $unZipResult")
            
            if (!unZipResult) {
                return Observable.error(
                    OtaException(OtaError.ERROR_FILE_EXCEPTION, "解压 .upex 文件失败")
                )
            }
            
            // 清理解压目录
            fileProcessor.deleteAllInDir(File(defaultPath))
        }
        
        // 创建传输状态
        val transferState = OtaTransferState(files.size).apply {
            state = State.PRE_TRANSFER
            index = 0
            progress = 0
        }
        
        return Observable.create { emitter ->
            // 初始化协议处理器
            protocolHandler.initTransferState(files, transferState,bleTransmission, emitter)
            
            // 计算文件 CRC（用于 OTA 传输请求）
            val firstFileBytes = fileProcessor.readFileBytes(files[0])
            if (firstFileBytes == null) {
                emitter.onError(OtaException(OtaError.ERROR_FILE_EXCEPTION, "读取第一个文件失败"))
                return@create
            }
            
            val fileCrc = OtaCrcUtils.getCrc(0xFFFF, firstFileBytes, firstFileBytes.size)
            
            // 发送传输请求
            val requestCmd = OtaCommandBuilder.buildTransferFile0ACmd(
                fileType = fileType,
                fileLen = fileLen.toInt(),
                fileCount = files.size,
                appendixSize = appendixSize,
                crc = fileCrc
            )
            
            communicator.sendMessage(requestCmd)
            
            LogUtil.d(TAG, "已发送 OTA 传输请求")
        }
    }
    
    /**
     * 取消传输
     * 
     * @return Single<Boolean> 取消结果
     */
    fun cancelTransfer(): Single<Boolean> {
        LogUtil.d(TAG, "取消 OTA 传输")
        
        // 立即停止发送线程（不等待设备回复）
        protocolHandler.stopTransferImmediately()
        
        return Single.create { emitter ->
            protocolHandler.setCancelEmitter(emitter)
            // 发送取消命令到设备
            communicator.sendMessage(OtaCommandBuilder.buildTransferCancelCmd())
        }
    }
    
    /**
     * 获取消息监听器
     * 客户需要在自己的蓝牙接收回调中调用此监听器
     * 
     * @return IOtaMessageListener 消息监听器
     */
    fun getMessageListener(): IOtaMessageListener {
        return messageListener
    }
    
    /**
     * 处理超时（可选，如果客户需要自己处理超时）
     * 
     * @param cmdId 超时的命令ID
     */
    fun handleTimeout(cmdId: Short) {
        protocolHandler.handleTimeout(cmdId)
    }
}
