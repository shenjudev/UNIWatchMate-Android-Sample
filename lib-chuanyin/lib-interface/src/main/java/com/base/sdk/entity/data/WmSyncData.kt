package com.base.sdk.entity.data

import android.text.format.DateFormat
import com.base.sdk.entity.apps.WmValueTypeData
import com.base.sdk.entity.settings.WmSleepSettings
import java.io.Serializable
import java.util.*

abstract class WmBaseSyncData(
    var timestamp: Long = 0
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
) {
    override fun toString(): String {
        return "WmSyncData(type=$type, timestamp=$timestamp, intervalType=$intervalType, value=$value)"
    }
}

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
) : WmBaseSyncData(), Serializable {
    override fun toString(): String {
        return "WmSportSummaryData(sportId=$sportId, valueType=$valueType)"
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
 * distance value 距离数据
 */
class WmDistanceData(
    /**
     * distance value
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
     * calorie value
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
        return "WmActivityDurationData(activity=$duration, duration=$duration)"
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
    var dateStamp: Int,// 日期时间 (年月日，4 bytes)
    var bedTime: Int,// 相对于date 入睡时间（秒， 4 bytes）
    var getUpTime: Int,// 相对于date 起床时间（秒， 4 bytes）
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

) : WmBaseSyncData() {
    override fun toString(): String {
        return "WmTodayTotalData(step=$step, distance=$distance, calorie=$calorie, deepSleep=$deepSleep, lightSleep=$lightSleep, heartRate=$heartRate, deltaStep=$deltaStep, deltaDistance=$deltaDistance, deltaCalorie=$deltaCalorie)"
    }
}

interface ICalculateSleepItem {
    fun getCalculateStatus(): Int
    fun getCalculateStartTime(): Int
}

class WmSleepItem(
    val status: Int,//状态
    val duration: Int//持续时间
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
    OXYGEN,
    BLOOD_PRESSURE,
    BLOOD_PRESSURE_MEASURE,
    REALTIME_RATE,
    PRESSURE,
    TEMPERATURE,
    GAME,
    ACTIVITY,
}

