package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmFind
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * (app-find)应用模块-查找功能
 */
abstract class AbAppFind {

    /**
     * find mobile(监听来自手表，查找手机)
     * @return 0:连续响 其它：响铃次数
     */
    abstract val observeFindMobile : Observable<WmFind>

    /**
     * 监听来自手表 停止查找手机 的消息
     */
    abstract val observeStopFindMobile: Observable<Any>

    /**
     * 监听来自手表 停止查找手表 的消息
     */
    abstract val observeStopFindWatch: Observable<Any>

    /**
     * stop find mobile(停止查找手机,上报给手表)
     */
    abstract fun stopFindMobile(): Single<Boolean>

    /**
     * find watch(查找手表)
     * @param ring_count 铃声次数
     */
    abstract fun findWatch(ring_count: WmFind): Single<Boolean>

    /**
     * stop find watch(停止查找手表，向手表发送命令)
     *
     * @return
     */
    abstract fun stopFindWatch(): Single<Boolean>
}

