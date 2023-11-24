package com.base.sdk.exception


class WmTransferException(val error: WmTransferError, val msg: String) : WmException() {

}

enum class WmTransferError(val code: Int) {
    ERROR_OTHER(0),
    ERROR_BUSY(1),
    ERROR_CRC(2),
    ERROR_LOW_MEMORY(3),
    ERROR_LOW_POWER(4),
    ERROR_TIME_OUT(5),
    ERROR_FILE_EXCEPTION(101),
    ERROR_DISCONNECT(102),
}

