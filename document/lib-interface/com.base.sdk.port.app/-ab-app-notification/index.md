//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppNotification](index.md)

# AbAppNotification

[androidJvm]\
abstract class [AbAppNotification](index.md)

App-notification 应用模块-通知

## Constructors

| | |
|---|---|
| [AbAppNotification](-ab-app-notification.md) | [androidJvm]<br>constructor() |

## Functions

| Name | Summary |
|---|---|
| [sendNotification](send-notification.md) | [androidJvm]<br>abstract fun [sendNotification](send-notification.md)(notification: [WmNotification](../../com.base.sdk.entity.apps/-wm-notification/index.md)): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>sendNotification 发送通知 |
| [sendNotificationSetting](send-notification-setting.md) | [androidJvm]<br>abstract fun [sendNotificationSetting](send-notification-setting.md)(wmNotificationSetting: [WmNotificationSetting](../../com.base.sdk.entity.apps/-wm-notification-setting/index.md)): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>sendNotificationSetting 发送通知设置 |
