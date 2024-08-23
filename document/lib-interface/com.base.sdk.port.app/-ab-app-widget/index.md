//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppWidget](index.md)

# AbAppWidget

[androidJvm]\
abstract class [AbAppWidget](index.md)

应用模块-组件(Application module - widget)

## Constructors

| | |
|---|---|
| [AbAppWidget](-ab-app-widget.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [getWidgetList](get-widget-list.md) | [androidJvm]<br>abstract var [getWidgetList](get-widget-list.md): Single&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmWidget](../../com.base.sdk.entity.apps/-wm-widget/index.md)&gt;&gt;<br>从设备获取组件列表(Get widget list from the device) |
| [observeWidgetList](observe-widget-list.md) | [androidJvm]<br>abstract var [observeWidgetList](observe-widget-list.md): Observable&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmWidget](../../com.base.sdk.entity.apps/-wm-widget/index.md)&gt;&gt;<br>监听组件列表变化(Listen for widget list changes) |

## Functions

| Name | Summary |
|---|---|
| [updateWidgetList](update-widget-list.md) | [androidJvm]<br>abstract fun [updateWidgetList](update-widget-list.md)(widgets: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmWidget](../../com.base.sdk.entity.apps/-wm-widget/index.md)&gt;): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>更新组件列表(Update widget list) |
