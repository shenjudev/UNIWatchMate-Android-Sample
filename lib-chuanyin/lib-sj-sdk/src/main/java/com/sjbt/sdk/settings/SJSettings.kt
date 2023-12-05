package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.*
import com.base.sdk.port.setting.AbWmSetting
import com.base.sdk.port.setting.AbWmSettings
import com.sjbt.sdk.ExceptionStateListener
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData

class SJSettings(val sjUniWatch: SJUniWatch) : AbWmSettings(){

    override val settingSportGoal = SettingSportGoal(sjUniWatch)
    override val settingPersonalInfo: AbWmSetting<WmPersonalInfo> = SettingPersonalInfo(sjUniWatch)
    override val settingSedentaryReminder: AbWmSetting<WmSedentaryReminder> =
        SettingSedentaryReminder(sjUniWatch)
    override val settingSoundAndHaptic: AbWmSetting<WmSoundAndHaptic> =
        SettingSoundAndHaptic(sjUniWatch)
    override val settingUnitInfo: AbWmSetting<WmUnitInfo> = SettingUnitInfo(sjUniWatch)
    override val settingWistRaise: AbWmSetting<WmWristRaise> = SettingWistRaise(sjUniWatch)
    override val settingAppView: AbWmSetting<WmAppView> = SettingAppView(sjUniWatch)
    override val settingDrinkWater: AbWmSetting<WmSedentaryReminder> =
        SettingDrinkWaterReminder(sjUniWatch)
    override val settingHeartRate: AbWmSetting<WmHeartRateAlerts> = SettingHeartRateAlerts(sjUniWatch)
    override val settingSleepSettings: AbWmSetting<WmSleepSettings> = SettingSleepSet(sjUniWatch)

}