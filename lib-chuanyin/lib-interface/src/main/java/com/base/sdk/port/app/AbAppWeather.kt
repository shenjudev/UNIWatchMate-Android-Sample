package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmWeather
import com.base.sdk.entity.apps.WmWeatherRequest
import com.base.sdk.entity.settings.WmUnitInfo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块 - 天气同步
 */
abstract class AbAppWeather {

    /**
     * pushWeather 为设备推送天气信息
     */
    abstract fun pushTodayWeather(
        weather: WmWeather,
        temperatureUnit: WmUnitInfo.TemperatureUnit
    ): Single<Boolean>

    /**
     * pushWeather 为设备推送7天天气信息
     */
    abstract fun pushSevenDaysWeather(
        weather: WmWeather,
        temperatureUnit: WmUnitInfo.TemperatureUnit
    ): Single<Boolean>

    /**
     * observeWeather 监听设备端天气请求
     */
    abstract val observeWeather: Observable<WmWeatherRequest>

}