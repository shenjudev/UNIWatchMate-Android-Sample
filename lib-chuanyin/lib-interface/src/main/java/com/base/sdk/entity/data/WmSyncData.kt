package com.base.sdk.entity.data

import com.base.sdk.entity.apps.WmValueTypeData
import com.base.sdk.entity.settings.WmSleepSettings
import java.io.Serializable

abstract class WmBaseSyncData(
    val timestamp: Long = 0
)

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
)

/**
 * 运动小结
 */
class WmSportSummaryData(
    /**
     * 运动Id
     */
    val sportId: Int,

    /**
     * 基本参数类型
     */
    val valueType: List<WmValueTypeData>
) : WmBaseSyncData(), Serializable

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
    val duration: Int

) : WmBaseSyncData()

/**
 * Oxygen value 血氧数据
 */
class WmOxygenData(
    /**
     * Oxygen value (SpO2)，0~100
     */
    val oxygen: Int
) : WmBaseSyncData()

/**
 * Step value 步数数据
 */
class WmStepData(
    /**
     * Step value
     */
    val step: Int,

    ) : WmBaseSyncData()

/**
 * distance value 距离数据
 */
class WmDistanceData(
    /**
     * distance value
     */
    val distance: Int,
) : WmBaseSyncData()

/**
 * calorie value 卡路里数据
 */
class WmCaloriesData(
    /**
     * calorie value
     */
    val calorie: Int,
) : WmBaseSyncData()

class WmBloodPressureData(
    /**
     * systolic blood pressure (unit mmHg)
     */
    val sbp: Int, //收缩压值

    /**
     * diastolic blood pressure (unit mmHg)
     */
    val dbp: Int //舒张压值
) : WmBaseSyncData()

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
) : WmBaseSyncData()

class WmRealtimeRateData(
    /**
     * Respiratory rate value (breaths per minute)
     */
    val rate: Int
) : WmBaseSyncData()

class WmPressureData(
    /**
     * Pressure value. Limit(0,256)
     */
    val pressure: Int
) : WmBaseSyncData()


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
) : WmBaseSyncData()


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
) : WmBaseSyncData()

/**
 * 活动时长
 */
class WmActivityData(
    val duration: Int
) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmActivityData(activity=$duration, duration=$duration)"
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

}

/**
 * 睡眠同步数据
 */
class WmSleepData(
    val wmSleepSettings: WmSleepSettings,
    val wmSleepSummary: WmSleepSummary,
    val wmSleepData: List<WmSleepItem>
) : WmBaseSyncData()

/**
 * 睡眠概览
 */
data class WmSleepSummary(
    var dateStamp: Long,// 日期时间戳 (毫秒)
    var bedTime: Long,// 入睡时间时间戳（毫秒）
    var getUpTime: Long,// 起床时间时间戳（毫秒）
    var totalSleepMinutes: Int,// 睡眠时长

    var sleepType: Int,// 睡眠类型 0：白天睡眠， 1：夜晚睡眠

    var deepSleepMinutes: Short,// 深睡时长(毫秒)
    var lightSleepMinutes: Short,// 浅睡时长(毫秒)
    var awakeSleepMinutes: Short,// 清醒时长(毫秒)
    var remSleepMinutes: Short,// 快速眼动时长(毫秒)

    var deepSleepCount: Int,// 深睡次数
    var lightSleepCount: Int,// 浅睡次数
    var awakeSleepCount: Int,// 清醒次数
    var remSleepCount: Int,// 快速眼动次数

    var awakePercentage: Int,// 清醒百分比
    var lightSleepPercentage: Int,// 浅睡百分比
    var deepSleepPercentage: Int,// 深睡百分比
    var remSleepPercentage: Int,// 眼动百分比

    var sleepScore: Int,// 睡眠得分

)

class WmGpsData(
    timestamp: Long,
    /**
     * Which [FcSportData] belongs to
     */
    val sportId: String,
    val items: List<WmGpsItem>
) : WmBaseSyncData()

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
)

/**
 * Today total data(今日总数据)
 */
class WmTodayTotalData(

    /**
     * Total steps
     */
    val step: Int, //总步数

    /**
     * Total distance. (unit m)
     */
    val distance: Int,//总数据，单位米

    /**
     *Total calorie. (unit calorie, not kCal)
     */
    val calorie: Int, //总卡路里数，单位卡，不是千卡

    /**
     * Total deep sleep time. (unit minutes)
     */
    val deepSleep: Int,//深睡总时长，单位分钟

    /**
     * Total light sleep time. (unit minutes)
     */
    val lightSleep: Int, //浅睡总时长，单位分钟

    /**
     * Average heart rate. (beats per minute)
     */
    val heartRate: Int, //平均心率

    /**
     * Step not save in item.
     */
    val deltaStep: Int, //未保存在item中的步数

    /**
     * Distance not save in item. (unit m)
     */
    val deltaDistance: Int,//未保存在item中的距离，单位米

    /**
     * Calorie not save in item. (unit calorie, not kCal)
     */
    val deltaCalorie: Int,//未保存在item中的卡路里数，单位卡，不是千卡

) : WmBaseSyncData()

interface ICalculateSleepItem {
    fun getCalculateStatus(): Int
    fun getCalculateStartTime(): Short
}

class WmSleepItem(
    val status: Int,//状态
    val duration: Short//开始时间
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
    override fun getCalculateStartTime(): Short {
        return duration
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
    HEART_RATE,
    OXYGEN,
    BLOOD_PRESSURE,
    BLOOD_PRESSURE_MEASURE,
    REALTIME_RATE,
    PRESSURE,
    TEMPERATURE,
    GAME,
    ACTIVITY,
}

