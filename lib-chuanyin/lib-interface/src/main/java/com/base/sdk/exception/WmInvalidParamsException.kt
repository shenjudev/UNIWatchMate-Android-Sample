package com.base.sdk.exception

/**
 * 表示传入参数错误引起的异常
 */
class WmInvalidParamsException(val msg: String) : WmException(msg)