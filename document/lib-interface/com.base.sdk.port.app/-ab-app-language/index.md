//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppLanguage](index.md)

# AbAppLanguage

[androidJvm]\
abstract class [AbAppLanguage](index.md)

应用模块 - 语言(Application module - language)

## Constructors

| | |
|---|---|
| [AbAppLanguage](-ab-app-language.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [syncLanguageList](sync-language-list.md) | [androidJvm]<br>abstract val [syncLanguageList](sync-language-list.md): Single&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[WmLanguage](../../com.base.sdk.entity.apps/-wm-language/index.md)&gt;&gt;<br>同步语言列表(Synchronize the language list) |

## Functions

| Name | Summary |
|---|---|
| [setLanguage](set-language.md) | [androidJvm]<br>abstract fun [setLanguage](set-language.md)(language: [WmLanguage](../../com.base.sdk.entity.apps/-wm-language/index.md)): Single&lt;[WmLanguage](../../com.base.sdk.entity.apps/-wm-language/index.md)&gt;<br>设定语言(Set language) |
