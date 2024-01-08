package com.sjbt.sdk.entity

import android.util.Log
import com.base.sdk.entity.settings.WmFunctionSupport
import com.sjbt.sdk.TAG_SJ
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SJFunctionSupport : WmFunctionSupport() {

    companion object {
        val FUNCTION_0 = 0
        val FUNCTION_1 = 1
        val FUNCTION_2 = 2
        val FUNCTION_3 = 3
        val FUNCTION_4 = 4
        val FUNCTION_5 = 5
        val FUNCTION_6 = 6
        val FUNCTION_7 = 7
        val FUNCTION_8 = 8
        val FUNCTION_9 = 9
        val FUNCTION_10 = 10
        val FUNCTION_11 = 11
        val FUNCTION_12 = 12
        val FUNCTION_13 = 13
        val FUNCTION_14 = 14
        val FUNCTION_15 = 15
        val FUNCTION_16 = 16
        val FUNCTION_17 = 17
        val FUNCTION_18 = 18
        val FUNCTION_19 = 19
        val FUNCTION_20 = 20
        val FUNCTION_21 = 21
        val FUNCTION_22 = 22
        val FUNCTION_23 = 23
        val FUNCTION_24 = 24
        val FUNCTION_25 = 25
        val FUNCTION_26 = 26
        val FUNCTION_27 = 27
        val FUNCTION_28 = 28
        val FUNCTION_29 = 29
        val FUNCTION_30 = 30
        val FUNCTION_31 = 31
        val FUNCTION_32 = 32
        val FUNCTION_33 = 33
        val FUNCTION_34 = 34
        val FUNCTION_35 = 35
        val FUNCTION_36 = 36
        val FUNCTION_37 = 37
        val FUNCTION_38 = 38
        val FUNCTION_39 = 39
        val FUNCTION_40 = 40
        val FUNCTION_41 = 41
        val FUNCTION_42 = 42
        val FUNCTION_43 = 43
        val FUNCTION_44 = 44
        val FUNCTION_45 = 45
        val FUNCTION_46 = 46
        val FUNCTION_47 = 47
        val FUNCTION_48 = 48
        val FUNCTION_49 = 49
        val FUNCTION_50 = 50
        val FUNCTION_51 = 51
        val FUNCTION_52 = 52
        val FUNCTION_53 = 53
        val FUNCTION_54 = 54
        val FUNCTION_55 = 55
        val FUNCTION_56 = 56
        val FUNCTION_57 = 57
        val FUNCTION_58 = 58
        val FUNCTION_59 = 59
        val FUNCTION_60 = 60

        fun toActionSupportBean(actionStateArray: ByteArray): WmFunctionSupport {
            val wmFunctionSupport = WmFunctionSupport()
            var actionPosition = 0
            val v = '0'

            val byteBuffer = ByteBuffer.wrap(actionStateArray).order(ByteOrder.LITTLE_ENDIAN)
            wmFunctionSupport.supportFunctionVersion = byteBuffer.get().toInt()

            var actListArray = ByteArray(16)
            byteBuffer.get(actListArray)

            byteBuffer.position(17)

            wmFunctionSupport.maxContacts = byteBuffer.short.toUShort().toInt()
            wmFunctionSupport.sideButtonCount = byteBuffer.get().toInt() and 0XFF
            wmFunctionSupport.fixedSportCount = byteBuffer.get().toInt() and 0XFF
            wmFunctionSupport.variableSportCount = byteBuffer.get().toInt() and 0XFF

            for (byteValue in actListArray) {

                var binaryString =
                    String.format("%8s", Integer.toBinaryString(byteValue.toInt() and 0xFF))
                        .replace(' ', v)
                val sb = StringBuilder(binaryString)
                binaryString = sb.reverse().toString()

                // 逐位判断二进制表示的每一位是0还是1
                for (i in 0..7) {
                    val bit = binaryString[i]
                    when (actionPosition) {
                        FUNCTION_0 -> {
                            wmFunctionSupport.supportWeatherState = bit.code - v.code
                            //Log.d(TAG_SJ, "weatherSupportState $bit")
                        }
                        FUNCTION_1 -> {
                            wmFunctionSupport.sportSupportState = bit.code - v.code
                            //Log.d(TAG_SJ, "sportSupportState $bit")
                        }
                        FUNCTION_2 -> {
                            wmFunctionSupport.supportRateState = bit.code - v.code
                            //Log.d(TAG_SJ, "rateSupportState $bit")
                        }
                        FUNCTION_3 -> {
                            wmFunctionSupport.supportCameraControlState = bit.code - v.code
                            //Log.d(TAG_SJ, "cameraSupportState $bit")
                        }
                        FUNCTION_4 -> {
                            wmFunctionSupport.supportNotifyMsgState = bit.code - v.code
                            //Log.d(TAG_SJ, "notifyMsgSupportState $bit")
                        }
                        FUNCTION_5 -> {
                            wmFunctionSupport.supportAlarmState = bit.code - v.code
                            //Log.d(TAG_SJ, "alarmSupportState $bit")
                        }
                        FUNCTION_6 -> {
                            wmFunctionSupport.supportTransferMusicState = bit.code - v.code
                            //Log.d(TAG_SJ, "musicSupportState $bit")
                        }
                        FUNCTION_7 -> {
                            wmFunctionSupport.supportContactState = bit.code - v.code
                            //Log.d(TAG_SJ, "contactSupportState $bit")
                        }
                        FUNCTION_8 -> {
                            wmFunctionSupport.supportFindDeviceState = bit.code - v.code
                            //Log.d(TAG_SJ, "searchDeviceSupportState $bit")
                        }
                        FUNCTION_9 -> {
                            wmFunctionSupport.supportFindPhoneState = bit.code - v.code
                            //Log.d(TAG_SJ, "searchPhoneSupportState $bit")
                        }
                        FUNCTION_10 -> {
                            wmFunctionSupport.supportAppViewState = bit.code - v.code
                            //Log.d(TAG_SJ, "appViewSupportState $bit")
                        }
                        FUNCTION_11 -> {
                            wmFunctionSupport.supportSetRingState = bit.code - v.code
                            //Log.d(TAG_SJ, "setRingSupportState $bit")
                        }
                        FUNCTION_12 -> {
                            wmFunctionSupport.supportSetNotifyTouchState = bit.code - v.code
                            //Log.d(TAG_SJ, "setNotifyTouchSupportState $bit")
                        }
                        FUNCTION_13 -> {
                            wmFunctionSupport.supportSetCrownTouchState = bit.code - v.code
                            //Log.d(TAG_SJ, "setWatchTouchSupportState $bit")
                        }
                        FUNCTION_14 -> {
                            wmFunctionSupport.supportSetSystemTouchState = bit.code - v.code
                            //Log.d(TAG_SJ, "setSystemTouchSupportState $bit")
                        }
                        FUNCTION_15 -> {
                            wmFunctionSupport.supportWristScreenState = bit.code - v.code
                            //Log.d(TAG_SJ, "armSupportState $bit")
                        }
                        FUNCTION_16 -> {
                            wmFunctionSupport.supportBloodOxygenState = bit.code - v.code
                            //Log.d(TAG_SJ, "bloodOxSupportState $bit")
                        }
                        FUNCTION_17 -> {
                            wmFunctionSupport.supportBloodPressState = bit.code - v.code
                            //Log.d(TAG_SJ, "bloodPressSupportState $bit")
                        }
                        FUNCTION_18 -> {
                            wmFunctionSupport.supportBloodSugarState = bit.code - v.code
                            //Log.d(TAG_SJ, "bloodSugarSupportState $bit")
                        }
                        FUNCTION_19 -> {
                            wmFunctionSupport.supportSleepState = bit.code - v.code
                            //Log.d(TAG_SJ, "sleepSupportState $bit")
                        }
                        FUNCTION_20 -> {
                            wmFunctionSupport.supportTransferEbookState = bit.code - v.code
                            //Log.d(TAG_SJ, "ebookSupportState $bit")
                        }
                        FUNCTION_21 -> {
                            wmFunctionSupport.supportSlowModel = bit.code - v.code
                            //Log.d(TAG_SJ, "supportSlowModel $bit")
                        }
                        FUNCTION_22 -> {
                            wmFunctionSupport.supportCameraPreview = bit.code - v.code
                            //Log.d(TAG_SJ, "supportCameraPreview $bit")
                        }
                        FUNCTION_23 -> {
                            wmFunctionSupport.supportVideoTransfer = bit.code - v.code
                            //Log.d(TAG_SJ, "supportVideoTransfer $bit")
                        }

                        FUNCTION_24 -> {
                            wmFunctionSupport.supportPayeeCode = bit.code - v.code
                            //Log.d(TAG_SJ, "supportPayeeCode $bit")
                        }
                        FUNCTION_25 -> {
                            wmFunctionSupport.supportDialMarket = bit.code - v.code
                            //Log.d(TAG_SJ, "supportDialMarket $bit")
                        }
                        FUNCTION_26 -> {
                            wmFunctionSupport.supportUnfoldNotification = bit.code - v.code
                            //Log.d(TAG_SJ, "supportExpandNotification $bit")
                        }
                        FUNCTION_27 -> {
                            wmFunctionSupport.supportBleDell = bit.code - v.code
                            //Log.d(TAG_SJ, "supportBluetoothDell $bit")
                        }
                        FUNCTION_28 -> {
                            wmFunctionSupport.supportShowBleDellSwitch = bit.code - v.code
                            //Log.d(TAG_SJ, "supportShowBluetoothDellSwitch $bit")
                        }
                        FUNCTION_29 -> {
                            wmFunctionSupport.supportEmergencyContact = bit.code - v.code
                            //Log.d(TAG_SJ, "supportEmergencyContact $bit")
                        }
                        FUNCTION_30 -> {
                            wmFunctionSupport.supportSyncCollectContact = bit.code - v.code
                            //Log.d(TAG_SJ, "supportSyncCollectContact $bit")
                        }
                        FUNCTION_31 -> {
                            wmFunctionSupport.supportQuickRespond = bit.code - v.code
                            //Log.d(TAG_SJ, "supportQuickRespond $bit")
                        }
                        FUNCTION_32 -> {
                            wmFunctionSupport.supportStepGoal = bit.code - v.code
                            //Log.d(TAG_SJ, "supportStepGoal $bit")
                        }
                        FUNCTION_33 -> {
                            wmFunctionSupport.supportCalorieGoal = bit.code - v.code
                            //Log.d(TAG_SJ, "supportCalorieGoal $bit")
                        }
                        FUNCTION_34 -> {
                            wmFunctionSupport.supportActDurationGoal = bit.code - v.code
                            //Log.d(TAG_SJ, "supportActDurationGoal $bit")
                        }
                        FUNCTION_35 -> {
                            wmFunctionSupport.supportSedentaryReminder = bit.code - v.code
                            //Log.d(TAG_SJ, "supportSedentaryReminder $bit")
                        }
                        FUNCTION_36 -> {
                            wmFunctionSupport.supportDrinkWaterReminder = bit.code - v.code
                            //Log.d(TAG_SJ, "supportDrinkWaterReminder $bit")
                        }
                        FUNCTION_37 -> {
                            wmFunctionSupport.supportWashHandsReminder = bit.code - v.code
                            //Log.d(TAG_SJ, "supportWashHandsReminder $bit")
                        }
                        FUNCTION_38 -> {
                            wmFunctionSupport.supportAutoRate = bit.code - v.code
                            //Log.d(TAG_SJ, "supportAutoRate $bit")
                        }
                        FUNCTION_39 -> {
                            wmFunctionSupport.supportREM = bit.code - v.code
                            //Log.d(TAG_SJ, "supportREM $bit")
                        }
                        FUNCTION_40 -> {
                            wmFunctionSupport.supportMultiSport = bit.code - v.code
                            //Log.d(TAG_SJ, "supportMultiSport $bit")
                        }
                        FUNCTION_41 -> {
                            wmFunctionSupport.supportShowFixMotionType = bit.code - v.code
                            //Log.d(TAG_SJ, "supportShowFixMotionType $bit")
                        }
                        FUNCTION_42 -> {
                            wmFunctionSupport.supportSportAutoRecogniseStart = bit.code - v.code
                            //Log.d(TAG_SJ, "supportSportAutoRecogniseStart $bit")
                        }
                        FUNCTION_43 -> {
                            wmFunctionSupport.supportSportAutoRecogniseEnd = bit.code - v.code
                            //Log.d(TAG_SJ, "supportSportAutoRecogniseEnd $bit")
                        }
                        FUNCTION_44 -> {
                            wmFunctionSupport.supportAlarmLabel = bit.code - v.code
                            //Log.d(TAG_SJ, "supportAlarmLabel $bit")
                        }
                        FUNCTION_45 -> {
                            wmFunctionSupport.supportAlarmRemark = bit.code - v.code
                            //Log.d(TAG_SJ, "supportAlarmRemark $bit")
                        }
                        FUNCTION_46 -> {
                            wmFunctionSupport.supportWorldClock = bit.code - v.code
                            //Log.d(TAG_SJ, "supportWorldClock $bit")
                        }
                        FUNCTION_47 -> {
                            wmFunctionSupport.supportAppChangeLanguage = bit.code - v.code
                            //Log.d(TAG_SJ, "supportAppChangeLanguage $bit")
                        }
                        FUNCTION_48 -> {
                            wmFunctionSupport.supportWidgets = bit.code - v.code
                            //Log.d(TAG_SJ, "supportWidgets $bit")
                        }
                        FUNCTION_49 -> {
                            wmFunctionSupport.supportAppControlVolume = bit.code - v.code
                            //Log.d(TAG_SJ, "supportAppControlVolume $bit")
                        }
                        FUNCTION_50 -> {
                            wmFunctionSupport.supportQuietHeartRateAlert = bit.code - v.code
                            //Log.d(TAG_SJ, "supportQuietHeartRateAlert $bit")
                        }
                        FUNCTION_51 -> {
                            wmFunctionSupport.supportSportHeartRateAlert = bit.code - v.code
                            //Log.d(TAG_SJ, "supportSportHeartRateAlert $bit")
                        }
                        FUNCTION_52 -> {
                            wmFunctionSupport.supportDailyHeartRateAlert = bit.code - v.code
                            //Log.d(TAG_SJ, "supportDailyHeartRateAlert $bit")
                        }
                        FUNCTION_53 -> {
                            wmFunctionSupport.supportContinuousOxygen = bit.code - v.code
                            //Log.d(TAG_SJ, "supportContinuousOxygen $bit")
                        }
                        FUNCTION_54 -> {
                            wmFunctionSupport.supportBTDisconnectReminder = bit.code - v.code
                            //Log.d(TAG_SJ, "supportBTDisconnectReminder $bit")
                        }
                        FUNCTION_55 -> {
                            wmFunctionSupport.supportBtBleSameName = bit.code - v.code
                            //Log.d(TAG_SJ, "supportBtBleSameName $bit")
                        }
                        FUNCTION_56 -> {
                            wmFunctionSupport.supportEventReminder = bit.code - v.code
                            //Log.d(TAG_SJ, "supportEventReminder $bit")
                        }
                        FUNCTION_57 -> {
                            wmFunctionSupport.supportScreenReminder = bit.code - v.code
                            //Log.d(TAG_SJ, "supportScreenReminder $bit")
                        }
                        FUNCTION_58 -> {
                            wmFunctionSupport.supportRebootDevice = bit.code - v.code
                            //Log.d(TAG_SJ, "supportRebootDevice $bit")
                        }
                        FUNCTION_59 -> {
//                            wmFunctionSupport.supportDialMarket = bit.code - v.code
//                            //Log.d(TAG_SJ, "supportDialMarket $bit")
                        }
                        FUNCTION_60 -> {
//                            wmFunctionSupport.supportDialMarket = bit.code - v.code
//                            //Log.d(TAG_SJ, "supportDialMarket $bit")
                        }
                    }
                    actionPosition++
                }
            }

            return wmFunctionSupport
        }
    }
}