package com.base.sdk.port.app

import com.base.sdk.entity.settings.WmDateTime
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块-同步时间
 */
abstract class AbAppDateTime {

    /**
     * 设置时间
     */
    abstract fun setDateTime(dateTime: WmDateTime?): Single<Boolean>

}