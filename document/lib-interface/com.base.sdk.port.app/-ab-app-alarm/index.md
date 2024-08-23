//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppAlarm](index.md)

# AbAppAlarm

[androidJvm]\
abstract class [AbAppAlarm](index.md)

应用模块-闹钟(Application module - alarm)

## Constructors

| | |
|---|---|
| [AbAppAlarm](-ab-app-alarm.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [getAlarmList](get-alarm-list.md) | [androidJvm]<br>abstract var [getAlarmList](get-alarm-list.md): Single&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmAlarm](../../com.base.sdk.entity.apps/-wm-alarm/index.md)&gt;&gt;<br>从设备获取闹钟列表(Get alarm list from device) |
| [observeAlarmList](observe-alarm-list.md) | [androidJvm]<br>abstract var [observeAlarmList](observe-alarm-list.md): Observable&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmAlarm](../../com.base.sdk.entity.apps/-wm-alarm/index.md)&gt;&gt;<br>监听闹钟列表变化(Listen for alarm list changes) |

## Functions

| Name | Summary |
|---|---|
| [updateAlarmList](update-alarm-list.md) | [androidJvm]<br>abstract fun [updateAlarmList](update-alarm-list.md)(alarms: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmAlarm](../../com.base.sdk.entity.apps/-wm-alarm/index.md)&gt;): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>更新闹钟列表(Update alarm list) |
