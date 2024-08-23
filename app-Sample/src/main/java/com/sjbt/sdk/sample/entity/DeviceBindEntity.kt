package com.sjbt.sdk.sample.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.base.sdk.entity.WmDeviceModel
import com.sjbt.sdk.sample.model.device.ConnectorDevice

@Entity
data class DeviceBindEntity(
        /**
         * 用户(User Id)
         */
        @PrimaryKey
        val userId: Long,
        /**
         * 设备地址(device address)
         */
        val address: String,
        /**
         * 设备名称(device name)
         */
        val name: String,

        /**
         * 支持的设备类型
         */
        val deviceType: String,

        /**
         * 设备厂家(equipment manufacturer)
         */
        val deviceMode: Int,
        /**
         * 设备连接状态(Device connection status)
         */
        val deviceConnectState: Int,
)

fun DeviceBindEntity.intToDeviceMode(): WmDeviceModel {
    if (deviceMode == 0) {
        return WmDeviceModel.SJ_WATCH
    } else {
        return WmDeviceModel.FC_WATCH
    }
}

internal fun DeviceBindEntity?.toModel(): ConnectorDevice? {
    return if (this == null) {
        null
    } else {
        ConnectorDevice(
                address = address,
                name = name,
                wmDeviceMode = intToDeviceMode(),
                deviceType = deviceType,
                isTryingBind = false,
                connectState = deviceConnectState
        )
    }
}