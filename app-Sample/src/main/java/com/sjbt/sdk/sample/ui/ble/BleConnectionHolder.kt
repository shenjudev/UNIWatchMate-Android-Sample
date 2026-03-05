package com.sjbt.sdk.sample.ui.ble

import com.polidea.rxandroidble3.RxBleConnection
import java.util.UUID

/**
 * 持有当前 BLE 连接，供 BleScanFragment 写入、OtaDemobleActivity 收发使用。
 * 连接成功后 set，进入 OTA 页后 get；退出 OTA 页不清理，由用户返回 BLE 页或进程结束自然释放。
 */
object BleConnectionHolder {

    @Volatile
    private var deviceAddress: String? = null

    @Volatile
    private var connection: RxBleConnection? = null

    fun set(address: String, conn: RxBleConnection) {
        deviceAddress = address
        connection = conn
    }

    /** 若当前保存的连接地址与给定一致则返回连接，否则返回 null */
    fun getConnection(address: String): RxBleConnection? {
        return if (deviceAddress == address) connection else null
    }

    fun clear() {
        deviceAddress = null
        connection = null
    }

    fun getCurrentAddress(): String? = deviceAddress

    // GATT UUID，与 BleScanFragment 一致，供 OtaDemobleActivity 使用
    val SERVICE_UUID = UUID.fromString("FFF00000-E8A5-BFE5-AE89-E7BB85E8819A")
    val CHAR_UUID_APP_TO_DEVICE = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
    val CHAR_UUID_DEVICE_TO_APP = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
//    val CHAR_UUID_ERROR2 = UUID.fromString("FFF3")
//    val CHAR_UUID_ERROR = UUID.fromString("0000FFF3-0000-1000-8000-00805F9B34FB")
}
