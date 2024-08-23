//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmTemperatureData](index.md)

# WmTemperatureData

[androidJvm]\
class [WmTemperatureData](index.md)(val body: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), val wrist: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)) : [WmBaseSyncData](../-wm-base-sync-data/index.md)

## Constructors

| | |
|---|---|
| [WmTemperatureData](-wm-temperature-data.md) | [androidJvm]<br>constructor(body: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), wrist: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [body](body.md) | [androidJvm]<br>val [body](body.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Temperature of your body(unit ℃)。 This value is generally in the normal body temperature range36℃-42℃. |
| [timestamp](../-wm-base-sync-data/timestamp.md) | [androidJvm]<br>var [timestamp](../-wm-base-sync-data/timestamp.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [wrist](wrist.md) | [androidJvm]<br>val [wrist](wrist.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>Temperature of your wrist(unit ℃)。 The range of this value is wider, because it is related to the ambient temperature, in extreme cases it may be below 0℃. |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
