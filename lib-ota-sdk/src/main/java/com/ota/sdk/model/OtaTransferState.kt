package com.ota.sdk.model

import java.io.File

/**
 * OTA 传输状态
 * 
 * @param total 总文件数
 */
class OtaTransferState(val total: Int) {
    /**
     * 传输任务总体状态
     */
    var state: State = State.PRE_TRANSFER
    
    /**
     * 当前正在传输的文件
     */
    var sendingFile: File? = null
    
    /**
     * 当前文件传输进度（0-100）
     */
    var progress: Int = 0
    
    /**
     * 正在传输第几个文件（从 0 开始）
     */
    var index: Int = 0
    
    override fun toString(): String {
        return "OtaTransferState(total=$total, state=$state, sendingFile=$sendingFile, progress=$progress, index=$index)"
    }
}

/**
 * 文件传输任务状态枚举
 */
enum class State {
    /** 预传输（准备阶段） */
    PRE_TRANSFER,
    
    /** 传输中 */
    TRANSFERRING,
    
    /** 成功 */
    SUCCESS,
    
    /** 失败 */
    FAIL
}
