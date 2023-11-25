package com.sjbt.sdk.dfu

import com.base.sdk.exception.WmTransferError
import com.base.sdk.exception.WmTransferException
import com.base.sdk.port.*
import com.sjbt.sdk.MAX_RETRY_COUNT
import com.sjbt.sdk.MSG_INTERVAL
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.OtaCmdInfo
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.FileUtils
import io.reactivex.rxjava3.core.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SJTransferFile(val sjUniWatch: SJUniWatch) : AbWmTransferFile() {

    private val TAG = "SJTransferFile"

    //文件传输相关
    private var mTransferFiles: List<File>? = null
    private var mFileDataArray: ByteArray? = null
    private var mSendingFile: File? = null

    private var mSelectFileCount = 0
    private var mSendFileCount = 0
    private var mCellLength = 0
    private var mOtaProcess = 0
    private var mCanceledSend = false
    private var mErrorSend: Boolean = false
    private var mDivide: Byte = 0
    private var mPackageCount = 0
    private var mLastDataLength: Int = 0
    private var mTransferRetryCount = 0
    var mTransferring = false

    val mSportMap = HashMap<FileType, Boolean>()
    var cancelTransferEmitter: SingleEmitter<Boolean>? = null
    var observableTransferEmitter: ObservableEmitter<WmTransferState>? = null

    var transferState: WmTransferState? = null

    override fun isSupport(fileType: FileType): Boolean {
        return mSportMap[fileType] == true
    }

    override fun cancelTransfer(): Single<Boolean> {
        return Single.create { emitter ->
            cancelTransferEmitter = emitter
            sjUniWatch.wmLog.logE(TAG, "cancel Transfer")
            sjUniWatch.sendNormalMsg(CmdHelper.transferCancelCmd)
        }
    }

    fun transferEnd() {
        try {
            sjUniWatch.clearMsg()
            mOtaProcess = 0
            mTransferRetryCount = 0
            mTransferring = false
            mSendFileCount = 0
//            removeCallBackRunner(mTransferTimeoutRunner)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun startTransfer(fileType: FileType, files: List<File>): Observable<WmTransferState> {
        mTransferFiles = files
        mSelectFileCount = files.size

        transferState = WmTransferState(
            mTransferFiles!!.size
        ).also {
            it.state = State.PRE_TRANSFER
            it.index = 0
            it.progress = 0
        }

        return Observable.create { emitter ->
            observableTransferEmitter = emitter

            var fileLen: Long = 0

            files.forEach {
                fileLen += it.length()
            }

            sjUniWatch.sendNormalMsg(
                CmdHelper.getTransferFile01Cmd(
                    fileType.type.toByte(),
                    fileLen.toInt(),
                    files.size
                )
            )
        }
    }

    fun transferFileBuz(msgBean: MsgBean) {
        when (msgBean.cmdId.toShort()) {
            CMD_ID_8001 -> {
                mSendFileCount = 0
                mErrorSend = false
                mCanceledSend = false

                val ota_allow = msgBean.payload[0] //是否容许升级 0允许 1不允许
                val reason = msgBean.payload[1].toInt() //是否容许升级 0允许 1不允许
                sjUniWatch.wmLog.logD(TAG, "1.Allow transfer:$ota_allow")
                if (ota_allow.toInt() == 1) {
                    mSendingFile = mTransferFiles!![0]

                    transferState?.let {
                        it.sendingFile = mSendingFile
                        it.state = State.TRANSFERRING
                        observableTransferEmitter?.onNext(transferState)

                    }

                    mSendingFile?.let { file ->
                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getTransferFile02Cmd(
                                file.length().toInt(),
                                file.name
                            )
                        )
                    }

                } else {

                    val error = when (reason) {
                        0 -> {
                            WmTransferError.ERROR_OTHER
                        }
                        1 -> {
                            WmTransferError.ERROR_BUSY
                        }
                        2 -> {
                            WmTransferError.ERROR_CRC
                        }
                        3 -> {
                            WmTransferError.ERROR_LOW_MEMORY
                        }
                        4 -> {
                            WmTransferError.ERROR_LOW_POWER
                        }
                        5 -> {
                            WmTransferError.ERROR_TIME_OUT
                        }
                        else -> {
                            WmTransferError.ERROR_OTHER
                        }
                    }

                    transferError(error, "device not allow transfer file , reason:$reason")
                }
            }
            CMD_ID_8002 -> {
                val lenArray = ByteArray(4)
                System.arraycopy(msgBean.payload, 0, lenArray, 0, lenArray.size)
                mOtaProcess = 0
                mCellLength = ByteBuffer.wrap(lenArray)
                    .order(ByteOrder.LITTLE_ENDIAN).int - 4
                sjUniWatch.wmLog.logD(TAG, "cell_length:$mCellLength")
                if (mCellLength > 0) {

                    Thread {
                        mFileDataArray =
                            FileUtils.readFileBytes(mTransferFiles!![mSendFileCount])

                        mFileDataArray?.let {
                            sjUniWatch.wmLog.logD(TAG, "start thread to read file array：" + it.size)
                            continueSendFileData(0, it)
                        }

                    }.start()

                } else {
                    mCanceledSend = true
                    transferEnd()
                    transferError(
                        WmTransferError.ERROR_FILE_EXCEPTION,
                        "transfer file fail,reason: cmd 02"
                    )
                }
            }

            CMD_ID_8003 -> {
                val isRight = msgBean.payload[0]
                mTransferring = true
                mErrorSend = isRight.toInt() != 1
                mOtaProcess = msgBean.payload[1].toInt()
                sjUniWatch.wmLog.logD(
                    TAG,
                    "back msg state:$mErrorSend ota_process:$mOtaProcess total pk count:$mPackageCount"
                )
                if (mErrorSend) { //失败
                    //                                        removeCallBackRunner(mTransferTimeoutRunner)
                    //                                        mOtaProcess = mOtaProcess > 0 ? mOtaProcess - 1 : mOtaProcess;
                    sjUniWatch.wmLog.logD(TAG, "error index：$mOtaProcess")
                    if (mTransferRetryCount < MAX_RETRY_COUNT) {
                        sendErrorMsg(mOtaProcess)
                    } else {
                        transferEnd()
                    }
                } else { //成功
                    mTransferRetryCount = 0
                    sjUniWatch.wmLog.logD(TAG, "Straighten up msg：$mOtaProcess")

                    Thread {
                        // 执行耗时操作
                        mFileDataArray =
                            FileUtils.readFileBytes(mTransferFiles!![mSendFileCount])

                        mFileDataArray?.let {

                            if (mOtaProcess.toInt() != mPackageCount - 1) {
                                mOtaProcess++
                                continueSendFileData(
                                    mOtaProcess,
                                    it
                                )
                            }

                        }
                    }.start()
                }
            }

            CMD_ID_8004 -> {
                mTransferRetryCount = 0
                mOtaProcess = 0
                //                                    removeCallBackRunner(mTransferTimeoutRunner)
                val dataSuccess = msgBean.payload[0]
                sjUniWatch.wmLog.logD(TAG, "transfer result:$dataSuccess")
                sjUniWatch.clearMsg()
                if (dataSuccess == 1.toByte()) {
                    mSendFileCount++
                    if (mSendFileCount >= mTransferFiles!!.size) {
                        mTransferring = false

                        transferState?.let {
                            it.state = State.FINISH
                            it.sendingFile = mSendingFile
                            it.progress = 100
                            it.index = mSendFileCount

                            observableTransferEmitter?.onNext(it)
                            observableTransferEmitter?.onComplete()

                        }

                        transferEnd()
                    } else {

                        transferState?.let {
                            it.state = State.TRANSFERRING
                            it.sendingFile = mSendingFile
                            it.progress = 100
                            it.index = mSendFileCount

                            observableTransferEmitter?.onNext(it)
                        }

                        mSendingFile = mTransferFiles!![mSendFileCount]

                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getTransferFile02Cmd(
                                FileUtils.readFileBytes(mSendingFile).size,
                                mSendingFile!!.name
                            )
                        )
                    }
                } else {
                    mTransferring = false

                    transferState?.let {
                        it.state = State.FINISH
                        it.sendingFile = mSendingFile
                        it.progress = mOtaProcess
                        it.index = mSendFileCount

                        observableTransferEmitter?.onNext(it)
                        observableTransferEmitter?.onComplete()
                    }

                    transferEnd()
                }
            }

            CMD_ID_8005 -> {
                mTransferring = false
                mCanceledSend = true
                sjUniWatch.wmLog.logE(TAG, "user cancel transfer：$mCanceledSend")
                cancelTransferEmitter?.onSuccess(true)
                transferEnd()
            }

            CMD_ID_8006 -> {
                mTransferring = false
                mCanceledSend = true
                val reasonCancel = msgBean.payload[0]
                sjUniWatch.wmLog.logE(TAG, "device cancel reason：$reasonCancel")
                transferEnd()
                transferError(WmTransferError.ERROR_BUSY, "file transfer error reason:06 Error")
            }
        }
    }

    fun transferError(code: WmTransferError, errMsg: String) {
        mTransferring = false
        mErrorSend = true
        if (observableTransferEmitter?.isDisposed == false) {
            observableTransferEmitter?.onError(
                WmTransferException(code, errMsg)
            )
        }
    }

    private fun continueSendFileData(startProcess: Int, dataArray: ByteArray) {
        mPackageCount = dataArray.size / mCellLength
        mLastDataLength = dataArray.size % mCellLength
        if (mLastDataLength != 0) {
            mPackageCount += 1
        }

        for (i in startProcess.toInt() until mPackageCount) {
            mOtaProcess = i
            if (mCanceledSend || mErrorSend) { //取消或者中途出错
                sjUniWatch.wmLog.logE(TAG, "cancel or error：$mOtaProcess")
                mTransferring = false
                break
            }

            if (dataArray == null) {
                break
            }

            try {
                mTransferring = true
                val info = getOtaDataInfoNew(dataArray, i)
                sjUniWatch.sendNormalMsg(CmdHelper.getTransfer03Cmd(i, info, mDivide))

                val processPercent = 100f * (mOtaProcess + 1) / mPackageCount

                transferState?.let {
                    it.progress = processPercent.toInt()
                    it.index = mSendFileCount + 1
                    it.state = State.TRANSFERRING
                    observableTransferEmitter?.onNext(it)
                }

                Thread.sleep(MSG_INTERVAL.toLong())
//                if (mOtaProcess == mPackageCount - 1) {
//                    mHandler.postDelayed(mTransferTimeoutRunner, TRANSFER_TIMEOUT)
//                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                mTransferring = false
                sjUniWatch.wmLog.logE(TAG, "continue sending error：" + e.message)

                observableTransferEmitter?.onError(e)

            }
        }
    }

    private fun sendErrorMsg(errorProcess: Int) {
        mTransferRetryCount++
        val bytes = CmdHelper.getTransfer03Cmd(
            errorProcess,
            getOtaDataInfoNew(mFileDataArray!!, errorProcess),
            mDivide
        )
        sjUniWatch.sendNormalMsg(bytes)
    }

    private fun getOtaDataInfoNew(dataArray: ByteArray, otaProcess: Int): OtaCmdInfo {
        val info = OtaCmdInfo()
        mDivide = if (otaProcess == 0 && mPackageCount > 1) {
            DIVIDE_Y_F_2
        } else {
            if (otaProcess == mPackageCount - 1) {
                DIVIDE_Y_E_2
            } else {
                DIVIDE_Y_M_2
            }
        }

//        sjUniWatch.wmLog.logD(TAG,"分包类型：" + mDivide);
        if (otaProcess != mPackageCount - 1) {
            info.offSet = otaProcess * mCellLength
            info.payload = ByteArray(mCellLength)
            System.arraycopy(
                dataArray,
                otaProcess * mCellLength,
                info.payload,
                0,
                info.payload.size
            )
        } else {
//            sjUniWatch.wmLog.logD(TAG,"最后一包长度：" + mLastDataLength);
            if (mLastDataLength == 0) {
                info.offSet = otaProcess * mCellLength
                info.payload = ByteArray(mCellLength)
                System.arraycopy(
                    dataArray,
                    otaProcess * mCellLength,
                    info.payload,
                    0,
                    info.payload.size
                )
            } else {
                info.offSet = otaProcess * mCellLength
                info.payload = ByteArray(mLastDataLength)
                System.arraycopy(
                    dataArray,
                    otaProcess * mCellLength,
                    info.payload,
                    0,
                    info.payload.size
                )
            }
        }
        return info
    }

    fun timeOut(msgBean: MsgBean) {
        if (mCanceledSend) {
            sjUniWatch.clearMsg()
            return
        }

        when (msgBean.head) {
            HEAD_FILE_SPP_A_2_D -> {
                mTransferring = false
                when (msgBean.cmdId.toShort()) {
                    CMD_ID_8001 -> {}
                    CMD_ID_8002 -> if (mTransferRetryCount < MAX_RETRY_COUNT) {
                        mTransferRetryCount++
                        mSendingFile = mTransferFiles!![mSendFileCount]
                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getTransferFile02Cmd(
                                FileUtils.readFileBytes(
                                    mSendingFile
                                ).size, mSendingFile!!.name
                            )
                        )
                    } else {
                        transferEnd()
                    }
                    CMD_ID_8003 ->
                        if (mTransferRetryCount < MAX_RETRY_COUNT) {
                            mTransferRetryCount++
                            sjUniWatch.sendNormalMsg(
                                CmdHelper.getTransfer03Cmd(
                                    mOtaProcess,
                                    getOtaDataInfoNew(mFileDataArray!!, mOtaProcess),
                                    mDivide
                                )
                            )
                        } else {
//                        if (mTransferFileListener != null) {
//                            mTransferFileListener.transferFail(FAIL_TYPE_TIMEOUT, "8003 time out")
//                        }
                        }

                    CMD_ID_8004 -> if (mTransferRetryCount < MAX_RETRY_COUNT) {
                        mTransferRetryCount++
                        val ota_data = CmdHelper.transfer04Cmd
                        sjUniWatch.sendNormalMsg(ota_data)
                    } else {
//                        if (mTransferFileListener != null) {
//                            mTransferFileListener.transferFail(FAIL_TYPE_TIMEOUT, "8004 time out")
//                        }
                    }
                }
            }
        }
    }

}