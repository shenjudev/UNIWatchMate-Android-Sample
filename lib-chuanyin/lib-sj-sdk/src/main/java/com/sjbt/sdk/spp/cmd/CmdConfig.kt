package com.sjbt.sdk.spp.cmd

const val RANDOM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
const val HEX_FFFF = 0xFFFF

/**
 * 以下是BiuApp 协议
 * Type 4-bytes | Length 4-bytes | Offset 4-bytes | CRC 4-bytes | Payload 4-bytes
 * 蓝牙命令HEAD
 */
const val HEAD_VERIFY = 0X0A.toByte()
const val HEAD_COMMON = 0X0B.toByte()
const val HEAD_SPORT_HEALTH = 0X0C.toByte()
const val HEAD_FILE_OPP = 0X0D.toByte() //OPP传输方式
const val HEAD_FILE_SPP_A_2_D = 0x0E.toByte() //App到设备端发送文件
const val HEAD_FILE_SPP_D_2_A = 0xFF.toByte() //从设备端到App端传文件
const val HEAD_DEVICE_ERROR = 0xEF.toByte() //从设备向App端报告错误
const val HEAD_COLLECT_DEBUG_DATA = 0xDF.toByte() //收集调试数据
const val HEAD_CAMERA_PREVIEW = 0x1A.toByte() //相机预览头
const val HEAD_NODE_TYPE = 0x30.toByte() //节点数据头
const val TRANSFER_KEY: Short = 0X7FFF

/**
 * COMMAND_ID 因为方向原因，发送的时候需要Command_Id 与运算 0x7FFF 携带方向 0X8001 & 0x7FFF = 0X0001
 */
const val CMD_ID_8001: Short = 0X01
const val CMD_ID_8002: Short = 0X02
const val CMD_ID_8003: Short = 0X03
const val CMD_ID_8004: Short = 0X04
const val CMD_ID_8005: Short = 0X05
const val CMD_ID_8006: Short = 0X06
const val CMD_ID_8007: Short = 0X07
const val CMD_ID_8008: Short = 0X08
const val CMD_ID_8009: Short = 0X09
const val CMD_ID_800A: Short = 0X0A
const val CMD_ID_800B: Short = 0X0B
const val CMD_ID_800C: Short = 0X0C
const val CMD_ID_800D: Short = 0X0D
const val CMD_ID_800E: Short = 0X0E
const val CMD_ID_800F: Short = 0X0F
const val CMD_ID_8010: Short = 0X10
const val CMD_ID_8011: Short = 0X11
const val CMD_ID_8012: Short = 0X12
const val CMD_ID_8013: Short = 0X13
const val CMD_ID_8014: Short = 0X14
const val CMD_ID_8015: Short = 0X15
const val CMD_ID_8017: Short = 0X17
const val CMD_ID_8018: Short = 0X18
const val CMD_ID_8019: Short = 0X19
const val CMD_ID_801A: Short = 0X1A
const val CMD_ID_801B: Short = 0X1B
const val CMD_ID_801C: Short = 0X1C
const val CMD_ID_801D: Short = 0X1D
const val CMD_ID_801E: Short = 0X1E
const val CMD_ID_8020: Short = 0X20
const val CMD_ID_8021: Short = 0X21
const val CMD_ID_8022: Short = 0X22
const val CMD_ID_8023: Short = 0X23
const val CMD_ID_8024: Short = 0X24
const val CMD_ID_8025: Short = 0X25
const val CMD_ID_8026: Short = 0X26
const val CMD_ID_8027: Short = 0x27
const val CMD_ID_8028: Short = 0x28
const val CMD_ID_8029: Short = 0x29
const val CMD_ID_802A: Short = 0x2a
const val CMD_ID_802B: Short = 0x2b
const val CMD_ID_802C: Short = 0x2C
const val CMD_ID_802D: Short = 0x2D
const val CMD_ID_802E: Short = 0x2E
const val CMD_ID_802F: Short = 0x2F
const val CMD_ID_8030: Short = 0x30

///**
// * 循环使用order_id
// */
//val CMD_ORDER_ARRAY = byteArrayOf(
//    0X01, 0X02, 0X03, 0X04, 0X05, 0X06, 0X07, 0X08, 0X09, 0X0A,
//    0X0B, 0X0C, 0X0D, 0X0E, 0X0F
//)

/**
 * 蓝牙命令组装通用方法
 *
 *
 * bit0~bit1:
 * [00] 不分片
 * [01] 分片，首包
 * [10] 分片，中间包
 * [11] 分片，尾包
 * bit3: [0]二进制数据；[1]json数据
 * bit4~bit7: 保留
 *
 *
 * 枚举所有命令对应十进制整数:
 * 不分片   二进制 对应二进制：00000000 = 0
 * 不分片    json 对应二进制：00000100 = 4
 *
 *
 * 分片首包 二进制 对应二进制：00000010 = 2
 * 分片中包 二进制 对应二进制：00000001 = 1
 * 分片尾包 二进制 对应二进制：00000011 = 3
 * 分片首包  json 对应二进制：00000110 = 6
 * 分片中包  json 对应二进制：00000101 = 5
 * 分片尾包  json 对应二进制：00000111 = 7
 *
 * @param offset     偏移量
 * @param crc
 * @param payload
 * @return
 */
const val DIVIDE_N_2: Byte = 0
const val DIVIDE_N_JSON: Byte = 4
const val DIVIDE_Y_F_2: Byte = 1
const val DIVIDE_Y_M_2: Byte = 2
const val DIVIDE_Y_E_2: Byte = 3
const val DIVIDE_Y_F_JSON: Byte = 5
const val DIVIDE_Y_M_JSON: Byte = 6
const val DIVIDE_Y_E_JSON: Byte = 7

const val DIAL_MSG_LEN = 17
const val BT_MSG_BASE_LEN = 16

/**
 * 节点类型数据配置
 **/
const val URN_0: Byte = '0'.code.toByte() //48
const val URN_1: Byte = '1'.code.toByte()
const val URN_2: Byte = '2'.code.toByte()
const val URN_3: Byte = '3'.code.toByte()
const val URN_4: Byte = '4'.code.toByte()
const val URN_5: Byte = '5'.code.toByte()
const val URN_6: Byte = '6'.code.toByte()
const val URN_7: Byte = '7'.code.toByte()
const val URN_8: Byte = '8'.code.toByte()
const val URN_9: Byte = '9'.code.toByte() //57
const val URN_A: Byte = 'A'.code.toByte() //65
const val URN_B: Byte = 'B'.code.toByte()
const val URN_C: Byte = 'C'.code.toByte()
const val URN_D: Byte = 'D'.code.toByte()
const val URN_E: Byte = 'E'.code.toByte()
const val URN_F: Byte = 'F'.code.toByte()
const val URN_G: Byte = 'G'.code.toByte()
const val URN_H: Byte = 'H'.code.toByte() //72

const val URN_CONNECT: Byte = URN_1
const val URN_SETTING: Byte = URN_2
const val URN_SETTING_SPORT: Byte = URN_1
const val URN_SETTING_PERSONAL: Byte = URN_2
const val URN_SETTING_UNIT: Byte = URN_3
const val URN_SETTING_LANGUAGE: Byte = URN_4
const val URN_SETTING_LANGUAGE_LIST: Byte = URN_1
const val URN_SETTING_LANGUAGE_SET: Byte = URN_2

const val URN_SETTING_SEDENTARY: Byte = URN_5
const val URN_SETTING_DRINK: Byte = URN_6
const val URN_SETTING_DATE_TIME: Byte = URN_7
const val URN_SETTING_SOUND: Byte = URN_8
const val URN_SETTING_ARM: Byte = URN_9
const val URN_SETTING_APP_VIEW: Byte = URN_A
const val URN_SETTING_DEVICE_INFO: Byte = URN_B

const val URN_APP_SETTING: Byte = URN_4
const val URN_APP_ALARM: Byte = URN_1
const val URN_APP_ALARM_LIST: Byte = URN_1

const val URN_APP_SPORT: Byte = URN_2
const val URN_APP_SPORT_LIST: Byte = URN_1

const val URN_APP_CONTACT: Byte = URN_3
const val URN_APP_CONTACT_COUNT: Byte = URN_1
const val URN_APP_CONTACT_LIST: Byte = URN_2
const val URN_APP_CONTACT_EMERGENCY: Byte = URN_3

const val URN_APP_WEATHER: Byte = URN_4
const val URN_APP_WEATHER_PUSH_TODAY: Byte = URN_1
const val URN_APP_WEATHER_PUSH_SEVEN_DAYS: Byte = URN_2

const val URN_APP_RATE: Byte = URN_5

const val URN_APP_CONTROL: Byte = URN_5

const val URN_APP_FIND_PHONE: Byte = URN_1
const val URN_APP_FIND_PHONE_START: Byte = URN_1
const val URN_APP_FIND_PHONE_STOP: Byte = URN_2

const val URN_APP_FIND_DEVICE: Byte = URN_2
const val URN_APP_FIND_DEVICE_START: Byte = URN_1
const val URN_APP_FIND_DEVICE_STOP: Byte = URN_2

const val URN_APP_MUSIC_CONTROL: Byte = URN_4

const val URN_SPORT_DATA: Byte = URN_6
const val URN_SPORT_STEP: Byte = URN_1
const val URN_SPORT_CALORIES: Byte = URN_2
const val URN_SPORT_ACTIVITY_LEN: Byte = URN_3
const val URN_SPORT_DAILY_ACTIVITY_LEN: Byte = URN_4
const val URN_SPORT_DISTANCE: Byte = URN_5
const val URN_SPORT_OXYGEN: Byte = URN_6
const val URN_SPORT_RATE: Byte = URN_7
const val URN_SPORT_RATE_RECORD: Byte = URN_2
const val URN_SPORT_RATE_REALTIME: Byte = URN_1
const val URN_SPORT_SLEEP: Byte = URN_8
const val URN_SPORT_SUMMARY: Byte = URN_9
const val URN_SPORT_10S_RATE: Byte = URN_A
const val URN_SPORT_10S_STEP_FREQUENCY: Byte = URN_B
const val URN_SPORT_10S_DISTANCE: Byte = URN_C
const val URN_SPORT_10S_CALORIES: Byte = URN_D

const val CHANGE_CAMERA = 0.toByte()
const val CHANGE_FLASH = 1.toByte()

//设备端支持的最大业务单元大小
const val MAX_BUSINESS_BUFFER_SIZE = 2048
const val SYNC_DATA_INTERVAL_HOUR = 60 * 60 * 1000
const val SYNC_DATA_INTERVAL_FIVE_MINUTES = 5 * 60 * 1000
const val SYNC_DATA_INTERVAL_TEN_SECONDS = 10 * 1000