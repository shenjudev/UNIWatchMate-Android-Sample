//[lib-interface](../../../index.md)/[com.base.sdk.entity.apps](../index.md)/[TodayWeather](index.md)

# TodayWeather

[androidJvm]\
data class [TodayWeather](index.md)(val curTemp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), val tempUnit: [WmUnitInfo.TemperatureUnit](../../com.base.sdk.entity.settings/-wm-unit-info/-temperature-unit/index.md), val humidity: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val uvIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val weatherCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val weatherDesc: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val date: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), val hour: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

## Constructors

| | |
|---|---|
| [TodayWeather](-today-weather.md) | [androidJvm]<br>constructor(curTemp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), tempUnit: [WmUnitInfo.TemperatureUnit](../../com.base.sdk.entity.settings/-wm-unit-info/-temperature-unit/index.md), humidity: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), uvIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), weatherCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), weatherDesc: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), date: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), hour: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [curTemp](cur-temp.md) | [androidJvm]<br>val [curTemp](cur-temp.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>当前温度（current temperature） |
| [date](date.md) | [androidJvm]<br>val [date](date.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>日期（date） |
| [hour](hour.md) | [androidJvm]<br>val [hour](hour.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>小时（hour） |
| [humidity](humidity.md) | [androidJvm]<br>val [humidity](humidity.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>湿度（humidity） |
| [tempUnit](temp-unit.md) | [androidJvm]<br>val [tempUnit](temp-unit.md): [WmUnitInfo.TemperatureUnit](../../com.base.sdk.entity.settings/-wm-unit-info/-temperature-unit/index.md)<br>温度单位（temperature unit） |
| [uvIndex](uv-index.md) | [androidJvm]<br>val [uvIndex](uv-index.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>紫外线指数（ultraviolet index） |
| [weatherCode](weather-code.md) | [androidJvm]<br>val [weatherCode](weather-code.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>白天天气代码（day weather code） |
| [weatherDesc](weather-desc.md) | [androidJvm]<br>val [weatherDesc](weather-desc.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>白天天气描述（day weather description） |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
