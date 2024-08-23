//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmBloodPressureMeasureData](index.md)

# WmBloodPressureMeasureData

[androidJvm]\
class [WmBloodPressureMeasureData](index.md)(val sbp: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val dbp: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val heartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [WmBaseSyncData](../-wm-base-sync-data/index.md)

## Constructors

| | |
|---|---|
| [WmBloodPressureMeasureData](-wm-blood-pressure-measure-data.md) | [androidJvm]<br>constructor(sbp: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), dbp: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), heartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [dbp](dbp.md) | [androidJvm]<br>val [dbp](dbp.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>diastolic blood pressure (unit mmHg) |
| [heartRate](heart-rate.md) | [androidJvm]<br>val [heartRate](heart-rate.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Additional heart rate values. This value exists only if WmDeviceInfo.Feature.BLOOD_PRESSURE_AIR_PUMP is support |
| [sbp](sbp.md) | [androidJvm]<br>val [sbp](sbp.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>systolic blood pressure (unit mmHg) |
| [timestamp](../-wm-base-sync-data/timestamp.md) | [androidJvm]<br>var [timestamp](../-wm-base-sync-data/timestamp.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
