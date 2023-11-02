package com.base.sdk.entity.data

import com.base.sdk.entity.apps.WmValueTypeData
import java.io.Serializable

abstract class WmBaseSyncData(
    /**
     * Timestamp of this data
     */
    val timestamp: Long,
)

/**
 * 运动小结
 */
class WmSportSummaryData(
    timestamp: Long,//开始时间，运动时长在valueType中
    val sportId: Int,
    /**
     * 基本参数类型
     */
    val valueType: List<WmValueTypeData>,
) : WmBaseSyncData(timestamp), Serializable {

}

/**
 * 平均 value 血氧数据
 */
class WmHeartRateData(
    timestamp: Long,
    val intervalTime: Long,
    /**
     * heart rate value (beats per minute)
     */
    val minHeartRate: Int,
    val maxHeartRate: Int,
    val avgHeartRate: Int,

    /**
     * activity duration, in seconds
     */
    val duration: Int,

    ) : WmBaseSyncData(timestamp)

/**
 * Oxygen value 血氧数据
 */
class WmOxygenData(
    timestamp: Long,
    val intervalTime: Long,
    /**
     * Oxygen value (SpO2)，0~100
     */
    val oxygen: Int,
) : WmBaseSyncData(timestamp)

/**
 * Step value 步数数据
 */
class WmStepData(
    timestamp: Long,
    val intervalTime: Long,//间隔时间5分钟或1小时，单位：秒
    /**
     * Step value
     */
    val step: Int,

    ) : WmBaseSyncData(timestamp)

/**
 * distance value 距离数据
 */
class WmDistanceData(
    timestamp: Long,
    val intervalTime: Long,
    /**
     * distance value
     */
    val distance: Int,

    ) : WmBaseSyncData(timestamp)

/**
 * calorie value 卡路里数据
 */
class WmCaloriesData(
    timestamp: Long,
    val intervalTime: Long,
    /**
     * calorie value
     */
    val calorie: Int,

    ) : WmBaseSyncData(timestamp)


class WmBloodPressureData(
    timestamp: Long,
    val intervalTime: Long,
    /**
     * systolic blood pressure (unit mmHg)
     */
    val sbp: Int, //收缩压值

    /**
     * diastolic blood pressure (unit mmHg)
     */
    val dbp: Int, //舒张压值
) : WmBaseSyncData(timestamp)

class WmBloodPressureMeasureData(
    timestamp: Long,
    val intervalTime: Long,
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
    val heartRate: Int,
) : WmBaseSyncData(timestamp)

class WmRealtimeRateData(
    timestamp: Long,
    val intervalTime: Long,
    /**
     * Respiratory rate value (breaths per minute)
     */
    val rate: Int,
) : WmBaseSyncData(timestamp)

class WmPressureData(
    timestamp: Long,
    val intervalTime: Long,
    /**
     * Pressure value. Limit(0,256)
     */
    val pressure: Int,
) : WmBaseSyncData(timestamp)


class WmTemperatureData(
    timestamp: Long,
    val intervalTime: Long,
    /**
     * Temperature of your body(unit ℃)。
     * This value is generally in the normal body temperature range[36℃-42℃].
     */
    val body: Float,
    /**
     * Temperature of your wrist(unit ℃)。
     * The range of this value is wider, because it is related to the ambient temperature, in extreme cases it may be below 0℃.
     */
    val wrist: Float,
) : WmBaseSyncData(timestamp)


class WmGameData(
    /**
     * Game start time
     */
    timestamp: Long,
    val intervalTime: Long,
    /**
     * Game Type
     */
    val type: Int,

    /**
     * Game duration in seconds
     */
    val duration: Int,
    val score: Int,
    val level: Int,
) : WmBaseSyncData(timestamp)

/**
 * 活动时长
 */
class WmActivityData(
    timestamp: Long,
    val intervalTime: Long,
    val duration: Int,
) : WmBaseSyncData(timestamp) {
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
    timestamp: Long,
    val intervalTime: Long,
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
) : WmBaseSyncData(timestamp) {
    companion object {
        const val DEFAULT_SAMPLING_RATE = 100
    }

}

