package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmSport
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块-运动
 */
abstract class AbAppSport {

    /**
     * getFixedSportList 获取固定运动列表
     */
    abstract val getFixedSportList: Single<List<WmSport>>


    /**
     * getDynamicSportList 获取动态运动列表
     */
    abstract val getDynamicSportList: Single<List<WmSport>>

    /**
     * getSupportSportList 获取支持的运动列表
     */
    abstract val getSupportSportList: Single<List<WmSport>>

    /**
     * updateDynamicSportList 更新动态运动列表
     */
    abstract fun updateDynamicSportList(list: List<WmSport>): Single<Boolean>

    /**
     * updateFixedSportList 更新固定运动列表
     */
    abstract fun updateFixedSportList(list: List<WmSport>): Single<Boolean>

}