# OTA Demo Activity 使用说明

## 简介

`OtaDemoActivity` 是一个完整的 OTA 升级演示 Activity，展示了如何使用 OTA SDK 进行设备固件升级。

## 功能特性

- ✅ 文件选择：支持选择 .up 或 .upex 文件
- ✅ 文件类型自动识别
- ✅ 实时传输状态显示
- ✅ 传输进度条显示
- ✅ 详细日志输出
- ✅ 取消传输功能

## 使用方法

### 1. 启动 Demo Activity

```kotlin
val intent = Intent(this, OtaDemoActivity::class.java)
startActivity(intent)
```

或者在 AndroidManifest.xml 中配置为启动 Activity。

### 2. 使用步骤

1. **选择文件**
   - 点击"选择 OTA 文件"按钮
   - 从文件管理器中选择 .up 或 .upex 文件
   - 文件信息会显示在界面上

2. **开始传输**
   - 点击"开始传输"按钮
   - 观察传输状态和进度
   - 查看日志了解详细信息

3. **取消传输**（可选）
   - 点击"取消传输"按钮
   - 传输会被中断

## 重要提示

### ⚠️ 需要实现蓝牙通信

Demo Activity 中的 `createBluetoothCommunicator()` 方法只是示例实现，**客户需要根据实际情况实现真正的蓝牙通信逻辑**。

```kotlin
private fun createBluetoothCommunicator(): IBluetoothCommunicator {
    return object : IBluetoothCommunicator {
        override fun sendMessage(data: ByteArray) {
            // TODO: 实现实际的蓝牙发送逻辑
            // 示例：
            bluetoothSocket?.outputStream?.write(data)
            bluetoothSocket?.outputStream?.flush()
        }
        
        override fun clearMessageQueue() {
            // TODO: 实现清空消息队列的逻辑
            messageQueue.clear()
        }
    }
}
```

### ⚠️ 需要实现消息接收

客户需要在蓝牙接收回调中调用 `onBluetoothDataReceived()` 方法：

```kotlin
// 在蓝牙接收回调中
fun onBluetoothDataReceived(data: ByteArray) {
    // 解析协议包
    val head = data[0]
    val cmdId = parseCmdId(data)
    
    // 判断是否是 OTA 消息并转发给 SDK
    if (OtaProtocolConstants.isOtaCommand(head, cmdId)) {
        val payload = extractPayload(data)
        otaDemoActivity.onBluetoothDataReceived(data)
    }
}
```

或者直接在 Activity 中实现：

```kotlin
class OtaDemoActivity : AppCompatActivity() {
    // ... 其他代码 ...
    
    // 在蓝牙接收回调中调用
    fun handleBluetoothMessage(rawData: ByteArray) {
        onBluetoothDataReceived(rawData)
    }
}
```

### ⚠️ 协议解析

Demo 中的 `parseCmdId()` 和 `extractPayload()` 方法只是示例实现，客户需要根据自己实际的协议格式进行修改。

## UI 说明

- **选择文件按钮**：打开文件选择器
- **开始传输按钮**：开始 OTA 传输
- **取消传输按钮**：取消当前传输
- **文件信息**：显示选中的文件信息
- **状态显示**：显示当前传输状态
- **进度条**：显示传输进度（0-100%）
- **日志区域**：显示详细的传输日志

## 文件类型支持

- **.up 文件**：普通 OTA 文件，使用 `OtaFileType.OTA`
- **.upex 文件**：压缩的 OTA 文件，使用 `OtaFileType.OTA_UPEX`，SDK 会自动解压

## 错误处理

Demo Activity 会处理以下错误情况：

- 文件不存在
- 不支持的文件类型
- 传输超时
- 设备断开连接
- 设备忙碌
- 设备内存不足
- 设备电量不足

所有错误都会在日志中显示，并在 Toast 中提示用户。

## 注意事项

1. **权限**：确保应用有文件读取权限
2. **蓝牙连接**：确保设备已连接蓝牙
3. **文件大小**：大文件传输可能需要较长时间
4. **线程安全**：SDK 内部已处理线程安全，但客户实现接口时也需要注意

## 完整示例

参考 `OtaDemoActivity.kt` 中的完整实现代码。
