//[lib-interface](../../../index.md)/[com.base.sdk](../index.md)/[AbUniWatch](index.md)/[connect](connect.md)

# connect

[androidJvm]\
abstract fun [connect](connect.md)(address: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), bindInfo: [WmBindInfo](../../com.base.sdk.entity/-wm-bind-info/index.md)): [WmDevice](../../com.base.sdk.entity/-wm-device/index.md)?

连接设备(Connect device)

#### Return

if the return value is null, it means that the device type cannot be recognized

#### Parameters

androidJvm

| | |
|---|---|
| address | mac address |
| bindInfo | bind info |

[androidJvm]\
abstract fun [connect](connect.md)(device: [BluetoothDevice](https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothDevice.html), bindInfo: [WmBindInfo](../../com.base.sdk.entity/-wm-bind-info/index.md)): [WmDevice](../../com.base.sdk.entity/-wm-device/index.md)?

连接设备(Connect device)

#### Return

if the return value is null, it means that the device type cannot be recognized

#### Parameters

androidJvm

| | |
|---|---|
| device | bluetooth device |
| bindInfo | bind info |
