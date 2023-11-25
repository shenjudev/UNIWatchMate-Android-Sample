package com.sjbt.sdk.app

import com.base.sdk.port.app.*
import com.sjbt.sdk.SJUniWatch

class SJApps(val sjUniWatch: SJUniWatch) : AbWmApps() {

    override var appAlarm: AbAppAlarm = AppAlarm(sjUniWatch)

    override var appCamera: AbAppCamera = AppCamera(sjUniWatch)

    override var appContact: AbAppContact = AppContact(sjUniWatch)

    override var appFind: AbAppFind = AppFind(sjUniWatch)

    override var appWeather: AbAppWeather = AppWeather(sjUniWatch)

    override var appSport: AbAppSport = AppSport(sjUniWatch)

    override var appNotification: AbAppNotification = AppNotification(sjUniWatch)

    override var appDial: AbAppDial = AppDial(sjUniWatch)

    override var appLanguage: AbAppLanguage = AppLanguage(sjUniWatch)

    override val appMusicControl:AbAppMusicControl = AppMusicControl(sjUniWatch)

    override val appDateTime: AbAppDateTime = AppDateTime(sjUniWatch)
}