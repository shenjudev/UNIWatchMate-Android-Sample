//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppSport](index.md)

# AbAppSport

[androidJvm]\
abstract class [AbAppSport](index.md)

应用模块-运动(Application module - sport)

## Constructors

| | |
|---|---|
| [AbAppSport](-ab-app-sport.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [getDynamicSportList](get-dynamic-sport-list.md) | [androidJvm]<br>abstract val [getDynamicSportList](get-dynamic-sport-list.md): Single&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmSport](../../com.base.sdk.entity.apps/-wm-sport/index.md)&gt;&gt;<br>getDynamicSportList 获取动态运动列表 |
| [getFixedSportList](get-fixed-sport-list.md) | [androidJvm]<br>abstract val [getFixedSportList](get-fixed-sport-list.md): Single&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmSport](../../com.base.sdk.entity.apps/-wm-sport/index.md)&gt;&gt;<br>getFixedSportList 获取固定运动列表 |
| [getSupportSportList](get-support-sport-list.md) | [androidJvm]<br>abstract val [getSupportSportList](get-support-sport-list.md): Single&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmSport](../../com.base.sdk.entity.apps/-wm-sport/index.md)&gt;&gt;<br>getSupportSportList 获取支持的运动列表 |

## Functions

| Name | Summary |
|---|---|
| [updateDynamicSportList](update-dynamic-sport-list.md) | [androidJvm]<br>abstract fun [updateDynamicSportList](update-dynamic-sport-list.md)(list: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmSport](../../com.base.sdk.entity.apps/-wm-sport/index.md)&gt;): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>updateDynamicSportList 更新动态运动列表 |
| [updateFixedSportList](update-fixed-sport-list.md) | [androidJvm]<br>abstract fun [updateFixedSportList](update-fixed-sport-list.md)(list: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmSport](../../com.base.sdk.entity.apps/-wm-sport/index.md)&gt;): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>updateFixedSportList 更新固定运动列表 |
