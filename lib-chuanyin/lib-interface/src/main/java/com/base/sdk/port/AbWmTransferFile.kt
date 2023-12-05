package com.base.sdk.port

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

/**
 * 传输文件功能抽象类
 */
abstract class AbWmTransferFile {
    abstract fun startTransfer(fileType: FileType, files: List<File>): Observable<WmTransferState>
    abstract fun cancelTransfer(): Single<Boolean>
}

/**
 * 传输文件类型
 */
enum class FileType(val type: Int) {
    MUSIC(1),//MP3类型
    OTA(2),//设备ota
    DIAL(3),//表盘
    DIAL_COVER(4),//表盘封面
    OTA_UPEX(5),//设备ota_upex
    TXT(6),
    AVI(7),
    SPORT(8),//运动文件（备用未定）
}

/**
 * 传输状态
 */
class WmTransferState(var total: Int) {
    var state: State = State.PRE_TRANSFER//传输任务总体状态
    var sendingFile: File? = null//返回当前传输的文件
    var progress: Int = 0//当前文件传输进度
    var index: Int = 0 //正在传输第几个文件
    override fun toString(): String {
        return "WmTransferState(total=$total, state=$state, sendingFile=$sendingFile, progress=$progress, index=$index)"
    }
}

/**
 * 文件传输任务状态
 */
enum class State {
    PRE_TRANSFER,
    TRANSFERRING,
    FINISH
}



