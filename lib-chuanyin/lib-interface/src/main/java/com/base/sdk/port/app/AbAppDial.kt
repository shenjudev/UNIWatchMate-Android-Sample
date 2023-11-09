package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmDial
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * App - Dial（应用模块 - 表盘）
 */
abstract class AbAppDial {
    /**
     * 同步表盘列表
     */
    abstract fun syncDialList() : Observable<List<WmDial>>

    /**
     * 删除表盘
     */
    abstract fun deleteDial(dialItem: WmDial): Single<WmDial>

    /**
     * 获取表盘封面图片
     */
    abstract fun parseDialThumpJpg(dialPath: String): ByteArray?


}