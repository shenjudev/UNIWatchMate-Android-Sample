//[lib-interface](../../../index.md)/[com.base.sdk.entity.data](../index.md)/[WmSportSummaryData](index.md)/[WmSportSummaryData](-wm-sport-summary-data.md)

# WmSportSummaryData

[androidJvm]\
constructor(date: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), startTime: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), endTime: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), duration: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html), sportId: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), sportType: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/index.html), step: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), calories: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), distance: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), actTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), maxRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), averageRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), minRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateLimitTime: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateUnAerobic: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateAerobic: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateFatBurning: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), rateWarmUp: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), maxStepSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), minStepSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), averageStepSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), fastPace: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), slowestPace: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), averagePace: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), fastSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), slowestSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), averageSpeed: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

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
