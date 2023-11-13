package com.base.sdk.entity.apps

import com.base.sdk.entity.common.WmWeek
import com.base.sdk.entity.settings.WmUnitInfo

/**
 * 天气（weather）
 */
data class WmWeatherForecast(
    /**
     * 最低温度（low temperature）
     * */
    val lowTemp: Float,
    /**
     * 最高温度（high temperature）
     * */
    val highTemp: Float,
    /**
     * 当前温度（current temperature）
     * */
    val curTemp: Float,
    /**
     * 温度单位（temperature unit）
     * */
    val tempUnit: WmUnitInfo.TemperatureUnit,
    /**
     * 湿度（humidity）
     * */
    val humidity: Float,
    val humidityNight: Float,

    /**
     * 紫外线指数（ultraviolet index）
     * */
    val uvIndex: Int,
    val uvIndexNight: Int,

    /**
     * 白天天气代码（day weather code）
     * */
    val dayCode: Int,
    /**
     * 夜晚天气代码（night weather code）
     * */
    val nightCode: Int,

//    /**
//     * 天气描述长度（day weather description len）
//     */
//    val dayDescLen: Int,

    /**
     * 白天天气描述（day weather description）
     * */
    val dayDesc: String,

//    /**
//     * 天气描述长度（night weather description len）
//     */
//    val nightDescLen: Int,

    /**
     * 夜晚天气描述（night weather description）
     * */
    val nightDesc: String,

    /**
     * 日期（date）
     * */
    val date: Long,
    /**
     * 星期（week）
     * */
    val week: WmWeek,

    ) {
    override fun toString(): String {
        return "WmWeatherForecast(lowTemp=$lowTemp, highTemp=$highTemp, curTemp=$curTemp, tempUnit=$tempUnit, humidity=$humidity, uvIndex=$uvIndex, dayCode=$dayCode, nightCode=$nightCode, dayDesc='$dayDesc', nightDesc='$nightDesc', date=$date, week=$week)"
    }
}

data class TodayWeather(
    /**
     * 当前温度（current temperature）
     * */
    val curTemp: Float,
    /**
     * 温度单位（temperature unit）
     * */
    val tempUnit: WmUnitInfo.TemperatureUnit,
    /**
     * 湿度（humidity）
     * */
    val humidity: Int,
    /**
     * 紫外线指数（ultraviolet index）
     * */
    val uvIndex: Int,

    /**
     * 白天天气代码（day weather code）
     * */
    val weatherCode: Int,

//    /**
//     * 天气描述长度
//     */
//    val weatherDescLen: Int,

    /**
     * 白天天气描述（day weather description）
     * */
    val weatherDesc: String,

    /**
     * 日期（date）
     * */
    val date: Long,

    /**
     * 小时（hour）
     * */
    val hour: Int


) {
    override fun toString(): String {
        return "TodayWeather(curTemp=$curTemp, tempUnit=$tempUnit, humidity=$humidity, uvIndex=$uvIndex, weatheCode=$weatherCode, weatherDesc='$weatherDesc', date=$date, hour=$hour)"
    }
}

data class WmLocation(
    /**
     * 国家（country）
     */
    val country: String,
    /**
     * 城市（city）
     */
    val city: String,

    /**
//     * 区域（district）
//     */
    val district: String,
    /**
     * 经度（longitude）
     */
    val longitude: Double,
    /**
     * 纬度（latitude）
     */
    val latitude: Double,
) {
    override fun toString(): String {
        return "WmLocation(country='$country', city='$city'"
    }
}

data class WmWeather(
    /**
     * 发布时间/毫秒（publish time）
     */
    val pubDate: Long,
    /**
     * 地理位置（location）
     */
    val location: WmLocation,
    /**
     * 7天天气预报（weather forecast for 7 days）
     */
    val weatherForecast: List<WmWeatherForecast>,
    /**
     * 今日24小时天气预报(24 hours weather forecast today)
     */
    val todayWeather: List<TodayWeather>,
) {
    override fun toString(): String {
        return "WmWeather(pubDate=$pubDate, location=$location, weatherForecast=$weatherForecast, todayWeather=$todayWeather)"
    }
}