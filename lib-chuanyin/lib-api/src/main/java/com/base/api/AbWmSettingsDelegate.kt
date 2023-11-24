package com.base.api

import com.base.sdk.AbUniWatch
import com.base.sdk.port.setting.AbWmSetting
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.entity.settings.*

internal class AbWmSettingsDelegate(
    private val watchObservable: BehaviorObservable<AbUniWatch>
) : AbWmSettings() {

    override var settingSportGoal: AbWmSetting<WmSportGoal>
        get() = watchObservable.value!!.wmSettings?.settingSportGoal
        set(value) {}
    override var settingPersonalInfo: AbWmSetting<WmPersonalInfo>
        get() = watchObservable.value!!.wmSettings?.settingPersonalInfo
        set(value) {}
    override var settingSedentaryReminder: AbWmSetting<WmSedentaryReminder>
        get() = watchObservable.value!!.wmSettings?.settingSedentaryReminder
        set(value) {}

    override var settingSoundAndHaptic: AbWmSetting<WmSoundAndHaptic>
        get() = watchObservable.value!!.wmSettings?.settingSoundAndHaptic
        set(value) {}

    override var settingUnitInfo: AbWmSetting<WmUnitInfo>
        get() = watchObservable.value!!.wmSettings?.settingUnitInfo
        set(value) {}

    override var settingWistRaise: AbWmSetting<WmWristRaise>
        get() = watchObservable.value!!.wmSettings?.settingWistRaise
        set(value) {}

    override var settingAppView: AbWmSetting<WmAppView>
        get() = watchObservable.value!!.wmSettings?.settingAppView
        set(value) {}

    override var settingDrinkWater: AbWmSetting<WmSedentaryReminder>
        get() = watchObservable.value!!.wmSettings?.settingDrinkWater
        set(value) {}

    override var settingHeartRate: AbWmSetting<WmHeartRateAlerts>
        get() = watchObservable.value!!.wmSettings?.settingHeartRate
        set(value) {}

    override val settingSleepSettings: AbWmSetting<WmSleepSettings>
        get() = watchObservable.value!!.wmSettings?.settingSleepSettings

}