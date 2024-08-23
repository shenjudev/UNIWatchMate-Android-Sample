//[lib-interface](../../../index.md)/[com.base.sdk.entity](../index.md)/[WmBindInfo](index.md)

# WmBindInfo

data class [WmBindInfo](index.md)(val userId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val userName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val macAddress: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), var bindType: [BindType](../-bind-type/index.md), val deviceType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), var model: [WmDeviceModel](../-wm-device-model/index.md) = WmDeviceModel.NOT_REG)

连接时所需的绑定信息(Binding information when connecting)

#### Parameters

androidJvm

| | |
|---|---|
| userId | 用户ID(User ID) |
| userName | 用户昵称(User nickname) |
| macAddress | 蓝牙地址(Bluetooth address) |
| bindType | 绑定类型(Binding type) |
| deviceType | 设备类型(Device type) |
| model | 设备型号(Device model) |

## Constructors

| | |
|---|---|
| [WmBindInfo](-wm-bind-info.md) | [androidJvm]<br>constructor(userId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), userName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), macAddress: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), bindType: [BindType](../-bind-type/index.md), deviceType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), model: [WmDeviceModel](../-wm-device-model/index.md) = WmDeviceModel.NOT_REG) |

## Properties

| Name | Summary |
|---|---|
| [bindType](bind-type.md) | [androidJvm]<br>var [bindType](bind-type.md): [BindType](../-bind-type/index.md) |
| [deviceType](device-type.md) | [androidJvm]<br>val [deviceType](device-type.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [macAddress](mac-address.md) | [androidJvm]<br>val [macAddress](mac-address.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [model](model.md) | [androidJvm]<br>var [model](model.md): [WmDeviceModel](../-wm-device-model/index.md) |
| [randomCode](random-code.md) | [androidJvm]<br>var [randomCode](random-code.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? |
| [userId](user-id.md) | [androidJvm]<br>val [userId](user-id.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [userName](user-name.md) | [androidJvm]<br>val [userName](user-name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
