//[lib-interface](../../../index.md)/[com.base.sdk.entity.apps](../index.md)/[WmWeatherForecast](index.md)

# WmWeatherForecast

[androidJvm]\
data class [WmWeatherForecast](index.md)(val lowTemp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), val highTemp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), val curTemp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), val tempUnit: [WmUnitInfo.TemperatureUnit](../../com.base.sdk.entity.settings/-wm-unit-info/-temperature-unit/index.md), val humidity: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), val humidityNight: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), val uvIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val uvIndexNight: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val dayCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val nightCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val dayDesc: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val nightDesc: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val date: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), val week: [WmWeek](../../com.base.sdk.entity.common/-wm-week/index.md))

天气（weather）

## Constructors

| | |
|---|---|
| [WmWeatherForecast](-wm-weather-forecast.md) | [androidJvm]<br>constructor(lowTemp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), highTemp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), curTemp: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), tempUnit: [WmUnitInfo.TemperatureUnit](../../com.base.sdk.entity.settings/-wm-unit-info/-temperature-unit/index.md), humidity: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), humidityNight: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), uvIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), uvIndexNight: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), dayCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), nightCode: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), dayDesc: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), nightDesc: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), date: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), week: [WmWeek](../../com.base.sdk.entity.common/-wm-week/index.md)) |

## Properties

| Name | Summary |
|---|---|
| [curTemp](cur-temp.md) | [androidJvm]<br>val [curTemp](cur-temp.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>当前温度（current temperature） |
| [date](date.md) | [androidJvm]<br>val [date](date.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>日期（date） |
| [dayCode](day-code.md) | [androidJvm]<br>val [dayCode](day-code.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>白天天气代码（day weather code） |
| [dayDesc](day-desc.md) | [androidJvm]<br>val [dayDesc](day-desc.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>白天天气描述（day weather description） |
| [highTemp](high-temp.md) | [androidJvm]<br>val [highTemp](high-temp.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>最高温度（high temperature） |
| [humidity](humidity.md) | [androidJvm]<br>val [humidity](humidity.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>湿度（humidity） |
| [humidityNight](humidity-night.md) | [androidJvm]<br>val [humidityNight](humidity-night.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html) |
| [lowTemp](low-temp.md) | [androidJvm]<br>val [lowTemp](low-temp.md): [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html)<br>最低温度（low temperature） |
| [nightCode](night-code.md) | [androidJvm]<br>val [nightCode](night-code.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>夜晚天气代码（night weather code） |
| [nightDesc](night-desc.md) | [androidJvm]<br>val [nightDesc](night-desc.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>夜晚天气描述（night weather description） |
| [tempUnit](temp-unit.md) | [androidJvm]<br>val [tempUnit](temp-unit.md): [WmUnitInfo.TemperatureUnit](../../com.base.sdk.entity.settings/-wm-unit-info/-temperature-unit/index.md)<br>温度单位（temperature unit） |
| [uvIndex](uv-index.md) | [androidJvm]<br>val [uvIndex](uv-index.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>紫外线指数（ultraviolet index） |
| [uvIndexNight](uv-index-night.md) | [androidJvm]<br>val [uvIndexNight](uv-index-night.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [week](week.md) | [androidJvm]<br>val [week](week.md): [WmWeek](../../com.base.sdk.entity.common/-wm-week/index.md)<br>星期（week） |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
