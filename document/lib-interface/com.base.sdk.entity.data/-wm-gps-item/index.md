//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmGpsItem](index.md)

# WmGpsItem

[androidJvm]\
class [WmGpsItem](index.md)(val duration: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val lng: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val lat: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), val altitude: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), val satellites: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val isRestart: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))

## Constructors

| | |
|---|---|
| [WmGpsItem](-wm-gps-item.md) | [androidJvm]<br>constructor(duration: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), lng: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), lat: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), altitude: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), satellites: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), isRestart: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [altitude](altitude.md) | [androidJvm]<br>val [altitude](altitude.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
| [duration](duration.md) | [androidJvm]<br>val [duration](duration.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The duration(unit seconds) of sport at which this item is generated |
| [isRestart](is-restart.md) | [androidJvm]<br>val [isRestart](is-restart.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Is it the first point after resuming sport? True for yes, false for not. |
| [lat](lat.md) | [androidJvm]<br>val [lat](lat.md): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [lng](lng.md) | [androidJvm]<br>val [lng](lng.md): [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [satellites](satellites.md) | [androidJvm]<br>val [satellites](satellites.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The number of satellites represents the strength of the signal at this time |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
