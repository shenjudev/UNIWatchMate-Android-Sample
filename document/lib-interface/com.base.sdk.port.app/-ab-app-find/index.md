//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppFind](index.md)

# AbAppFind

[androidJvm]\
abstract class [AbAppFind](index.md)

应用模块-查找功能(Application module - find function)

## Constructors

| | |
|---|---|
| [AbAppFind](-ab-app-find.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [observeFindMobile](observe-find-mobile.md) | [androidJvm]<br>abstract val [observeFindMobile](observe-find-mobile.md): Observable&lt;[WmFind](../../com.base.sdk.entity.apps/-wm-find/index.md)&gt;<br>监听来自手表的查找手机消息（Listen for the find mobile message from the watch) |
| [observeStopFindMobile](observe-stop-find-mobile.md) | [androidJvm]<br>abstract val [observeStopFindMobile](observe-stop-find-mobile.md): Observable&lt;[Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>监听来自手表的停止查找手机消息（Listen for the stop find mobile message from the watch) |
| [observeStopFindWatch](observe-stop-find-watch.md) | [androidJvm]<br>abstract val [observeStopFindWatch](observe-stop-find-watch.md): Observable&lt;[Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;<br>监听来自手表的停止查找手表消息（Listen for the stop find watch message from the watch) |

## Functions

| Name | Summary |
|---|---|
| [findWatch](find-watch.md) | [androidJvm]<br>abstract fun [findWatch](find-watch.md)(ring_count: [WmFind](../../com.base.sdk.entity.apps/-wm-find/index.md)): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>查找手表(Find watch) |
| [stopFindMobile](stop-find-mobile.md) | [androidJvm]<br>abstract fun [stopFindMobile](stop-find-mobile.md)(): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>停止查找手机,发送给手表（Stop find mobile, send to the watch) |
| [stopFindWatch](stop-find-watch.md) | [androidJvm]<br>abstract fun [stopFindWatch](stop-find-watch.md)(): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>stop find watch(停止查找手表，向手表发送命令) |
