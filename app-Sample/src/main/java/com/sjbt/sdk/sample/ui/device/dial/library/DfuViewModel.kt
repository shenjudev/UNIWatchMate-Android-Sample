package com.sjbt.sdk.sample.ui.device.dial.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.port.FileType
import com.base.sdk.port.WmTransferState
import com.blankj.utilcode.util.FileIOUtils
import com.github.kilnn.tool.dialog.prompt.PromptDialogHolder
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.dialog.CallBack
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.showFailed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import timber.log.Timber
import java.io.File


class DfuViewModel : ViewModel() {

    sealed class DfuEvent {
        class OnSuccess(val installed: DialMock) : DfuEvent()
        class OnFail(val error: Throwable) : DfuEvent()
    }

    private val _flowDfuEvent = MutableStateFlow<DfuEvent?>(null)
    val flowDfuEvent = _flowDfuEvent.asStateFlow()

    private var dfuJob: Job? = null

    fun startDfu(dialMock: DialMock, callBack: CallBack<WmTransferState>) {
        dfuJob?.cancel()
        dfuJob = viewModelScope.launch {
            try {
                val curMillis = System.currentTimeMillis()
                var dialPath =
                    MyApplication.instance.filesDir.absolutePath + "/" + curMillis + ".dial"
                var coverPath =
                    MyApplication.instance.filesDir.absolutePath + "/" + curMillis + ".jpg"
                if (dialMock.dialCoverRes >= 0) {
                    val inputStream = MyApplication.instance.assets.open(dialMock.dialAssert!!)
                    FileIOUtils.writeFileFromIS(dialPath, inputStream)
                    val dialCoverArray = UNIWatchMate.wmApps.appDial.parseDialThumpJpg(dialPath)
                    FileIOUtils.writeFileFromBytesByChannel(coverPath, dialCoverArray, true)
                } else {
                    dialPath = dialMock.dialAssert!!
                    val dialCoverArray = UNIWatchMate.wmApps.appDial.parseDialThumpJpg(dialPath)
                    FileIOUtils.writeFileFromBytesByChannel(coverPath, dialCoverArray, true)
                }

                val coverList = mutableListOf<File>()
                val dialList = mutableListOf<File>()
                coverList.add(File(coverPath))
                dialList.add(File(dialPath))
                UNIWatchMate.wmTransferFile.startTransfer(FileType.DIAL_COVER, coverList,
                    dialList[0].length().toInt())
                    .asFlow()
//                    .catch {
//                        UNIWatchMate.wmLog.logI("DfuViewModel","startTransfer DIAL_COVER fail ${it.message}")
//                    }
                    .collect {
                        callBack.callBack(it)
                    }
                Timber.i("startTransfer DIAL")
                UNIWatchMate.wmTransferFile.startTransfer(FileType.DIAL, dialList)
                    .asFlow()
//                    .catch {
//                        UNIWatchMate.wmLog.logI("DfuViewModel","startTransfer DIAL fail ${it.message}")
//                    }
                    .collect {
                        callBack.callBack(it)
                    }

                _flowDfuEvent.value = DfuEvent.OnSuccess(dialMock)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    e.printStackTrace()
                    _flowDfuEvent.value = DfuEvent.OnFail(e)
                }
            }
        }
    }

    fun cancelInstall() {
        UNIWatchMate.wmTransferFile.cancelTransfer().subscribe { success ->
            Timber.i("cancelTransfer:$success")
            ToastUtil.showToast("cancel success:$success")
        }
    }

    fun isDfuIng(): Boolean {
        return dfuJob?.isActive == true
    }

    override fun onCleared() {
        super.onCleared()
    }
}

/**
 * General prompt message for Dfu
 */
fun PromptDialogHolder.showDfuFail(context: Context, throwable: Throwable) {
    Timber.w(throwable)
    showFailed(throwable)
}