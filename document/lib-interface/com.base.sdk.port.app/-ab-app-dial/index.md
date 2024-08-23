//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppDial](index.md)

# AbAppDial

[androidJvm]\
abstract class [AbAppDial](index.md)

应用模块 - 表盘(Application module - dial)

## Constructors

| | |
|---|---|
| [AbAppDial](-ab-app-dial.md) | [androidJvm]<br>constructor() |

## Functions

| Name | Summary |
|---|---|
| [deleteDial](delete-dial.md) | [androidJvm]<br>abstract fun [deleteDial](delete-dial.md)(dialItem: [WmDial](../../com.base.sdk.entity.apps/-wm-dial/index.md)): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>删除表盘(Delete the dial) |
| [getCurrentDial](get-current-dial.md) | [androidJvm]<br>abstract fun [getCurrentDial](get-current-dial.md)(): Single&lt;[WmDial](../../com.base.sdk.entity.apps/-wm-dial/index.md)&gt;<br>获取当前表盘(Get the current dial) |
| [getDialList](get-dial-list.md) | [androidJvm]<br>abstract fun [getDialList](get-dial-list.md)(): Observable&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmDial](../../com.base.sdk.entity.apps/-wm-dial/index.md)&gt;&gt;<br>同步表盘列表(Synchronize the dial list) |
| [observerCurrentDial](observer-current-dial.md) | [androidJvm]<br>abstract fun [observerCurrentDial](observer-current-dial.md)(): Observable&lt;[WmDial](../../com.base.sdk.entity.apps/-wm-dial/index.md)&gt;<br>监听当前表盘(Listen for the current dial) |
| [parseDialThumpJpg](parse-dial-thump-jpg.md) | [androidJvm]<br>abstract fun [parseDialThumpJpg](parse-dial-thump-jpg.md)(dialPath: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html)?<br>获取表盘封面图片(Get the cover picture of the dial) |
| [setDial](set-dial.md) | [androidJvm]<br>abstract fun [setDial](set-dial.md)(dialItem: [WmDial](../../com.base.sdk.entity.apps/-wm-dial/index.md)): Single&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>设置为当前表盘(Set as the current dial) |
