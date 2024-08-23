//[lib-interface](../../../index.md)/[com.base.sdk.entity.apps](../index.md)/[WmWeatherRequest](index.md)

# WmWeatherRequest

data class [WmWeatherRequest](index.md)(val bcp: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), val wmWeatherTime: [WmWeatherTime](../-wm-weather-time/index.md))

设备端天气请求(Device weather request)

#### Parameters

androidJvm

| | |
|---|---|
| bcp | 语言代码（BCP 47 format for languages） |
| wmWeatherTime | 请求当天，还是未来7天(Request the current day, or the next 7 days) |

## Constructors

| | |
|---|---|
| [WmWeatherRequest](-wm-weather-request.md) | [androidJvm]<br>constructor(bcp: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), wmWeatherTime: [WmWeatherTime](../-wm-weather-time/index.md)) |

## Properties

| Name | Summary |
|---|---|
| [bcp](bcp.md) | [androidJvm]<br>val [bcp](bcp.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [wmWeatherTime](wm-weather-time.md) | [androidJvm]<br>val [wmWeatherTime](wm-weather-time.md): [WmWeatherTime](../-wm-weather-time/index.md) |
