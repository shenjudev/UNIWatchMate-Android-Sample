# BLE 搜索连接与 OTA SDK 集成指南

> 适用项目：UNIWatchMate Android Sample  
> 核心库：`lib-ota-sdk`、`RxAndroidBle3`

---

> **入口提示**  
> 在 App 主页底部导航栏中点击 **"BLE搜索连接OTA"** 按钮，  
> 系统通过 `DeviceFragmentDirections.toBleScan()` 跳转到 BLE 搜索页（`BleScanFragment`），  
> 从此处开始执行设备扫描 → 连接 → OTA 的完整流程。
>
> ```kotlin
> // DeviceFragment 中的跳转代码
> findNavController().navigate(DeviceFragmentDirections.toBleScan())
> ```

---

## 目录

1. [整体流程概览](#1-整体流程概览)
2. [GATT 服务与特征 UUID](#2-gatt-服务与特征-uuid)
3. [BLE 扫描](#3-ble-扫描)
4. [BLE 连接与 GATT 服务发现](#4-ble-连接与-gatt-服务发现)
5. [BleConnectionHolder — 连接共享](#5-bleconnectionholder--连接共享)
6. [OTA SDK 集成](#6-ota-sdk-集成)
   - [6.1 依赖接口总览](#61-依赖接口总览)
   - [6.2 初始化 OtaTransferManager](#62-初始化-otatransfermanager)
   - [6.3 实现 IBluetoothCommunicator（发送）](#63-实现-ibluetoothcommunicator发送)
   - [6.4 接收 BLE 数据并转发给 SDK](#64-接收-ble-数据并转发给-sdk)
   - [6.5 启动 OTA 传输](#65-启动-ota-传输)
   - [6.6 处理传输状态回调](#66-处理传输状态回调)
   - [6.7 取消传输](#67-取消传输)
   - [6.8 BLE 断开与发送失败处理](#68-ble-断开与发送失败处理)
7. [文件类型说明](#7-文件类型说明)
8. [错误码说明](#8-错误码说明)
9. [协议常量参考](#9-协议常量参考)
10. [完整集成示意（时序）](#10-完整集成示意时序)
11. [常见问题](#11-常见问题)

---

## 1. 整体流程概览

```
BleScanFragment                BleConnectionHolder          OtaDemobleActivity
      │                               │                            │
      │── 扫描BLE设备 ──────────────────────────────────────────>│
      │                               │                            │
      │── 连接设备 ──────────────────>│                            │
      │   establishConnection()       │                            │
      │   requestMtu()                │                            │
      │   discoverServices()          │                            │
      │                               │                            │
      │── set(mac, connection) ──────>│                            │
      │── setNegotiatedMtu(mtu) ─────>│                            │
      │                               │                            │
      │── startActivity(OtaDemobleActivity) ──────────────────────>│
      │                               │                            │
      │                               │<── getConnection(mac) ─────│
      │                               │                            │── initOtaSdk()
      │                               │                            │── setupBleNotifications()
      │                               │                            │
      │                               │         BLE通知数据         │
      │                               │────────────────────────────>│
      │                               │                            │── messageListener.onMessageReceived(bytes)
      │                               │                            │
      │                               │         OTA传输请求         │
      │                               │<───────────────────────────│ startTransfer()
```

---

## 2. GATT 服务与特征 UUID

在 `BleConnectionHolder` 中统一定义，供扫描页和 OTA 页共用：

| 名称 | UUID | 方向 |
|------|------|------|
| Service UUID | `FFF00000-E8A5-BFE5-AE89-E7BB85E8819A` | — |
| APP → 设备（写）| `0000FFF1-0000-1000-8000-00805F9B34FB` | APP 发送数据 |
| 设备 → APP（通知）| `0000FFF2-0000-1000-8000-00805F9B34FB` | 设备回复数据 |

---

## 3. BLE 扫描

使用 **RxAndroidBle3** 进行扫描，扫描时间 10 秒，仅展示厂商 ID 低字节为 `0xA0` 或 `0xC0` 的设备。

```kotlin
// BleScanFragment.kt
scanDisposable = rxBleClient.scanBleDevices(
    ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
)
.take(10, TimeUnit.SECONDS)
.subscribeOn(Schedulers.io())
.observeOn(AndroidSchedulers.mainThread())
.subscribe(
    { result: ScanResult ->
        // 过滤：只显示厂商ID低字节为 0xA0 或 0xC0 的设备
        if (!hasManufacturerIdFirstByte(result, setOf(0xA0, 0xC0))) return@subscribe
        val mac  = result.bleDevice.macAddress
        val name = result.bleDevice.name
        val rssi = result.rssi
        deviceMap[mac] = BleScanDevice(mac, name, rssi)
        refreshDeviceList()
    },
    { t -> /* 扫描出错 */ },
    { /* 扫描结束 */ }
)
```

**厂商数据过滤逻辑：**

```kotlin
private fun hasManufacturerIdFirstByte(result: ScanResult, allowedFirstBytes: Set<Int>): Boolean {
    val manufacturerData = result.scanRecord?.manufacturerSpecificData ?: return false
    for (i in 0 until manufacturerData.size()) {
        val key = manufacturerData.keyAt(i)
        if ((key and 0xFF) in allowedFirstBytes) return true
    }
    return false
}
```

---

## 4. BLE 连接与 GATT 服务发现

点击列表中的设备后，执行连接 → MTU 协商 → 服务发现，三步链式调用：

```kotlin
// 请求 MTU 515（实际值以设备与系统协商为准）
private const val BLE_REQUESTED_MTU = 515

connectionDisposable = rxBleClient.getBleDevice(mac)
    .establishConnection(false)                      // 1. 建立连接
    .flatMapSingle { connection ->
        BleConnectionHolder.set(mac, connection)     // 保存连接
        connection.requestMtu(BLE_REQUESTED_MTU)     // 2. 协商 MTU
            .flatMap { negotiatedMtu ->
                BleConnectionHolder.setNegotiatedMtu(negotiatedMtu)
                connection.discoverServices()         // 3. 发现 GATT 服务
            }
    }
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        { rxBleDeviceServices ->
            val services = rxBleDeviceServices.getBluetoothGattServices()
            if (hasRequiredServiceAndCharacteristics(services)) {
                // 连接成功，跳转 OTA 页
                startActivity(Intent(context, OtaDemobleActivity::class.java).apply {
                    putExtra(OtaDemobleActivity.EXTRA_DEVICE_ADDRESS, mac)
                    putExtra(OtaDemobleActivity.EXTRA_DEVICE_NAME, name)
                })
            } else {
                // 设备缺少所需 GATT 服务
                ToastUtil.showToast("设备无所需 GATT 服务")
            }
        },
        { t -> /* 连接失败 */ }
    )
```

**GATT 服务验证：** 确认设备包含 Service UUID 以及收发两个特征：

```kotlin
private fun hasRequiredServiceAndCharacteristics(services: List<BluetoothGattService>): Boolean {
    val service = services.find { it.uuid == BleConnectionHolder.SERVICE_UUID } ?: return false
    val charUuids = service.characteristics.map { it.uuid }.toSet()
    return charUuids.contains(BleConnectionHolder.CHAR_UUID_APP_TO_DEVICE) &&
           charUuids.contains(BleConnectionHolder.CHAR_UUID_DEVICE_TO_APP)
}
```

> **注意：** `BleScanFragment.onStop()` 不要 dispose `connectionDisposable`。跳转到 OTA 页时 Fragment 会触发 `onStop`，若此处断开连接，OTA 页拿到的连接将失效。连接仅在切换设备或 `onDestroy` 时释放。

---

## 5. BleConnectionHolder — 连接共享

`BleConnectionHolder` 是一个单例对象，负责跨页面共享 BLE 连接：

```kotlin
object BleConnectionHolder {
    // GATT UUID 常量
    val SERVICE_UUID          = UUID.fromString("FFF00000-E8A5-BFE5-AE89-E7BB85E8819A")
    val CHAR_UUID_APP_TO_DEVICE = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
    val CHAR_UUID_DEVICE_TO_APP = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")

    // 写入连接（BleScanFragment 调用）
    fun set(address: String, conn: RxBleConnection)

    // 写入协商 MTU（可选，OTA 页展示用）
    fun setNegotiatedMtu(mtu: Int)
    fun getNegotiatedMtu(): Int?

    // 读取连接（OtaDemobleActivity 调用）
    fun getConnection(address: String): RxBleConnection?

    // 清理（onDestroy 调用）
    fun clear()
}
```

---

## 6. OTA SDK 集成

> **重要：OTA SDK 的职责边界**
>
> OTA SDK **不直接操作蓝牙**，它只负责：
> - **构建**符合协议规范的命令包（字节数组）
> - **解析**从设备收到的协议包，驱动内部状态机
>
> **蓝牙数据的实际收发完全由 APP 负责：**
> - **发送**：APP 实现 `IBluetoothCommunicator.sendMessage()`，在其中调用 BLE 写特征，SDK 通过此回调将命令包交给 APP 发出
> - **接收**：APP 订阅 BLE 通知特征，收到字节数组后主动调用 `IOtaMessageListener.onMessageReceived()`，将数据"喂"给 SDK 解析
>
> 简而言之：**SDK 管协议，APP 管蓝牙**。

### 6.1 依赖接口总览

OTA SDK 暴露以下三个核心接口：

| 接口 | 方向 | 说明 |
|------|------|------|
| `IBluetoothCommunicator` | APP 实现，SDK 调用 | SDK 构建好协议包后回调此接口，APP 负责通过 BLE 写特征发出 |
| `IOtaMessageListener` | SDK 提供，APP 调用 | APP 收到 BLE 通知数据后主动调用此接口，SDK 负责解析 |
| `ILogger` | APP 实现，SDK 调用（可选） | SDK 日志输出回调 |

---

### 6.2 初始化 OtaTransferManager

```kotlin
// 1. 实现蓝牙通信接口（见 6.3）
val communicator = createBluetoothCommunicator()

// 2. 实现日志接口（可选，不传则 SDK 静默运行）
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

// 3. 创建管理器
val otaManager = OtaTransferManager(
    context      = applicationContext,
    communicator = communicator,
    logger       = logger           // 可省略
)

// 4. 获取消息监听器（接收时用）
val messageListener = otaManager.getMessageListener()
```

> **注意：** SDK 内置 CRC16-CCITT 算法，无需额外配置。

---

### 6.3 实现 IBluetoothCommunicator（发送）

SDK 构建好完整协议包后，通过 `sendMessage()` 回调给 APP，APP 负责将其通过 BLE 写入设备特征 `0xFFF1`：

```kotlin
private fun createBluetoothCommunicator(): IBluetoothCommunicator {
    return object : IBluetoothCommunicator {
        override fun sendMessage(data: ByteArray) {
            if (data.isEmpty()) return

            val conn = bleConnection
            if (conn == null) {
                // 连接已断开，通知 SDK
                otaManager?.notifyBleDisconnected()
                return
            }

            conn.writeCharacteristic(BleConnectionHolder.CHAR_UUID_APP_TO_DEVICE, data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { /* 发送成功 */ },
                    { t ->
                        // 发送失败，通知 SDK 终止传输
                        otaManager?.notifyTransferSendFailed(t.message ?: "未知错误")
                    }
                )
        }
    }
}
```

---

### 6.4 接收 BLE 数据并转发给 SDK

订阅设备 → APP 的通知特征 `0xFFF2`，收到数据后交给 `IOtaMessageListener`：

```kotlin
// 订阅 BLE 通知
bleConnection?.setupNotification(BleConnectionHolder.CHAR_UUID_DEVICE_TO_APP)
    ?.flatMap { it }
    ?.subscribeOn(Schedulers.io())
    ?.observeOn(AndroidSchedulers.mainThread())
    ?.subscribe(
        { bytes -> onBluetoothDataReceived(bytes) },
        { t ->
            if (t is BleDisconnectedException) onBleLinkLost()
        }
    )

// 转发给 OTA SDK（使用单线程按顺序处理，避免状态机混乱）
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
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理蓝牙消息失败", e)
        }
    }
}
```

> **线程安全：** 使用 `limitedParallelism(1)` 单线程协程处理，保证数据按接收顺序投递给 OTA 状态机，避免并发乱序。

---

### 6.5 启动 OTA 传输

支持两种文件类型（`.up` 和 `.upex`），通过 `OtaFileType` 区分：

```kotlin
val file = File("/path/to/firmware.up")         // 或 firmware.upex
val fileType = OtaFileType.OTA                  // 或 OtaFileType.OTA_UPEX

transferDisposable = otaManager.startTransfer(
    fileType        = fileType,
    files           = listOf(file),
    appendixSize    = 0,           // 附加数据大小，通常为 0
    bleTransmission = true         // BLE 传输时必须为 true（开启高速模式）
)
.observeOn(AndroidSchedulers.mainThread())
.subscribe(
    { state -> handleTransferState(state) },
    { error -> handleTransferError(error) }
)
```

**参数说明：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `fileType` | `OtaFileType` | 是 | `OTA`（`.up`）或 `OTA_UPEX`（`.upex`） |
| `files` | `List<File>` | 是 | 固件文件列表，`.upex` 时只传第一个文件 |
| `appendixSize` | `Int` | 否 | 附加数据大小，默认 0 |
| `bleTransmission` | `Boolean` | 否 | BLE 传输时应设为 `true`，开启高速传输模式 |

---

### 6.6 处理传输状态回调

`startTransfer()` 返回 `Observable<OtaTransferState>`，在 `onNext` 中处理各阶段状态：

```kotlin
private fun handleTransferState(state: OtaTransferState) {
    when (state.state) {
        State.PRE_TRANSFER -> {
            // 准备阶段：已发送传输请求，等待设备确认
            progressBar.progress = 0
            tvStatus.text = "准备传输..."
        }
        State.TRANSFERRING -> {
            // 传输中：state.progress 为 0-100 的整数进度
            progressBar.progress = state.progress
            tvStatus.text = "传输中 ${state.progress}%"
        }
        State.SUCCESS -> {
            if (state.index >= state.total) {
                // 全部文件传输完成
                progressBar.progress = 100
                tvStatus.text = "传输成功！"
            } else {
                // 多文件场景：当前文件完成，还有下一个
                tvStatus.text = "文件 ${state.index + 1}/${state.total} 完成"
            }
        }
        State.FAIL -> {
            tvStatus.text = "传输失败"
        }
    }
}
```

**OtaTransferState 字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `state` | `State` | 当前阶段（见枚举） |
| `progress` | `Int` | 当前文件传输进度（0–100） |
| `index` | `Int` | 当前传输的文件序号（从 0 开始） |
| `total` | `Int` | 总文件数 |
| `sendingFile` | `File?` | 正在传输的文件 |

---

### 6.7 取消传输

取消会先停止发送线程，再向设备发送取消命令：

```kotlin
otaManager.cancelTransfer()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(
        { success -> tvStatus.text = "传输已取消" },
        { error   -> Log.e(TAG, "取消失败: ${error.message}") }
    )

// 同时释放传输订阅
transferDisposable?.dispose()
transferDisposable = null
```

---

### 6.8 BLE 断开与发送失败处理

**BLE 链路断开（如设备关机）：**

```kotlin
private val otaAbortHandled = AtomicBoolean(false)  // 防止重复触发

private fun onBleLinkLost() {
    if (!otaAbortHandled.compareAndSet(false, true)) return
    bleConnection = null
    // 通知 SDK：链路已断，无需发取消命令，直接停止并上报 ERROR_DISCONNECT
    otaManager?.notifyBleDisconnected()
}
```

监听 BLE 连接状态变化：

```kotlin
rxBleClient.getBleDevice(mac)
    .observeConnectionStateChanges()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe { state ->
        if (state == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
            onBleLinkLost()
        }
    }
```

**GATT 写失败（如信号差）：**

```kotlin
private fun onTransferSendFailed(t: Throwable) {
    if (!otaAbortHandled.compareAndSet(false, true)) return
    // 通知 SDK：发送失败，停止传输并上报 ERROR_OTHER
    otaManager?.notifyTransferSendFailed(t.message ?: "未知错误")
}
```

---

## 7. 文件类型说明

| 枚举值 | 文件扩展名 | type 值 | 说明 |
|--------|-----------|---------|------|
| `OtaFileType.OTA` | `.up` | 2 | 普通 OTA 固件文件，直接传输 |
| `OtaFileType.OTA_UPEX` | `.upex` | 5 | 压缩包，SDK 会自动解压后传输 |

`.upex` 文件解压路径：`<externalFilesDir>/ota_unzip/`（外置存储不可用时退回到 `filesDir`）

---

## 8. 错误码说明

`startTransfer()` 的 `onError` 回调中会抛出 `OtaException`，通过 `error.error` 获取错误码：

| 错误码 | code 值 | 说明 |
|--------|---------|------|
| `ERROR_OTHER` | 0 | 其他未分类错误 |
| `ERROR_BUSY` | 1 | 设备当前忙碌，无法升级 |
| `ERROR_LOW_MEMORY` | 2 | 设备存储空间不足 |
| `ERROR_OVER_MAX_COUNT` | 3 | 超过最大文件数量限制 |
| `ERROR_LOW_POWER` | 4 | 设备电量不足 |
| `ERROR_TIME_OUT` | 5 | 等待设备回复超时 |
| `ERROR_FILE_EXCEPTION` | 101 | 固件文件不存在、读取失败或格式错误 |
| `ERROR_DISCONNECT` | 102 | BLE 连接断开 |

```kotlin
private fun handleTransferError(error: Throwable) {
    val msg = when (error) {
        is OtaException -> when (error.error) {
            OtaError.ERROR_BUSY        -> "设备忙碌，请稍后再试"
            OtaError.ERROR_LOW_MEMORY  -> "设备存储空间不足"
            OtaError.ERROR_LOW_POWER   -> "设备电量不足，请充电后再升级"
            OtaError.ERROR_TIME_OUT    -> "传输超时，请检查连接"
            OtaError.ERROR_FILE_EXCEPTION -> "固件文件异常"
            OtaError.ERROR_DISCONNECT  -> "设备已断开连接"
            else -> "传输失败: ${error.message}"
        }
        else -> "未知错误: ${error.message}"
    }
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
```

---

## 9. 常见问题

**Q：扫描不到设备？**  
- 确认已申请 `BLUETOOTH_SCAN`、`BLUETOOTH_CONNECT`（Android 12+）和 `ACCESS_FINE_LOCATION` 权限。  
- 确认设备正在广播且厂商 ID 低字节为 `0xA0` 或 `0xC0`（过滤条件由 `MANUFACTURER_ID_FIRST_BYTES` 控制）。

**Q：连接后提示"设备无所需 GATT 服务"？**  
- 确认设备固件支持 Service UUID `FFF00000-E8A5-BFE5-AE89-E7BB85E8819A`，以及 FFF1、FFF2 两个特征。

**Q：OTA 传输中途 BLE 断开，SDK 会怎么处理？**  
- SDK 收到 `notifyBleDisconnected()` 后立即停止所有发送，并在 `startTransfer` 的 `onError` 回调中抛出 `OtaException(OtaError.ERROR_DISCONNECT, ...)`。

**Q：`bleTransmission` 参数有什么作用？**  
- 设为 `true` 时，SDK 会在传输前向设备发送进入高速模式的指令（`CMD_8016`），充分利用大 MTU 提升传输速率。BLE 场景下应始终设为 `true`。

**Q：是否需要手动处理超时？**  
- 通常不需要。SDK 内部在各阶段发送后会自动启动超时计时。仅在需要自定义超时策略时，才调用 `otaManager.handleTimeout(cmdId)`。

**Q：`.upex` 与 `.up` 文件的区别？**  
- `.up` 是可直接传输的裸固件文件；`.upex` 是 tar 压缩包，SDK 会自动解压到缓存目录后再传输，解压成功与否均有日志输出。

**Q：多次调用 `startTransfer` 会怎样？**  
- SDK 不做队列管理。请在上一次传输 `onError` 或全部文件 `SUCCESS` 后，再发起新的传输。

---

*文档对应代码版本：lib-ota-sdk（参见 `lib-ota-sdk/src/main/java/com/ota/sdk/`）*
