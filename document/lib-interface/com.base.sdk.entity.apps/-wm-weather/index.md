//[lib-interface](../../../index.md)/[com.base.sdk.entity.apps](../index.md)/[WmWeather](index.md)

# WmWeather

[androidJvm]\
data class [WmWeather](index.md)(val pubDate: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), val location: [WmLocation](../-wm-location/index.md), val weatherForecast: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmWeatherForecast](../-wm-weather-forecast/index.md)&gt;, val todayWeather: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[TodayWeather](../-today-weather/index.md)&gt;)

## Constructors

| | |
|---|---|
| [WmWeather](-wm-weather.md) | [androidJvm]<br>constructor(pubDate: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), location: [WmLocation](../-wm-location/index.md), weatherForecast: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmWeatherForecast](../-wm-weather-forecast/index.md)&gt;, todayWeather: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[TodayWeather](../-today-weather/index.md)&gt;) |

## Properties

| Name | Summary |
|---|---|
| [location](location.md) | [androidJvm]<br>val [location](location.md): [WmLocation](../-wm-location/index.md)<br>地理位置（location） |
| [pubDate](pub-date.md) | [androidJvm]<br>val [pubDate](pub-date.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>发布时间/毫秒（publish time） |
| [todayWeather](today-weather.md) | [androidJvm]<br>val [todayWeather](today-weather.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[TodayWeather](../-today-weather/index.md)&gt;<br>今日24小时天气预报(24 hours weather forecast today) |
| [weatherForecast](weather-forecast.md) | [androidJvm]<br>val [weatherForecast](weather-forecast.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmWeatherForecast](../-wm-weather-forecast/index.md)&gt;<br>7天天气预报（weather forecast for 7 days） |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
