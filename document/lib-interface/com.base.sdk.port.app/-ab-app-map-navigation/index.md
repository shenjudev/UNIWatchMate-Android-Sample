//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppMapNavigation](index.md)

# AbAppMapNavigation

[androidJvm]\
abstract class [AbAppMapNavigation](index.md)

应用模块-导航(Application module - navigation)

## Constructors

| | |
|---|---|
| [AbAppMapNavigation](-ab-app-map-navigation.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [observeNavigationState](observe-navigation-state.md) | [androidJvm]<br>abstract val [observeNavigationState](observe-navigation-state.md): Observable&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>监听设备端导航开启状态(Listen for the navigation open state from the device) |

## Functions

| Name | Summary |
|---|---|
| [openCloseNavigation](open-close-navigation.md) | [androidJvm]<br>abstract fun [openCloseNavigation](open-close-navigation.md)(open: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>App打开/关闭导航(App opens/closes the navigation) |
| [respondNaviOpen](respond-navi-open.md) | [androidJvm]<br>abstract fun [respondNaviOpen](respond-navi-open.md)(isAppFront: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html))<br>响应设备端打开导航的请求(Respond to the request to open the navigation on the device) |
| [sendNaviFrame](send-navi-frame.md) | [androidJvm]<br>abstract fun [sendNaviFrame](send-navi-frame.md)(naviFrame: [WmVideoFrameInfo](../../com.base.sdk.entity.apps/-wm-video-frame-info/index.md))<br>发送导航视频数据(Send navigation video data) |
| [startNaviVideo](start-navi-video.md) | [androidJvm]<br>abstract fun [startNaviVideo](start-navi-video.md)(): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>开始导航视频传输(Start navigation video transmission) |
