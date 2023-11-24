package com.base.sdk.entity.settings

/**
 * WmFunctionSupport 功能是否支持数据结构定义
 */
open class WmFunctionSupport {

    var supportFunctionVersion = 0
    var supportDialState = 0
    var supportFindDeviceState = 0
    var supportFindPhoneState = 0
    var supportOtaState = 0
    var supportTransferEbookState = 0
    var supportSleepState = 0
    var supportCameraControlState = 0
    var sportSupportState = 0
    var supportRateState = 0
    var supportBloodOxygenState = 0
    var supportBloodPressState = 0
    var supportBloodSugarState = 0
    var supportNotifyMsgState = 0
    var supportAlarmState = 0
    var supportTransferMusicState = 0
    var supportContactState = 0
    var supportAppViewState = 0
    var supportSetRingState = 0
    var supportSetNotifyTouchState = 0
    var supportSetCrownTouchState = 0
    var supportSetSystemTouchState = 0
    var supportWristScreenState = 0
    var supportWeatherState = 0
    var supportSlowModel = 0
    var supportCameraPreview = 0
    var supportVideoTransfer = 0

    var supportPayeeCode = 0
    /**
     *
     * 是否支持 表盘市场
     */
    var supportDialMarket = 0
    var supportUnfoldNotification = 0
    var supportBleDell = 0
    var supportShowBleDellSwitch = 0
    var supportEmergencyContact = 0
    var supportSyncCollectContact = 0
    var supportQuickRespond = 0
    var supportStepGoal = 0
    var supportCalorieGoal = 0
    var supportActDurationGoal = 0
    var supportSedentaryReminder = 0
    var supportDrinkWaterReminder = 0
    var supportWashHandsReminder = 0
    var supportAutoRate = 0
    var supportREM = 0
    var supportMultiSport = 0
    var supportShowFixMotionType = 0
    var supportSportAutoRecogniseStart = 0
    var supportSportAutoRecogniseEnd = 0
    var supportAlarmLabel = 0
    var supportAlarmRemark = 0
    var supportWorldClock = 0
    var supportAppChangeLanguage = 0
    var supportAppControlVolume = 0
    var supportWidgets = 0
    var supportQuietHeartRateAlert = 0
    var supportSportHeartRateAlert = 0
    var supportDailyHeartRateAlert = 0
    var supportContinuousOxygen = 0
    var supportBTDisconnectReminder = 0
    var supportBtBleSameName = 0
    var supportEventReminder = 0
    var supportScreenReminder = 0
    var supportRebootDevice = 0

    var maxContacts = 0
    var sideButtonCount = 0
    var fixedSportCount = 0
    var variableSportCount = 0

    override fun toString(): String {
        return "WmFunctionSupport(actVersion=$supportFunctionVersion, dialSupportState=$supportDialState, searchDeviceSupportState=$supportFindDeviceState, searchPhoneSupportState=$supportFindPhoneState, otaSupportState=$supportOtaState, ebookSupportState=$supportTransferEbookState, sleepSupportState=$supportSleepState, cameraSupportState=$supportCameraControlState, sportSupportState=$sportSupportState, rateSupportState=$supportRateState, bloodOxSupportState=$supportBloodOxygenState, bloodPressSupportState=$supportBloodPressState, bloodSugarSupportState=$supportBloodSugarState, notifyMsgSupportState=$supportNotifyMsgState, alarmSupportState=$supportAlarmState, musicSupportState=$supportTransferMusicState, contactSupportState=$supportContactState, appViewSupportState=$supportAppViewState, setRingSupportState=$supportSetRingState, setNotifyTouchSupportState=$supportSetNotifyTouchState, setWatchTouchSupportState=$supportSetCrownTouchState, setSystemTouchSupportState=$supportSetSystemTouchState, armSupportState=$supportWristScreenState, weatherSupportState=$supportWeatherState, supportSlowModel=$supportSlowModel, supportCameraPreview=$supportCameraPreview, supportVideoTransfer=$supportVideoTransfer, supportPayeeCode=$supportPayeeCode, supportDialMarket=$supportDialMarket, supportExpandNotification=$supportUnfoldNotification, supportBluetoothDell=$supportBleDell, supportShowBluetoothDellSwitch=$supportShowBleDellSwitch, supportEmergencyContact=$supportEmergencyContact, supportSyncCollectContact=$supportSyncCollectContact, supportQuickRespond=$supportQuickRespond, supportStepGoal=$supportStepGoal, supportCalorieGoal=$supportCalorieGoal, supportActDurationGoal=$supportActDurationGoal, supportSedentaryReminder=$supportSedentaryReminder, supportDrinkWaterReminder=$supportDrinkWaterReminder, supportWashHandsReminder=$supportWashHandsReminder, supportAutoRate=$supportAutoRate, supportREM=$supportREM, supportMultiSport=$supportMultiSport, supportFixMotionType=$supportShowFixMotionType, supportSportAutoRecogniseStart=$supportSportAutoRecogniseStart, supportSportAutoRecogniseEnd=$supportSportAutoRecogniseEnd, supportAlarmLabel=$supportAlarmLabel, supportAlarmRemark=$supportAlarmRemark, supportWorldClock=$supportWorldClock, supportAppChangeLanguage=$supportAppChangeLanguage, supportAppControlVolume=$supportAppControlVolume, supportWidgets=$supportWidgets, supportQuietHeartRateAlert=$supportQuietHeartRateAlert, supportSportHeartRateAlert=$supportSportHeartRateAlert, supportDailyHeartRateAlert=$supportDailyHeartRateAlert, supportContinuousOxygen=$supportContinuousOxygen, supportBTDisconnectReminder=$supportBTDisconnectReminder, supportBtBleSameName=$supportBtBleSameName, supportEventReminder=$supportEventReminder, supportScreenReminder=$supportScreenReminder, supportRebootDevice=$supportRebootDevice, maxContacts=$maxContacts, sideButtonCount=$sideButtonCount, fixedSportCount=$fixedSportCount, variableSportCount=$variableSportCount)"
    }

    /**
     * 是否支持 通知
     */

    /**
     * 通知列表是否全部展开
     */

    /**
     * 是否支持 通话蓝牙
     */
//    SUPPORT_BLE_PHONE,

    /**
     * 是否支持 显示关闭通话蓝牙
     */
//    SUPPORT_CLOSE_BLE_PHONE,

    /**
     * 是否支持 同步联系人
     */
//    SUPPORT_CONTACTS,

    /**
     * 是否支持紧急联系人
     *
     */
//    SUPPORT_EMERGENCY_CONTACT,

    /**
     * 是否支持 同步收藏联系人
     */
//    SUPPORT_FAVORITE_CONTACTS,

    /**
     * 是否支持 快捷回复
     */
//    SUPPORT_QUICK_REPLY,

    /**
     * 是否支持步数目标
     */
//    SUPPORT_STEP_GOAL,

    /**
     * 是否支持卡路里目标
     *
     */
//    SUPPORT_CALORIE_GOAL,

    /**
     * 是否支持活动时长目标
     *
     */
//    SUPPORT_ACTIVITY_DURATION_GOAL,
    /**
     * 是否支持 久坐提醒
     */
//    SUPPORT_REMINDER_LONG_SIT,

    /**
     * 是否支持 喝水提醒
     */
//    SUPPORT_REMINDER_DRINK_WATER,

    /**
     * 是否支持 洗手提醒
     */
//    SUPPORT_REMINDER_WASH_HAND,

    /**
     * 是否支持 心率自动检测
     */
//    SUPPORT_HEART_RATE_MONITOR,

    /**
     * 是否支持 REM快速眼动
     */
//    SUPPORT_REM,

    /**
     * 是否支持 运动分类
     */
//    SUPPORT_SPORT_TYPE,

    /**
     * 是否显示固定运动类型
     */
//    SPORT_SHOW_FIXED,

    /**
     * 是否支持 运动自识别开始
     */
//    SUPPORT_SPORT_AUTO_START,

    /**
     * 是否支持 运动自识别结束
     */
//    SUPPORT_SPORT_AUTO_PAUSE,

    /**
     * 是否支持 闹钟
     */
//    SUPPORT_ALARM,
//
//    /**
//     * 是否支持闹钟标签
//     *
//     */
//    SUPPORT_ALARM_LABEL,
//
//    /**
//     * 是否支持闹钟备注
//     *
//     */
//    SUPPORT_ALARM_REMARK,
//
//    /**
//     * 是否支持 天气
//     */
//    SUPPORT_WEATHER,
//
//    /**
//     * 是否支持 查找手机
//     */
//    SUPPORT_FIND_PHONE,
//
//    /**
//     * 是否支持 查找手表
//     */
//    SUPPORT_FIND_WEAR,
//
//    /**
//     * 是否支持 世界时钟
//     */
//    SUPPORT_WORLD_CLOCK,
//
//    /**
//     * 是否支持 摇摇拍照
//     */
//    SUPPORT_HID_BLE,
//
//    /**
//     * 是否支持 遥控拍照
//     * APP内置拍照功能
//     *
//     */
//    SUPPORT_REMOTE_CAMERA,
//
//    /**
//     * 是否支持拍照预览
//     */
//    SUPPORT_CAMERA_PREVIEW,
//
//    /**
//     * 是否支持 设备语言
//     */
//    SUPPORT_LANGUAGE,
//    /**
//     * 是否支持 小部件
//     */
//    SUPPORT_SMALL_FUNCTION,
//    /**
//     * 是否支持音量调节
//     *
//     * @return
//     */
//    SUPPORT_VOLUME_CONTROL,
//
//    /**
//     * 是否支持安静心率过高提
//     * 与日常心率过高提醒互斥，
//     * 支持日常心率提醒，则不细分运动心率、安静心率过高提醒
//     * {@link BaseWatchFunctions#isSupportDailyHeartRateWarning()}
//     *
//     * @return
//     */
//    SUPPORT_RESTING_HEART_RATE_WARNING,
//    /**
//     * 是否支持运动心率过高提醒
//     * 与日常心率过高提醒互斥，
//     * 支持日常心率提醒，则不细分运动心率、安静心率过高提醒
//     * {@link BaseWatchFunctions#isSupportDailyHeartRateWarning()}
//     *
//     * @return
//     */
//    SUPPORT_EXERCISE_HEART_RATE_WARNING,
//
//    /**
//     * 是否支持日常心率过高提醒
//     * 与运动心率、安静心率过高提醒互斥，
//     * 支持日常心率提醒，则不细分运动心率、安静心率过高提醒
//     * {@link BaseWatchFunctions#isSupportRestingHeartRateWarning()}
//     * {@link BaseWatchFunctions#isSupportExerciseHeartRateWarning()}
//     *
//     * @return
//     */
//    SUPPORT_DAILY_HEART_RATE_WARNING,
//    /**
//     * 是否支持连续血氧
//     *
//     * @return
//     */
//    SUPPORT_CONTINUOUS_BLOOD_OXYGEN,
//
//    /**
//     * 是否支持蓝牙断连提醒设置
//     *
//     * @return
//     */
//    SUPPORT_BLUETOOTH_SETTINGS,
//    /**
//     * 是否支持导入本地音乐
//     *
//     * @return
//     */
//    SUPPORT_IMPORT_LOCAL_MUSIC,
//
//    /**
//     * 通话蓝牙与BLE是否同名
//     *
//     * @return
//     */
//    BT_BLE_SAME_NAME,
//
//    /**
//     * 是否支持事件提醒
//     *
//     * @return
//     */
//    SUPPORT_EVENT_REMINDER,
//
//    /**
//     * 是否支持亮屏提醒
//     *
//     * @return
//     */
//    SUPPORT_SCREEN_DURATION,
//
//    /**
//     * 是否支持 重启设备
//     */
//    SUPPORT_REBOOT
//    */

}