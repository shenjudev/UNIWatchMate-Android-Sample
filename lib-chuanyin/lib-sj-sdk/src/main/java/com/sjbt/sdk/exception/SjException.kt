package com.sjbt.sdk.exception

import com.base.sdk.exception.WmException

class SjException : WmException {
    constructor() : super()

    constructor(message: String?) : super(message)

    constructor(cause: Throwable?) : super(cause)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}