package com.base.sdk.entity

/**
 * 连接返回设备信息
 */
open class WmDevice(var mode: WmDeviceModel) {
    var name: String? = null
    var address: String? = null
    var isRecognized = false
    var randomCode: String? = null
    override fun toString(): String {
        return "WmDevice(mode=$mode, address='$address', name=$name, isRecognized=$isRecognized)"
    }

}