//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmSportSummaryData](index.md)

# WmSportSummaryData

class [WmSportSummaryData](index.md)(val date: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), val startTime: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), val endTime: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), val duration: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), val sportId: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val sportType: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html), val step: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val calories: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val distance: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val actTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val maxRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val averageRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val minRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val rateLimitTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val rateUnAerobic: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val rateAerobic: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val rateFatBurning: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val rateWarmUp: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val maxStepSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val minStepSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val averageStepSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val fastPace: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val slowestPace: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val averagePace: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val fastSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val slowestSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), val averageSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) : [WmBaseSyncData](../-wm-base-sync-data/index.md), [Serializable](https://developer.android.com/reference/kotlin/java/io/Serializable.html)

运动小结(Sport Summary)

#### Parameters

androidJvm

| | |
|---|---|
| date | 训练开始日期(Date of training) |
| startTime | 训练开始时间戳(毫秒)(Start time stamp of training, in milliseconds) |
| endTime | 训练结束时间戳(毫秒)(End time stamp of training, in milliseconds) |
| duration | 训练持续时间(Seconds of training) |
| sportId | 运动ID(Sport ID) |
| sportType | 运动类型(Sport type) |
| step | 步数(Steps) |
| calories | 卡路里（kcal）（Calories(kcal) |
| distance | 距离 (Distance, in meters) |
| actTime | 活动时长(Activity time, in seconds) |
| maxRate | 最大心率(Maximum heart rate) |
| averageRate | 平均心率(Average heart rate) |
| minRate | 最小心率(Minimum heart rate) |
| rateLimitTime | 心率 -- 极限时长  / 单位:  秒(Heart rate limit time, in seconds) |
| rateUnAerobic | 心率 -- 无氧耐力时长  / 单位:  秒(Heart rate without oxygen support time, in seconds) |
| rateAerobic | 心率 -- 有氧耐力时长  / 单位:  秒(Heart rate with oxygen support time, in seconds) |
| rateFatBurning | 心率 -- 燃脂时长  / 单位:  秒(Heart rate fat burning time, in seconds) |
| rateWarmUp | 心率 -- 热身时长  / 单位:  秒(Heart rate warm up time, in seconds) |
| maxStepSpeed | 最大步频 / 单位:步/分钟(Maximum step speed, in steps per minute) |
| minStepSpeed | 最小步频(Minimum step speed) |
| averageStepSpeed | 平均步频(Average step speed) |
| fastPace | 最快配速(用时最少为最快) / 单位: 非游泳:秒/公里， 游泳:秒/百米(Fast pace, in non-swim seconds per kilometer, in swim seconds per hundred meters) |
| slowestPace | 最慢配速 / 单位: 非游泳:秒/公里， 游泳:秒/百米(Slowest pace, in non-swim seconds per kilometer, in swim seconds per hundred meters) |
| averagePace | 平均配速(Average pace) |

## Constructors

| | |
|---|---|
| [WmSportSummaryData](-wm-sport-summary-data.md) | [androidJvm]<br>constructor(date: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), startTime: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), endTime: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), duration: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), sportId: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), sportType: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html), step: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), calories: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), distance: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), actTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), maxRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), averageRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), minRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateLimitTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateUnAerobic: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateAerobic: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateFatBurning: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateWarmUp: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), maxStepSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), minStepSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), averageStepSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), fastPace: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), slowestPace: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), averagePace: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), fastSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), slowestSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), averageSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [actTime](act-time.md) | [androidJvm]<br>val [actTime](act-time.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [averagePace](average-pace.md) | [androidJvm]<br>val [averagePace](average-pace.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [averageRate](average-rate.md) | [androidJvm]<br>val [averageRate](average-rate.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [averageSpeed](average-speed.md) | [androidJvm]<br>val [averageSpeed](average-speed.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [averageStepSpeed](average-step-speed.md) | [androidJvm]<br>val [averageStepSpeed](average-step-speed.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [calories](calories.md) | [androidJvm]<br>val [calories](calories.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [date](date.md) | [androidJvm]<br>val [date](date.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [distance](distance.md) | [androidJvm]<br>val [distance](distance.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [duration](duration.md) | [androidJvm]<br>val [duration](duration.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [endTime](end-time.md) | [androidJvm]<br>val [endTime](end-time.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [fastPace](fast-pace.md) | [androidJvm]<br>val [fastPace](fast-pace.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [fastSpeed](fast-speed.md) | [androidJvm]<br>val [fastSpeed](fast-speed.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [maxRate](max-rate.md) | [androidJvm]<br>val [maxRate](max-rate.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [maxStepSpeed](max-step-speed.md) | [androidJvm]<br>val [maxStepSpeed](max-step-speed.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [minRate](min-rate.md) | [androidJvm]<br>val [minRate](min-rate.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [minStepSpeed](min-step-speed.md) | [androidJvm]<br>val [minStepSpeed](min-step-speed.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [rateAerobic](rate-aerobic.md) | [androidJvm]<br>val [rateAerobic](rate-aerobic.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [rateFatBurning](rate-fat-burning.md) | [androidJvm]<br>val [rateFatBurning](rate-fat-burning.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [rateLimitTime](rate-limit-time.md) | [androidJvm]<br>val [rateLimitTime](rate-limit-time.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [rateUnAerobic](rate-un-aerobic.md) | [androidJvm]<br>val [rateUnAerobic](rate-un-aerobic.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [rateWarmUp](rate-warm-up.md) | [androidJvm]<br>val [rateWarmUp](rate-warm-up.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [slowestPace](slowest-pace.md) | [androidJvm]<br>val [slowestPace](slowest-pace.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [slowestSpeed](slowest-speed.md) | [androidJvm]<br>val [slowestSpeed](slowest-speed.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [sportId](sport-id.md) | [androidJvm]<br>val [sportId](sport-id.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [sportType](sport-type.md) | [androidJvm]<br>val [sportType](sport-type.md): [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html) |
| [startTime](start-time.md) | [androidJvm]<br>val [startTime](start-time.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |
| [step](step.md) | [androidJvm]<br>val [step](step.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [tenSecondsCaloriesData](ten-seconds-calories-data.md) | [androidJvm]<br>var [tenSecondsCaloriesData](ten-seconds-calories-data.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmCaloriesData](../-wm-calories-data/index.md)&gt;? |
| [tenSecondsDistanceData](ten-seconds-distance-data.md) | [androidJvm]<br>var [tenSecondsDistanceData](ten-seconds-distance-data.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmDistanceData](../-wm-distance-data/index.md)&gt;? |
| [tenSecondsHeartRate](ten-seconds-heart-rate.md) | [androidJvm]<br>var [tenSecondsHeartRate](ten-seconds-heart-rate.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmRealtimeRateData](../-wm-realtime-rate-data/index.md)&gt;? |
| [tenSecondsStepFrequencyData](ten-seconds-step-frequency-data.md) | [androidJvm]<br>var [tenSecondsStepFrequencyData](ten-seconds-step-frequency-data.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmStepFrequencyData](../-wm-step-frequency-data/index.md)&gt;? |
| [timestamp](../-wm-base-sync-data/timestamp.md) | [androidJvm]<br>var [timestamp](../-wm-base-sync-data/timestamp.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
