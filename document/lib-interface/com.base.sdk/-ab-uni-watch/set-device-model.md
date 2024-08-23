//[lib-interface](../../../index.md)/[com.base.sdk](../index.md)/[AbUniWatch](index.md)/[setDeviceModel](set-device-model.md)

# setDeviceModel

[androidJvm]\
abstract fun [setDeviceModel](set-device-model.md)(wmDeviceModel: [WmDeviceModel](../../com.base.sdk.entity/-wm-device-model/index.md)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

设置设备模式，支持多个SDK实现(Set device mode, support multiple SDK implementations) If a SDK supports multiple modes, save the current mode so that [getDeviceModel](get-device-model.md) can be obtained.

#### Return

if the return value is false, it means that the device mode is not supported

#### Parameters

androidJvm

| | |
|---|---|
| wmDeviceModel | device model |
