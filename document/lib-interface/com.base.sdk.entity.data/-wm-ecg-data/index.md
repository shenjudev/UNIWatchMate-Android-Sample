//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmEcgData](index.md)

# WmEcgData

[androidJvm]\
class [WmEcgData](index.md)(val samplingRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val items: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)&gt;) : [WmBaseSyncData](../-wm-base-sync-data/index.md)

The ecg data.

If WmDeviceInfo.Feature.TI_ECG is supported, you can adjust the speed and amplitude of ECG data.

## Constructors

| | |
|---|---|
| [WmEcgData](-wm-ecg-data.md) | [androidJvm]<br>constructor(samplingRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), items: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)&gt;) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Properties

| Name | Summary |
|---|---|
| [items](items.md) | [androidJvm]<br>val [items](items.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)&gt;<br>Ecg values |
| [samplingRate](sampling-rate.md) | [androidJvm]<br>val [samplingRate](sampling-rate.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Sampling rate (number of ECG values per second) |
| [timestamp](../-wm-base-sync-data/timestamp.md) | [androidJvm]<br>var [timestamp](../-wm-base-sync-data/timestamp.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
