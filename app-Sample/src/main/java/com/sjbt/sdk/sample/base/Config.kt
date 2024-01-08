package com.sjbt.sdk.sample.base

import android.os.Environment
import com.sjbt.sdk.sample.MyApplication.Companion.instance

object Config {
    val BASE_PATH = instance.externalCacheDir!!.absolutePath
    val APP_PATH = BASE_PATH + "/biu/"
    val APP_VIDEO_PATH = APP_PATH + "crop_video/"
    val APP_CROP_PIC_PATH = APP_VIDEO_PATH + "crop_pic/"
    val APP_DOWNLOAD =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/biu2us/"
    val APP_DOWNLOAD_PATH = APP_PATH + "/up/"
    val APP_DEBUG_EXPORT_FILE = APP_PATH + "/debug/"
    val APP_DIAL_PATH = APP_PATH + "/dial/"
    val APP_DIAL_THUMP_PATH = APP_PATH + "/dial/thump/"
    const val BT_REQUEST_CODE = 110
    const val FT_REQUEST_CODE = BT_REQUEST_CODE + 1
    const val BT_REQUEST_CODE_SETTING = FT_REQUEST_CODE + 1
    const val PERMISSION_REQUEST_CODE = 1101
    const val PERMISSION_INSTALL_APK = PERMISSION_REQUEST_CODE + 1
    const val CLICK_CANCEL = 0
    const val CLICK_OK = 1
    const val CLICK_FINISH = 2
    const val CLICK_RETRY = 3


    const val SCAN_START = 4
    const val SCAN_STOP = SCAN_START + 1
    const val SCAN_FAIL = SCAN_STOP + 1

    //    public static final String WEB_BASE_URL = "https://metawatchapp.com";
    //    public static final String WEB_BASE_URL = "https://account-dev.aimetawatch.com";
    const val WEB_BASE_URL = "https://aimetawatch.com"

    const val FILE_TYPE_MP3 = ""
    const val FILE_TYPE_TXT = "txt"
    const val FILE_TYPE_VIDEO = "video"
    enum class SportTypeName(val id: Int) {
        SPORT_RUN(0),
        SPORT_WALKING(1),
        SPORT_RIDING(2),
        SPORT_FITNESS(4),
        SPORT_OUTDOOR(5),
        SPORT_BALL(6),
        SPORT_YOGA(7),
        SPORT_ICE(8),
        SPORT_DANCE(9),
        SPORT_LEISURE(11),
        SPORT_OTHERS(12);

    }
}