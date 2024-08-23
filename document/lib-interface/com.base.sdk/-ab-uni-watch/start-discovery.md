//[lib-interface](../../../index.md)/[com.base.sdk](../index.md)/[AbUniWatch](index.md)/[startDiscovery](start-discovery.md)

# startDiscovery

[androidJvm]\
abstract fun [startDiscovery](start-discovery.md)(scanTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), wmTimeUnit: [WmTimeUnit](../../com.base.sdk.entity.common/-wm-time-unit/index.md), deviceModel: [WmDeviceModel](../../com.base.sdk.entity/-wm-device-model/index.md), tag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): Observable&lt;[WmDiscoverDevice](../../com.base.sdk.entity.common/-wm-discover-device/index.md)&gt;

开始扫描设备(Start scanning device)

#### Parameters

androidJvm

| | |
|---|---|
| scanTime | 扫描时间(scan time, unit: second) |
| wmTimeUnit | 扫描时间单位(scan time unit) |
| deviceModel | 设备类型(device type) |
| tag | 过滤设备名字前/后缀(filter device name prefix/suffix, eg. oraimo Watch Neo) |
