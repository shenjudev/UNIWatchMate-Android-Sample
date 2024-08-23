//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmSleepSummary](index.md)

# WmSleepSummary

[androidJvm]\
data class [WmSleepSummary](index.md)(var dateStamp: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), var bedTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var getUpTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var totalSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var sleepType: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var awakeSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var lightSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var deepSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var remSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var awakeSleepCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var lightSleepCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var deepSleepCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var remSleepCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var awakePercentage: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var lightSleepPercentage: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var deepSleepPercentage: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var remSleepPercentage: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var sleepScore: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

SleepSummary 睡眠概览

## Constructors

| | |
|---|---|
| [WmSleepSummary](-wm-sleep-summary.md) | [androidJvm]<br>constructor(dateStamp: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), bedTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), getUpTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), totalSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), sleepType: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), awakeSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), lightSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), deepSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), remSleepMinutes: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), awakeSleepCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), lightSleepCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), deepSleepCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), remSleepCount: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), awakePercentage: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), lightSleepPercentage: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), deepSleepPercentage: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), remSleepPercentage: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), sleepScore: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [awakePercentage](awake-percentage.md) | [androidJvm]<br>var [awakePercentage](awake-percentage.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The percentage of awake |
| [awakeSleepCount](awake-sleep-count.md) | [androidJvm]<br>var [awakeSleepCount](awake-sleep-count.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Awake sleep count |
| [awakeSleepMinutes](awake-sleep-minutes.md) | [androidJvm]<br>var [awakeSleepMinutes](awake-sleep-minutes.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>awake sleep minutes |
| [bedTime](bed-time.md) | [androidJvm]<br>var [bedTime](bed-time.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Sleep time relative to dateStamp, in milliseconds |
| [dateStamp](date-stamp.md) | [androidJvm]<br>var [dateStamp](date-stamp.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>Sleep date, in milliseconds |
| [deepSleepCount](deep-sleep-count.md) | [androidJvm]<br>var [deepSleepCount](deep-sleep-count.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Deep sleep count |
| [deepSleepMinutes](deep-sleep-minutes.md) | [androidJvm]<br>var [deepSleepMinutes](deep-sleep-minutes.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Deep sleep minutes |
| [deepSleepPercentage](deep-sleep-percentage.md) | [androidJvm]<br>var [deepSleepPercentage](deep-sleep-percentage.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The percentage of deep sleep |
| [getUpTime](get-up-time.md) | [androidJvm]<br>var [getUpTime](get-up-time.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Get up time relative to dateStamp, in milliseconds |
| [lightSleepCount](light-sleep-count.md) | [androidJvm]<br>var [lightSleepCount](light-sleep-count.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Light sleep count |
| [lightSleepMinutes](light-sleep-minutes.md) | [androidJvm]<br>var [lightSleepMinutes](light-sleep-minutes.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>light sleep minutes |
| [lightSleepPercentage](light-sleep-percentage.md) | [androidJvm]<br>var [lightSleepPercentage](light-sleep-percentage.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The percentage of light sleep |
| [remSleepCount](rem-sleep-count.md) | [androidJvm]<br>var [remSleepCount](rem-sleep-count.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Rem sleep count |
| [remSleepMinutes](rem-sleep-minutes.md) | [androidJvm]<br>var [remSleepMinutes](rem-sleep-minutes.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Rem sleep minutes |
| [remSleepPercentage](rem-sleep-percentage.md) | [androidJvm]<br>var [remSleepPercentage](rem-sleep-percentage.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The percentage of rem sleep |
| [sleepScore](sleep-score.md) | [androidJvm]<br>var [sleepScore](sleep-score.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Sleep score |
| [sleepType](sleep-type.md) | [androidJvm]<br>var [sleepType](sleep-type.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Sleep type, 0: white day, 1: night |
| [totalSleepMinutes](total-sleep-minutes.md) | [androidJvm]<br>var [totalSleepMinutes](total-sleep-minutes.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Total sleep minutes |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
