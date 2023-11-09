package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmSport
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块-运动
 */
abstract class AbAppSport {

    /**
     * syncSportList 同步运动列表
     */
    abstract val getSportList : Single<List<WmSport>>

    /**
     * sortFixedSportList 运动列表排序
     */
    abstract fun updateSportList(list: List<WmSport>): Single<Boolean>

}