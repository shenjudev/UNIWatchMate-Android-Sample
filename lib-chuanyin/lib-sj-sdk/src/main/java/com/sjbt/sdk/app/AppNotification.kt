package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmNotification
import com.base.sdk.port.app.AbAppNotification
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.core.SingleOnSubscribe

class AppNotification(sjUniWatch: SJUniWatch) : AbAppNotification() {
    val sjUniWatch = sjUniWatch
    var sendNotificationEmitter: SingleEmitter<Boolean>? = null

    val TAG = "AppNotification"
    val sms_package_name = "com.android.sms"
    val others_package_name = "com.android.others"
    // 所有的应用的包名列表(除了短信和others)
    val appPackageList = listOf(
        "com.facebook.katana",
        "com.google.android.gm",
        "com.instagram.android",
        "jp.naver.line.android",
        "com.linkedin.android",
        "com.facebook.orca",
        "com.microsoft.office.outlook",
        "com.tencent.mobileqq",
        "com.skype.raider",
        "com.snapchat.android",
        "org.telegram.messenger",
        "com.twitter.android",
        "com.tencent.mm",
        "com.whatsapp",
        "com.whatsapp.w4b"
    )
    // 所有短信的包名列表
    val smsPackageList = listOf(
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.lge.message",
        "com.htc.sense.mms",
        "com.motorola.messaging",
        "com.sonyericsson.conversations",
        "com.miui.mms",
        "com.huawei.message",
        "com.oppo.mms",
        "com.vivo.mms",
        "net.oneplus.mms",
        "com.meizu.mms",
        "com.oppo.mms"
    )

    fun onTimeOut(nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    override fun sendNotification(notification: WmNotification): Single<Boolean> {
        return Single.create { emitter ->
            sendNotificationEmitter = emitter

            // 短信应用包名统一，其它应用包名统一
            if (smsPackageList.indexOf(notification.appPackage) != -1) {
                notification.appPackage = sms_package_name
            } else if (appPackageList.indexOf(notification.appPackage) == -1) {
                notification.appPackage = others_package_name
            }

            sjUniWatch.wmLog.logD(TAG, "sendNotification: $notification")

            sjUniWatch.sendNormalMsg(CmdHelper.getNotificationCmd(notification))
        }
    }
}