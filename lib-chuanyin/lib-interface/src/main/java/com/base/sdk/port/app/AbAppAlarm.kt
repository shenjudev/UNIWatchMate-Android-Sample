package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmAlarm
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块-闹钟
 */
abstract class AbAppAlarm {
    /**
     * updateAlarmList 更新闹钟列表
     */
    abstract fun updateAlarmList(alarms:List<WmAlarm>): Single<Boolean>

    /**
     *  从设备端获取闹钟列表，并监听闹钟列表变化
     */
    abstract var observeAlarmList: Observable<List<WmAlarm>>

}