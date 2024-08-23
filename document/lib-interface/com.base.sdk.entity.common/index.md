//[lib-interface](../../index.md)/[com.base.sdk.entity.common](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [WmDeviceStorageInfo](-wm-device-storage-info/index.md) | [androidJvm]<br>data class [WmDeviceStorageInfo](-wm-device-storage-info/index.md)(val memory: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html))<br>Device storage information |
| [WmDiscoverDevice](-wm-discover-device/index.md) | [androidJvm]<br>data class [WmDiscoverDevice](-wm-discover-device/index.md)(val device: [BluetoothDevice](https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothDevice.html), val rss: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))<br>Discovery device |
| [WmNoDisturb](-wm-no-disturb/index.md) | [androidJvm]<br>data class [WmNoDisturb](-wm-no-disturb/index.md)(var isEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), var timeRange: [WmTimeRange](-wm-time-range/index.md))<br>No-disturb(免打扰) |
| [WmStorageType](-wm-storage-type/index.md) | [androidJvm]<br>enum [WmStorageType](-wm-storage-type/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmStorageType](-wm-storage-type/index.md)&gt; |
| [WmTimeFrequency](-wm-time-frequency/index.md) | [androidJvm]<br>enum [WmTimeFrequency](-wm-time-frequency/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmTimeFrequency](-wm-time-frequency/index.md)&gt; <br>Time frequency(频次) |
| [WmTimeRange](-wm-time-range/index.md) | [androidJvm]<br>data class [WmTimeRange](-wm-time-range/index.md)(var startHour: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var startMinute: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var endHour: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var endMinute: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))<br>Time range(时间范围) |
| [WmTimeUnit](-wm-time-unit/index.md) | [androidJvm]<br>enum [WmTimeUnit](-wm-time-unit/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmTimeUnit](-wm-time-unit/index.md)&gt; <br>时间单位(Time unit) |
| [WmWeek](-wm-week/index.md) | [androidJvm]<br>enum [WmWeek](-wm-week/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmWeek](-wm-week/index.md)&gt; <br>Week(星期) |
