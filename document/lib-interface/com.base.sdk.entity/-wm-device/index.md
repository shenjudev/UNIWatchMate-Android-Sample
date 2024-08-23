//[lib-interface](../../../index.md)/[com.base.sdk.entity](../index.md)/[WmDevice](index.md)

# WmDevice

open class [WmDevice](index.md)(var mode: [WmDeviceModel](../-wm-device-model/index.md))

连接时返回的设备信息(Device information returned when connecting)

#### Parameters

androidJvm

| | |
|---|---|
| mode | 设备型号(Device model) |

## Constructors

| | |
|---|---|
| [WmDevice](-wm-device.md) | [androidJvm]<br>constructor(mode: [WmDeviceModel](../-wm-device-model/index.md)) |

## Properties

| Name | Summary |
|---|---|
| [address](address.md) | [androidJvm]<br>var [address](address.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>device mac address |
| [isRecognized](is-recognized.md) | [androidJvm]<br>var [isRecognized](is-recognized.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Whether the device is recognized |
| [mode](mode.md) | [androidJvm]<br>var [mode](mode.md): [WmDeviceModel](../-wm-device-model/index.md) |
| [name](name.md) | [androidJvm]<br>var [name](name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>device name |
| [randomCode](random-code.md) | [androidJvm]<br>var [randomCode](random-code.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>Random code from the device, used to authenticate |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
