//[lib-interface](../../../index.md)/[com.base.sdk.entity.settings](../index.md)/[WmDateTime](index.md)

# WmDateTime

[androidJvm]\
data class [WmDateTime](index.md)(val timeZone: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val timestamp: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)?, val currentTime: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, val currentDate: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?)

The date and time information of the watch (日期与时间同步信息)

## Constructors

| | |
|---|---|
| [WmDateTime](-wm-date-time.md) | [androidJvm]<br>constructor(timeZone: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, timestamp: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)?, currentTime: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, currentDate: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) |

## Properties

| Name | Summary |
|---|---|
| [currentDate](current-date.md) | [androidJvm]<br>val [currentDate](current-date.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>The current date, such as &quot;2020-01-20&quot; |
| [currentTime](current-time.md) | [androidJvm]<br>val [currentTime](current-time.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>The current time, such as &quot;12:00:00&quot; |
| [timestamp](timestamp.md) | [androidJvm]<br>val [timestamp](timestamp.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)?<br>The timestamp, the milliseconds since January 1, 1970, 00:00:00 GMT. Optional. |
| [timeZone](time-zone.md) | [androidJvm]<br>val [timeZone](time-zone.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?<br>The time zone ID string, such as &quot;Asia/Shanghai&quot;, &quot;America/Los_Angeles&quot;, &quot;Europe/London&quot;, etc. |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
