//[lib-interface](../../../index.md)/[com.base.sdk](../index.md)/[AbUniWatch](index.md)

# AbUniWatch

[androidJvm]\
abstract class [AbUniWatch](index.md)

sdk接口抽象类 封装了几大功能模块 sdk需要实现此接口，并实现每一个功能模块下的功能 拿到此接口实例即可操作sdk实现的所有功能

sdk interface abstract class Encapsulates several major functional modules The sdk implementation class needs to implement this interface and implement the functions under each functional module App can operate all functions implemented by sdk after getting an instance of this interface implementation class.

## Constructors

| | |
|---|---|
| [AbUniWatch](-ab-uni-watch.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [observeBatteryChange](observe-battery-change.md) | [androidJvm]<br>abstract val [observeBatteryChange](observe-battery-change.md): Observable&lt;[WmBatteryInfo](../../com.base.sdk.entity.data/-wm-battery-info/index.md)&gt;<br>监听电量信息变化(Observe battery information change) |
| [observeConnectBack](observe-connect-back.md) | [androidJvm]<br>abstract val [observeConnectBack](observe-connect-back.md): Observable&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;<br>监听回连事件(Observe reconnect event) |
| [observeConnectState](observe-connect-state.md) | [androidJvm]<br>abstract val [observeConnectState](observe-connect-state.md): Observable&lt;[WmConnectState](../../com.base.sdk.entity.apps/-wm-connect-state/index.md)&gt;<br>监听连接状态(Observe connect state) |
| [wmApps](wm-apps.md) | [androidJvm]<br>abstract val [wmApps](wm-apps.md): [AbWmApps](../../com.base.sdk.port.app/-ab-wm-apps/index.md)<br>应用模块(Application module) |
| [wmLog](wm-log.md) | [androidJvm]<br>abstract val [wmLog](wm-log.md): [AbWmLog](../../com.base.sdk.port.log/-ab-wm-log/index.md)<br>日志(Log) |
| [wmSettings](wm-settings.md) | [androidJvm]<br>abstract val [wmSettings](wm-settings.md): [AbWmSettings](../../com.base.sdk.port.setting/-ab-wm-settings/index.md)<br>设置模块(Settings module) |
| [wmSync](wm-sync.md) | [androidJvm]<br>abstract val [wmSync](wm-sync.md): [AbWmSyncs](../../com.base.sdk.port.sync/-ab-wm-syncs/index.md)<br>同步模块(Sync module) |
| [wmSyncTestFile](wm-sync-test-file.md) | [androidJvm]<br>abstract val [wmSyncTestFile](wm-sync-test-file.md): [AbWmSyncTestFile](../../com.base.sdk.port/-ab-wm-sync-test-file/index.md)<br>测试文件同步(Test file sync) |
| [wmTransferFile](wm-transfer-file.md) | [androidJvm]<br>abstract val [wmTransferFile](wm-transfer-file.md): [AbWmTransferFile](../../com.base.sdk.port/-ab-wm-transfer-file/index.md)<br>文件传输(File transfer) |

## Functions

| Name | Summary |
|---|---|
| [connect](connect.md) | [androidJvm]<br>abstract fun [connect](connect.md)(device: [BluetoothDevice](https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothDevice.html), bindInfo: [WmBindInfo](../../com.base.sdk.entity/-wm-bind-info/index.md)): [WmDevice](../../com.base.sdk.entity/-wm-device/index.md)?<br>abstract fun [connect](connect.md)(address: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), bindInfo: [WmBindInfo](../../com.base.sdk.entity/-wm-bind-info/index.md)): [WmDevice](../../com.base.sdk.entity/-wm-device/index.md)?<br>连接设备(Connect device) |
| [connectScanQr](connect-scan-qr.md) | [androidJvm]<br>abstract fun [connectScanQr](connect-scan-qr.md)(bindInfo: [WmBindInfo](../../com.base.sdk.entity/-wm-bind-info/index.md)): [WmDevice](../../com.base.sdk.entity/-wm-device/index.md)?<br>扫描二维码连接(connect by scanning qr code) |
| [disconnect](disconnect.md) | [androidJvm]<br>abstract fun [disconnect](disconnect.md)()<br>断开连接(Disconnect) |
| [getBatteryInfo](get-battery-info.md) | [androidJvm]<br>abstract fun [getBatteryInfo](get-battery-info.md)(): Single&lt;[WmBatteryInfo](../../com.base.sdk.entity.data/-wm-battery-info/index.md)&gt;<br>获取电量信息(Get battery information) |
| [getConnectState](get-connect-state.md) | [androidJvm]<br>abstract fun [getConnectState](get-connect-state.md)(): [WmConnectState](../../com.base.sdk.entity.apps/-wm-connect-state/index.md)<br>获取连接状态(Get connect state) |
| [getDeviceInfo](get-device-info.md) | [androidJvm]<br>abstract fun [getDeviceInfo](get-device-info.md)(): Single&lt;[WmDeviceInfo](../../com.base.sdk.entity.settings/-wm-device-info/index.md)&gt;<br>获取设备信息(Get device information) |
| [getDeviceModel](get-device-model.md) | [androidJvm]<br>abstract fun [getDeviceModel](get-device-model.md)(): [WmDeviceModel](../../com.base.sdk.entity/-wm-device-model/index.md)?<br>获取当前设备模式(Get current device mode) |
| [getDeviceStorageInfo](get-device-storage-info.md) | [androidJvm]<br>abstract fun [getDeviceStorageInfo](get-device-storage-info.md)(memoryType: [WmStorageType](../../com.base.sdk.entity.common/-wm-storage-type/index.md)): Single&lt;[WmDeviceStorageInfo](../../com.base.sdk.entity.common/-wm-device-storage-info/index.md)&gt;<br>获取设备存储空间信息(Get device storage space information) |
| [getFunctionSupportState](get-function-support-state.md) | [androidJvm]<br>abstract fun [getFunctionSupportState](get-function-support-state.md)(): [WmFunctionSupport](../../com.base.sdk.entity.settings/-wm-function-support/index.md)<br>查询功能清单配置(Query function list configuration) |
| [reboot](reboot.md) | [androidJvm]<br>abstract fun [reboot](reboot.md)(): Completable<br>设备重启(Device reboot) |
| [reset](reset.md) | [androidJvm]<br>abstract fun [reset](reset.md)(): Completable<br>恢复出厂设置(Restore factory settings) |
| [setAppCurrentDevice](set-app-current-device.md) | [androidJvm]<br>abstract fun [setAppCurrentDevice](set-app-current-device.md)(wmDevice: [WmDevice](../../com.base.sdk.entity/-wm-device/index.md))<br>App当前连接的设备(App current connected device) |
| [setDeviceModel](set-device-model.md) | [androidJvm]<br>abstract fun [setDeviceModel](set-device-model.md)(wmDeviceModel: [WmDeviceModel](../../com.base.sdk.entity/-wm-device-model/index.md)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>设置设备模式，支持多个SDK实现(Set device mode, support multiple SDK implementations) If a SDK supports multiple modes, save the current mode so that [getDeviceModel](get-device-model.md) can be obtained. |
| [setLogEnable](set-log-enable.md) | [androidJvm]<br>abstract fun [setLogEnable](set-log-enable.md)(logEnable: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))<br>是否支持日志打印(Whether to print log) |
| [startDiscovery](start-discovery.md) | [androidJvm]<br>abstract fun [startDiscovery](start-discovery.md)(scanTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), wmTimeUnit: [WmTimeUnit](../../com.base.sdk.entity.common/-wm-time-unit/index.md), deviceModel: [WmDeviceModel](../../com.base.sdk.entity/-wm-device-model/index.md), tag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): Observable&lt;[WmDiscoverDevice](../../com.base.sdk.entity.common/-wm-discover-device/index.md)&gt;<br>开始扫描设备(Start scanning device) |
| [stopDiscovery](stop-discovery.md) | [androidJvm]<br>abstract fun [stopDiscovery](stop-discovery.md)()<br>停止蓝牙扫描(Stop Bluetooth scanning) |
