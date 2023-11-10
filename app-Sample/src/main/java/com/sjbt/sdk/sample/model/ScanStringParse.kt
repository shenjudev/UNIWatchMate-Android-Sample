package com.sjbt.sdk.sample.model

import com.base.sdk.entity.WmDeviceModel

/**
 * Created by qiyachao
 * on 2023_10_28
 */
data class ScanStringParse(
    val randomCode: String,
    val modelType: WmDeviceModel,
    val projectName: String,
    val schemeMacAddress: String
) {

}