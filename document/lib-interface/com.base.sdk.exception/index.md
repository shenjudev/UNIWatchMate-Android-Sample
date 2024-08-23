//[lib-interface](../../index.md)/[com.base.sdk.exception](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [WmDisconnectedException](-wm-disconnected-exception/index.md) | [androidJvm]<br>class [WmDisconnectedException](-wm-disconnected-exception/index.md)(address: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null) : [WmException](-wm-exception/index.md)<br>表示设备连接的异常，一般在SDK不能识别此设备的时候出现 (Exception when the device is not recognized by the SDK) |
| [WmException](-wm-exception/index.md) | [androidJvm]<br>abstract class [WmException](-wm-exception/index.md) : [RuntimeException](https://developer.android.com/reference/kotlin/java/lang/RuntimeException.html)<br>统一抛出的异常，其他SDK抛出的异常最好转换为 WmException或其子类 (Unified exception, the exception thrown by other SDKs should be converted to WmException or its subclass) |
| [WmInvalidParamsException](-wm-invalid-params-exception/index.md) | [androidJvm]<br>class [WmInvalidParamsException](-wm-invalid-params-exception/index.md)(val msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [WmException](-wm-exception/index.md)<br>表示传入参数错误引起的异常 (Exception when the parameters entered by the user are invalid) |
| [WmTimeOutException](-wm-time-out-exception/index.md) | [androidJvm]<br>class [WmTimeOutException](-wm-time-out-exception/index.md)(val msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [WmException](-wm-exception/index.md)<br>表示设备消息超时引起的异常 (Exception when the device message is timed out) |
| [WmTransferError](-wm-transfer-error/index.md) | [androidJvm]<br>enum [WmTransferError](-wm-transfer-error/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmTransferError](-wm-transfer-error/index.md)&gt; |
| [WmTransferException](-wm-transfer-exception/index.md) | [androidJvm]<br>class [WmTransferException](-wm-transfer-exception/index.md)(val error: [WmTransferError](-wm-transfer-error/index.md), val msg: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) : [WmException](-wm-exception/index.md)<br>表示设备端传输引起的异常（Exception when the device is transferred by the device) |
