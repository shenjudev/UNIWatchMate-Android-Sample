package com.base.sdk.port.sync

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 同步数据抽象父类
 * 所有同步数据模块共同继承
 */
abstract class AbSyncData<T> {

    /**
     * 最近更新时间
     */
    abstract fun latestSyncTime(): Long

    /**
     * 同步数据（最近7日）
     */
    abstract fun syncData(startTime: Long): Observable<T>

    /**
     * 观察数据监听
     */
    abstract var observeSyncData: Observable<T>

}