//[lib-interface](../../../index.md)/[com.base.sdk.port.sync](../index.md)/[AbSyncData](index.md)

# AbSyncData

[androidJvm]\
abstract class [AbSyncData](index.md)&lt;[T](index.md)&gt;

同步数据抽象父类,所有同步数据模块共同继承 (Abstract parent class for all synchronization data modules)

## Constructors

| | |
|---|---|
| [AbSyncData](-ab-sync-data.md) | [androidJvm]<br>constructor() |

## Properties

| Name | Summary |
|---|---|
| [observeSyncData](observe-sync-data.md) | [androidJvm]<br>abstract var [observeSyncData](observe-sync-data.md): Observable&lt;[T](index.md)&gt;<br>观察数据监听 (Observe data) |

## Functions

| Name | Summary |
|---|---|
| [latestSyncTime](latest-sync-time.md) | [androidJvm]<br>abstract fun [latestSyncTime](latest-sync-time.md)(): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>最近更新时间 (The latest update time) |
| [syncData](sync-data.md) | [androidJvm]<br>abstract fun [syncData](sync-data.md)(startTime: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)): Observable&lt;[T](index.md)&gt;<br>同步数据(Sync data) |
