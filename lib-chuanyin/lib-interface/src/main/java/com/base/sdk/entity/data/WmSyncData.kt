package com.base.sdk.entity.data

import android.text.format.DateFormat
import com.base.sdk.entity.settings.WmSleepSettings
import java.io.Serializable
import java.util.*

abstract class WmBaseSyncData(
) {
    var timestamp: Long = 0
}

/**
 * Sync Value 同步数据
 */
class WmSyncData<T : WmBaseSyncData>(
    /**
     * data type
     */
    val type: WmSyncDataType,

    /**
     * Timestamp of this data
     */
    val timestamp: Long,

    /**
     * time interval type
     */
    val intervalType: WmIntervalType,//间隔时间类型

    /**
     * Sync value
     */
    val value: List<T>,
) {
    override fun toString(): String {
        return "WmSyncData(type=$type, timestamp=$timestamp, intervalType=$intervalType, value=$value)"
    }
}

/**
 * 运动小结
 *
/// 训练开始日期
uint32_t date;
/// 训练开始时间戳(毫秒)
uint32_t ts_start;
/// 训练结束时间戳(毫秒)
uint32_t ts_end;
uint16_t sport_id;
/// 训练类型, 4种类型
uint8_t sport_type;
/// 步数
uint32_t step;
/// 卡路里：kcal
uint32_t calories;
/// 距离：米
uint32_t distance;
//活动时长
uint16_t act_time;
/// 最大心率
uint8_t max_hr;
/// 平均心率
uint8_t avg_hr;
/// 最小心率
uint8_t min_hr;
/// 心率 -- 极限时长  / 单位:  秒
uint16_t hr_limit_time;
/// 心率 -- 无氧耐力时长  / 单位:  秒
uint16_t hr_anaerobic;
/// 心率 -- 有氧耐力时长  / 单位:  秒
uint16_t hr_aerobic;
/// 心率 -- 燃脂时长  / 单位:  秒
uint16_t hr_fat_burning;
/// 心率 -- 热身时长  / 单位:  秒
uint16_t hr_warm_up;
// 最大步频 / 单位:步/分钟
uint16_t max_step_speed;
// 最小步频
uint16_t min_step_speed;
//平均步频
uint16_t avg_step_speed;
// 最快配速(用时最少为最快) / 单位: 非游泳:秒/公里， 游泳:秒/百米
uint16_t fast_pace;
// 最慢配速 / 单位: 非游泳:秒/公里， 游泳:秒/百米
uint16_t slowest_pace;
//平均配速
uint16_t avg_pace;
// 最快速度 /单位: 公里/小时
uint16_t fast_speed;
// 最慢速度
uint16_t slowest_speed;
// 平均速度
uint16_t avg_speed;
/// 公里/英里配速
uint16_t paces[0];
 *
 */
class WmSportSummaryData(
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val sportId: Int,
    val sportType: Byte,
    val step: Int,
    val calories: Int,
    val distance: Int,
    val actTime: Short,
    val maxRate: Byte,
    val averageRate: Byte,
    val minRate: Byte,
    val rateLimitTime: Short,
    val rateUnAerobic: Short,
    val rateAerobic: Short,
    val rateFatBurning: Short,
    val rateWarmUp: Short,
    val maxStepSpeed: Short,
    val minStepSpeed: Short,
    val averageStepSpeed: Short,
    val fastPace: Short,
    val slowestPace: Short,
    val averagePace: Short,
    val fastSpeed: Short,
    val slowestSpeed: Short,
    val averageSpeed: Short
) : WmBaseSyncData(), Serializable {

    var tenSecondsHeartRate: List<WmRealtimeRateData>? = null
    var tenSecondsStepFrequencyData: List<WmStepFrequencyData>? = null
    var tenSecondsDistanceData: List<WmDistanceData>? = null
    var tenSecondsCaloriesData: List<WmCaloriesData>? = null

    override fun toString(): String {
        return "WmSportSummaryData(date=$date, startTime=$startTime, endTime=$endTime, sportId=$sportId, sportType=$sportType, step=$step, calories=$calories, distance=$distance, actTime=$actTime, maxRate=$maxRate, averageRate=$averageRate, minRate=$minRate, rateLimitTime=$rateLimitTime, rateUnAerobic=$rateUnAerobic, rateAerobic=$rateAerobic, rateFatBurning=$rateFatBurning, rateWarmUp=$rateWarmUp, maxStepSpeed=$maxStepSpeed, minStepSpeed=$minStepSpeed, averageStepSpeed=$averageStepSpeed, fastPace=$fastPace, slowestPace=$slowestPace, averagePace=$averagePace, fastSpeed=$fastSpeed, slowestSpeed=$slowestSpeed, averageSpeed=$averageSpeed, tenSecondsHeartRate=$tenSecondsHeartRate, tenSecondsStepFrequencyData=$tenSecondsStepFrequencyData, tenSecondsDistanceData=$tenSecondsDistanceData, tenSecondsCaloriesData=$tenSecondsCaloriesData)"
    }

}

/**
 * 日常活动时长
 * sportType 运动类型 跑步类....
 * duration 秒
 */
class WmDailyActivityDurationData(val sportType: Int, var duration: Int) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmDailyActivityDurationData(dateStamp=$timestamp sportType=$sportType, duration=$duration)"
    }
}

/**
 * 平均 value 血氧数据
 */
class WmHeartRateData(
    /**
     * heart rate value (beats per minute)
     */
    val minHeartRate: Int,
    val maxHeartRate: Int,
    val avgHeartRate: Int,

    /**
     * activity duration, in seconds
     */
    val duration: Int = 0

) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmHeartRateData(minHeartRate=$minHeartRate, maxHeartRate=$maxHeartRate, avgHeartRate=$avgHeartRate, duration=$duration)"
    }
}

/**
 * Oxygen value 血氧数据
 */
class WmOxygenData(
    /**
     * Oxygen value (SpO2)，0~100
     */
    val oxygen: Int
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmOxygenData(oxygen=$oxygen)"
    }
}

/**
 * Step value 步数数据
 */
class WmStepData(
    /**
     * Step value
     */
    val step: Int,

    ) : WmBaseSyncData() {
    override fun toString(): String {

        val timestamp = Date(timestamp).time
        val timeStampFormat = DateFormat.format("yyyy-MM-dd HH:mm:ss", timestamp)

        return "WmStepData(date=${timeStampFormat}  step=$step)"
    }
}

/**
 * distance value 距离数据 米
 */
class WmDistanceData(
    /**
     * distance value 米
     */
    val distance: Int,
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmDistanceData(distance=$distance)"
    }
}

/**
 * calorie value 卡路里数据
 */
class WmCaloriesData(
    /**
     * calorie value 单位：卡 calorie
     */
    val calorie: Int,
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmCaloriesData(calorie=$calorie)"
    }
}

class WmBloodPressureData(
    /**
     * systolic blood pressure (unit mmHg)
     */
    val sbp: Int, //收缩压值

    /**
     * diastolic blood pressure (unit mmHg)
     */
    val dbp: Int //舒张压值
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmBloodPressureData(sbp=$sbp, dbp=$dbp)"
    }
}

class WmBloodPressureMeasureData(
    /**
     * systolic blood pressure (unit mmHg)
     */
    val sbp: Int, //收缩压值

    /**
     * diastolic blood pressure (unit mmHg)
     */
    val dbp: Int, //舒张压值

    /**
     * Additional heart rate values.
     * This value exists only if [WmDeviceInfo.Feature.BLOOD_PRESSURE_AIR_PUMP] is support
     */
    val heartRate: Int
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmBloodPressureMeasureData(sbp=$sbp, dbp=$dbp, heartRate=$heartRate)"
    }
}

/**
 * realtime heart rate 实时心率
 */
class WmRealtimeRateData(
    /**
     * Respiratory rate value (breaths per minute)
     */
    val rate: Int
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmRealtimeRateData(rate=$rate)"
    }
}

/**
 * step frequency per 10 seconds 每十秒中步频
 */
class WmStepFrequencyData(
    /**
     * Respiratory frequency value
     */
    val frequency: Int
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmStepFrequencyData(frequency=$frequency)"
    }
}

class WmPressureData(
    /**
     * Pressure value. Limit(0,256)
     */
    val pressure: Int
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmPressureData(pressure=$pressure)"
    }
}


class WmTemperatureData(
    /**
     * Temperature of your body(unit ℃)。
     * This value is generally in the normal body temperature range[36℃-42℃].
     */
    val body: Float,
    /**
     * Temperature of your wrist(unit ℃)。
     * The range of this value is wider, because it is related to the ambient temperature, in extreme cases it may be below 0℃.
     */
    val wrist: Float
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmTemperatureData(body=$body, wrist=$wrist)"
    }
}


class WmGameData(
    /**
     * Game Type
     */
    val type: Int,

    /**
     * Game duration in seconds
     */
    val duration: Int,
    val score: Int,
    val level: Int
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmGameData(type=$type, duration=$duration, score=$score, level=$level)"
    }
}

/**
 * Duration of continuous activity per hour 每小时持续活动的时长
 * duration :seconds
 */
class WmActivityDurationData(
    val duration: Int
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmActivityDurationData(duration=$duration)"
    }
}

/**
 * The ecg data.
 *
 * If [WmDeviceInfo.Feature.TI_ECG] is supported, you can adjust the speed and amplitude of ECG data.
 */
class WmEcgData(
    /**
     * Sampling rate (number of ECG values per second)
     *
     * Such as 100Hz represents 10ms of a data point
     */
    val samplingRate: Int,
    /**
     * Ecg values
     */
    val items: List<Int>,
) : WmBaseSyncData() {
    companion object {
        const val DEFAULT_SAMPLING_RATE = 100
    }

    override fun toString(): String {
        return "WmEcgData(samplingRate=$samplingRate, items=$items)"
    }


}

/**
 * Sleep synchronization data 睡眠同步数据
 */
class WmSleepData(
    val wmSleepSettings: WmSleepSettings,
    val wmSleepSummary: WmSleepSummary,
    val wmSleepData: List<WmSleepItem>
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmSleepData(wmSleepSettings=$wmSleepSettings, wmSleepSummary=$wmSleepSummary, wmSleepData=$wmSleepData)"
    }
}

/**
 * SleepSummary 睡眠概览
 */
data class WmSleepSummary(
    var dateStamp: Long,// 日期时间 (年月日，豪秒，4 bytes)
    var bedTime: Long,// 相对于date 入睡时间（豪秒， 4 bytes）
    var getUpTime: Long,// 相对于date 起床时间（豪秒， 4 bytes）
    var totalSleepMinutes: Int,// 睡眠时长（分钟，4 bytes）

    var sleepType: Int,// 睡眠类型 0：白天睡眠， 1：夜晚睡眠(1 byte)

    var awakeSleepMinutes: Int,// 清醒时长（分钟, 2 bytes）
    var lightSleepMinutes: Int,// 浅睡时长（分钟, 2 bytes）
    var deepSleepMinutes: Int,// 深睡时长（分钟, 2 bytes）
    var remSleepMinutes: Int,// 快速眼动时长（分钟, 2 bytes）

    var awakeSleepCount: Int,// 清醒次数（2 bytes）
    var lightSleepCount: Int,// 浅睡次数（2 bytes）
    var deepSleepCount: Int,// 深睡次数（2 bytes）
    var remSleepCount: Int,// 快速眼动次数（2 bytes）

    var awakePercentage: Int,// 清醒百分比（*100, 2 bytes）
    var lightSleepPercentage: Int,// 浅睡百分比（*100, 2 bytes）
    var deepSleepPercentage: Int,// 深睡百分比（*100, 2 bytes）
    var remSleepPercentage: Int,// 眼动百分比（*100, 2 bytes）

    var sleepScore: Int,// 睡眠得分(1 byte)

) {
    override fun toString(): String {
        return "WmSleepSummary(dateStamp=$dateStamp, bedTime=$bedTime, getUpTime=$getUpTime, totalSleepMinutes=$totalSleepMinutes, sleepType=$sleepType, deepSleepMinutes=$deepSleepMinutes, lightSleepMinutes=$lightSleepMinutes, awakeSleepMinutes=$awakeSleepMinutes, remSleepMinutes=$remSleepMinutes, deepSleepCount=$deepSleepCount, lightSleepCount=$lightSleepCount, awakeSleepCount=$awakeSleepCount, remSleepCount=$remSleepCount, awakePercentage=$awakePercentage, lightSleepPercentage=$lightSleepPercentage, deepSleepPercentage=$deepSleepPercentage, remSleepPercentage=$remSleepPercentage, sleepScore=$sleepScore)"
    }
}

class WmGpsData(
    timestamp: Long,
    /**
     * Which [FcSportData] belongs to
     */
    val sportId: String,
    val items: List<WmGpsItem>
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmGpsData(sportId='$sportId', items=$items)"
    }
}

class WmGpsItem(
    /**
     * The duration(unit seconds) of sport at which this item is generated
     */
    val duration: Int,
    val lng: Double,
    val lat: Double,
    val altitude: Float,

    /**
     * The number of satellites represents the strength of the signal at this time
     */
    val satellites: Int,
    /**
     * Is it the first point after resuming sport?
     * True for yes, false for not.
     */
    val isRestart: Boolean,
) {
    override fun toString(): String {
        return "WmGpsItem(duration=$duration, lng=$lng, lat=$lat, altitude=$altitude, satellites=$satellites, isRestart=$isRestart)"
    }
}

interface ICalculateSleepItem {
    fun getCalculateStatus(): Int
    fun getCalculateStartTime(): Int
}

class WmSleepItem(
    val status: Int,//状态
    val duration: Int//持续时间（分钟）
) : ICalculateSleepItem {
    companion object {
        /**
         * Sleep status of deep sleep
         */
        const val STATUS_DEEP = 1 //深睡

        /**
         * Sleep status of light sleep
         */
        const val STATUS_LIGHT = 2 //浅睡

        /**
         * Sleep status of sober sleep
         */
        const val STATUS_SOBER = 3 //清醒

        /**
         * Sleep status of REM sleep
         */
        const val STATUS_REM = 4 //快速眼动

    }

    //计算状态
    override fun getCalculateStatus(): Int {
        return status
    }

    //计算开始时间
    override fun getCalculateStartTime(): Int {
        return duration
    }

    override fun toString(): String {
        return "WmSleepItem(status=$status, duration=$duration)"
    }

}

/**
 * 间隔类型
 */
enum class WmIntervalType(val seconds: Int) {
    UNKNOWN(0),
    ONE_HOUR(60 * 60),
    FIVE_MINUTES(5 * 60),
    TEN_SECONDS(10)
}

enum class WmSyncDataType {
    SPORT_SUMMARY,
    STEP,
    DISTANCE,
    CALORIE,
    HEART_RATE_FIVE_MINUTES,
    HEART_RATE_ONE_HOUR,
    SLEEP,
    OXYGEN,
    BLOOD_PRESSURE,
    BLOOD_PRESSURE_MEASURE,
    PRESSURE,
    TEMPERATURE,
    GAME,
    ACTIVITY_DURATION,
    DAILY_ACTIVITY_DURATION,
}

/**
 * 请求数据的时间类
 * startTime 请求开始时间 如果为0，则默认请求7天前的开始时间
 * endTime  结束时间，如果传0，表示截止当前时间
 */
data class WmSyncTime(val startTime: Long, val endTime: Long)