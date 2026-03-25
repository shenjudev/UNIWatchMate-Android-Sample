# 经典蓝牙 SPP 直连 OTA SDK 集成指南

> 适用项目：UNIWatchMate Android Sample  
> 核心库：`lib-ota-sdk`、Android 原生 Bluetooth API  
> Demo 入口类：`OtaDemo2Activity`

---

> **入口提示**  
> 在 App 主页（`DeviceFragment`）顶部点击 **"OTA SDK Demo 通用设备 连接bt即可"** 按钮，  
> 系统通过以下代码直接跳转到 SPP 直连 OTA Demo 页面（`OtaDemo2Activity`）：
>
> ```kotlin
> // DeviceFragment 中的跳转代码
> viewBind.itemOtaSdkDemo2 -> {
>     val intent = Intent(requireContext(), OtaDemo2Activity::class.java)
>     startActivity(intent)
> }
> ```
>
> **前提条件：** 目标设备已通过系统蓝牙设置与手机完成**配对**（无需绑定到 UNIWatchMate SDK）。  
> 进入页面后，App 会自动遍历已配对设备列表，逐一尝试建立 SPP 连接，连接成功即可选文件并开始 OTA。

---

## 目录

1. [核心特点与适用场景](#1-核心特点与适用场景)
2. [整体流程概览](#2-整体流程概览)
3. [OTA SDK 职责边界](#3-ota-sdk-职责边界)
4. [权限要求](#4-权限要求)
5. [SPP 连接建立](#5-spp-连接建立)
6. [SPP 数据收发](#6-spp-数据收发)
   - [6.1 发送（输出流）](#61-发送输出流)
   - [6.2 接收（读循环）](#62-接收读循环)
7. [OTA SDK 集成](#7-ota-sdk-集成)
   - [7.1 初始化 OtaTransferManager](#71-初始化-otatransfermanager)
   - [7.2 实现 IBluetoothCommunicator（发送）](#72-实现-ibluetoothcommunicator发送)
   - [7.3 接收数据并转发给 SDK](#73-接收数据并转发给-sdk)
   - [7.4 启动 OTA 传输](#74-启动-ota-传输)
   - [7.5 处理传输状态回调](#75-处理传输状态回调)
   - [7.6 取消传输](#76-取消传输)
8. [文件类型说明](#8-文件类型说明)
9. [错误码说明](#9-错误码说明)
10. [生命周期管理](#10-生命周期管理)
11. [与其他两种 OTA Demo 的对比](#11-与其他两种-ota-demo-的对比)
12. [常见问题](#12-常见问题)

---

## 1. 核心特点与适用场景

`OtaDemo2Activity` 是**完全独立**的 SPP 直连 OTA 方案，具有以下特点：

- **使用 Android 原生 `BluetoothSocket` 直接建立 SPP 连接
- **通用性强**：只要设备支持标准 SPP（UUID `00001101-0000-1000-8000-00805F9B34F7`）即可使用，不限定绅聚专有设备
- **自动连接**：进入页面后自动遍历系统已配对设备，无需手动扫描选择
- **自管理收发**：内置 SPP 读循环协程，自行处理数据接收；发送通过持有的 `OutputStream` 直写

**适用场景：** 需要对任意支持 SPP 的蓝牙设备进行 OTA 升级，且设备已提前通过系统设置配对。

---

## 2. 整体流程概览

```
DeviceFragment（主页）      OtaDemo2Activity                   OTA SDK
       │                          │                               │
       │── 点击"OTA SDK Demo 2" ─>│                               │
       │   startActivity(Intent)  │── initOtaSdk() ─────────────>│
       │                          │   OtaTransferManager()        │
       │                          │                               │
       │                          │── initSppConnection()         │
       │                          │   遍历系统已配对设备列表        │
       │                          │   createInsecureRfcommSocket  │
       │                          │   socket.connect()            │
       │                          │   ✓ 连接成功，显示设备名/地址  │
       │                          │   startReadLoop()             │
       │                          │   (后台协程持续读 InputStream) │
       │                          │                               │
       │                          │── 选择固件文件                 │
       │                          │   (.up / .upex)              │
       │                          │                               │
       │                          │── startTransfer() ───────────>│
       │                          │                               │── 构建 OTA 请求包
       │                          │<── IBluetoothCommunicator ────│    sendMessage(bytes)
       │                          │   outStream.write(bytes)      │
       │                          │   outStream.flush()           │──> 发送到设备
       │                          │                               │
       │                          │   设备回包（SPP 读循环收到）   │
       │                          │── onBluetoothDataReceived() ─>│── 解析协议，驱动状态机
       │                          │                               │
       │                          │<── OtaTransferState ──────────│
       │                          │   TRANSFERRING(progress%)     │
       │                          │   SUCCESS / FAIL              │
```

---

## 3. OTA SDK 职责边界

> **重要：OTA SDK 不直接操作蓝牙**，它只负责：
> - **构建**符合协议规范的命令包（字节数组）
> - **解析**从设备收到的协议包，驱动内部状态机
>
> **蓝牙数据的实际收发完全由 APP 负责：**
> - **发送**：APP 实现 `IBluetoothCommunicator.sendMessage()`，在其中调用 `outStream.write(data)` 写入 SPP 输出流
> - **接收**：APP 的 SPP 读循环从 `inStream` 读出数据后，主动调用 `IOtaMessageListener.onMessageReceived()` 喂给 SDK 解析
>
> 简而言之：**SDK 管协议，APP 管蓝牙**。

---

## 4. 权限要求

`OtaDemo2Activity` 在 `onCreate` 时自动申请所需权限，无需调用方预先处理：

| 权限 | 适用版本 | 用途 |
|------|---------|------|
| `BLUETOOTH_CONNECT` | Android 12+（API 31+）| 连接已配对设备 |
| `BLUETOOTH_SCAN` | Android 12+（API 31+）| 扫描/枚举设备 |
| `ACCESS_FINE_LOCATION` | 所有版本 | 蓝牙扫描附加要求 |
| `ACCESS_COARSE_LOCATION` | 所有版本 | 蓝牙扫描附加要求 |
| `READ_MEDIA_*` | Android 13+（API 33+）| 选择固件文件 |
| `READ_EXTERNAL_STORAGE` | Android 12 及以下 | 选择固件文件 |

权限申请逻辑：

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ...
    if (hasRequiredPermissions()) {
        initSppConnection()   // 权限已就绪，直接连接
    } else {
        ActivityCompat.requestPermissions(this, getRequiredPermissions(), REQUEST_CODE)
        // 用户授权后在 onRequestPermissionsResult 中调用 initSppConnection()
    }
}
```

---

## 5. SPP 连接建立

页面启动后，在 IO 协程中自动遍历系统已配对设备，依次尝试 SPP 连接：

```kotlin
private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34F7")
private val sppScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

private fun initSppConnection() {
    sppScope.launch {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter ?: return@launch  // 蓝牙不可用

        // 获取所有已配对设备
        val pairedDevices = adapter.bondedDevices?.toList() ?: emptyList()
        if (pairedDevices.isEmpty()) {
            // 提示：请先在系统设置中配对设备
            return@launch
        }

        for (device in pairedDevices) {
            try {
                // 使用 Insecure（免 PIN 码重验证）的 RFCOMM Socket
                val trySocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
                trySocket.connect()                    // 阻塞直到成功或异常

                // 连接成功
                socket    = trySocket
                outStream = socket?.outputStream       // 发送用
                inStream  = socket?.inputStream        // 接收用

                withContext(Dispatchers.Main) {
                    // 更新 UI：显示已连接的设备名和地址
                    updateConnectionStatusConnected(device.name, device.address)
                }

                startReadLoop(sppScope)               // 启动接收循环
                break                                 // 连接成功，退出遍历
            } catch (e: Throwable) {
                // 当前设备连接失败，继续尝试下一个
                trySocket?.close()
            }
        }
    }
}
```

**连接关键点：**
- 使用 `createInsecureRfcommSocketToServiceRecord` 而非 `createRfcommSocketToServiceRecord`，避免重复 PIN 验证弹窗
- 遍历全部已配对设备，只要有一个连接成功即停止
- 连接建立后立即启动 SPP 读循环

---

## 6. SPP 数据收发

### 6.1 发送（输出流）

发送时直接调用 `outStream.write()` + `flush()`，同步写入 SPP 输出流：

```kotlin
fun getDataOutputStream(): OutputStream? = outStream

// 在 IBluetoothCommunicator.sendMessage() 中调用：
val outputStream = getDataOutputStream() ?: return  // null 表示 SPP 未连接
outputStream.write(data)
outputStream.flush()
```

### 6.2 接收（读循环）

SPP 连接成功后，在 `sppScope`（IO 协程）中启动阻塞式读循环，持续从 `inStream` 读取数据：

```kotlin
private suspend fun startReadLoop(scope: CoroutineScope) {
    val buffer = ByteArray(1024 * 1024)  // 1MB 缓冲区
    val stream = inStream ?: return
    try {
        while (scope.isActive) {
            val length = stream.read(buffer)  // 阻塞直到有数据
            if (length > 0) {
                val data = ByteArray(length)
                System.arraycopy(buffer, 0, data, 0, length)
                // 转发给 OTA SDK 处理
                onBluetoothDataReceived(data)
            } else if (length < 0) {
                break  // 流已关闭（设备断开）
            }
        }
    } catch (e: Exception) {
        if (scope.isActive) {
            // SPP 读异常，通常意味着连接已断开
            Log.e(TAG, "SPP 读异常", e)
        }
    }
}
```

> **线程说明：** 读循环运行在 `sppScope`（IO 线程），收到数据后调用 `onBluetoothDataReceived()`，该方法内部再切换到单线程协程处理，保证数据按接收顺序喂给 OTA 状态机。

---

## 7. OTA SDK 集成

### 7.1 初始化 OtaTransferManager

```kotlin
private var otaManager: OtaTransferManager? = null
private var messageListener: IOtaMessageListener? = null

private fun initOtaSdk() {
    val communicator = createBluetoothCommunicator()

    val logger = object : ILogger {
        override fun logD(tag: String, message: String, location: String?) {
            Log.d(tag, if (location != null) "[$location] $message" else message)
        }
        override fun logI(tag: String, message: String, location: String?) {
            Log.i(tag, if (location != null) "[$location] $message" else message)
        }
        override fun logW(tag: String, message: String, location: String?) {
            Log.w(tag, if (location != null) "[$location] $message" else message)
        }
        override fun logE(tag: String, message: String, location: String?, throwable: Throwable?) {
            Log.e(tag, if (location != null) "[$location] $message" else message, throwable)
        }
    }

    otaManager = OtaTransferManager(
        context      = applicationContext,
        communicator = communicator,
        logger       = logger
    )

    messageListener = otaManager?.getMessageListener()
    // SDK 内置 CRC16-CCITT 算法，无需额外配置
}
```

---

### 7.2 实现 IBluetoothCommunicator（发送）

SDK 构建好协议包后通过 `sendMessage()` 回调给 APP，APP 写入 SPP 输出流：

```kotlin
private fun createBluetoothCommunicator(): IBluetoothCommunicator {
    return object : IBluetoothCommunicator {
        override fun sendMessage(data: ByteArray) {
            if (data.isEmpty()) return

            val outputStream = getDataOutputStream()
            if (outputStream == null) {
                Log.e(TAG, "SPP 未连接，无法发送")
                return
            }

            try {
                outputStream.write(data)
                outputStream.flush()
            } catch (e: Exception) {
                Log.e(TAG, "SPP 发送失败: ${e.message}", e)
            }
        }
    }
}
```

---

### 7.3 接收数据并转发给 SDK

SPP 读循环收到数据后调用此方法，使用单线程协程按序转发给 OTA SDK：

```kotlin
private val dataProcessingScope = CoroutineScope(
    Dispatchers.IO.limitedParallelism(1) + SupervisorJob()
)

fun onBluetoothDataReceived(rawData: ByteArray) {
    dataProcessingScope.launch {
        try {
            // SDK 内部自动判断是否为 OTA 消息
            val isOtaMessage = messageListener?.onMessageReceived(rawData) ?: false
            if (!isOtaMessage) {
                // 非 OTA 消息，可自行处理业务逻辑
                Log.d(TAG, "收到非 OTA 消息: ${rawData.size} 字节")
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理蓝牙消息失败", e)
        }
    }
}
```

> **为何需要两层协程？**  
> - `sppScope`（IO 线程）：专职阻塞读取 SPP 数据，不能被其他任务阻塞  
> - `dataProcessingScope`（单线程 IO）：保证数据按接收顺序串行喂给 OTA 状态机，避免并发乱序

---

### 7.4 启动 OTA 传输

```kotlin
// 发送前检查 SPP 连接状态
if (getDataOutputStream() == null) {
    Toast.makeText(this, "SPP 未连接，请确保设备已配对并连接", Toast.LENGTH_LONG).show()
    return
}

transferDisposable = otaManager?.startTransfer(
    fileType        = fileType,     // OtaFileType.OTA 或 OtaFileType.OTA_UPEX
    files           = listOf(file),
    appendixSize    = 0,
    bleTransmission = false         // SPP 场景保持默认 false，无需高速模式
)
?.observeOn(AndroidSchedulers.mainThread())
?.subscribe(
    { state -> handleTransferState(state) },
    { error -> handleTransferError(error)  }
)
```

**参数说明：**

| 参数 | SPP 场景值 | 说明 |
|------|-----------|------|
| `fileType` | `OTA` 或 `OTA_UPEX` | 根据文件扩展名选择 |
| `files` | 单文件列表 | 固件文件 |
| `appendixSize` | `0` | 附加数据大小，通常为 0 |
| `bleTransmission` | **`false`** | SPP 场景无需高速模式 |

---

### 7.5 处理传输状态回调

```kotlin
private fun handleTransferState(state: OtaTransferState) {
    when (state.state) {
        State.PRE_TRANSFER -> {
            tvStatus.text = "准备传输..."
        }
        State.TRANSFERRING -> {
            progressBar.progress = state.progress
            tvProgress.text = "${state.progress}%"
            tvStatus.text = "传输中 ${state.progress}%"
        }
        State.SUCCESS -> {
            if (state.index >= state.total) {
                tvStatus.text = "传输成功！"
                progressBar.progress = 100
                Toast.makeText(this, "传输成功！", Toast.LENGTH_SHORT).show()
                resetUIAfterTransfer()
            } else {
                tvStatus.text = "文件 ${state.index + 1}/${state.total} 完成"
            }
        }
        State.FAIL -> {
            tvStatus.text = "传输失败"
            resetUIAfterTransfer()
        }
    }
}
```

---

### 7.6 取消传输

```kotlin
otaManager?.cancelTransfer()
    ?.observeOn(AndroidSchedulers.mainThread())
    ?.subscribe(
        { tvStatus.text = "传输已取消" },
        { error -> Log.e(TAG, "取消失败: ${error.message}") }
    )

// 同时立即释放传输订阅
transferDisposable?.dispose()
transferDisposable = null
```

---

## 8. 文件类型说明

| 枚举值 | 扩展名 | 说明 |
|--------|--------|------|
| `OtaFileType.OTA` | `.up` | 裸固件文件，直接传输 |
| `OtaFileType.OTA_UPEX` | `.upex` | tar 压缩包，SDK 自动解压后传输 |

> **`content://` URI 处理：** 选择器返回 `content://` URI 时，Demo 通过 `openInputStream` 将文件复制到 `cacheDir`，不依赖 `ContentResolver.query()`（跨 FileProvider 应用 query 会抛 `Permission Denial`）。

---

## 9. 错误码说明

| 错误码 | code | 说明 |
|--------|------|------|
| `ERROR_OTHER` | 0 | 其他未分类错误 |
| `ERROR_BUSY` | 1 | 设备忙碌 |
| `ERROR_LOW_MEMORY` | 2 | 设备存储空间不足 |
| `ERROR_OVER_MAX_COUNT` | 3 | 超过最大文件数量 |
| `ERROR_LOW_POWER` | 4 | 设备电量不足 |
| `ERROR_TIME_OUT` | 5 | 等待设备回复超时 |
| `ERROR_FILE_EXCEPTION` | 101 | 固件文件不存在、损坏或格式错误 |
| `ERROR_DISCONNECT` | 102 | 蓝牙连接断开 |

```kotlin
private fun handleTransferError(error: Throwable) {
    val msg = when (error) {
        is OtaException -> when (error.error) {
            OtaError.ERROR_BUSY           -> "设备忙碌，请稍后再试"
            OtaError.ERROR_LOW_MEMORY     -> "设备存储空间不足"
            OtaError.ERROR_LOW_POWER      -> "设备电量不足"
            OtaError.ERROR_TIME_OUT       -> "传输超时，请检查连接"
            OtaError.ERROR_FILE_EXCEPTION -> "固件文件异常"
            OtaError.ERROR_DISCONNECT     -> "设备已断开连接"
            else                          -> "传输失败: ${error.message}"
        }
        else -> "未知错误: ${error.message}"
    }
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
```

---

## 10. 生命周期管理

```kotlin
override fun onDestroy() {
    super.onDestroy()

    // 1. 清理 OTA 传输订阅
    transferDisposable?.dispose()

    // 2. 停止 SPP 读循环（取消 sppScope 后 read() 阻塞会被中断）
    sppScope.cancel()

    // 3. 关闭 BluetoothSocket（释放 RFCOMM 连接）
    try { socket?.close() } catch (e: Exception) { /* ignore */ }
    socket    = null
    outStream = null
    inStream  = null

    // 4. 停止数据处理协程
    dataProcessingScope.cancel()
}
```

> **注意：** `sppScope.cancel()` 会让 `startReadLoop` 中的 `stream.read()` 在下次迭代检查时退出；同时 `socket.close()` 会立即中断阻塞中的 `read()` 调用，两者配合确保彻底释放资源。

---


## 11. 常见问题

**Q：进入页面后一直显示"正在连接"，无法连接？**  
- 确认目标设备已在系统蓝牙设置中配对成功（已配对 ≠ 已连接）。  
- 确认目标设备支持标准 SPP（UUID `00001101-0000-1000-8000-00805F9B34F7`）。  
- 部分设备连接 SPP 时需要设备处于广播状态，请确认设备已开机且蓝牙可被发现。

**Q：已配对多个设备，会连哪个？**  
- Demo 遍历 `bondedDevices` 列表，按系统返回顺序逐一尝试，第一个连接成功的设备生效。  
- 如需指定设备，可根据 `device.name` 或 `device.address` 增加过滤逻辑。

**Q：`createInsecureRfcommSocketToServiceRecord` 和 `createRfcommSocketToServiceRecord` 有何区别？**  
- `Insecure` 版本（本 Demo 使用）：建立连接时不需要用户重新输入 PIN 码，适合已配对设备的静默重连。  
- 非 `Insecure` 版本：每次连接可能弹出 PIN 码验证对话框，影响用户体验。

**Q：SPP 读循环和 OTA 数据处理各自的协程有何分工？**  
- `sppScope`：阻塞读取 SPP 数据，必须独立运行，不能被其他任务干扰。  
- `dataProcessingScope`（单线程）：保证 OTA 状态机按顺序接收数据，避免乱序导致协议异常。

**Q：选择文件时提示权限问题？**  
- Android 13+ 需要 `READ_MEDIA_*` 权限；Android 12 及以下需要 `READ_EXTERNAL_STORAGE`。  
- Demo 在 `onCreate` 中已统一申请，若用户拒绝则无法选择文件。

**Q：OTA 传输期间 SPP 断开会怎样？**  
- 读循环中 `inStream.read()` 会抛出异常，读循环退出。  
- SDK 由于收不到设备回包，会在等待超时后通过 `onError` 抛出 `OtaException(OtaError.ERROR_TIME_OUT, ...)`。  
- 建议在读循环异常处理中主动调用 `otaManager?.notifyBleDisconnected()` 以立即终止传输（BT 场景同样适用此接口）。

---

*文档对应代码：`OtaDemo2Activity.kt` / `lib-ota-sdk/`*
