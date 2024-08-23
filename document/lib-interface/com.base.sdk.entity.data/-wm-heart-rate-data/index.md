//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmHeartRateData](index.md)

# WmHeartRateData

[androidJvm]\
class [WmHeartRateData](index.md)(val maxHeartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val minHeartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val avgHeartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val duration: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0) : [WmBaseSyncData](../-wm-base-sync-data/index.md)

心率数据(Heart rate data)

## Constructors

| | |
|---|---|
| [WmHeartRateData](-wm-heart-rate-data.md) | [androidJvm]<br>constructor(maxHeartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), minHeartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), avgHeartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), duration: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0) |

## Properties

| Name | Summary |
|---|---|
| [avgHeartRate](avg-heart-rate.md) | [androidJvm]<br>val [avgHeartRate](avg-heart-rate.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>average heart rate value |
| [duration](duration.md) | [androidJvm]<br>val [duration](duration.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = 0<br>activity duration, in seconds |
| [maxHeartRate](max-heart-rate.md) | [androidJvm]<br>val [maxHeartRate](max-heart-rate.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>maximum heart rate value |
| [minHeartRate](min-heart-rate.md) | [androidJvm]<br>val [minHeartRate](min-heart-rate.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>minimum heart rate value |
| [timestamp](../-wm-base-sync-data/timestamp.md) | [androidJvm]<br>var [timestamp](../-wm-base-sync-data/timestamp.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
