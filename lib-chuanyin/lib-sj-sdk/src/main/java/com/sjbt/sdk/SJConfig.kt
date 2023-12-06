package com.sjbt.sdk

import com.base.sdk.FunctionType
import com.base.sdk.entity.settings.WmFunctionSupport

const val TAG_SJ = "SJ_SDK>>>>>"
const val MAX_RETRY_COUNT = 3
const val MSG_INTERVAL = 15
const val MSG_INTERVAL_FRAME: Long = 15
const val MSG_INTERVAL_SLOW = 40

const val ALARM_NAME_LEN = 20
const val ALARM_LEN = ALARM_NAME_LEN + 5 //name len + id hour minute second enable
const val BT_ADDRESS: String = "bt_mac"
const val DEVICE_MANUFACTURER_CODE = 0xA1

fun getFuncState(wmFunctionSupport: WmFunctionSupport, functionType: FunctionType): Int {
    when (functionType) {
        FunctionType.SUPPORT_CAMERA_PREVIEW -> {
            return wmFunctionSupport.supportCameraPreview
        }

        FunctionType.SUPPORT_DIAL_MARKET -> {
            return wmFunctionSupport.supportDialMarket
        }

        FunctionType.BT_BLE_SAME_NAME -> {
            return wmFunctionSupport.supportBtBleSameName
        }

        FunctionType.SPORT_SHOW_FIXED -> {
            return wmFunctionSupport.supportShowFixMotionType
        }

        FunctionType.NOTIFY_APPS_UNFOLD -> {
            return wmFunctionSupport.supportUnfoldNotification
        }

        FunctionType.SUPPORT_REBOOT -> {
            return wmFunctionSupport.supportRebootDevice
        }

        FunctionType.SUPPORT_ACTIVITY_DURATION_GOAL -> {
            return wmFunctionSupport.supportActDurationGoal
        }

        FunctionType.SUPPORT_ALARM -> {
            return 1
        }

        FunctionType.SUPPORT_ALARM_LABEL -> {
            return wmFunctionSupport.supportAlarmLabel
        }

        FunctionType.SUPPORT_ALARM_REMARK -> {
            return wmFunctionSupport.supportAlarmRemark
        }

        FunctionType.SUPPORT_WEATHER -> {
            return wmFunctionSupport.supportWeatherState
        }

        FunctionType.SUPPORT_FIND_PHONE -> {
            return  wmFunctionSupport.supportFindPhoneState
        }

        FunctionType.SUPPORT_FIND_WEAR -> {
            return wmFunctionSupport.supportFindDeviceState
        }

        FunctionType.SUPPORT_NOTIFY -> {
            return 1
        }

        FunctionType.SUPPORT_BLE_PHONE -> {
            return wmFunctionSupport.supportBleDell
        }

        FunctionType.SUPPORT_CLOSE_BLE_PHONE -> {
            return wmFunctionSupport.supportShowBleDellSwitch
        }
        FunctionType.SUPPORT_CONTACTS -> {
            return 1
        }

        FunctionType.SUPPORT_EMERGENCY_CONTACT -> {
            return 1
        }

        FunctionType.SUPPORT_FAVORITE_CONTACTS -> {
            return 0
        }

        FunctionType.SUPPORT_QUICK_REPLY -> {
            return 0
        }

        FunctionType.SUPPORT_STEP_GOAL -> {
            return 1
        }

        FunctionType.SUPPORT_CALORIE_GOAL -> {
            return 0
        }

        FunctionType.SUPPORT_REMINDER_LONG_SIT -> {
            return 1
        }

        /**
         * 是否支持 喝水提醒
         */
        FunctionType.SUPPORT_REMINDER_DRINK_WATER -> {
            return 1
        }

        /**
         * 是否支持 洗手提醒
         */
        FunctionType.SUPPORT_REMINDER_WASH_HAND -> {
            return 0
        }

        /**
         * 是否支持 心率自动检测
         */
        FunctionType.SUPPORT_HEART_RATE_MONITOR -> {
            return 1
        }

        /**
         * 是否支持 REM快速眼动
         */
        FunctionType.SUPPORT_REM -> {
            return 0
        }

        /**
         * 是否支持 运动分类
         */
        FunctionType.SUPPORT_SPORT_TYPE -> {
            return 0
        }


        /**
         * 是否支持 运动自识别开始
         */
        FunctionType.SUPPORT_SPORT_AUTO_START -> {
            return 0
        }

        /**
         * 是否支持 运动自识别结束
         */
        FunctionType.SUPPORT_SPORT_AUTO_PAUSE -> {
            return 0
        }

        /**
         * 是否支持 世界时钟
         */
        FunctionType.SUPPORT_WORLD_CLOCK -> {
            return 0
        }

        /**
         * 是否支持 摇摇拍照
         */
        FunctionType.SUPPORT_HID_BLE -> {
            return 1
        }

        /**
         * 是否支持 遥控拍照
         * APP内置拍照功能
         *
         */
        FunctionType.SUPPORT_REMOTE_CAMERA -> {
            return 1
        }

        /**
         * 是否支持 设备语言
         */
        FunctionType.SUPPORT_LANGUAGE -> {
            return 1
        }
        /**
         * 是否支持 小部件
         */
        FunctionType.SUPPORT_SMALL_FUNCTION -> {
            return 0
        }
        /**
         * 是否支持音量调节
         *
         * @return
         */
        FunctionType.SUPPORT_VOLUME_CONTROL -> {
            return 0
        }

        /**
         * 是否支持安静心率过高提
         * 与日常心率过高提醒互斥，
         * 支持日常心率提醒，则不细分运动心率、安静心率过高提醒
         * {@link BaseWatchFunctions#isSupportDailyHeartRateWarning()}
         *
         * @return
         */
        FunctionType.SUPPORT_RESTING_HEART_RATE_WARNING -> {
            return 1
        }
        /**
         * 是否支持运动心率过高提醒
         * 与日常心率过高提醒互斥，
         * 支持日常心率提醒，则不细分运动心率、安静心率过高提醒
         * {@link BaseWatchFunctions#isSupportDailyHeartRateWarning()}
         *
         * @return
         */
        FunctionType.SUPPORT_EXERCISE_HEART_RATE_WARNING -> {
            return 1
        }

        /**
         * 是否支持日常心率过高提醒
         * 与运动心率、安静心率过高提醒互斥，
         * 支持日常心率提醒，则不细分运动心率、安静心率过高提醒
         * {@link BaseWatchFunctions#isSupportRestingHeartRateWarning()}
         * {@link BaseWatchFunctions#isSupportExerciseHeartRateWarning()}
         *
         * @return
         */
        FunctionType.SUPPORT_DAILY_HEART_RATE_WARNING -> {
            return 1
        }
        /**
         * 是否支持连续血氧
         *
         * @return
         */
        FunctionType.SUPPORT_CONTINUOUS_BLOOD_OXYGEN -> {
            return 0
        }

        /**
         * 是否支持蓝牙断连提醒设置
         *
         * @return
         */
        FunctionType.SUPPORT_BLUETOOTH_SETTINGS -> {
            return 1
        }
        /**
         * 是否支持导入本地音乐
         *
         * @return
         */
        FunctionType.SUPPORT_IMPORT_LOCAL_MUSIC -> {
            return 1
        }

        /**
         * 是否支持事件提醒
         *
         * @return
         */
        FunctionType.SUPPORT_EVENT_REMINDER -> {
            return 0
        }

        /**
         * 是否支持亮屏提醒
         *
         * @return
         */
        FunctionType.SUPPORT_SCREEN_DURATION -> {
            return 0
        }

        FunctionType.SUPPORT_REBOOT ->{
            return 1
        }


        else -> {
            return 0
        }
    }
}
