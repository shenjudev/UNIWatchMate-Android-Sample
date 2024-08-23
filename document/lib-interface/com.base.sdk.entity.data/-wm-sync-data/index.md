//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmSyncData](index.md)

# WmSyncData

[androidJvm]\
class [WmSyncData](index.md)&lt;[T](index.md) : [WmBaseSyncData](../-wm-base-sync-data/index.md)&gt;(val type: [WmSyncDataType](../-wm-sync-data-type/index.md), val timestamp: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), val intervalType: [WmIntervalType](../-wm-interval-type/index.md), val value: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](index.md)&gt;)

Sync Value 同步数据

## Constructors

| | |
|---|---|
| [WmSyncData](-wm-sync-data.md) | [androidJvm]<br>constructor(type: [WmSyncDataType](../-wm-sync-data-type/index.md), timestamp: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), intervalType: [WmIntervalType](../-wm-interval-type/index.md), value: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](index.md)&gt;) |

## Properties

| Name | Summary |
|---|---|
| [intervalType](interval-type.md) | [androidJvm]<br>val [intervalType](interval-type.md): [WmIntervalType](../-wm-interval-type/index.md)<br>time interval type |
| [timestamp](timestamp.md) | [androidJvm]<br>val [timestamp](timestamp.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>Timestamp of this data |
| [type](type.md) | [androidJvm]<br>val [type](type.md): [WmSyncDataType](../-wm-sync-data-type/index.md)<br>data type |
| [value](value.md) | [androidJvm]<br>val [value](value.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[T](index.md)&gt;<br>Sync value |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
