package com.sjbt.sdk.sample.model.device

import com.base.sdk.entity.WmDeviceModel
import com.sjbt.sdk.sample.entity.DeviceBindEntity


/**
 * ToNote:Avoid declare as a data class, because sometimes  need trigger connection, even when the device is not changed
 */
class ConnectorDevice(
    /**
     * Device mac address
     */
    val address: String,

    /**
     * Device name
     */
    val name: String,
    val wmDeviceMode: WmDeviceModel,

    /**
     * 支持的设备类型
     */
    val deviceType: String,

    /**
     * Is trying to bind
     */
    val isTryingBind: Boolean,
    val connectState: Int

) {
    override fun toString(): String {
        return "ConnectorDevice(address='$address', name='$name', wmDeviceMode=$wmDeviceMode, isTryingBind=$isTryingBind, connectState=$connectState)"
    }
}
fun ConnectorDevice.deviceModeToInt(): Int {
    if (wmDeviceMode == WmDeviceModel.SJ_WATCH) {
        return 0
    }else{
        return 1
    }
}