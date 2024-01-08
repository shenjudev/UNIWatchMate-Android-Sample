package com.base.sdk.exception

/**
 * 表示设备消息超时引起的异常
 */
class WmTimeOutException(val msg: String) : WmException(msg)