package com.lensmoo.business.ui.device

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ota.sdk.core.OtaTransferManager
import com.ota.sdk.api.IBluetoothCommunicator
import com.ota.sdk.api.ILogger
import com.ota.sdk.api.IOtaMessageListener
import com.ota.sdk.model.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import com.sjbt.sdk.sample.R
import kotlinx.coroutines.*

/**
 * OTA SDK 演示 Activity（SPP 直连版）
 *
 * 流程：
 * 1. 初始化：与已配对设备列表中第一个设备建立标准 SPP 连接（系统只配对一个绅聚眼镜）
 * 2. getDataOutputStream() 提供输出流给 OTA 发送
 * 3. 选择升级文件，进行 OTA 升级
 *
 * 连接方式：标准 SPP，UUID 00001101-0000-1000-8000-00805F9B34F7
 * 蓝牙数据由本 Activity 内 SPP 读循环接收后交给 OTA SDK 处理。
 */
class OtaDemo2Activity : AppCompatActivity() {

    companion object {
        private const val TAG = "OtaDemo2Activity"
        private const val REQUEST_CODE_SELECT_FILE = 1001
        private const val REQUEST_CODE_BLUETOOTH_CONNECT = 1002
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34F7")
    }

    // 蓝牙 SPP 连接
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var btDevice: BluetoothDevice? = null
    private var socket: BluetoothSocket? = null
    private var outStream: OutputStream? = null
    private var inStream: InputStream? = null
    private val sppScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // UI 组件
    private lateinit var btnSelectFile: Button
    private lateinit var btnStartTransfer: Button
    private lateinit var btnCancelTransfer: Button
    private lateinit var tvConnectionStatus: TextView
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
        setContentView(R.layout.activity_ota_demo2)

        initViews()
        initOtaSdk()
        setupClickListeners()

        if (hasRequiredPermissions()) {
            initSppConnection()
        } else {
            updateConnectionStatusDisconnected("未连接（需要蓝牙、位置与存储权限）")
            appendLog("申请蓝牙、位置与存储权限以连接设备与选择文件...")
            ActivityCompat.requestPermissions(
                this,
                getRequiredPermissions(),
                REQUEST_CODE_BLUETOOTH_CONNECT
            )
        }
    }

    /**
     * 按系统版本返回所需权限：蓝牙、位置、存储。
     * - Android 13+ (API 33+)：蓝牙+位置+READ_MEDIA_*。
     * - Android 12 (API 31-32)：蓝牙+位置+READ/WRITE_EXTERNAL_STORAGE。
     * - Android 11 及以下：位置+READ/WRITE_EXTERNAL_STORAGE。
     */
    private fun getRequiredPermissions(): Array<String> {
        val storage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val bluetoothAndLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        return bluetoothAndLocation + storage
    }

    private fun hasRequiredPermissions(): Boolean {
        if (!hasBluetoothAndLocationPermissions()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasBluetoothAndLocationPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) return false
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_BLUETOOTH_CONNECT) {
            val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.indexOf(Manifest.permission.BLUETOOTH_CONNECT).let { idx ->
                    idx >= 0 && idx < grantResults.size && grantResults[idx] == PackageManager.PERMISSION_GRANTED
                }
            } else {
                grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            }
            if (granted) {
                appendLog("已获得所需权限，开始连接...")
                initSppConnection()
            } else {
                updateConnectionStatusDisconnected("未连接（需要蓝牙、位置与存储权限）")
                appendLog("✗ 未授予所需权限，无法连接设备")
                Toast.makeText(this, "需要蓝牙、位置与存储权限才能连接设备与选择文件", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 初始化 SPP 连接：与已配对设备列表中第一个设备连接（标准 SPP），并启动读线程。
     */
    private fun initSppConnection() {
        sppScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(this@OtaDemo2Activity, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED
                ) {
                    withContext(Dispatchers.Main) {
                        updateConnectionStatusDisconnected("未连接（需要蓝牙权限）")
                        appendLog("✗ 需要蓝牙权限 BLUETOOTH_CONNECT 才能连接设备")
                        Toast.makeText(this@OtaDemo2Activity, "请授予蓝牙权限", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val manager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                mBluetoothAdapter = manager?.adapter
                if (mBluetoothAdapter == null) {
                    withContext(Dispatchers.Main) {
                        updateConnectionStatusDisconnected("未连接（蓝牙不可用）")
                        appendLog("✗ 蓝牙不可用")
                    }
                    return@launch
                }
                val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter?.bondedDevices
                val deviceList = pairedDevices?.toList() ?: emptyList()
                Log.d(TAG, "Bluetooth pairedDevices size: ${deviceList.size}")
                if (deviceList.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        updateConnectionStatusDisconnected("未连接（请先配对设备）")
                        appendLog("✗ 没有已配对的设备，请先配对绅聚眼镜")
                    }
                    return@launch
                }
                var connected = false
                for (device in deviceList) {
                    if (!isActive) break
                    var trySocket: BluetoothSocket? = null
                    try {
                        withContext(Dispatchers.Main) {
                            appendLog("尝试连接: ${device.name ?: "未知"} (${device.address})")
                        }
                        Log.d(TAG, "try connect address:${device.address} name:${device.name}")
                        trySocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
                        trySocket.connect()
                        socket = trySocket
                        btDevice = device
                        outStream = socket?.outputStream
                        inStream = socket?.inputStream
                        connected = true
                        withContext(Dispatchers.Main) {
                            updateConnectionStatusConnected(btDevice?.name, btDevice?.address)
                            appendLog("✓ SPP 已连接: ${btDevice?.name ?: btDevice?.address}")
                        }
                        startReadLoop(sppScope)
                        break
                    } catch (e: Throwable) {
                        Log.w(TAG, "连接失败 ${device.address}: ${e.message}")
                        try {
                            trySocket?.close()
                        } catch (_: Exception) { }
                        trySocket = null
                    }
                }
                if (!connected) {
                    withContext(Dispatchers.Main) {
                        updateConnectionStatusDisconnected("未连接（已尝试 ${deviceList.size} 个设备均失败）")
                        appendLog("✗ 已遍历 ${deviceList.size} 个已配对设备，均无法建立 SPP 连接")
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SPP 连接失败：权限被拒绝", e)
                withContext(Dispatchers.Main) {
                    updateConnectionStatusDisconnected("未连接（请授予蓝牙权限）")
                    appendLog("✗ SPP 连接失败：请授予蓝牙权限")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "SPP 连接失败", t)
                withContext(Dispatchers.Main) {
                    updateConnectionStatusDisconnected("未连接（连接失败: ${t.message}）")
                    appendLog("✗ SPP 连接失败: ${t.message}")
                }
            }
        }
    }

    /**
     * 在后台循环读取 SPP 数据，收到数据后交给 OTA 处理。
     */
    private suspend fun startReadLoop(scope: CoroutineScope) {
        val buffer = ByteArray(1024 * 1024)
        val stream = inStream ?: return
        try {
            while (scope.isActive) {
                val length = stream.read(buffer)
                if (length > 0) {
                    val data = ByteArray(length)
                    System.arraycopy(buffer, 0, data, 0, length)
                    withContext(Dispatchers.Main) {
                        appendLog("收到消息 read ${data.size} bytes ${data.toHexString()}")
                    }
                    onBluetoothDataReceived(data)
                } else if (length < 0) {
                    break
                }
            }
        } catch (e: Exception) {
            if (scope.isActive) {
                Log.e(TAG, "SPP 读异常", e)
                withContext(Dispatchers.Main) {
                    appendLog("✗ SPP 读异常: ${e.message}")
                }
            }
        }
    }

    /**
     * 获取 SPP 输出流，供 OTA 发送数据使用。
     */
    fun getDataOutputStream(): OutputStream? = outStream

    /**
     * 初始化视图
     */
    private fun initViews() {
        btnSelectFile = findViewById(R.id.btnSelectFile2)
        btnStartTransfer = findViewById(R.id.btnStartTransfer2)
        btnCancelTransfer = findViewById(R.id.btnCancelTransfer2)
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus2)
        tvSelectedFile = findViewById(R.id.tvSelectedFile2)
        tvStatus = findViewById(R.id.tvStatus2)
        tvProgress = findViewById(R.id.tvProgress2)
        tvLog = findViewById(R.id.tvLog2)
        progressBar = findViewById(R.id.progressBar2)
        updateConnectionStatusDisconnected("正在连接...")
    }

    /**
     * 在主线程更新连接状态为「已连接」，并显示设备信息。
     */
    private fun updateConnectionStatusConnected(deviceName: String?, deviceAddress: String?) {
        runOnUiThread {
            val name = deviceName?.takeIf { it.isNotBlank() } ?: "未知设备"
            val addr = deviceAddress ?: ""
            tvConnectionStatus.text = "已连接\n设备名称：$name\n设备地址：$addr"
            tvConnectionStatus.setTextColor(0xFF2E7D32.toInt())
        }
    }

    /**
     * 在主线程更新连接状态为「未连接」。
     */
    private fun updateConnectionStatusDisconnected(message: String) {
        runOnUiThread {
            tvConnectionStatus.text = message
            tvConnectionStatus.setTextColor(0xFF666666.toInt())
        }
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
        appendLog("蓝牙数据由本页 SPP 读循环接收并交给 OTA 处理")
    }

    /**
     * 创建蓝牙通信接口实现
     * 使用本 Activity 的 getDataOutputStream()（SPP 输出流）发送 OTA 数据。
     */
    private fun createBluetoothCommunicator(): IBluetoothCommunicator {
        return object : IBluetoothCommunicator {
            override fun sendMessage(data: ByteArray) {
                try {
                    // 检查数据长度
                    if (data.isEmpty()) {
                        appendLog("数据为空，无法发送")
                        Log.e(TAG, "数据为空，无法发送")
                        return
                    }

                    // 使用本 Activity 的 SPP 输出流
                    val outputStream = getDataOutputStream()
                    if (outputStream == null) {
                        appendLog("✗ 蓝牙未连接，无法发送消息")
                        Log.e(TAG, "蓝牙未连接，outputStream 为 null")
                        return
                    }

                    // 记录发送时间
                    val sendTime = System.currentTimeMillis()

                    // 发送数据
                    outputStream.write(data)
                    outputStream.flush()

                    // 计算耗时
                    val elapsed = System.currentTimeMillis() - sendTime

                    // 日志记录
                    appendLog("✓ 发送消息成功 (耗时: ${elapsed}ms)")
                    if (data.size < 200) {
                        appendLog("→ 数据: ${data.toHexString()}")
                    }
                    appendLog("→ 数据长度: ${data.size} 字节")

                    Log.d(TAG, "发送消息成功: ${data.size} 字节")

                } catch (e: Exception) {
                    appendLog("✗ 发送消息失败: ${e.message}")
                }
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
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
                data.flags = data.flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
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
     * 从 URI 获取可用的文件路径。
     * - file:// 直接返回路径。
     * - content:// 不调用 query()（跨应用会 Permission Denial），只用 openInputStream 复制到应用 cache，返回 cache 文件路径。
     */
    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "file" -> uri.path
                "content" -> copyContentUriToCache(uri)
                else -> copyContentUriToCache(uri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取文件路径失败", e)
            null
        }
    }

    /**
     * 将 content:// URI 用 openInputStream 读取并复制到应用 cache，返回 cache 文件路径。
     * 不依赖 query()，避免跨应用 FileProvider 的 Permission Denial。
     */
    private fun copyContentUriToCache(uri: Uri): String? {
        val displayName = getDisplayNameFromUri(uri)
        val safeName = (displayName?.takeIf { it.endsWith(".up", true) || it.endsWith(".upex", true) })
            ?: "ota_file.up"
        val tempFile = File(cacheDir, safeName)
        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        return tempFile.absolutePath
    }

    /**
     * 从 content URI 获取文件名：仅在本应用可 query 时使用，否则从 URI 解析或返回 null。
     */
    private fun getDisplayNameFromUri(uri: Uri): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        } catch (_: SecurityException) {
            uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
        }
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

        // 检查蓝牙连接状态
        if (getDataOutputStream() == null) {
            Toast.makeText(
                this,
                "蓝牙未连接，请确保设备已连接",
                Toast.LENGTH_LONG
            ).show()
            appendLog("✗ OTA 传输终止：蓝牙未连接")
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
        transferDisposable = otaManager?.startTransfer(fileType, listOf(file))
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
        Log.e(TAG,message)
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

        // 清理传输订阅
        transferDisposable?.dispose()

        // 取消 SPP 连接与读循环
        sppScope.cancel()
        try {
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "关闭 socket 异常", e)
        }
        socket = null
        outStream = null
        inStream = null
        updateConnectionStatusDisconnected("未连接")

        // 取消协程作用域（停止所有后台数据处理任务）
        dataProcessingScope.cancel()
    }
}

