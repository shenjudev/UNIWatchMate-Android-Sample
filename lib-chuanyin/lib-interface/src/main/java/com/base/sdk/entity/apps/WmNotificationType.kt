package com.base.sdk.entity.apps

/**
 * 通知类型包名定义
 */
enum class WmNotificationType(
    val type:Int = 0,
    val packageName:String=""
) {
    TELEPHONY_INCOMING(0, "com.android.incallui"),
    TELEPHONY_ANSWERED(1, "com.android.incallui"),
    TELEPHONY_REJECTED(2, "com.android.incallui"),
    SMS(3, "com.android.mms"),
    QQ(4, "com.tencent.mobileqq"),
    WECHAT(5, "com.tencent.mm"),
    FACEBOOK(6, "com.facebook.katana"),
    TWITTER(7, "com.twitter.android"),
    LINKEDIN(8, "com.linkedin.android"),
    INSTAGRAM(9, "com.instagram.android"),
    PINTEREST(10, "com.pinterest"),
    WHATSAPP(11, "com.whatsapp"),
    LINE(12, "jp.naver.line.android"),
    FACEBOOK_MESSENGER(13, "com.facebook.orca"),
    KAKAO(14, "com.kakao.talk"),
    SKYPE(15, "com.skype.raider"),
    EMAIL(16, "com.google.android.gm"),
    TELEGRAM(17, "org.telegram.messenger"),
    VIBER(18, "com.viber.voip"),
    CALENDAR(19, "com.android.calendar"),
    SNAPCHAT(20, "com.snapchat.android"),
    TELEPHONY_MISSED(21, "com.android.incallui"),
    HIKE(22, "com.bsb.hike"),
    YOUTUBE(23, "com.google.android.youtube"),
    APPLE_MUSIC(24, "com.apple.android.music"),
    ZOOM(25, "us.zoom.videomeetings"),
    TIKTOK(26, "com.zhiliaoapp.musically"),

    OTHERS_APP(100, ""),

}