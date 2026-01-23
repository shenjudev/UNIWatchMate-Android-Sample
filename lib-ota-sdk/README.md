# OTA SDK 使用文档

## 简介

OTA SDK 是一个独立的设备 OTA（Over-The-Air）升级 SDK，用于通过蓝牙进行设备固件升级。

## 特性

- ✅ 完全独立，不依赖任何现有 SDK
- ✅ 支持普通 OTA 文件和 .upex 压缩文件
- ✅ 完整的协议封装，客户只需实现简单的通信接口
- ✅ 基于 RxJava3，提供响应式编程支持
- ✅ 详细的传输状态和错误处理
- ✅ 灵活的日志架构，支持自定义日志输出和调用位置追踪

## 快速开始

### 1. 添加依赖

在项目的 `build.gradle` 中添加：

```gradle
dependencies {
    implementation project(':lib-chuanyin:lib-ota-sdk')
}
```

### 2. 实现通信接口

首先，需要实现 `IBluetoothCommunicator` 接口：

```kotlin
class MyBluetoothCommunicator : IBluetoothCommunicator {
    override fun sendMessage(data: ByteArray) {
        // 使用自己的蓝牙库发送数据
        bluetoothSocket.outputStream.write(data)
        bluetoothSocket.outputStream.flush()
    }
    
    override fun clearMessageQueue() {
        // 清空自己的消息队列
        messageQueue.clear()
    }
}
```

### 3. 实现日志接口（可选但推荐）

SDK 通过 `ILogger` 接口暴露日志，由使用者决定如何处理：

```kotlin
val logger = object : ILogger {
    override fun logD(tag: String, message: String, location: String?) {
        // location 包含调用位置信息，格式：文件名:行号 方法名()
        val msg = if (location != null) "[$location] $message" else message
        Log.d(tag, msg)
    }
    
    override fun logI(tag: String, message: String, location: String?) {
        val msg = if (location != null) "[$location] $message" else message
        Log.i(tag, msg)
    }
    
    override fun logE(tag: String, message: String, location: String?, throwable: Throwable?) {
        val msg = if (location != null) "[$location] $message" else message
        Log.e(tag, msg, throwable)
    }
    
    override fun logW(tag: String, message: String, location: String?) {
        val msg = if (location != null) "[$location] $message" else message
        Log.w(tag, msg)
    }
}
```

**日志输出示例**：
```
D/OtaTransferManager: [OtaTransferManager.kt:63 startTransfer()] 开始 OTA 传输: fileType=OTA, files=1
D/OtaProtocolHandler: [OtaProtocolHandler.kt:111 handleTransferRequestResponse()] 设备回复传输请求: transferEnable=1
```

详细的日志架构说明请参考 [LOGGING_ARCHITECTURE.md](LOGGING_ARCHITECTURE.md)

### 4. 创建 OTA 管理器

### 4. 创建 OTA 管理器

```kotlin
val communicator = MyBluetoothCommunicator()
val logger = MyLogger()  // 可选，实现 ILogger 接口
val crcCalculator = MyCrcCalculator()  // CRC 计算实现

val otaManager = OtaTransferManager(
    context = applicationContext,
    communicator = communicator,
    logger = logger,
    crcCalculator = crcCalculator
)
```

### 5. 设置消息监听

在蓝牙接收回调中，将接收到的原始数据直接传给 SDK：

```kotlin
// 获取消息监听器
val messageListener = otaManager.getMessageListener()

// 在蓝牙接收回调中
fun onBluetoothDataReceived(rawData: ByteArray) {
    // 直接传给 OTA SDK，SDK 会自动判断是否是 OTA 消息
    val isOtaMessage = messageListener.onMessageReceived(rawData)
    
    if (isOtaMessage) {
        // SDK 已处理此消息
        println("OTA 消息已处理")
    } else {
        // 不是 OTA 消息，你可以自行处理
        handleOtherMessage(rawData)
    }
}
```

**注意**：
- 无需解析协议，SDK 内部会自动解析和判断
- 如果是 OTA 消息，SDK 返回 `true` 并自动处理
- 如果不是 OTA 消息，SDK 返回 `false`，你可以继续处理

### 6. 开始传输

```kotlin
val otaFile = File("/path/to/ota/file.bin")

otaManager.startTransfer(
    fileType = OtaFileType.OTA,
    files = listOf(otaFile)
).subscribe(
    { state ->
        when (state.state) {
            State.PRE_TRANSFER -> {
                println("准备传输")
            }
            State.TRANSFERRING -> {
                println("传输中: ${state.progress}%")
            }
            State.SUCCESS -> {
                println("传输成功")
            }
            State.FAIL -> {
                println("传输失败")
            }
        }
    },
    { error ->
        if (error is OtaException) {
            println("OTA 错误: ${error.error}, ${error.message}")
        }
    }
)
```

## API 文档

### OtaTransferManager

#### 方法

##### startTransfer

开始 OTA 传输。

```kotlin
fun startTransfer(
    fileType: OtaFileType,
    files: List<File>,
    appendixSize: Int = 0
): Observable<OtaTransferState>
```

**参数：**
- `fileType`: 文件类型（`OtaFileType.OTA` 或 `OtaFileType.OTA_UPEX`）
- `files`: 文件列表
- `appendixSize`: 附加数据大小（默认为 0）

**返回：**
- `Observable<OtaTransferState>`: 传输状态流

##### cancelTransfer

取消传输。

```kotlin
fun cancelTransfer(): Single<Boolean>
```

**返回：**
- `Single<Boolean>`: 取消结果

##### getMessageListener

获取消息监听器，用于接收设备回复。

```kotlin
fun getMessageListener(): IOtaMessageListener
```

**返回：**
- `IOtaMessageListener`: 消息监听器

### OtaTransferState

传输状态数据类。

**属性：**
- `total: Int` - 总文件数
- `state: State` - 传输状态
- `sendingFile: File?` - 当前正在传输的文件
- `progress: Int` - 传输进度（0-100）
- `index: Int` - 当前文件索引

### OtaFileType

文件类型枚举。

- `OTA(2)` - 普通 OTA 文件
- `OTA_UPEX(5)` - OTA .upex 压缩文件

### OtaError

错误码枚举。

- `ERROR_OTHER(0)` - 其他错误
- `ERROR_BUSY(1)` - 设备忙碌
- `ERROR_LOW_MEMORY(2)` - 设备内存不足
- `ERROR_OVER_MAX_COUNT(3)` - 超过最大数量
- `ERROR_LOW_POWER(4)` - 设备电量不足
- `ERROR_TIME_OUT(5)` - 传输超时
- `ERROR_FILE_EXCEPTION(101)` - 文件异常
- `ERROR_DISCONNECT(102)` - 设备断开连接

## 协议说明

### 协议包格式

OTA 协议使用 TLOCP 格式：

```
+--------+--------+--------+--------+--------+--------+--------+--------+--------+
|  Head  | Order  |  CmdId |Divide+ |Payload | Offset |  CRC   |  Payload Data   |
| (1byte)|(1byte) |(2bytes)| Length | Length |(4bytes)|(4bytes)|   (可变长度)    |
|        |        |        |(2bytes)|(2bytes)|        |        |                 |
+--------+--------+--------+--------+--------+--------+--------+--------+--------+
```

### 协议常量

- `HEAD_FILE_SPP_A_2_D = 0x0E` - OTA 文件传输协议头
- `CMD_ID_800A = 0x0A` - OTA 传输请求/回复
- `CMD_ID_8002 = 0x02` - 文件信息/分片大小
- `CMD_ID_8003 = 0x03` - 数据包传输
- `CMD_ID_8004 = 0x04` - 传输确认
- `CMD_ID_8005 = 0x05` - 取消传输
- `CMD_ID_8006 = 0x06` - 设备主动取消
- `CMD_ID_8008 = 0x08` - OTA 超时处理

### 判断 OTA 消息

使用 `OtaProtocolConstants.isOtaCommand(head, cmdId)` 判断是否是 OTA 相关消息。

## 注意事项

1. **线程安全**：SDK 内部已处理线程安全，但客户在实现接口时需要注意线程安全。

2. **消息转发**：客户必须在蓝牙接收回调中，将 OTA 相关消息转发给 SDK，否则传输无法进行。

3. **Context 传入**：创建 `OtaTransferManager` 时需要传入 `Context`，用于 .upex 文件解压。

4. **文件验证**：SDK 会验证文件是否存在和文件大小，但客户也应该在调用前进行验证。

5. **错误处理**：建议客户实现完整的错误处理逻辑，特别是网络断开、设备断开等情况。

## 示例代码

完整示例请参考项目中的示例代码。

## 许可证

[许可证信息]
