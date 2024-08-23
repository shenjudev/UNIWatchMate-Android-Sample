//[lib-interface](../../../index.md)/[com.base.sdk.entity.settings](../index.md)/[WmHeartRateAlerts](index.md)

# WmHeartRateAlerts

[androidJvm]\
open class [WmHeartRateAlerts](index.md)(var isEnableHrAutoMeasure: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), var maxHeartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = DEFAULT_MAX_HEART_RATE, var exerciseHeartRateAlert: [WmHeartRateAlerts.HeartRateThresholdAlert](-heart-rate-threshold-alert/index.md) = HeartRateThresholdAlert(), var restingHeartRateAlert: [WmHeartRateAlerts.HeartRateThresholdAlert](-heart-rate-threshold-alert/index.md) = HeartRateThresholdAlert(), val age: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))

Heart rate alerts(心率提醒)

## Constructors

| | |
|---|---|
| [WmHeartRateAlerts](-wm-heart-rate-alerts.md) | [androidJvm]<br>constructor(age: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html))constructor(isEnableHrAutoMeasure: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), maxHeartRate: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = DEFAULT_MAX_HEART_RATE, exerciseHeartRateAlert: [WmHeartRateAlerts.HeartRateThresholdAlert](-heart-rate-threshold-alert/index.md) = HeartRateThresholdAlert(), restingHeartRateAlert: [WmHeartRateAlerts.HeartRateThresholdAlert](-heart-rate-threshold-alert/index.md) = HeartRateThresholdAlert(), age: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |
| [HeartRateThresholdAlert](-heart-rate-threshold-alert/index.md) | [androidJvm]<br>data class [HeartRateThresholdAlert](-heart-rate-threshold-alert/index.md)(var threshold: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) = THRESHOLDS[0], var isEnable: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false) |

## Properties

| Name | Summary |
|---|---|
| [age](age.md) | [androidJvm]<br>val [age](age.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Age(年龄) |
| [exerciseHeartRateAlert](exercise-heart-rate-alert.md) | [androidJvm]<br>var [exerciseHeartRateAlert](exercise-heart-rate-alert.md): [WmHeartRateAlerts.HeartRateThresholdAlert](-heart-rate-threshold-alert/index.md)<br>Exercise heart rate alert(运动心率提醒) |
| [isEnableHrAutoMeasure](is-enable-hr-auto-measure.md) | [androidJvm]<br>var [isEnableHrAutoMeasure](is-enable-hr-auto-measure.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Heart rate auto measure(心率自动测量开关) |
| [maxHeartRate](max-heart-rate.md) | [androidJvm]<br>var [maxHeartRate](max-heart-rate.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Max heart rate(最大心率) |
| [restingHeartRateAlert](resting-heart-rate-alert.md) | [androidJvm]<br>var [restingHeartRateAlert](resting-heart-rate-alert.md): [WmHeartRateAlerts.HeartRateThresholdAlert](-heart-rate-threshold-alert/index.md)<br>Resting heart rate alert(静息心率提醒) |

## Functions

| Name | Summary |
|---|---|
| [refreshIntervals](refresh-intervals.md) | [androidJvm]<br>fun [refreshIntervals](refresh-intervals.md)() |
| [restoreDefaultMaxHeartRate](restore-default-max-heart-rate.md) | [androidJvm]<br>fun [restoreDefaultMaxHeartRate](restore-default-max-heart-rate.md)()<br>Restore default max heart rate(恢复默认最大心率) |
| [setExerciseHeartRateAlert](set-exercise-heart-rate-alert.md) | [androidJvm]<br>fun [setExerciseHeartRateAlert](set-exercise-heart-rate-alert.md)(isEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), threshold: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |
| [setRestingHeartRateAlert](set-resting-heart-rate-alert.md) | [androidJvm]<br>fun [setRestingHeartRateAlert](set-resting-heart-rate-alert.md)(isEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), threshold: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)) |
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
