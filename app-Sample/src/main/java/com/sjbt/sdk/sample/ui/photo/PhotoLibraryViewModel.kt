package com.sjbt.sdk.sample.ui.photo

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.apps.WmVideoFrameInfo
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Date
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.di.internal.SingleInstance
import com.sjbt.sdk.sample.utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.Locale


// 定义UI状态类
data class PhotoLibraryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val photoCount: Int = 0,
    val newPhotoCount: Int = 0,
    val localPhotoCount: Int = 0
)

// 定义事件类
sealed class PhotoLibraryEvent {
    object StartSendPhoto : PhotoLibraryEvent()
    data class PhotoSendProgress(val progress: Int) : PhotoLibraryEvent()
    data class PhotoSendComplete(val success: Boolean, val imagePath: String) : PhotoLibraryEvent()
    data class PhotoReceived(val imagePath: String) : PhotoLibraryEvent()
    data class PhotoNamesReceived(val names: List<String>) : PhotoLibraryEvent()
    object DeviceNotConnected : PhotoLibraryEvent()
}

/***
 *相册导入图片流程：
 * 1 调用UNIWatchMate.wmApps.appPhotoLibrary.letDeviceSendPhotoNames()方法请求设备端发送设备图片列表数据。设备端返回 0 成功 时 ，设备 会进入 "发送图片状态"(其它功能受限)
 * 2 UNIWatchMate.wmApps.appPhotoLibrary.observeImgElementOrImgNames中可以收到设备端发送的图片名称列表，.frameType为2时，就是图片名称列表数据
 * 3 传输图片数据时，图片太大则需要分多次传输， 调用UNIWatchMate.wmApps.appPhotoLibrary.letDeviceSendPhotoElementCount(name)方法，获取该图片的分片数量 elementCount
 * 4 调用UNIWatchMate.wmApps.appPhotoLibrary.letDeviceSendPhotoByElmentIndex(index:Int)方法让设备发送 该图片的该index的图片数据，首个index的值为1
 * 5 UNIWatchMate.wmApps.appPhotoLibrary.observeImgElementOrImgNames中可以收到设备端发送的该图片该分片index的图片数据，.frameType为9时，就是图片分片数据
 * 6 获取到该图片所有的分片数据后，组合一起就是该图片的数据了。 之后根据图片名称列表 依次进行3-5步骤 获取其它图片的数据
 * 7 导入所有图片数据或者终止后，需要调用 调用UNIWatchMate.wmApps.appPhotoLibrary.letDeviceDelPhoto() 来结束 "发送图片状态"，否则设备端其它功能会受限
 */

class PhotoLibraryViewModel : ViewModel() {
    private val TAG = "PhotoLibraryViewModel"

    // 状态相关的Flow
    private val _uiState = MutableStateFlow(PhotoLibraryUiState())
    val uiState: StateFlow<PhotoLibraryUiState> = _uiState.asStateFlow()

    // 事件相关的Flow
    private val _events = MutableSharedFlow<PhotoLibraryEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<PhotoLibraryEvent> = _events

    // 照片数据
    private val _photoItems = MutableStateFlow<List<PhotoLibraryItem>>(emptyList())
    val photoItems: StateFlow<List<PhotoLibraryItem>> = _photoItems.asStateFlow()

    // 照片数据
    private val _importingIndex = MutableStateFlow<Int>(0)
    val importingIndex: StateFlow<Int> = _importingIndex.asStateFlow()


    // 导入状态
    private val _importingPhoto = MutableStateFlow(false)
    val importingPhoto: StateFlow<Boolean> = _importingPhoto.asStateFlow()

    // 选择模式状态
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    // 已选择的照片
    private val _selectedPhotos = MutableStateFlow<Set<PhotoItem>>(emptySet())
    val selectedPhotos: StateFlow<Set<PhotoItem>> = _selectedPhotos.asStateFlow()

    private val _photoNameList = MutableStateFlow<List<String>>(emptyList())
    val photoNameList: StateFlow<List<String>> = _photoNameList.asStateFlow()

    var curPhotoElementCount = 0
    var curPhotoElementIndex = 0
    var save2AlbumCount = 0
    var jpegDataOutStream = ByteArrayOutputStream()
    var lastSuccessTime: Long = 0

    // 用于记录未删除的设备端文件
    private val _failedToDeleteOnDevice = mutableListOf<String>()

    init {
        // 处理设备发送的图片数据
        viewModelScope.launch {
            //接收设备端图片的分片数据 及 设备端的图片名称列表数据
            //videoFrame.frameType为2时，数据为图片名称列表数据，为方法 letDeviceSendPhotoNames请求的结果
            //videoFrame.frameType为9时,数据为图片的分片数据，为方法 letDeviceSendPhotoByElmentIndex 请求的结果
            UNIWatchMate.wmApps.appPhotoLibrary.observeImgElementOrImgNames.asFlow()
                .collect { videoFrame ->
                    processVideoFrame(videoFrame)
                }
        }
    }

    /**
     * 处理从设备接收的视频帧数据、图片名称列表数据
     */
    private suspend fun processVideoFrame(videoFrame: WmVideoFrameInfo) {
        videoFrame?.let {
            Log.e("时间测试", "图片接收过程：${videoFrame.frameType}")
            LogUtils.e("时间测试 图片接收过程：${videoFrame.frameType}")

            when (videoFrame.frameType) {
                2 -> {
                    //收到图片名称列表
                    withContext(Dispatchers.Main) {
                        val newState = _uiState.value.copy(
                            isLoading = false
                        )
                        _uiState.value = newState
                    }
                    // 处理图片名称列表
                    videoFrame.frameData?.let { data ->
                        val deviceNames = String(data, Charsets.UTF_8)
                        LogUtils.i("时间测试 deviceNames = ${deviceNames}")
                        val namesArray =
                            deviceNames.split("|").filter { it.isNotEmpty() }.toMutableList()
                        if (namesArray.isEmpty() || (namesArray.size == 1 && namesArray[0].length < 6)) {
                            LogUtils.e("收到图片名称：为空")
                            withContext(Dispatchers.Main) {
                                ToastUtil.showToast(MyApplication.instance.getString(R.string.no_photo_import))
                            }
                            stopImport(true)
                            return
                        }
                        LogUtils.i("收到图片名称数量：${namesArray.size}")
                        // 将 phoneImgUrlFromDevice 中的 URL 转换为文件名进行比较
                        val existingFileNames = photoItems.value
                            .filterIsInstance<PhotoLibraryItem.Photo>()
                            .map {
                                it.photo.name
                            }

                        val names = namesArray.filter { !existingFileNames.contains(it) }
                        LogUtils.i("过滤之后的名称：${names}")

                        if (names.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                ToastUtil.showToast(MyApplication.instance.getString(R.string.no_photo_import))
                            }
                            stopImport(true)
                            return
                        }
                        _importingPhoto.value = true
                        _importingIndex.value = 0
                        _photoNameList.value = names
//                        _events.emit(PhotoLibraryEvent.StartSendPhoto)
                        startReceiveImg()
                    }
                }

                9 -> {
                    // 处理图片的分盘数据
                    videoFrame.frameData?.let { jpegData ->
                        if (curPhotoElementCount <= 0) {
                            LogUtils.e("收到图片 但是 curPhotoElementCount =$curPhotoElementCount")
                            return
                        }
                        jpegDataOutStream.write(jpegData)
                        if (curPhotoElementIndex < curPhotoElementCount) {
                            //全部收到了
                            LogUtils.i("curPhotoElementIndex < curPhotoElementCount : $curPhotoElementIndex < $curPhotoElementCount)")
                            curPhotoElementIndex++
                            startReceiveImg()
                            return
                        }
                        val takeTime = System.currentTimeMillis() - lastSuccessTime
                        LogUtils.e("收到完整图片 curPhotoElementCount =$curPhotoElementCount curPhotoElementIndex = $curPhotoElementIndex  takeTime = ${takeTime} ")
                        lastSuccessTime = System.currentTimeMillis()
                        curPhotoElementCount = 0
                        val photoNameList = _photoNameList.value
                        if (importingIndex.value >= photoNameList.size) {
                            LogUtils.i("index 错误 importingIndex.value = ${(importingIndex.value)}  photoNameList count = ${photoNameList.size}")
                            return
                        }
                        val photoName = "${photoNameList[importingIndex.value]}"

                        saveReceivedPhoto(jpegDataOutStream, photoName)

                        _importingIndex.value = (_importingIndex.value + 1)
                        startReceiveImg()

                    }
                }

                else -> {

                }
            }
        }
    }

    /**
     * 保存接收到的图片数据到文件
     */
    private fun saveReceivedPhoto(jpegDataOutStream: ByteArrayOutputStream, photoName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 创建设备图片目录
                val deviceImgDir =
                    File(MyApplication.instance.getExternalFilesDir(null), "deviceImg")
                if (!deviceImgDir.exists()) {
                    deviceImgDir.mkdirs()
                }

                // 创建图片文件
                val imageFile = File(deviceImgDir, photoName)

                // 写入图片数据
                FileOutputStream(imageFile).use { output ->
                    output.write(jpegDataOutStream.toByteArray())
                    output.flush()
                }

//                var urlList = photoNameList.value
//                var photoItemList =   _photoItems.value as MutableList
////                urlList.append(imageUrl)
//                _photoItems.value =  photoItemList
                loadPhotos()
                // 通知UI
                withContext(Dispatchers.Main) {
                    _events.emit(PhotoLibraryEvent.PhotoReceived(imageFile.absolutePath))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val newState = _uiState.value.copy(
                        errorMessage = "保存照片失败: ${e.message}"
                    )
                    _uiState.value = newState
                }
            }
        }
    }

    /**
     * 让设备发送照片名称，返回结果为：设备是否响应命令, 设备端返回 0 成功 时 ，设备 会进入 "发送图片状态"(其它功能受限)
     *
     * 设备发送的照片名称通过UNIWatchMate.wmApps.appPhotoLibrary.observeImgElementOrImgNames 方法监听，
     * processVideoFrame方法中videoFrame.frameType 为2则说明数据是设备端的照片名称列表
     */
    fun letDeviceSendPhotoNames() {
        lastSuccessTime = System.currentTimeMillis()
        viewModelScope.launch {

            if (SingleInstance.deviceManager.flowConnectorStateInfo.value.state == WmConnectState.BIND_SUCCESS) {
                withContext(Dispatchers.Main) {
                    val newState = _uiState.value.copy(
                        isLoading = true
                    )
                    _uiState.value = newState
                }
                UNIWatchMate.wmApps.appPhotoLibrary.letDeviceSendPhotoNames().toObservable()
                    .asFlow().catch { exception ->
                    // 处理异常情况
                    LogUtils.e(TAG, "发送照片名称请求失败: ${exception.message}")
                    withContext(Dispatchers.Main) {
                        val newState = _uiState.value.copy(
                            isLoading = false
                        )
                        _uiState.value = newState
                    }
                    withContext(Dispatchers.Main) {
                        ToastUtil.showToast("设备忙")
                    }
                }.collect { result ->
                    if (result != 0) {
                        // 发送请求失败
                        LogUtils.e(TAG, "发送照片名称请求失败: result = ${result}")
                        withContext(Dispatchers.Main) {
                            val newState = _uiState.value.copy(
                                isLoading = false
                            )
                            _uiState.value = newState
                        }
                        withContext(Dispatchers.Main) {
                            ToastUtil.showToast("设备忙")
                        }
                    }
                    // Success (result == 0) is handled by observeImgElementOrImgNames -> PhotoNamesReceived
                }
            } else {
                _events.emit(PhotoLibraryEvent.DeviceNotConnected)
            }
        }
    }

    /**
     * 获取设备指定名称照片的分片数量
     */
    fun letDeviceSendPhotoElementCount(name: String) {
        viewModelScope.launch {
            _events.emit(PhotoLibraryEvent.StartSendPhoto)
            if (SingleInstance.deviceManager.flowConnectorStateInfo.value.state == WmConnectState.BIND_SUCCESS) {
                UNIWatchMate.wmApps.appPhotoLibrary.letDeviceSendPhotoElementCount(name)
                    .toObservable().asFlow()
                    .catch {
                        LogUtils.e("letDeviceSendPhotoElementCount error ${it.message}")
                        withContext(Dispatchers.Main) {
                            val newState = _uiState.value.copy(
                                isLoading = false
                            )
                            _uiState.value = newState
                        }
                        withContext(Dispatchers.Main) {
                            ToastUtil.showToast("设备忙")
                        }
                        stopImport(true)
                    }
                    .collect { count ->
                        // The original onLetDeviceSendPhotoElementCount was empty.
                        // Log or handle 'count' if necessary.
                        jpegDataOutStream.reset()
                        curPhotoElementCount = count
                        if (curPhotoElementCount < 1) {
                            LogUtils.e("getDevicePhotoElementCount is 0")
                            stopImport(true)
                        } else {
                            curPhotoElementIndex = 1
                            curPhotoElementCount = count
                            LogUtils.d(
                                "PhotoLibraryViewModel",
                                "Device photo element count for $name: $count"
                            )
                            startReceiveImg()
                        }
                    }
            } else {
                _events.emit(PhotoLibraryEvent.DeviceNotConnected)
            }
        }
    }

//    /**
//     * 获取设备照片总数量
//     */
//    fun getDevicePhotoCount() {
//        viewModelScope.launch {
//            if (SingleInstance.deviceManager.flowConnectorStateInfo.value.state == WmConnectState.BIND_SUCCESS) {
//                DeviceConnectManagement.getInstance().getDevicePhotoCount().toObservable().asFlow().collect { count ->
//                    _uiState.value = _uiState.value.copy(newPhotoCount = count)
//                }
//            } else {
//                _events.emit(PhotoLibraryEvent.DeviceNotConnected)
//            }
//        }
//    }

    /**
     * 切换选择模式
     */
    fun toggleSelectionMode() {
        viewModelScope.launch {
            val currentMode = _isSelectionMode.value
            _isSelectionMode.value = !currentMode

            // 退出选择模式时清空选择
            if (!_isSelectionMode.value) {
                _selectedPhotos.value = emptySet()
            }
        }
    }

    /**
     * 选择或取消选择照片
     */
    fun togglePhotoSelection(photo: PhotoItem) {
        viewModelScope.launch {
            val currentSelected = _selectedPhotos.value.toMutableSet()
            if (currentSelected.contains(photo)) {
                currentSelected.remove(photo)
            } else {
                currentSelected.add(photo)
            }
            _selectedPhotos.value = currentSelected
        }
    }

    /**
     * 开始导入
     */
    fun startImport() {
        stopImport(false, false)
        if (_isSelectionMode.value) {
            toggleSelectionMode()
        }
        letDeviceSendPhotoNames()
    }

    /**
     * 停止导入
     */
    fun stopImport(letDeviceEnd: Boolean = true, manual: Boolean = false) {
        _importingPhoto.value = false
        _photoNameList.value = emptyList()
        curPhotoElementCount = 0
        curPhotoElementIndex = 0
        save2AlbumCount = 0
        _importingIndex.value = 0
        if (letDeviceEnd) {
            if (manual) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        val newState = _uiState.value.copy(
                            isLoading = true
                        )
                        _uiState.value = newState
                    }
                }
            }
            letDeviceEndSendPhotoState()
        }
    }

    /**
     * 让设备发送图片分片数据，index为分片索引
     * 调用letDeviceSendPhotoElementCount方法获取分片数量后，串行让设备发送该图片分片数据
     */
    fun letDeviceSendPhotoByElmentIndex(index: Int) {
        viewModelScope.launch {
            _events.emit(PhotoLibraryEvent.StartSendPhoto)
            if (SingleInstance.deviceManager.flowConnectorStateInfo.value.state == WmConnectState.BIND_SUCCESS) {
                UNIWatchMate.wmApps.appPhotoLibrary.letDeviceSendPhotoByElementIndex(index)
                    .toObservable().asFlow().catch {
                        it.printStackTrace()
                        LogUtils.e("letDeviceSendPhotoElement ${it.message}")
                        stopImport(true)
                    }.collect { result ->
                        LogUtils.d(
                            "PhotoLibraryViewModel",
                            "Send photo by element index result: $result"
                        )
                        if (result != 0) {
                            LogUtils.e("letDeviceSendPhotoElement rs != 0")
                            stopImport(true)
                        }
                        // 成功结果通过 observeImgElementOrImgNames 回调处理
                    }
            } else {
                _events.emit(PhotoLibraryEvent.DeviceNotConnected)
            }
        }
    }

    fun startReceiveImg() {
        val namesList = photoNameList.value

        if (importingIndex.value >= namesList.size) {
            if (_importingPhoto.value) {
                ToastUtil.showToast(MyApplication.instance.getString(R.string.import_complete))
                LogUtils.i("导入完成")
            }
            stopImport(true)
        } else {
            if (curPhotoElementCount > 0) {
                ("letDeviceSendPhotoElement curPhotoElementIndex = $curPhotoElementIndex")
                letDeviceSendPhotoByElmentIndex(curPhotoElementIndex)
            } else {
                val photoName = namesList[importingIndex.value]
                letDeviceSendPhotoElementCount(photoName)
            }
        }
    }

    /**
     * 结束设备照片发送状态，接受完图片后必须结束 照片发送状态，否则设备状态会有问题
     */
    fun letDeviceEndSendPhotoState() {
        viewModelScope.launch {
            if (SingleInstance.deviceManager.flowConnectorStateInfo.value.state == WmConnectState.BIND_SUCCESS) {
                UNIWatchMate.wmApps.appPhotoLibrary.letDeviceEndSendPhotoState()
                    .toObservable().asFlow().catch {
                        withContext(Dispatchers.Main) {
                            val newState = _uiState.value.copy(
                                isLoading = false
                            )
                            _uiState.value = newState
                        }
                    }.collect { result ->

                        withContext(Dispatchers.Main) {
                            Thread.sleep(500)
                            val newState = _uiState.value.copy(
                                isLoading = false
                            )
                            _uiState.value = newState
                        }
                        LogUtils.d("PhotoLibraryViewModel", "End send photo state result: $result")
                        _importingPhoto.value = false
                        // 可以在此处添加结束发送状态的事件通知
                        // 例如：_events.emit(PhotoLibraryEvent.PhotoSendStateEnded(result == 0))
                    }
            } else {
                _events.emit(PhotoLibraryEvent.DeviceNotConnected)
            }
        }
    }

    /**
     * 从文件名中解析日期时间
     * 文件名格式：XXX-250520-162841.jpeg
     * 其中250520表示25/05/20(日/月/年)，162841表示16:28:41(时:分:秒)
     */
    private fun parseDateFromFileName(fileName: String): Date? {
        try {
            // 使用正则表达式匹配日期和时间部分
            val regex = ".*-(\\d{6})-(\\d{6})\\..+".toRegex()
            val matchResult = regex.find(fileName) ?: return null

            val datePart = matchResult.groupValues[1] // 250520
            val timePart = matchResult.groupValues[2] // 162841

            if (datePart.length != 6 || timePart.length != 6) return null

            // 解析日期部分
            val day = datePart.substring(4, 6).toInt()
            val month = datePart.substring(2, 4).toInt() - 1 // 月份从0开始
            val year = 2000 + datePart.substring(0, 2).toInt() // 假设是21世纪

            // 解析时间部分
            val hour = timePart.substring(0, 2).toInt()
            val minute = timePart.substring(2, 4).toInt()
            val second = timePart.substring(4, 6).toInt()

            // 创建日历实例并设置时间
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, hour, minute, second)

//            LogUtils.d("Photo Date", "从文件名解析日期成功: ${fileName} -> ${calendar.time}")
            return calendar.time
        } catch (e: Exception) {
            LogUtils.e("Photo Date", "从文件名解析日期失败: ${fileName}, ${e.message}")
            return null
        }
    }

    /**
     * 加载照片
     */
    fun loadPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deviceImgDir =
                    File(MyApplication.instance.getExternalFilesDir(null), "deviceImg")
                if (deviceImgDir.exists()) {
                    val imageFiles = deviceImgDir.listFiles { file ->
                        file.isFile && (file.name.endsWith(".jpg", true)
                                || file.name.endsWith(".jpeg", true)
                                || file.name.endsWith(".png", true))
                    }

                    val photoItems = imageFiles?.map { file ->
                        // 优先从文件名解析日期，如果解析失败则使用文件修改时间
                        val dateFromFileName = parseDateFromFileName(file.name)
                        PhotoItem(
                            id = "ID${file.name}",
                            uri = Uri.fromFile(file),
                            name = file.name,
                            date = dateFromFileName ?: Date(file.lastModified())
                        )
                    }?.sortedByDescending { it.date } ?: emptyList()

                    // 更新本地照片数量
                    val newState = _uiState.value.copy(localPhotoCount = photoItems.size)
                    _uiState.value = newState

                    LogUtils.d("照片加载", "共加载 ${photoItems.size} 张照片")

                    // 按日期分组
                    val groupedPhotos = photoItems.groupBy { photo ->
                        Calendar.getInstance().apply {
                            time = photo.date
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                    }

                    LogUtils.d("照片分组", "共分为 ${groupedPhotos.size} 个日期组")

                    // 转换为列表项
                    val newItems = mutableListOf<PhotoLibraryItem>()
                    groupedPhotos.entries.sortedByDescending { it.key }.forEach { (date, photos) ->
                        // 添加日期header
                        newItems.add(PhotoLibraryItem.Header(date))
                        LogUtils.d(
                            "照片分组",
                            "日期: ${
                                SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(date)
                            }, 照片数: ${photos.size}"
                        )

                        // 添加该日期下的所有照片
                        photos.forEach { photo ->
                            newItems.add(PhotoLibraryItem.Photo(photo))
                        }
                    }

                    LogUtils.d(
                        "照片列表",
                        "列表项总数: ${newItems.size}, 其中Header: ${newItems.count { it is PhotoLibraryItem.Header }}, 照片: ${newItems.count { it is PhotoLibraryItem.Photo }}"
                    )

                    // 更新UI
                    withContext(Dispatchers.Main) {
                        _photoItems.value = newItems
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        LogUtils.d("照片列表", "空列表")

                        _photoItems.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val newState = _uiState.value.copy(
                        errorMessage = "加载照片失败: ${e.message}"
                    )
                    _uiState.value = newState
                }
            }
        }
    }


    /**
     * 删除选中的照片
     * UNIWatchMate.wmApps.appPhotoLibrary.letDeviceDelPhoto方法通过名称删除设备端照片，删除多个照片时需要串行执行
     */
    fun deleteSelectedPhotos() {
        viewModelScope.launch(Dispatchers.IO) {

            if (_importingPhoto.value) {
                ToastUtil.showToast(MyApplication.instance.getString(R.string.importing_dot))
                return@launch
            }
            withContext(Dispatchers.Main) {
                val newState = _uiState.value.copy(
                    isLoading = true
                )
                _uiState.value = newState
            }
            val photosToDelete = _selectedPhotos.value.toList()
            var successDeletedCount = 0
            // 使用顺序处理而不是并行处理
            for (photo in photosToDelete) {
                try {
                    // 删除本地文件
                    val file = File(photo.uri.path ?: "")
                    if (file.exists()) {
                        file.delete()
                    }
                    // 删除设备端对应的图片，必须等上一个完成才能进行下一个
                    if (SingleInstance.deviceManager.flowConnectorStateInfo.value.state == WmConnectState.BIND_SUCCESS) {
                        // 使用Single.await()等待操作完成并获取结果
                        val result =
                            UNIWatchMate.wmApps.appPhotoLibrary.letDeviceDelPhoto(photo.name)
                                .toObservable().firstOrError().await()

                        LogUtils.d(TAG, "删除设备端图片结果：${photo.name}, result=${result}")

                        if (result != 0) {
                            // 如果删除失败，记录日志但继续处理下一个
                            LogUtils.e(TAG, "删除设备端图片失败：${photo.name}, result=${result}")
                            withContext(Dispatchers.Main) {
                                ToastUtil.showToast(MyApplication.instance.getString(R.string.fail_del_of_device))
                            }
                            _failedToDeleteOnDevice.add(photo.name)
                            break
                        } else {
                            successDeletedCount++
                        }
                    } else {
                        //设备端未连接状态的话，记录未从设备端删除的文件
                        LogUtils.e(TAG, "删除设备端图片失败：设备未连接")
                        _failedToDeleteOnDevice.add(photo.name) // 记录未能从设备删除的文件
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LogUtils.e(TAG, "删除照片失败: ${e.message}")
                    _failedToDeleteOnDevice.add(photo.name) // 记录异常导致未删除的文件
                }
            }

            // 清空选中的照片并刷新
            withContext(Dispatchers.Main) {
                _selectedPhotos.value = emptySet()
                _isSelectionMode.value = false
                loadPhotos()
            }
            withContext(Dispatchers.Main) {
                val newState = _uiState.value.copy(
                    isLoading = false
                )
                _uiState.value = newState
            }
        }
    }

    /**
     * 删除选中的照片
     */
    fun deletePhotos(photo: PhotoItem) {
        viewModelScope.launch(Dispatchers.IO) {

            // 使用顺序处理而不是并行处理
            try {
                // 删除本地文件
                val file = File(photo.uri.path ?: "")
                // 删除设备端对应的图片，必须等上一个完成才能进行下一个
                if (SingleInstance.deviceManager.flowConnectorStateInfo.value.state == WmConnectState.BIND_SUCCESS) {
                    // 使用Single.await()等待操作完成并获取结果
                    val result = UNIWatchMate.wmApps.appPhotoLibrary.letDeviceDelPhoto(photo.name)
                        .toObservable().firstOrError().await()

                    LogUtils.d(TAG, "删除设备端图片结果：${photo.name}, result=${result}")

                    if (result != 0) {
                        // 如果删除失败，记录日志但继续处理下一个
                        LogUtils.e(TAG, "删除设备端图片失败：${photo.name}, result=${result}")
                        _failedToDeleteOnDevice.add(photo.name)
                        withContext(Dispatchers.Main) {
                            ToastUtil.showToast(MyApplication.instance.getString(R.string.fail_del_of_device))
                        }
                    } else {
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                } else {
                    //设备端未连接状态的话，记录未从设备端删除的文件
                    LogUtils.e(TAG, "删除设备端图片失败：设备未连接")
                    _failedToDeleteOnDevice.add(photo.name) // 记录未能从设备删除的文件
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.e(TAG, "删除照片失败: ${e.message}")
                _failedToDeleteOnDevice.add(photo.name) // 记录异常导致未删除的文件
            }
            // 清空选中的照片并刷新
            withContext(Dispatchers.Main) {
                loadPhotos()
            }

        }
    }
}


// 为Adapter定义的PhotoLibraryItem
sealed class PhotoLibraryItem {
    data class Header(val date: Date) : PhotoLibraryItem()
    data class Photo(val photo: PhotoItem) : PhotoLibraryItem()
} 