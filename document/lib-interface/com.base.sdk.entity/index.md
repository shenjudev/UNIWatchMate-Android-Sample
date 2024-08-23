//[lib-interface](../../index.md)/[com.base.sdk.entity](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [BindType](-bind-type/index.md) | [androidJvm]<br>enum [BindType](-bind-type/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[BindType](-bind-type/index.md)&gt; |
| [WmBindInfo](-wm-bind-info/index.md) | [androidJvm]<br>data class [WmBindInfo](-wm-bind-info/index.md)(val userId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val userName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val macAddress: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), var bindType: [BindType](-bind-type/index.md), val deviceType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), var model: [WmDeviceModel](-wm-device-model/index.md) = WmDeviceModel.NOT_REG)<br>连接时所需的绑定信息(Binding information when connecting) |
| [WmDevice](-wm-device/index.md) | [androidJvm]<br>open class [WmDevice](-wm-device/index.md)(var mode: [WmDeviceModel](-wm-device-model/index.md))<br>连接时返回的设备信息(Device information returned when connecting) |
| [WmDeviceModel](-wm-device-model/index.md) | [androidJvm]<br>enum [WmDeviceModel](-wm-device-model/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmDeviceModel](-wm-device-model/index.md)&gt; <br>Device Factory |
