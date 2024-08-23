//[lib-interface](../../../index.md)/[com.base.sdk.port.setting](../index.md)/[AbWmSetting](index.md)

# AbWmSetting

[androidJvm]\
abstract class [AbWmSetting](index.md)&lt;[T](index.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt;

所有设置模块父类(All settings module parent class)

## Constructors

| | |
|---|---|
| [AbWmSetting](-ab-wm-setting.md) | [androidJvm]<br>constructor() |

## Functions

| Name | Summary |
|---|---|
| [get](get.md) | [androidJvm]<br>abstract fun [get](get.md)(): Single&lt;[T](index.md)&gt; |
| [observeChange](observe-change.md) | [androidJvm]<br>abstract fun [observeChange](observe-change.md)(): Observable&lt;[T](index.md)&gt; |
| [set](set.md) | [androidJvm]<br>abstract fun [set](set.md)(obj: [T](index.md)): Single&lt;[T](index.md)&gt; |
