package com.base.sdk.exception

/**
 * 表示设备连接的异常，一般在SDK不能识别此设备的时候出现
 */
class WmDisconnectedException(
    address: String? = null
) : WmException()