package com.sjbt.sdk

import com.base.sdk.FunctionType
import com.sjbt.sdk.entity.ActionSupport

const val TAG_SJ = "SJ_SDK>>>>>"
const val MAX_RETRY_COUNT = 3
const val MSG_INTERVAL = 15
const val MSG_INTERVAL_FRAME: Long = 15
const val MSG_INTERVAL_SLOW = 40

const val ALARM_NAME_LEN = 20
const val BT_ADDRESS: String = "bt_mac"
const val DEVICE_MANUFACTURER_CODE = 0xA1

fun getFuncState(actionSupport: ActionSupport?, functionType: FunctionType): Boolean {
    when (functionType) {
        FunctionType.SUPPORT_CAMERA_PREVIEW -> {
            return true
        }

        FunctionType.SUPPORT_DIAL_MARKET -> {
            return true
        }

        FunctionType.BT_BLE_SAME_NAME -> {
            return true
        }

        FunctionType.SPORT_SHOW_FIXED -> {
            return false
        }

        FunctionType.NOTIFY_APPS_UNFOLD -> {
            return false
        }

        FunctionType.SUPPORT_REBOOT -> {
            return true
        }

        FunctionType.SUPPORT_ACTIVITY_DURATION_GOAL -> {
            return false
        }

        FunctionType.SUPPORT_ALARM -> {
            return true
        }

        FunctionType.SUPPORT_ALARM_LABEL -> {
            return false
        }

        FunctionType.SUPPORT_ALARM_REMARK -> {
            return true
        }

        FunctionType.SUPPORT_WEATHER -> {
            return true
        }

        FunctionType.SUPPORT_FIND_PHONE -> {
            return true
        }

        FunctionType.SUPPORT_FIND_WEAR -> {
            return true
        }

        FunctionType.SUPPORT_NOTIFY -> {
            return true
        }

        FunctionType.SUPPORT_BLE_PHONE -> {
            return false
        }

        FunctionType.SUPPORT_CLOSE_BLE_PHONE -> {
            return false
        }
        FunctionType.SUPPORT_CONTACTS -> {
            return true
        }

        FunctionType.SUPPORT_EMERGENCY_CONTACT -> {
            return true
        }

        FunctionType.SUPPORT_FAVORITE_CONTACTS -> {
            return false
        }

        FunctionType.SUPPORT_QUICK_REPLY -> {
            return false
        }

        FunctionType.SUPPORT_STEP_GOAL -> {
            return true
        }

        FunctionType.SUPPORT_CALORIE_GOAL -> {
            return false
        }

        FunctionType.SUPPORT_REMINDER_LONG_SIT -> {
            return true
        }

        /**
         * 是否支持 喝水提醒
         */
        FunctionType.SUPPORT_REMINDER_DRINK_WATER -> {
            return true
        }

        /**
         * 是否支持 洗手提醒
         */
        FunctionType.SUPPORT_REMINDER_WASH_HAND -> {
            return false
        }

        /**
         * 是否支持 心率自动检测
         */
        FunctionType.SUPPORT_HEART_RATE_MONITOR -> {
            return true
        }

        /**
         * 是否支持 REM快速眼动
         */
        FunctionType.SUPPORT_REM -> {
            return false
        }

        /**
         * 是否支持 运动分类
         */
        FunctionType.SUPPORT_SPORT_TYPE -> {
            return false
        }


        /**
         * 是否支持 运动自识别开始
         */
        FunctionType.SUPPORT_SPORT_AUTO_START -> {
            return false
        }

        /**
         * 是否支持 运动自识别结束
         */
        FunctionType.SUPPORT_SPORT_AUTO_PAUSE -> {
            return false
        }

        /**
         * 是否支持 世界时钟
         */
        FunctionType.SUPPORT_WORLD_CLOCK -> {
            return false
        }

        /**
         * 是否支持 摇摇拍照
         */
        FunctionType.SUPPORT_HID_BLE -> {
            return true
        }

        /**
         * 是否支持 遥控拍照
         * APP内置拍照功能
         *
         */
        FunctionType.SUPPORT_REMOTE_CAMERA -> {
            return true
        }

        /**
         * 是否支持 设备语言
         */
        FunctionType.SUPPORT_LANGUAGE -> {
            return true
        }
        /**
         * 是否支持 小部件
         */
        FunctionType.SUPPORT_SMALL_FUNCTION -> {
            return false
        }
        /**
         * 是否支持音量调节
         *
         * @return
         */
        FunctionType.SUPPORT_VOLUME_CONTROL -> {
            return false
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
            return true
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
            return true
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
            return true
        }
        /**
         * 是否支持连续血氧
         *
         * @return
         */
        FunctionType.SUPPORT_CONTINUOUS_BLOOD_OXYGEN -> {
            return false
        }

        /**
         * 是否支持蓝牙断连提醒设置
         *
         * @return
         */
        FunctionType.SUPPORT_BLUETOOTH_SETTINGS -> {
            return true
        }
        /**
         * 是否支持导入本地音乐
         *
         * @return
         */
        FunctionType.SUPPORT_IMPORT_LOCAL_MUSIC -> {
            return true
        }

        /**
         * 是否支持事件提醒
         *
         * @return
         */
        FunctionType.SUPPORT_EVENT_REMINDER -> {
            return false
        }

        /**
         * 是否支持亮屏提醒
         *
         * @return
         */
        FunctionType.SUPPORT_SCREEN_DURATION -> {
            return false
        }


        else -> {
            return false
        }
    }
}
