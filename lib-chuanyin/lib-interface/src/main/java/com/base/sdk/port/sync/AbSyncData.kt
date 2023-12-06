package com.base.sdk.port.sync

import com.base.sdk.entity.data.WmSyncTime
import io.reactivex.rxjava3.core.Observable

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
     * 同步数据
     * startTime 请求开始时间 如果为0，则默认请求7天前的开始时间，默认结束时间是当前
     */
    abstract fun syncData(startTime: Long): Observable<T>

    /**
     * 观察数据监听
     */
    abstract var observeSyncData: Observable<T>

}