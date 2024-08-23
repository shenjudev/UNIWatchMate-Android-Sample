//[lib-interface](../../../index.md)/[com.base.sdk.entity.apps](../index.md)/[WmAlarm](index.md)

# WmAlarm

class [WmAlarm](index.md)(var alarmName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), var hour: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var minute: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), var repeatOptions: [Set](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)&lt;[AlarmRepeatOption](../-alarm-repeat-option/index.md)&gt;)

闹钟数据结构(Alarm)

#### Parameters

androidJvm

| | |
|---|---|
| alarmName | Alarm name, max length 20 |
| hour | Hour |
| minute | Minute |
| repeatOptions | Repeat options |

## Constructors

| | |
|---|---|
| [WmAlarm](-wm-alarm.md) | [androidJvm]<br>constructor(alarmName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), hour: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), minute: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html), repeatOptions: [Set](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)&lt;[AlarmRepeatOption](../-alarm-repeat-option/index.md)&gt;) |

## Properties

| Name | Summary |
|---|---|
| [alarmName](alarm-name.md) | [androidJvm]<br>var [alarmName](alarm-name.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [hour](hour.md) | [androidJvm]<br>var [hour](hour.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [isOn](is-on.md) | [androidJvm]<br>var [isOn](is-on.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [minute](minute.md) | [androidJvm]<br>var [minute](minute.md): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [repeatOptions](repeat-options.md) | [androidJvm]<br>var [repeatOptions](repeat-options.md): [Set](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)&lt;[AlarmRepeatOption](../-alarm-repeat-option/index.md)&gt; |

## Functions

| Name | Summary |
|---|---|
| [toString](to-string.md) | [androidJvm]<br>open override fun [toString](to-string.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
