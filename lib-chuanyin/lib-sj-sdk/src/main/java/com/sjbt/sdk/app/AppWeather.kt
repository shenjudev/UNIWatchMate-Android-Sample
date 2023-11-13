package com.sjbt.sdk.app

import android.text.TextUtils
import com.base.sdk.entity.apps.WmWeather
import com.base.sdk.entity.apps.WmWeatherRequest
import com.base.sdk.entity.apps.WmWeatherTime
import com.base.sdk.entity.settings.WmUnitInfo
import com.base.sdk.port.app.AbAppWeather
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.subjects.PublishSubject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.*

class AppWeather(val sjUniWatch: SJUniWatch) : AbAppWeather() {
    private var pushWeatherEmitter: SingleEmitter<Boolean>? = null
    private val requestWeather: PublishSubject<WmWeatherRequest> = PublishSubject.create()
    override val observeWeather: PublishSubject<WmWeatherRequest> = requestWeather
    private val TAG = "AppWeather"

    override fun pushTodayWeather(
        weather: WmWeather,
        temperatureUnit: WmUnitInfo.TemperatureUnit
    ): Single<Boolean> {

        return Single.create {
            pushWeatherEmitter = it

            var cityLen = weather.location.city?.let { it.toByteArray().size }
            var countryLen = weather.location.country?.let { it.toByteArray().size }

            var totalLen = 7 + cityLen + countryLen + 2 + 2
            sjUniWatch.wmLog.logD(TAG, "today weather payload_len:" + totalLen)

            weather.todayWeather.forEach {
                totalLen += it.weatherDesc.toByteArray().size + 13 + 1
            }

            sjUniWatch.wmLog.logD(TAG, "today weather payload_len:" + totalLen)
            val payloadPackage = getWriteTodayWeatherCmd(
                totalLen,
                temperatureUnit,
                weather
            )

            sjUniWatch.wmLog.logD(
                TAG,
                "today weather package count:" + payloadPackage.itemCount
            )

            sjUniWatch.sendWriteNodeCmdList(
                payloadPackage
            )
        }
    }

    override fun pushSevenDaysWeather(
        weather: WmWeather,
        temperatureUnit: WmUnitInfo.TemperatureUnit
    ): Single<Boolean> {
        return Single.create {
            pushWeatherEmitter = it

            sjUniWatch.wmLog.logD(TAG, "weather_len:" + weather)

            sjUniWatch.observableMtu.subscribe { mtu ->
                var cityLen = weather.location.city?.let { it.toByteArray().size }
                var countryLen = weather.location.country?.let { it.toByteArray().size }

                var sevenDayLen = 7 + cityLen + countryLen + 2 + 2
                sjUniWatch.wmLog.logD(TAG, "7 days weather payload_len:" + sevenDayLen)

                weather.weatherForecast.forEach {
                    sevenDayLen += it.dayDesc.toByteArray().size + it.nightDesc.toByteArray().size + 18 + 2
                }

                sjUniWatch.wmLog.logD(TAG, "7 days weather total bytes:" + sevenDayLen)

                val payloadPackage = getWriteSevenDaysWeatherCmd(
                    sevenDayLen,
                    temperatureUnit,
                    weather
                )

                sjUniWatch.wmLog.logD(
                    TAG,
                    "7 days weather package count:" + payloadPackage.itemCount
                )

                sjUniWatch.sendWriteNodeCmdList(
                    payloadPackage
                )

            }
        }
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
        sjUniWatch.wmLog.logE(TAG, "msg time out:" + msgBean)
    }


    fun weatherBusiness(payloadPackage: PayloadPackage, nodeData: NodeData) {
        when (nodeData.urn[2]) {

            URN_APP_WEATHER_PUSH_TODAY -> {

                if (payloadPackage.actionType == RequestType.REQ_TYPE_EXECUTE.type) {
                    val bcp = String(nodeData.data)
                    val weatherRequest = WmWeatherRequest(bcp, WmWeatherTime.TODAY)

                    sjUniWatch.wmLog.logD(TAG, "weatherRequest TODAY:" + weatherRequest)
                    observeWeather?.onNext(weatherRequest)

                } else {
                    if (nodeData.dataLen.toInt() == 1) {
                        val result = nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                        sjUniWatch.wmLog.logD(TAG, "weather push result:$result")
                        pushWeatherEmitter?.onSuccess(result)
                    }
                }
            }

            URN_APP_WEATHER_PUSH_SEVEN_DAYS -> {

                if (payloadPackage.actionType == RequestType.REQ_TYPE_EXECUTE.type) {
                    val bcp = String(nodeData.data)
                    val weatherRequest = WmWeatherRequest(bcp, WmWeatherTime.SEVEN_DAYS)
                    sjUniWatch.wmLog.logD(TAG, "weatherRequest SEVEN_DAYS:" + weatherRequest)
                    observeWeather?.onNext(weatherRequest)

                } else {
                    if (nodeData.dataLen.toInt() == 1) {
                        val result = nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                        sjUniWatch.wmLog.logD(TAG, "weather push result:$result")
                        pushWeatherEmitter?.onSuccess(result)
                    }
                }

            }
        }
    }

    /**
     * 获取当天天气命令
     * 摄氏度 = (华氏度 - 32°F) ÷ 1.8；华氏度 = 32°F+ 摄氏度 × 1.8
     */
    private fun getWriteTodayWeatherCmd(
        totalLen: Int,
        temperatureUnit: WmUnitInfo.TemperatureUnit,
        wmWeather: WmWeather
    ): PayloadPackage {
        val payloadPackage = PayloadPackage()

        val byteBuffer: ByteBuffer = ByteBuffer.allocate(totalLen).order(ByteOrder.LITTLE_ENDIAN)

        //时间
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = wmWeather.pubDate
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要加1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        byteBuffer.putShort(year.toShort())
        byteBuffer.put(month.toByte())
        byteBuffer.put(day.toByte())
        byteBuffer.put(hour.toByte())
        byteBuffer.put(minute.toByte())
        byteBuffer.put(second.toByte())

        //位置
        byteBuffer.put((wmWeather.location.country.toByteArray().size + 1).toByte())
        byteBuffer.put((wmWeather.location.city.toByteArray().size + 1).toByte())

        byteBuffer.put(wmWeather.location.country.toByteArray())
        byteBuffer.put(0)//字符串结束符
        byteBuffer.put(wmWeather.location.city.toByteArray())
        byteBuffer.put(0)//字符串结束符

        //当天
        wmWeather.todayWeather.forEach {

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要加1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)

            byteBuffer.putShort(year.toShort())
            byteBuffer.put(month.toByte())
            byteBuffer.put(day.toByte())
            byteBuffer.put(hour.toByte())
            byteBuffer.put(minute.toByte())
            byteBuffer.put(second.toByte())

            val currTemp = when (temperatureUnit) {
                WmUnitInfo.TemperatureUnit.CELSIUS -> {//摄氏度
                    it.curTemp * 100
                }

                WmUnitInfo.TemperatureUnit.FAHRENHEIT -> {//华氏度
//                 摄氏度=(华氏度 - 32°F) ÷ 1.8；
                    (it.curTemp - 32) / 1.8 * 100
                }
            }

            byteBuffer.putShort(currTemp.toShort())
            byteBuffer.putShort(it.humidity.toShort())
            byteBuffer.put(it.uvIndex.toByte())
            byteBuffer.put(it.weatherCode.toByte())
            byteBuffer.put((it.weatherDesc.toByteArray().size + 1).toByte())
            byteBuffer.put(it.weatherDesc.toByteArray())
            byteBuffer.put(0)//字符串结束符
        }

        payloadPackage.putData(
            CmdHelper.getUrnId(URN_APP_SETTING, URN_APP_WEATHER, URN_APP_WEATHER_PUSH_TODAY),
            byteBuffer.array()
        )

        return payloadPackage
    }

    /**
     * 获取当天天气命令
     * 摄氏度 = (华氏度 - 32°F) ÷ 1.8；华氏度 = 32°F+ 摄氏度 × 1.8
     */
    private fun getWriteSevenDaysWeatherCmd(
        totalLen: Int,
        temperatureUnit: WmUnitInfo.TemperatureUnit,
        wmWeather: WmWeather
    ): PayloadPackage {
        val payloadPackage = PayloadPackage()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(totalLen).order(ByteOrder.LITTLE_ENDIAN)

        //时间
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = wmWeather.pubDate
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要加1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        byteBuffer.putShort(year.toShort())
        byteBuffer.put(month.toByte())
        byteBuffer.put(day.toByte())
        byteBuffer.put(hour.toByte())
        byteBuffer.put(minute.toByte())
        byteBuffer.put(second.toByte())

        //位置
        byteBuffer.put((wmWeather.location.country.toByteArray().size + 1).toByte())
        byteBuffer.put((wmWeather.location.city.toByteArray().size + 1).toByte())
        byteBuffer.put(wmWeather.location.country.toByteArray())
        byteBuffer.put(0)//字符串结束符
        byteBuffer.put(wmWeather.location.city.toByteArray())
        byteBuffer.put(0)//字符串结束符

        wmWeather.weatherForecast.forEach {

            //时间
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要加1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val week = calendar.get(Calendar.WEEK_OF_MONTH)

            byteBuffer.putShort(year.toShort())
            byteBuffer.put(month.toByte())
            byteBuffer.put(day.toByte())
            byteBuffer.put(hour.toByte())
            byteBuffer.put(minute.toByte())
            byteBuffer.put(second.toByte())
//            byteBuffer.put(week.toByte())
            byteBuffer.put(it.week.ordinal.toByte())

            when (temperatureUnit) {
                WmUnitInfo.TemperatureUnit.CELSIUS -> {//摄氏度
                    byteBuffer.putShort((it.lowTemp * 100).toInt().toShort())
                    byteBuffer.putShort((it.highTemp * 100).toInt().toShort())
                    byteBuffer.putShort((it.curTemp * 100).toInt().toShort())
                }

                WmUnitInfo.TemperatureUnit.FAHRENHEIT -> {//华氏度
//                 摄氏度=(华氏度 - 32°F) ÷ 1.8；
                    byteBuffer.putShort(((it.lowTemp - 32) * 100 / 1.8).toInt().toShort())
                    byteBuffer.putShort(((it.highTemp - 32) * 100 / 1.8).toInt().toShort())
                    byteBuffer.putShort(((it.highTemp - 32) * 100 / 1.8).toInt().toShort())
                }
            }

            byteBuffer.putShort((it.humidity * 100).toInt().toShort())
            byteBuffer.putShort((it.humidityNight * 100).toInt().toShort())
            byteBuffer.put(it.uvIndex.toByte())
            byteBuffer.put(it.uvIndexNight.toByte())

            byteBuffer.put(it.dayCode.toByte())
            byteBuffer.put(it.nightCode.toByte())

            byteBuffer.put((it.dayDesc.toByteArray().size + 1).toByte())
            byteBuffer.put((it.nightDesc.toByteArray().size + 1).toByte())

            byteBuffer.put(it.dayDesc.toByteArray())
            byteBuffer.put(0)//字符串结束符
            byteBuffer.put(it.nightDesc.toByteArray())
            byteBuffer.put(0)//字符串结束符

        }

        payloadPackage.putData(
            CmdHelper.getUrnId(URN_APP_SETTING, URN_APP_WEATHER, URN_APP_WEATHER_PUSH_SEVEN_DAYS),
            byteBuffer.array()
        )

        return payloadPackage
    }

}