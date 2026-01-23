package com.ota.sdk.core

import com.ota.sdk.command.OtaCommandBuilder
import com.ota.sdk.constants.OtaProtocolConstants
import com.ota.sdk.api.IBluetoothCommunicator
import com.ota.sdk.model.*
import com.ota.sdk.utils.OtaFileProcessor
import com.ota.sdk.utils.LogUtil
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.SingleEmitter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * OTA 协议处理器
 * 负责处理设备回复的消息
 */
internal class OtaProtocolHandler(
    private val communicator: IBluetoothCommunicator,
    private val fileProcessor: OtaFileProcessor
) {
    private val TAG = "OtaProtocolHandler"
    
    // 传输相关状态
    private var transferFiles: List<File>? = null
    private var fileDataArray: ByteArray? = null
    private var sendingFile: File? = null
    private var sendFileCount = 0
    private var selectFileCount = 0
    private var cellLength = 0
    private var otaProcess = 0
    private var packageCount = 0
    private var lastDataLength = 0
    private var transferRetryCount = 0
    private var commonFileTransferCancel = false
    private var errorSend = false
    private var divide: Byte = 0
    
    // 数据发送线程（用于取消时中断）
    private var sendDataThread: Thread? = null
    
    // 状态和回调
    private var transferState: OtaTransferState? = null
    private var observableTransferEmitter: ObservableEmitter<OtaTransferState>? = null
    private var cancelTransferEmitter: SingleEmitter<Boolean>? = null
    
    // 常量
    private val MAX_CONNECT_RETRY_COUNT = 3
    private val MSG_INTERVAL = 15
    
    /**
     * 初始化传输状态
     */
    fun initTransferState(
        files: List<File>,
        state: OtaTransferState,
        emitter: ObservableEmitter<OtaTransferState>
    ) {
        transferFiles = files
        selectFileCount = files.size
        sendFileCount = 0
        transferState = state
        observableTransferEmitter = emitter
        commonFileTransferCancel = false
        errorSend = false
        transferRetryCount = 0
        otaProcess = 0
        sendDataThread = null  // 重置线程引用
    }
    
    /**
     * 处理设备回复消息
     */
    fun handleMessage(head: Byte, cmdId: Short, payload: ByteArray) {
        LogUtil.d(TAG, "收到消息: head=0x${head.toString(16)}, cmdId=0x${cmdId.toString(16)}, payload=${payload.toHexString()}")
        
        when (cmdId) {
            OtaProtocolConstants.CMD_ID_8001, OtaProtocolConstants.CMD_ID_800A -> {
                handleTransferRequestResponse(payload)
            }
            OtaProtocolConstants.CMD_ID_8002 -> {
                handleFileInfoResponse(payload)
            }
            OtaProtocolConstants.CMD_ID_8003 -> {
                handleDataPacketResponse(payload)
            }
            OtaProtocolConstants.CMD_ID_8004 -> {
                handleTransferConfirmResponse(payload)
            }
            OtaProtocolConstants.CMD_ID_8005 -> {
                handleUserCancelResponse()
            }
            OtaProtocolConstants.CMD_ID_8006 -> {
                handleDeviceCancelResponse(payload)
            }
        }
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
    /**
     * 处理传输请求回复（0x8001/0x800A）
     */
    private fun handleTransferRequestResponse(payload: ByteArray) {
        sendFileCount = 0
        errorSend = false
        commonFileTransferCancel = false
        
        val transferEnable = payload[0]  // 1=允许, 0=不允许
        val reason = payload[1].toInt()
        
        LogUtil.d(TAG, "设备回复传输请求: transferEnable=$transferEnable, reason=$reason")
        
        if (transferEnable.toInt() == 1) {  // 允许传输
            sendingFile = transferFiles!![0]
            
            transferState?.let {
                it.sendingFile = sendingFile
                it.state = State.TRANSFERRING
                observableTransferEmitter?.onNext(it)
            }
            
            sendingFile?.let { file ->
                val fileBytes = fileProcessor.readFileBytes(file)
                if (fileBytes != null) {
                    communicator.sendMessage(
                        OtaCommandBuilder.buildTransferFile02Cmd(
                            fileBytes.size,
                            file.name
                        )
                    )
                } else {
                    transferError(OtaError.ERROR_FILE_EXCEPTION, "读取文件失败")
                }
            }
        } else {  // 不允许传输
            transferFail(reason, "设备不允许传输文件, 错误码: $reason")
        }
    }
    
    /**
     * 处理文件信息回复（0x8002）
     */
    private fun handleFileInfoResponse(payload: ByteArray) {
        val lenArray = ByteArray(4)
        System.arraycopy(payload, 0, lenArray, 0, lenArray.size)
        otaProcess = 0
        cellLength = ByteBuffer.wrap(lenArray)
            .order(ByteOrder.LITTLE_ENDIAN).int - 4
        
        LogUtil.d(TAG, "设备回复分片大小: cell_length=$cellLength")
        
        if (cellLength > 0 && !commonFileTransferCancel) {
            Thread {
                val file = transferFiles!![sendFileCount]
                fileDataArray = fileProcessor.readFileBytes(file)
                
                fileDataArray?.let {
                    LogUtil.d(TAG, "开始读取文件数据，大小: ${it.size}")
                    continueSendFileData(0, it)
                } ?: run {
                    transferError(OtaError.ERROR_FILE_EXCEPTION, "读取文件数据失败")
                }
            }.start()
        } else {
            commonFileTransferCancel = true
            transferEnd(true)
            transferError(OtaError.ERROR_FILE_EXCEPTION, "传输文件失败，原因: cmd 02")
        }
    }
    
    /**
     * 处理数据包回复（0x8003）
     */
    private fun handleDataPacketResponse(payload: ByteArray) {
        val byteBuffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
        errorSend = byteBuffer.get().toInt() != 1  // 1=成功, 其他=失败
        val errorIndex = byteBuffer.int
        
        LogUtil.d(TAG, "数据包回复: errorSend=$errorSend, errorIndex=$errorIndex, totalPackageCount=$packageCount")
        
        if (errorSend) {  // 失败，需要重传
            LogUtil.d(TAG, "数据包传输失败，错误索引: $errorIndex")
            if (transferRetryCount < MAX_CONNECT_RETRY_COUNT) {
                sendErrorMsg(errorIndex)
            } else {
                transferEnd(true)
                transferError(OtaError.ERROR_TIME_OUT, "数据包传输失败，重试次数超限")
            }
        } else {  // 成功
            transferRetryCount = 0
            LogUtil.d(TAG, "数据包传输成功，索引: $errorIndex")
            
            sendDataThread = Thread {
                val file = transferFiles!![sendFileCount]
                fileDataArray = fileProcessor.readFileBytes(file)
                
                fileDataArray?.let {
                    // 如果不是最后一包，继续发送下一包
                    if (errorIndex != packageCount - 1) {
                        continueSendFileData(errorIndex + 1, it)
                    }
                }
            }
            sendDataThread?.start()
        }
    }
    
    /**
     * 处理传输确认回复（0x8004）
     */
    private fun handleTransferConfirmResponse(payload: ByteArray) {
        transferRetryCount = 0
        otaProcess = 0
        
        val dataSuccess = payload[0].toInt()  // 1=成功, 0=失败
        LogUtil.d(TAG, "传输确认回复: dataSuccess=$dataSuccess")
        

        if (dataSuccess == 1) {  // 成功
            sendFileCount++
            LogUtil.d(TAG, "当前文件传输完成，文件索引: $sendFileCount")
            
            if (sendFileCount >= transferFiles!!.size) {  // 所有文件传输完成
                transferState?.let {
                    it.state = State.SUCCESS
                    it.sendingFile = sendingFile
                    it.progress = 100
                    it.index = sendFileCount
                    
                    observableTransferEmitter?.onNext(it)
                    observableTransferEmitter?.onComplete()
                }
                LogUtil.d(TAG, "所有文件传输完成")
                transferEnd(true)
            } else {  // 继续传输下一个文件
                LogUtil.d(TAG, "开始传输下一个文件")
                
                sendingFile = transferFiles!![sendFileCount]
                val fileBytes = sendingFile?.let { fileProcessor.readFileBytes(it) }
                
                if (fileBytes != null) {
                    communicator.sendMessage(
                        OtaCommandBuilder.buildTransferFile02Cmd(
                            fileBytes.size,
                            sendingFile!!.name
                        )
                    )
                    
                    transferState?.let {
                        it.state = State.SUCCESS
                        it.sendingFile = sendingFile
                        it.progress = 100
                        it.index = sendFileCount
                        
                        observableTransferEmitter?.onNext(it)
                    }
                } else {
                    transferError(OtaError.ERROR_FILE_EXCEPTION, "读取下一个文件失败")
                }
            }
        } else {  // 失败
            LogUtil.d(TAG, "传输确认失败，当前文件索引: $sendFileCount")
            
            transferState?.let {
                it.sendingFile = sendingFile
                val processPercent = 100f * (otaProcess + 1) / packageCount
                it.progress = processPercent.toInt()
                it.state = State.FAIL
                it.index = sendFileCount
                
                observableTransferEmitter?.onNext(it)
                observableTransferEmitter?.onComplete()
            }
            
            transferEnd(false)
        }

    }
    
    /**
     * 处理用户取消回复（0x8005）
     */
    private fun handleUserCancelResponse() {
        LogUtil.e(TAG, "用户取消传输")
        
        // 立即设置取消标志
        commonFileTransferCancel = true
        
        // 中断发送数据的线程
        sendDataThread?.let {
            if (it.isAlive) {
                LogUtil.d(TAG, "中断发送数据线程")
                it.interrupt()
            }
        }
        sendDataThread = null
        
        cancelTransferEmitter?.onSuccess(true)
        
        observableTransferEmitter?.let {
            if (!it.isDisposed) {
                it.onComplete()
            }
        }
        
        transferEnd(false)
    }
    
    /**
     * 处理设备主动取消回复（0x8006）
     */
    private fun handleDeviceCancelResponse(payload: ByteArray) {
        LogUtil.e(TAG, "设备主动取消传输")
        
        // 立即设置取消标志
        commonFileTransferCancel = true
        
        // 中断发送数据的线程
        sendDataThread?.let {
            if (it.isAlive) {
                LogUtil.d(TAG, "中断发送数据线程")
                it.interrupt()
            }
        }
        sendDataThread = null
        
        val reasonCancel = payload[0].toInt()
        LogUtil.e(TAG, "取消原因: $reasonCancel")
        
        transferEnd(false)
        transferFail(reasonCancel, "设备主动取消传输，错误码: $reasonCancel")
    }
    
    /**
     * 继续发送文件数据
     */
    private fun continueSendFileData(startProcess: Int, dataArray: ByteArray) {
        packageCount = dataArray.size / cellLength
        lastDataLength = dataArray.size % cellLength
        
        if (lastDataLength != 0) {
            packageCount += 1
        }
        
        for (i in startProcess until packageCount) {
            // 检查线程中断标志
            if (Thread.currentThread().isInterrupted) {
                LogUtil.w(TAG, "发送线程被中断")
                break
            }
            
            // 检查取消或出错标志
            if (commonFileTransferCancel || errorSend) {
                LogUtil.e(TAG, "传输取消或出错，当前进度: $otaProcess")
                break
            }
            
            otaProcess = i
            
            try {
                val info = getOtaDataInfoNew(dataArray, i)
                divide = OtaCommandBuilder.getDivideType(i, packageCount)
                
                LogUtil.d(TAG, "发送数据包: 序号=$i, 分包类型=$divide, 信息=$info")
                
                communicator.sendMessage(
                    OtaCommandBuilder.buildTransfer03Cmd(i, info, divide)
                )
                
                val processPercent = 100f * (otaProcess + 1) / packageCount
                
                transferState?.let {
                    it.progress = processPercent.toInt()
                    it.index = sendFileCount
                    it.state = State.TRANSFERRING
                    observableTransferEmitter?.onNext(it)
                }
                
                Thread.sleep(MSG_INTERVAL.toLong())
                LogUtil.d(TAG, "传输进度: $processPercent%")
                
            } catch (e: InterruptedException) {
                // 线程被中断，退出循环
                LogUtil.w(TAG, "发送数据包被中断")
                Thread.currentThread().interrupt() // 重新设置中断标志
                break
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtil.e(TAG, "发送数据包出错: ${e.message}", e)
                transferError(OtaError.ERROR_OTHER, "发送数据包出错: ${e.message}")
                break
            }
        }
    }
    
    /**
     * 获取 OTA 数据包信息
     */
    private fun getOtaDataInfoNew(dataArray: ByteArray, otaProcess: Int): OtaCmdInfo {
        val info = OtaCmdInfo()
        
        if (otaProcess != packageCount - 1) {  // 不是最后一包
            info.offSet = otaProcess * cellLength
            info.payload = ByteArray(cellLength)
            System.arraycopy(
                dataArray,
                otaProcess * cellLength,
                info.payload,
                0,
                info.payload.size
            )
        } else {  // 最后一包
            if (lastDataLength == 0) {
                info.offSet = otaProcess * cellLength
                info.payload = ByteArray(cellLength)
                System.arraycopy(
                    dataArray,
                    otaProcess * cellLength,
                    info.payload,
                    0,
                    info.payload.size
                )
            } else {
                info.offSet = otaProcess * cellLength
                info.payload = ByteArray(lastDataLength)
                System.arraycopy(
                    dataArray,
                    otaProcess * cellLength,
                    info.payload,
                    0,
                    info.payload.size
                )
            }
        }
        
        return info
    }
    
    /**
     * 发送错误重传消息
     */
    private fun sendErrorMsg(errorProcess: Int) {
        transferRetryCount++
        val info = getOtaDataInfoNew(fileDataArray!!, errorProcess)
        divide = OtaCommandBuilder.getDivideType(errorProcess, packageCount)
        
        communicator.sendMessage(
            OtaCommandBuilder.buildTransfer03Cmd(errorProcess, info, divide)
        )
    }
    
    /**
     * 传输结束
     */
    private fun transferEnd(cancelToDevice: Boolean) {
        try {
            if (cancelToDevice) {
                communicator.sendMessage(OtaCommandBuilder.buildTransferCancelCmd())
            }
            
            // 中断发送数据的线程
            sendDataThread?.let {
                if (it.isAlive) {
                    LogUtil.d(TAG, "传输结束，中断发送数据线程")
                    it.interrupt()
                }
            }
            sendDataThread = null
            
            otaProcess = 0
            transferRetryCount = 0
            sendFileCount = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 传输失败
     */
    private fun transferFail(reason: Int, tips: String) {
        val error = OtaError.fromCode(reason)
        transferError(error, tips)
    }
    
    /**
     * 传输错误
     */
    fun transferError(code: OtaError, errMsg: String) {
        LogUtil.e(TAG, errMsg)
        errorSend = true
        
        observableTransferEmitter?.let {
            if (!it.isDisposed) {
                it.onError(OtaException(code, errMsg))
            }
        }
    }
    
    /**
     * 设置取消传输回调
     */
    fun setCancelEmitter(emitter: SingleEmitter<Boolean>) {
        cancelTransferEmitter = emitter
    }
    
    /**
     * 立即停止传输（不等待设备回复）
     * 用于用户主动取消传输时立即生效
     */
    fun stopTransferImmediately() {
        LogUtil.w(TAG, "立即停止传输")
        
        // 1. 设置取消标志
        commonFileTransferCancel = true
        
        // 2. 中断发送数据的线程
        sendDataThread?.let {
            if (it.isAlive) {
                LogUtil.d(TAG, "立即中断发送数据线程")
                it.interrupt()
            }
        }
        sendDataThread = null
    }
    
    /**
     * 处理超时
     */
    fun handleTimeout(cmdId: Short) {
        if (commonFileTransferCancel) {
            return
        }
        
        when (cmdId) {
            OtaProtocolConstants.CMD_ID_8001 -> {
                transferError(OtaError.ERROR_TIME_OUT, "0x8001 超时")
            }
            OtaProtocolConstants.CMD_ID_800A -> {
                transferError(OtaError.ERROR_TIME_OUT, "0x800A 超时")
                communicator.sendMessage(
                    OtaCommandBuilder.buildTransfer0AOtaOutTime08Cmd()
                )
            }
            OtaProtocolConstants.CMD_ID_8002 -> {
                if (transferRetryCount < MAX_CONNECT_RETRY_COUNT) {
                    transferRetryCount++
                    sendingFile = transferFiles!![sendFileCount]
                    val fileBytes = sendingFile?.let { fileProcessor.readFileBytes(it) }
                    if (fileBytes != null) {
                        communicator.sendMessage(
                            OtaCommandBuilder.buildTransferFile02Cmd(
                                fileBytes.size,
                                sendingFile!!.name
                            )
                        )
                    }
                } else {
                    transferEnd(true)
                }
            }
            OtaProtocolConstants.CMD_ID_8003 -> {
                if (transferRetryCount < MAX_CONNECT_RETRY_COUNT) {
                    transferRetryCount++
                    communicator.sendMessage(
                        OtaCommandBuilder.buildTransfer03Cmd(
                            otaProcess,
                            getOtaDataInfoNew(fileDataArray!!, otaProcess),
                            divide
                        )
                    )
                } else {
                    transferError(OtaError.ERROR_TIME_OUT, "0x8003 超时")
                }
            }
            OtaProtocolConstants.CMD_ID_8004 -> {
                if (transferRetryCount < MAX_CONNECT_RETRY_COUNT) {
                    transferRetryCount++
                    communicator.sendMessage(OtaCommandBuilder.buildTransfer04Cmd())
                }
            }
        }
    }
}
