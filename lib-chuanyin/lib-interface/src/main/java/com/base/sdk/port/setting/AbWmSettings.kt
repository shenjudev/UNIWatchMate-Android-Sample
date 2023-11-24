package com.base.sdk.port.setting

import com.base.sdk.entity.settings.*

abstract class AbWmSettings {
    /**
     * sportGoalSetting 运动目标设置
     */
    abstract val settingSportGoal: AbWmSetting<WmSportGoal>

    /**
     * personalInfoSetting 个人信息设置
     */
    abstract val settingPersonalInfo: AbWmSetting<WmPersonalInfo>

    /**
     * Sedentary reminder(久坐提醒)
     */
    abstract val settingSedentaryReminder: AbWmSetting<WmSedentaryReminder>

    /**
     * soundAndHapticSetting(声音和触感设置)
     */
    abstract val settingSoundAndHaptic: AbWmSetting<WmSoundAndHaptic>

    /**
     * unitInfoSetting(单位设置)
     */
    abstract val settingUnitInfo: AbWmSetting<WmUnitInfo>

    /**
     * wistRaiseSetting 抬腕设置
     */
    abstract val settingWistRaise: AbWmSetting<WmWristRaise>

    /**
     * appViewSetting 应用视图设置
     */
    abstract val settingAppView: AbWmSetting<WmAppView>

    /**
     * drinkWaterSetting 喝水提醒设置
     */
    abstract val settingDrinkWater: AbWmSetting<WmSedentaryReminder>

    /**
     * rateSetting 心率提醒设置
     */
    abstract val settingHeartRate: AbWmSetting<WmHeartRateAlerts>

    /**
     * sleepSetting 睡眠设置
     */
    abstract val settingSleepSettings: AbWmSetting<WmSleepSettings>
}