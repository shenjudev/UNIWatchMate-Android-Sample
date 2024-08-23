package com.sjbt.sdk.sample.base

import java.util.UUID

object BTConfig {
    val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    const val BT_REQUEST_CODE = 110
    const val FT_REQUEST_CODE = BT_REQUEST_CODE + 1
    const val BT_REQUEST_CODE_SETTING = FT_REQUEST_CODE + 1
    const val CONNECT_STATE_DISCONNECTED = 0
    const val CONNECT_STATE_CONNECTED = 2
    const val CONNECT_STATE_CONNECT_FAIL = 3
    const val CONNECT_FAIL_NO_RESPOND = 1
    const val CONNECT_FAIL_BAD_ADDRESS = CONNECT_FAIL_NO_RESPOND + 1
    const val CONNECT_FAIL_BT_DISABLE = CONNECT_FAIL_BAD_ADDRESS + 1
    const val CONNECT_FAIL_VERIFY_TIMEOUT = CONNECT_FAIL_BT_DISABLE + 1
    const val CONNECT_FAIL_OTHER = CONNECT_FAIL_VERIFY_TIMEOUT + 1
    const val CONNECT_RETRY_COUNT = 2
    const val FILE_TRANSFER_JPG: Byte = 4
    const val FILE_TRANSFER_BIN: Byte = 3
    const val FILE_TRANSFER_UP: Byte = 2
    const val FILE_TRANSFER_UPEX: Byte = 5
    const val FAIL_TYPE_BT_DISCONNECT = 400
    const val FAIL_TYPE_FILE_NOT_EXIST = FAIL_TYPE_BT_DISCONNECT + 1
    const val FAIL_TYPE_FILE_EMPTY = FAIL_TYPE_FILE_NOT_EXIST + 1
    const val FAIL_TYPE_FILE_EXCEPTION = FAIL_TYPE_FILE_EMPTY + 1
    const val FAIL_TYPE_USER_CANCEL = FAIL_TYPE_FILE_EXCEPTION + 1
    const val FAIL_TYPE_DEVICE_CANCEL = FAIL_TYPE_USER_CANCEL + 1
    const val FAIL_TYPE_TIMEOUT = FAIL_TYPE_DEVICE_CANCEL + 1
    const val FAIL_TYPE_ERROR = FAIL_TYPE_TIMEOUT + 1
    const val MAX_RETRY_COUNT = 5
    const val MSG_INTERVAL = 15
    const val MSG_INTERVAL_FRAME = 15
    const val MSG_INTERVAL_SLOW = 40
    const val OTA_STEP_MAIN_PROCESS = 200
    const val OTA_STEP_VICE_PROCESS = 201
    const val UP = "up"
    const val UP_EX = "upex"
    const val JPG = "jpg"
    const val DIAL = "dial"
}