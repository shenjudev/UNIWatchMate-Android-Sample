package com.sjbt.sdk.entity

import android.util.Log
import com.sjbt.sdk.TAG_SJ

class ActionSupport {
    var backFromDev = false
    var dialSupportState = 0
    var searchDeviceSupportState = 0
    var searchPhoneSupportState = 0
    var otaSupportState = 0
    var ebookSupportState = 0
    var sleepSupportState = 0
    var cameraSupportState = 0
    var sportSupportState = 0
    var rateSupportState = 0
    var bloodOxSupportState = 0
    var bloodPressSupportState = 0
    var bloodSugarSupportState = 0
    var notifyMsgSupportState = 0
    var alarmSupportState = 0
    var musicSupportState = 0
    var contactSupportState = 0
    var appViewSupportState = 0
    var setRingSupportState = 0
    var setNotifyTouchSupportState = 0
    var setWatchTouchSupportState = 0
    var setSystemTouchSupportState = 0
    var armSupportState = 0
    var weatherSupportState = 0
    var supportSlowModel = 0
    var supportCameraPreview = 0
    var supportVideoTransfer = 0

    companion object {
        fun toActionSupportBean(actionStateArray: ByteArray): ActionSupport {
            val actionSupport = ActionSupport()
            actionSupport.backFromDev = true
            var actionPosition = 0
            val v = '0'
            for (b in actionStateArray) {
                var binaryString =
                    String.format("%8s", Integer.toBinaryString(b.toInt() and 0xFF)).replace(' ', v)
                val sb = StringBuilder(binaryString)
                binaryString = sb.reverse().toString()
                Log.d(TAG_SJ, "字节的二进制表示：$binaryString")

                // 逐位判断二进制表示的每一位是0还是1
                for (i in 0..7) {
                    val bit = binaryString[i]
                    when (actionPosition) {
                        0 -> {
                            actionSupport.weatherSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "天气为 $bit")
                        }
                        1 -> {
                            actionSupport.sportSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "健身为 $bit")
                        }
                        2 -> {
                            actionSupport.rateSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "心率为 $bit")
                        }
                        3 -> {
                            actionSupport.cameraSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "相机为 $bit")
                        }
                        4 -> {
                            actionSupport.notifyMsgSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "通知为 $bit")
                        }
                        5 -> {
                            actionSupport.alarmSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "闹钟为 $bit")
                        }
                        6 -> {
                            actionSupport.musicSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "音乐为 $bit")
                        }
                        7 -> {
                            actionSupport.contactSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "联系人为 $bit")
                        }
                        8 -> {
                            actionSupport.searchDeviceSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "查找手表为 $bit")
                        }
                        9 -> {
                            actionSupport.searchPhoneSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "查找手机为 $bit")
                        }
                        10 -> {
                            actionSupport.appViewSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "【设置】应用视图为 $bit")
                        }
                        11 -> {
                            actionSupport.setRingSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "【设置】来电响铃为$bit")
                        }
                        12 -> {
                            actionSupport.setNotifyTouchSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "【设置】通知触感为$bit")
                        }
                        13 -> {
                            actionSupport.setWatchTouchSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "【设置】表冠触感为$bit")
                        }
                        14 -> {
                            actionSupport.setSystemTouchSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "【设置】系统触感反馈为 $bit")
                        }
                        15 -> {
                            actionSupport.armSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "【设置】抬腕亮屏为 $bit")
                        }
                        16 -> {
                            actionSupport.bloodOxSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "血氧为$bit")
                        }
                        17 -> {
                            actionSupport.bloodPressSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "血压为$bit")
                        }
                        18 -> {
                            actionSupport.bloodSugarSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "血糖为$bit")
                        }
                        19 -> {
                            actionSupport.sleepSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "睡眠为$bit")
                        }
                        20 -> {
                            actionSupport.ebookSupportState = bit.code - v.code
                            Log.d(TAG_SJ, "电子书为$bit")
                        }
                        21 -> {
                            actionSupport.supportSlowModel = bit.code - v.code
                            Log.d(TAG_SJ, "支持慢速模式为$bit")
                        }
                        22 -> {
                            actionSupport.supportCameraPreview = bit.code - v.code
                            Log.d(TAG_SJ, "支持相机预览模式为$bit")
                        }
                        23 -> {
                            actionSupport.supportVideoTransfer = bit.code - v.code
                            Log.d(TAG_SJ, "支持视频上传为$bit")
                        }
                    }
                    actionPosition++
                }
            }
            return actionSupport
        }
    }

    override fun toString(): String {
        return "ActionSupport{" +
                "backFromDev=" + backFromDev +
                ", dialSupportState=" + dialSupportState +
                ", searchDeviceSupportState=" + searchDeviceSupportState +
                ", searchPhoneSupportState=" + searchPhoneSupportState +
                ", otaSupportState=" + otaSupportState +
                ", ebookSupportState=" + ebookSupportState +
                ", sleepSupportState=" + sleepSupportState +
                ", cameraSupportState=" + cameraSupportState +
                ", sportSupportState=" + sportSupportState +
                ", rateSupportState=" + rateSupportState +
                ", bloodOxSupportState=" + bloodOxSupportState +
                ", bloodPressSupportState=" + bloodPressSupportState +
                ", bloodSugarSupportState=" + bloodSugarSupportState +
                ", notifyMsgSupportState=" + notifyMsgSupportState +
                ", alarmSupportState=" + alarmSupportState +
                ", musicSupportState=" + musicSupportState +
                ", contactSupportState=" + contactSupportState +
                ", appViewSupportState=" + appViewSupportState +
                ", setRingSupportState=" + setRingSupportState +
                ", setNotifyTouchSupportState=" + setNotifyTouchSupportState +
                ", setWatchTouchSupportState=" + setWatchTouchSupportState +
                ", setSystemTouchSupportState=" + setSystemTouchSupportState +
                ", armSupportState=" + armSupportState +
                ", weatherSupportState=" + weatherSupportState +
                ", supportSlowModel=" + supportSlowModel +
                ", videoSupportState=" + supportVideoTransfer +
                '}'
    }
}