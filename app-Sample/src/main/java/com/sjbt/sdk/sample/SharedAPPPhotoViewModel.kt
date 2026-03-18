package com.sjbt.sdk.sample

import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.apps.WmVideoFrameInfo
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.di.internal.SingleInstance
import com.sjbt.sdk.sample.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 接收设备传输照片的 ViewModel。
 * 无存储设备拍照后，设备会通过 [UNIWatchMate.wmApps.appPhotoLibrary.observeDeviceTakePhotoElementCount]
 * 下发照片分片数量，此后通过 [UNIWatchMate.wmApps.appPhotoLibrary.observeImgElementOrImgNames] 接收各分片数据。
 */
class SharedAPPPhotoViewModel : ViewModel() {
    private val TAG = "SharedAPPPhotoViewModel"

    var curPhotoElementCount = 0
    var curPhotoElementIndex = 0
    var save2AlbumCount = 0
    var jpegDataOutStream = ByteArrayOutputStream()
    var lastSuccessTime: Long = 0

    private val _receiveDevicePhotoOfNoStorage = kotlinx.coroutines.flow.MutableSharedFlow<Boolean>()
    val receiveDevicePhotoOfNoStorage: kotlinx.coroutines.flow.SharedFlow<Boolean> = _receiveDevicePhotoOfNoStorage

    init {
        // 接收设备发送的图片分片数据（frameType == 9）
        viewModelScope.launch {
            UNIWatchMate.wmApps.appPhotoLibrary.observeImgElementOrImgNames.asFlow()
                .collect { videoFrame ->
                    processVideoFrame(videoFrame)
                }
        }

        // 收到设备发送照片分片的起始：设备下发分片数量后，按索引逐片请求并拼接
        viewModelScope.launch(Dispatchers.IO) {
            UNIWatchMate.wmApps.appPhotoLibrary.observeDeviceTakePhotoElementCount.asFlow()
                .collect { count ->
                    jpegDataOutStream.reset()
                    curPhotoElementCount = count
                    LogUtils.e("getDevicePhotoElementCount curPhotoElementCount = $curPhotoElementCount")

                    if (curPhotoElementCount < 1) {
                        LogUtils.e("getDevicePhotoElementCount is 0")
                        receiveImgFail()
                    } else {
                        curPhotoElementIndex = 1
                        curPhotoElementCount = count
                        LogUtils.d("SharedAPPPhotoViewModel", "Device photo element count: $count")
                        withContext(Dispatchers.Main) {
                            showLoading()
                        }
                        startReceiveImg()
                    }
                }
        }
    }

    private suspend fun showLoading() {
        withContext(Dispatchers.Main) {
            val top = ActivityUtils.getTopActivity()
            if (top is BaseActivity) {
                top.showLoadingDlg(MyApplication.instance.getString(R.string.importing_dot))
            }
        }
    }

    private suspend fun hideLoading() {
        withContext(Dispatchers.Main) {
            hideLoadingOnMainThread()
        }
    }

    private fun hideLoadingOnMainThread() {
        val top = ActivityUtils.getTopActivity()
        if (top is BaseActivity) {
            top.hideLoadingDlg()
        }
    }

    fun notifyReceiveDevicePhotoOfNoStorage(begin: Boolean) {
        viewModelScope.launch {
            LogUtils.d(TAG, "发送 无存储设备发送图片事件: $begin")
            _receiveDevicePhotoOfNoStorage.emit(begin)
        }
    }

    /**
     * 处理从设备接收的图片分片（frameType == 9）
     */
    private suspend fun processVideoFrame(videoFrame: WmVideoFrameInfo) {
        videoFrame?.let {
            Log.d(TAG, "图片接收过程：frameType=${videoFrame.frameType}")
            LogUtils.d(TAG, "图片接收过程：frameType=${videoFrame.frameType}")

            when (videoFrame.frameType) {
                9 -> {
                    videoFrame.frameData?.let { jpegData ->
                        if (curPhotoElementCount <= 0) {
                            LogUtils.e("收到图片但 curPhotoElementCount=$curPhotoElementCount")
                            return
                        }
                        jpegDataOutStream.write(jpegData)
                        if (curPhotoElementIndex < curPhotoElementCount) {
                            LogUtils.d(TAG, "curPhotoElementIndex < curPhotoElementCount: $curPhotoElementIndex < $curPhotoElementCount")
                            curPhotoElementIndex++
                            startReceiveImg()
                            return
                        }
                        val takeTime = System.currentTimeMillis() - lastSuccessTime
                        LogUtils.d(TAG, "收到完整图片 curPhotoElementCount=$curPhotoElementCount curPhotoElementIndex=$curPhotoElementIndex takeTime=${takeTime}ms")
                        lastSuccessTime = System.currentTimeMillis()
                        curPhotoElementCount = 0
                        val dateFormat = SimpleDateFormat("yyMMdd-HHmmss", Locale.US)
                        val photoName = "Device-${dateFormat.format(Date())}.jpeg"
                        saveReceivedPhoto(jpegDataOutStream, photoName)
                        letDeviceEndSendPhotoState()
                    }
                }
                else -> { }
            }
        }
    }

    /**
     * 将接收到的 JPEG 数据保存到应用目录，并可选写入系统相册
     */
    private fun saveReceivedPhoto(jpegDataOutStream: ByteArrayOutputStream, photoName: String) {
        val jpegData = jpegDataOutStream.toByteArray()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 与相册界面一致：保存到 getExternalFilesDir(null)/deviceImg
                val dir = File(MyApplication.instance.getExternalFilesDir(null), "deviceImg")
                if (!dir.exists()) dir.mkdirs()
                val savedFile = File(dir, photoName)
                FileOutputStream(savedFile).use { it.write(jpegData) }
                LogUtils.d(TAG, "原始图片已保存: ${savedFile.name}")

                viewModelScope.launch(Dispatchers.Main) {
                    hideLoadingOnMainThread()
                    kotlinx.coroutines.delay(100)
                    notifyReceiveDevicePhotoOfNoStorage(true)
                }

                // 保存到系统相册
                try {
                    val bitmap = BitmapFactory.decodeFile(savedFile.absolutePath) ?: return@launch
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, photoName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                        }
                    }
                    val contentResolver = MyApplication.instance.contentResolver
                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                            contentResolver.update(uri, contentValues, null, null)
                        }
                        LogUtils.d(TAG, "图片已保存到系统相册: $photoName")
                        save2AlbumCount++
                    }
                } catch (e: Exception) {
                    LogUtils.e("保存到系统相册失败: ${e.message}")
                    withContext(Dispatchers.Main) {
                        ToastUtil.showToast(MyApplication.instance.getString(R.string.fail_save))
                    }
                }

                withContext(Dispatchers.Main) {
                    ToastUtil.showToast(MyApplication.instance.getString(R.string.photo_saved_go_to_album))
                    notifyReceiveDevicePhotoOfNoStorage(true)
                }
            } catch (e: Exception) {
                LogUtils.e(TAG, "保存图片失败: ${e.message}")
                viewModelScope.launch {
                    receiveImgFail()
                }
            }
        }
    }

    /**
     * 请求设备发送指定索引的图片分片
     */
    fun letDeviceSendPhotoByElmentIndex(index: Int) {
        viewModelScope.launch {
            if (SingleInstance.deviceManager.flowConnectorStateInfo.value.state == WmConnectState.BIND_SUCCESS) {
                UNIWatchMate.wmApps.appPhotoLibrary.letDeviceSendPhotoByElementIndex(index)
                    .toObservable().asFlow().catch {
                        it.printStackTrace()
                        LogUtils.e(TAG, "letDeviceSendPhotoByElementIndex error: ${it.message}")
                        receiveImgFail()
                    }.collect { result ->
                        LogUtils.d(TAG, "Send photo by element index result: $result")
                        if (result != 0) {
                            LogUtils.e(TAG, "letDeviceSendPhotoByElementIndex result != 0")
                            receiveImgFail()
                        }
                    }
            } else {
                receiveImgFail()
            }
        }
    }

    private suspend fun receiveImgFail() {
        hideLoading()
        notifyReceiveDevicePhotoOfNoStorage(false)
        withContext(Dispatchers.Main) {
            ToastUtil.showToast(MyApplication.instance.getString(R.string.tip_failed))
        }
        letDeviceEndSendPhotoState()
    }

    fun startReceiveImg() {
        if (curPhotoElementCount > 0) {
            LogUtils.d(TAG, "letDeviceSendPhotoByElementIndex curPhotoElementIndex = $curPhotoElementIndex")
            letDeviceSendPhotoByElmentIndex(curPhotoElementIndex)
        } else {
            letDeviceEndSendPhotoState()
        }
    }

    /**
     * 结束设备照片发送状态，接收完或失败后需调用
     */
    fun letDeviceEndSendPhotoState() {
        viewModelScope.launch {
            if (SingleInstance.deviceManager.flowConnectorStateInfo.value.state == WmConnectState.BIND_SUCCESS) {
                UNIWatchMate.wmApps.appPhotoLibrary.letDeviceEndSendPhotoState()
                    .toObservable().asFlow().catch { }
                    .collect { result ->
                        LogUtils.d(TAG, "End send photo state result: $result")
                    }
            } else {
                LogUtils.d(TAG, "letDeviceEndSendPhotoState skip: not connected")
            }
        }
    }
}
