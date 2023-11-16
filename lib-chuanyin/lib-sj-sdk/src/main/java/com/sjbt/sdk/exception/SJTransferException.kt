package com.sjbt.sdk.exception

import com.base.sdk.exception.WmException

class SJTransferException(val code: Int, val msg: String) : WmException() {

}

