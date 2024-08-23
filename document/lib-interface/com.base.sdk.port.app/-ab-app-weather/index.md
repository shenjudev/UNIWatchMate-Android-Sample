//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppWeather](index.md)

# AbAppWeather

[androidJvm]\
abstract class [AbAppWeather](index.md)

应用模块 - 天气同步(Application module - weather)

## Constructors

| | |
|---|---|
| [AbAppWeather](-ab-app-weather.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [observeWeather](observe-weather.md) | [androidJvm]<br>abstract val [observeWeather](observe-weather.md): Observable&lt;[WmWeatherRequest](../../com.base.sdk.entity.apps/-wm-weather-request/index.md)&gt;<br>监听设备端天气请求(Listen for the weather request from the device) |

## Functions

| Name | Summary |
|---|---|
| [pushSevenDaysWeather](push-seven-days-weather.md) | [androidJvm]<br>abstract fun [pushSevenDaysWeather](push-seven-days-weather.md)(weather: [WmWeather](../../com.base.sdk.entity.apps/-wm-weather/index.md), temperatureUnit: [WmUnitInfo.TemperatureUnit](../../com.base.sdk.entity.settings/-wm-unit-info/-temperature-unit/index.md)): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>为设备推送7天天气信息(Push 7-day weather information) |
| [pushTodayWeather](push-today-weather.md) | [androidJvm]<br>abstract fun [pushTodayWeather](push-today-weather.md)(weather: [WmWeather](../../com.base.sdk.entity.apps/-wm-weather/index.md), temperatureUnit: [WmUnitInfo.TemperatureUnit](../../com.base.sdk.entity.settings/-wm-unit-info/-temperature-unit/index.md)): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>为设备推送天气信息(Push weather information) |
