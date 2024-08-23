//[lib-interface](../../../index.md)/[com.base.sdk.entity.apps](../index.md)/[WmFind](index.md)

# WmFind

data class [WmFind](index.md)(val count: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val timeSeconds: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

查找手机/手表(Find phone/watch)

#### Parameters

androidJvm

| | |
|---|---|
| count | number of rings，which depends on the device firmware, 0: continuous ringing, others: number of rings |
| timeSeconds | ring time in seconds |

## Constructors

| | |
|---|---|
| [WmFind](-wm-find.md) | [androidJvm]<br>constructor(count: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), timeSeconds: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [count](count.md) | [androidJvm]<br>val [count](count.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [timeSeconds](time-seconds.md) | [androidJvm]<br>val [timeSeconds](time-seconds.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
