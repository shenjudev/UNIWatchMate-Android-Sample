//[lib-interface](../../../index.md)/[com.base.sdk.entity.settings](../index.md)/[WmDeviceInfo](index.md)

# WmDeviceInfo

[androidJvm]\
data class [WmDeviceInfo](index.md)(val model: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val macAddress: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val version: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val deviceId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val bluetoothName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val deviceName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val dialAbility: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val screen: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), var lang: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var cw: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var ch: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var ncw: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var nch: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

Device information(设备信息)

## Constructors

| | |
|---|---|
| [WmDeviceInfo](-wm-device-info.md) | [androidJvm]<br>constructor(model: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), macAddress: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), version: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), deviceId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), bluetoothName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), deviceName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), dialAbility: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), screen: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), lang: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), cw: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), ch: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), ncw: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), nch: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [bluetoothName](bluetooth-name.md) | [androidJvm]<br>val [bluetoothName](bluetooth-name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>bluetooth name(蓝牙名称) |
| [ch](ch.md) | [androidJvm]<br>var [ch](ch.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>设备屏幕 高 screen height |
| [cw](cw.md) | [androidJvm]<br>var [cw](cw.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>设备屏幕 宽 screen width |
| [deviceId](device-id.md) | [androidJvm]<br>val [deviceId](device-id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>device id(设备id) |
| [deviceName](device-name.md) | [androidJvm]<br>val [deviceName](device-name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>device name(设备名称) |
| [dialAbility](dial-ability.md) | [androidJvm]<br>val [dialAbility](dial-ability.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>适配表盘 |
| [lang](lang.md) | [androidJvm]<br>var [lang](lang.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>设备当前语言 device language |
| [macAddress](mac-address.md) | [androidJvm]<br>val [macAddress](mac-address.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>device mac address(设备mac地址) |
| [model](model.md) | [androidJvm]<br>val [model](model.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>device model(设备型号) |
| [nch](nch.md) | [androidJvm]<br>var [nch](nch.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>导航 宽 |
| [ncw](ncw.md) | [androidJvm]<br>var [ncw](ncw.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>导航 高 |
| [screen](screen.md) | [androidJvm]<br>val [screen](screen.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>屏幕规格 screen model |
| [version](version.md) | [androidJvm]<br>val [version](version.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>device version(设备版本) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
