//[lib-interface](../../../index.md)/[com.base.sdk.port.app](../index.md)/[AbAppMusicControl](index.md)

# AbAppMusicControl

[androidJvm]\
abstract class [AbAppMusicControl](index.md)

应用模块-音乐控制(Application module - music control)

## Constructors

| | |
|---|---|
| [AbAppMusicControl](-ab-app-music-control.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [observableMusicControl](observable-music-control.md) | [androidJvm]<br>abstract val [observableMusicControl](observable-music-control.md): Observable&lt;[WmMusicControlType](../../com.base.sdk.entity.apps/-wm-music-control-type/index.md)&gt;<br>监听音乐控制(Listen for the music control) |

## Functions

| Name | Summary |
|---|---|
| [syncMusicInfo](sync-music-info.md) | [androidJvm]<br>abstract fun [syncMusicInfo](sync-music-info.md)(it: [WmMusicInfo](../../com.base.sdk.entity.apps/-wm-music-info/index.md)): Observable&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)&gt;<br>同步音乐信息(Synchronize the music information) |
