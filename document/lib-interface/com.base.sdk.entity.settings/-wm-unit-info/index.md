//[lib-interface](../../../index.md)/[com.base.sdk.entity.settings](../index.md)/[WmUnitInfo](index.md)

# WmUnitInfo

[androidJvm]\
data class [WmUnitInfo](index.md)(var weightUnit: [WmUnitInfo.WeightUnit](-weight-unit/index.md) = WeightUnit.KG, var temperatureUnit: [WmUnitInfo.TemperatureUnit](-temperature-unit/index.md), var timeFormat: [WmUnitInfo.TimeFormat](-time-format/index.md), var distanceUnit: [WmUnitInfo.DistanceUnit](-distance-unit/index.md))

Unit info(单位同步数据)

## Constructors

| | |
|---|---|
| [WmUnitInfo](-wm-unit-info.md) | [androidJvm]<br>constructor(weightUnit: [WmUnitInfo.WeightUnit](-weight-unit/index.md) = WeightUnit.KG, temperatureUnit: [WmUnitInfo.TemperatureUnit](-temperature-unit/index.md), timeFormat: [WmUnitInfo.TimeFormat](-time-format/index.md), distanceUnit: [WmUnitInfo.DistanceUnit](-distance-unit/index.md)) |

## Types

| Name | Summary |
|---|---|
| [DistanceUnit](-distance-unit/index.md) | [androidJvm]<br>enum [DistanceUnit](-distance-unit/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmUnitInfo.DistanceUnit](-distance-unit/index.md)&gt; |
| [TemperatureUnit](-temperature-unit/index.md) | [androidJvm]<br>enum [TemperatureUnit](-temperature-unit/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmUnitInfo.TemperatureUnit](-temperature-unit/index.md)&gt; |
| [TimeFormat](-time-format/index.md) | [androidJvm]<br>enum [TimeFormat](-time-format/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmUnitInfo.TimeFormat](-time-format/index.md)&gt; |
| [WeightUnit](-weight-unit/index.md) | [androidJvm]<br>enum [WeightUnit](-weight-unit/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)&lt;[WmUnitInfo.WeightUnit](-weight-unit/index.md)&gt; |

## Properties

| Name | Summary |
|---|---|
| [distanceUnit](distance-unit.md) | [androidJvm]<br>var [distanceUnit](distance-unit.md): [WmUnitInfo.DistanceUnit](-distance-unit/index.md)<br>Distance unit KM, MILE |
| [temperatureUnit](temperature-unit.md) | [androidJvm]<br>var [temperatureUnit](temperature-unit.md): [WmUnitInfo.TemperatureUnit](-temperature-unit/index.md)<br>Temperature unit CELSIUS, FAHRENHEIT |
| [timeFormat](time-format.md) | [androidJvm]<br>var [timeFormat](time-format.md): [WmUnitInfo.TimeFormat](-time-format/index.md)<br>Time format TWELVE_HOUR, TWENTY_FOUR_HOUR |
| [weightUnit](weight-unit.md) | [androidJvm]<br>var [weightUnit](weight-unit.md): [WmUnitInfo.WeightUnit](-weight-unit/index.md)<br>Weight unit KG, LB |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
