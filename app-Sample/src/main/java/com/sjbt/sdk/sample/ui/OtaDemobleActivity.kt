package com.lensmoo.business.ui.device

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ota.sdk.core.OtaTransferManager
import com.ota.sdk.api.IBluetoothCommunicator
import com.ota.sdk.api.ILogger
import com.ota.sdk.api.IOtaMessageListener
import com.ota.sdk.model.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import com.blankj.utilcode.util.LogUtils
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.ui.ble.BleConnectionHolder
import com.polidea.rxandroidble3.RxBleConnection
import kotlinx.coroutines.*
import java.io.File

/**
 * OTA SDK 演示 Activity（简化版）
 *
 * 功能：
 * 1. 选择 .up 或 .upex 文件
 * 2. 调用 OTA SDK 进行传输
 * 3. 显示传输状态和进度
 *
 * 【重要说明】
 * 连接状态与收发均使用 BLE（BleConnectionHolder / RxBleConnection），
 * 从 BLE 搜索页连接成功后跳转进入，通过特征 0xFFF1 发送、0xFFF2/0xFFF3 接收。
 *
 * 用户只需要：
 * 1. 在收到蓝牙数据时调用 onBluetoothDataReceived(rawData)
 *
 * 【集成步骤】
 * 1. 复制这个文件到您的项目
 * 2. 在您的蓝牙接收代码中调用 otaDemoActivity?.onBluetoothDataReceived(rawData)
 * 3. 运行并测试 OTA 功能
 */
class OtaDemobleActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "OtaDemobleActivity"
        private const val REQUEST_CODE_SELECT_FILE = 1001
        const val EXTRA_DEVICE_ADDRESS = "device_address"
        const val EXTRA_DEVICE_NAME = "device_name"
    }

    /** 从 BLE 搜索页传入的设备地址，用于从 BleConnectionHolder 取连接 */
    private var deviceAddress: String? = null

    /** 当前 BLE 连接（与 BleConnectionHolder 中一致），用于发送与接收 */
    private var bleConnection: RxBleConnection? = null

    /** 特征通知订阅，onDestroy 时 dispose */
    private val notificationDisposables = CompositeDisposable()

    // UI 组件
    private lateinit var btnSelectFile: Button
    private lateinit var btnStartTransfer: Button
    private lateinit var btnCancelTransfer: Button
    private lateinit var tvSelectedFile: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvLog: TextView
    private lateinit var progressBar: ProgressBar

    // OTA SDK 相关
    private var otaManager: OtaTransferManager? = null
    private var messageListener: IOtaMessageListener? = null
    private var transferDisposable: Disposable? = null

    // 选中的文件
    private var selectedFile: File? = null
    private var selectedFileType: OtaFileType? = null

    // 协程作用域（用于后台处理蓝牙数据）
    // 使用单线程调度器，保证数据按接收顺序处理，避免 OTA 状态机混乱
    private val dataProcessingScope = CoroutineScope(
        Dispatchers.IO.limitedParallelism(1) + SupervisorJob()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ota_demo_ble)

        deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS)
        bleConnection = deviceAddress?.let { BleConnectionHolder.getConnection(it) }
        if (deviceAddress == null || bleConnection == null) {
            Toast.makeText(this, "请先在 BLE 搜索页连接设备", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        intent.getStringExtra(EXTRA_DEVICE_NAME)?.takeIf { it.isNotBlank() }?.let {
            title = "OTA: $it"
        }

        initViews()
        initOtaSdk()
        setupBleNotifications()
        setupClickListeners()
    }

    /**
     * 订阅设备→APP 特征与错误码特征，收到数据后交给 onBluetoothDataReceived。
     */
    private fun setupBleNotifications() {
        val conn = bleConnection ?: return
        val charDeviceToApp = BleConnectionHolder.CHAR_UUID_DEVICE_TO_APP
//        val charError = BleConnectionHolder.CHAR_UUID_ERROR
//        val charError2 = BleConnectionHolder.CHAR_UUID_ERROR2
        notificationDisposables.add(
            conn.setupNotification(charDeviceToApp)
                .flatMap { it }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { bytes -> onBluetoothDataReceived(bytes) },
                    { t -> Log.e(TAG, "通知(设备→APP)异常", t); appendLog("通知异常: ${t.message}") }
                )
        )
//        notificationDisposables.add(
//            conn.setupNotification(charError)
//                .flatMap { it }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                    { bytes -> onBluetoothDataReceived(bytes) },
//                    { t -> Log.e(TAG, "通知(错误码)异常", t); appendLog("错误码通知异常: ${t.message}") }
//                )
//        )
//        notificationDisposables.add(
//                conn.setupNotification(charError2)
//                        .flatMap { it }
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(
//                                { bytes -> onBluetoothDataReceived(bytes) },
//                                { t -> Log.e(TAG, "通知(错误码)异常", t); appendLog("错误码通知异常: ${t.message}") }
//                        )
//        )
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        btnSelectFile = findViewById(R.id.btnSelectFileBle)
        btnStartTransfer = findViewById(R.id.btnStartTransferBle)
        btnCancelTransfer = findViewById(R.id.btnCancelTransferBle)
        tvSelectedFile = findViewById(R.id.tvSelectedFileBle)
        tvStatus = findViewById(R.id.tvStatusBle)
        tvProgress = findViewById(R.id.tvProgressBle)
        tvLog = findViewById(R.id.tvLogBle)
        progressBar = findViewById(R.id.progressBarBle)
    }

    /**
     * 初始化 OTA SDK
     */
    private fun initOtaSdk() {
        // OTA SDK 已内置 CRC16-CCITT 算法，与原 SDK 的 Native 实现完全一致
        // 无需额外配置，SDK 会自动使用内置实现
        appendLog("✓ OTA SDK 使用内置 CRC16-CCITT 算法")

        // 创建蓝牙通信接口实现
        // 注意：这里需要客户实现实际的蓝牙通信逻辑
        val communicator = createBluetoothCommunicator()

        // 创建日志接口实现
        val logger = object : ILogger {
            override fun logD(tag: String, message: String, location: String?) {
                val msg = if (location != null) "[$location] $message" else message
                Log.d(tag, msg)
                appendLog("D/$tag: $msg")
            }

            override fun logI(tag: String, message: String, location: String?) {
                val msg = if (location != null) "[$location] $message" else message
                Log.i(tag, msg)
                appendLog("I/$tag: $msg")
            }

            override fun logE(tag: String, message: String, location: String?, throwable: Throwable?) {
                val msg = if (location != null) "[$location] $message" else message
                Log.e(tag, msg, throwable)
                appendLog("E/$tag: $msg")
                throwable?.let {
                    appendLog("  ↳ ${it.message}")
                }
            }

            override fun logW(tag: String, message: String, location: String?) {
                val msg = if (location != null) "[$location] $message" else message
                Log.w(tag, msg)
                appendLog("W/$tag: $msg")
            }
        }

        // 创建 OTA 管理器
        otaManager = OtaTransferManager(
            context = applicationContext,
            communicator = communicator,
            logger = logger
        )

        // 获取消息监听器
        messageListener = otaManager?.getMessageListener()


        appendLog("OTA SDK 初始化完成")

    }

    /**
     * 创建蓝牙通信接口实现：使用 BLE 连接（BleConnectionHolder）写特征 0xFFF1 发送数据。
     */
    private fun createBluetoothCommunicator(): IBluetoothCommunicator {
        return object : IBluetoothCommunicator {
            override fun sendMessage(data: ByteArray) {
                if (data.isEmpty()) {
                    appendLog("数据为空，无法发送")
                    Log.e(TAG, "数据为空，无法发送")
                    return
                }
                val conn = bleConnection
                if (conn == null) {
                    appendLog("✗ BLE 连接已断开，无法发送")
                    Log.e(TAG, "BLE 连接为 null")
                    return
                }
                val sendTime = System.currentTimeMillis()
                conn.writeCharacteristic(BleConnectionHolder.CHAR_UUID_APP_TO_DEVICE, data)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            val elapsed = System.currentTimeMillis() - sendTime
                            appendLog("✓ 发送成功 ${data.size} 字节 (${elapsed}ms)")
                            if (data.size <= 200) {
                                appendLog("→ ${data.joinToString(" ") { "%02X".format(it) }}")
                            }
                            LogUtils.d(TAG, "发送消息成功: ${data.size} 字节")
                        },
                        { t ->
                            appendLog("✗ 发送失败: ${t.message}")
                            Log.e(TAG, "发送消息失败", t)
                            LogUtils.e(TAG, "发送消息失败: ${t.message}")
                        }
                    )
            }
        }
    }

    /**
     * 设置点击监听器
     */
    private fun setupClickListeners() {
        // 选择文件按钮
        btnSelectFile.setOnClickListener {
            selectFile()
        }

        // 开始传输按钮
        btnStartTransfer.setOnClickListener {
            startTransfer()
        }

        // 取消传输按钮
        btnCancelTransfer.setOnClickListener {
            cancelTransfer()
        }
    }

    /**
     * 选择文件
     */
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/octet-stream", "*/*"))
        }

        try {
            startActivityForResult(
                Intent.createChooser(intent, "选择 OTA 文件"),
                REQUEST_CODE_SELECT_FILE
            )
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开文件选择器: ${e.message}", Toast.LENGTH_SHORT).show()
            appendLog("选择文件失败: ${e.message}")
        }
    }

    /**
     * 处理文件选择结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                handleSelectedFile(uri)
            }
        }
    }

    /**
     * 处理选中的文件
     */
    private fun handleSelectedFile(uri: Uri) {
        try {
            // 获取文件路径
            val filePath = getFilePathFromUri(uri)
            if (filePath == null) {
                Toast.makeText(this, "无法获取文件路径", Toast.LENGTH_SHORT).show()
                appendLog("无法获取文件路径")
                return
            }

            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show()
                appendLog("文件不存在: $filePath")
                return
            }

            // 判断文件类型
            val fileName = file.name.lowercase()
            val fileType = when {
                fileName.endsWith(".up") -> OtaFileType.OTA
                fileName.endsWith(".upex") -> OtaFileType.OTA_UPEX
                else -> {
                    Toast.makeText(this, "不支持的文件类型，请选择 .up 或 .upex 文件", Toast.LENGTH_SHORT).show()
                    appendLog("不支持的文件类型: $fileName")
                    return
                }
            }

            selectedFile = file
            selectedFileType = fileType

            // 更新 UI
            tvSelectedFile.text = "已选择: ${file.name}\n路径: $filePath\n大小: ${formatFileSize(file.length())}"
            btnStartTransfer.isEnabled = true

            appendLog("文件选择成功: ${file.name}")
            appendLog("文件类型: $fileType")
            appendLog("文件大小: ${formatFileSize(file.length())}")

        } catch (e: Exception) {
            Toast.makeText(this, "处理文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
            appendLog("处理文件失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 从 URI 获取文件路径
     */
    private fun getFilePathFromUri(uri: Uri): String? {
        var filePath: String? = null

        try {
            // 尝试通过 content resolver 获取
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0) {
                        val displayName = cursor.getString(columnIndex)
                        // 尝试从 URI 获取实际路径
                        if (uri.scheme == "file") {
                            filePath = uri.path
                        } else {
                            // 对于 content:// URI，需要复制到临时文件
                            val tempFile = File(cacheDir, displayName)
                            contentResolver.openInputStream(uri)?.use { input ->
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            filePath = tempFile.absolutePath
                        }
                    }
                }
            }

            // 如果还是无法获取，尝试直接使用 URI path
            if (filePath == null && uri.scheme == "file") {
                filePath = uri.path
            }

        } catch (e: Exception) {
            Log.e(TAG, "获取文件路径失败", e)
        }

        return filePath
    }

    /**
     * 开始传输
     */
    private fun startTransfer() {
        val file = selectedFile
        val fileType = selectedFileType

        if (file == null || fileType == null) {
            Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show()
            return
        }

        if (!file.exists()) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show()
            return
        }

        // 检查 BLE 连接状态（使用 BleConnectionHolder 中的连接）
        if (bleConnection == null || deviceAddress == null || BleConnectionHolder.getConnection(deviceAddress!!) == null) {
            Toast.makeText(
                this,
                "蓝牙未连接，请返回 BLE 搜索页重新连接设备",
                Toast.LENGTH_LONG
            ).show()
            appendLog("✗ OTA 传输终止：BLE 未连接")
            return
        }

        appendLog("开始 OTA 传输...")
        appendLog("文件: ${file.name}")
        appendLog("类型: $fileType")

        // 更新 UI
        btnStartTransfer.isEnabled = false
        btnCancelTransfer.isEnabled = true
        btnSelectFile.isEnabled = false
        tvStatus.text = "准备传输..."
        progressBar.progress = 0
        tvProgress.text = "0%"

        // 开始传输
        transferDisposable = otaManager?.startTransfer(fileType, listOf(file), bleTransmission = true)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { state ->
                    handleTransferState(state)
                },
                { error ->
                    handleTransferError(error)
                }
            )
    }

    /**
     * 处理传输状态
     */
    private fun handleTransferState(state: OtaTransferState) {
        when (state.state) {
            State.PRE_TRANSFER -> {
                tvStatus.text = "准备传输..."
                appendLog("状态: 准备传输")
            }
            State.TRANSFERRING -> {
                tvStatus.text = "传输中..."
                progressBar.progress = state.progress
                tvProgress.text = "${state.progress}%"
                appendLog("状态: 传输中 - ${state.progress}%")
            }
            State.SUCCESS -> {
                if (state.index >= state.total) {
                    tvStatus.text = "传输成功！"
                    progressBar.progress = 100
                    tvProgress.text = "100%"
                    appendLog("状态: 传输成功！")
                    Toast.makeText(this, "传输成功！", Toast.LENGTH_SHORT).show()

                    // 重置 UI
                    btnStartTransfer.isEnabled = true
                    btnCancelTransfer.isEnabled = false
                    btnSelectFile.isEnabled = true
                } else {
                    tvStatus.text = "文件 ${state.index + 1}/${state.total} 传输完成"
                    appendLog("状态: 文件 ${state.index + 1}/${state.total} 传输完成")
                }
            }
            State.FAIL -> {
                tvStatus.text = "传输失败"
                appendLog("状态: 传输失败")
                Toast.makeText(this, "传输失败", Toast.LENGTH_SHORT).show()

                // 重置 UI
                btnStartTransfer.isEnabled = true
                btnCancelTransfer.isEnabled = false
                btnSelectFile.isEnabled = true
            }
        }
    }

    /**
     * 处理传输错误
     */
    private fun handleTransferError(error: Throwable) {
        val errorMsg = when (error) {
            is OtaException -> {
                val msg = when (error.error) {
                    OtaError.ERROR_BUSY -> "设备忙碌"
                    OtaError.ERROR_LOW_MEMORY -> "设备内存不足"
                    OtaError.ERROR_LOW_POWER -> "设备电量不足"
                    OtaError.ERROR_TIME_OUT -> "传输超时"
                    OtaError.ERROR_FILE_EXCEPTION -> "文件异常"
                    OtaError.ERROR_DISCONNECT -> "设备断开连接"
                    else -> "传输失败: ${error.message}"
                }
                appendLog("传输错误: ${error.error} - $msg")
                msg
            }
            else -> {
                appendLog("未知错误: ${error.message}")
                "未知错误: ${error.message}"
            }
        }

        tvStatus.text = errorMsg
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()

        // 重置 UI
        btnStartTransfer.isEnabled = true
        btnCancelTransfer.isEnabled = false
        btnSelectFile.isEnabled = true
    }

    /**
     * 取消传输（显示确认弹窗）
     */
    private fun cancelTransfer() {
        // 显示确认弹窗
        AlertDialog.Builder(this)
            .setTitle("确认取消") // 设置对话框标题
            .setMessage("确定要取消当前的 OTA 传输吗？") // 设置对话框消息内容
            .setNegativeButton("取消") { dialog, _ ->
                // 用户点击"取消"按钮，关闭对话框，不执行任何操作
                dialog.dismiss()
            }
            .setPositiveButton("确定") { dialog, _ ->
                // 用户点击"确定"按钮，执行取消传输操作
                doCancelTransfer()
                dialog.dismiss()
            }
            .setCancelable(true) // 允许点击对话框外部区域关闭对话框
            .show() // 显示对话框
    }

    /**
     * 执行取消传输操作
     */
    private fun doCancelTransfer() {
        appendLog("用户确认取消传输...")

        // 立即重置 UI，让用户可以再次点击开始传输
        resetUIAfterCancel()

        // 发送取消命令到设备（异步操作，不影响 UI 重置）
        otaManager?.cancelTransfer()
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { success ->
                    appendLog("传输已取消")
                    tvStatus.text = "传输已取消"
                    Toast.makeText(this, "传输已取消", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    appendLog("取消传输失败: ${error.message}")
                    Toast.makeText(this, "取消传输失败", Toast.LENGTH_SHORT).show()
                }
            )
    }

    /**
     * 取消传输后重置 UI
     */
    private fun resetUIAfterCancel() {
        // 重置按钮状态
        btnStartTransfer.isEnabled = true
        btnCancelTransfer.isEnabled = false
        btnSelectFile.isEnabled = true

        // 清理传输订阅
        transferDisposable?.dispose()
        transferDisposable = null

        // 重置进度
        progressBar.progress = 0
        tvProgress.text = "0%"
    }


    /**
     * 接收蓝牙原始数据并转发给 OTA SDK
     *
     * 【使用说明】
     * 这是 Demo 的核心方法，当您的蓝牙通信模块收到原始数据时，
     * 请调用这个方法将数据传给 OTA SDK。
     *
     * 【线程安全】
     * 此方法会在后台单线程（IO Dispatcher）中按顺序处理数据，避免阻塞主线程。
     * 使用单线程保证数据按接收顺序处理，避免 OTA 状态机混乱。
     * 日志输出会自动切换到主线程更新 UI。
     *
     * @param rawData 完整的协议包数据（包含协议头、命令ID、payload等）
     *
     * 【数据格式】
     * 原始数据应该是完整的蓝牙协议包，格式如下：
     * - 字节 0: head（协议头）
     * - 字节 1: cmdOrder（命令序号）
     * - 字节 2-3: cmdId（命令ID，小端序）
     * - 字节 4-15: 协议字段（根据具体协议）
     * - 字节 16+: payload（数据载荷）
     *
     * 【集成示例】
     * ```kotlin
     * // 在您的蓝牙接收处理代码中
     * fun onBleDataReceived(data: ByteArray) {
     *     // 1. 先处理您自己的业务逻辑
     *     handleMyOwnLogic(data)
     *
     *     // 2. 转发给 OTA Demo（会自动在后台单线程按顺序处理）
     *     otaDemoActivity?.onBluetoothDataReceived(data)
     * }
     * ```
     */
    fun onBluetoothDataReceived(rawData: ByteArray) {
        // 在后台单线程中按顺序处理数据，避免阻塞调用线程和状态机混乱
        dataProcessingScope.launch {
            try {
                // 直接传给 OTA SDK，SDK 内部会自动判断是否是 OTA 消息并处理
                val isOtaMessage = messageListener?.onMessageReceived(rawData) ?: false

                if (isOtaMessage) {
                    // 在主线程更新 UI（日志）
                    withContext(Dispatchers.Main) {
                        appendLog("✓ 收到 OTA 消息: ${rawData.size} 字节")
                        if (rawData.size<40){
                            appendLog("✓ 收到 OTA 消息: hex=${rawData.joinToString(" ") { "%02X".format(it) }}")
                        }
                    }
                    Log.d(TAG, "收到 OTA 消息: ${rawData.size} 字节, hex=${rawData.joinToString(" ") { "%02X".format(it) }}")
                } else {
                    // 不是 OTA 消息，使用者可以自行处理
                    Log.d(TAG, "收到非 OTA 消息: ${rawData.size} 字节")
                }

            } catch (e: Exception) {
                Log.e(TAG, "处理蓝牙消息失败: ${e.message}", e)
                // 在主线程更新 UI（错误日志）
                withContext(Dispatchers.Main) {
                    appendLog("✗ 处理蓝牙消息失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 添加日志
     */
    private fun appendLog(message: String) {
        runOnUiThread {
            val timestamp = System.currentTimeMillis()
            val timeStr = android.text.format.DateFormat.format("HH:mm:ss", timestamp)
            tvLog.append("[$timeStr] $message\n")

            // 自动滚动到底部
            val scrollView = tvLog.parent as? android.widget.ScrollView
            scrollView?.post {
                scrollView.fullScroll(android.view.View.FOCUS_DOWN)
            }
        }
        LogUtils.e(TAG,message)
    }

    /**
     * 格式化文件大小
     */
    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$size B"
        }
    }

    /**
     * ByteArray 转十六进制字符串（用于日志）
     */
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationDisposables.clear()
        transferDisposable?.dispose()
        dataProcessingScope.cancel()
    }
}

