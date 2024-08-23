//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmSleepItem](index.md)

# WmSleepItem

class [WmSleepItem](index.md)(val status: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val duration: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [ICalculateSleepItem](../-i-calculate-sleep-item/index.md)

睡眠项(Sleep item)

#### Parameters

androidJvm

| | |
|---|---|
| status | 睡眠状态（Sleep status） |
| duration | 持续时间（分钟）(Sleep duration in minutes) |

## Constructors

| | |
|---|---|
| [WmSleepItem](-wm-sleep-item.md) | [androidJvm]<br>constructor(status: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), duration: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Properties

| Name | Summary |
|---|---|
| [duration](duration.md) | [androidJvm]<br>val [duration](duration.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [status](status.md) | [androidJvm]<br>val [status](status.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [getCalculateStartTime](get-calculate-start-time.md) | [androidJvm]<br>open override fun [getCalculateStartTime](get-calculate-start-time.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [getCalculateStatus](get-calculate-status.md) | [androidJvm]<br>open override fun [getCalculateStatus](get-calculate-status.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
