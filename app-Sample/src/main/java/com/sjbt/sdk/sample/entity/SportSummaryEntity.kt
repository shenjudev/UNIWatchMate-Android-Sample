package com.sjbt.sdk.sample.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SportSummaryEntity(
    val timestamp: Long,//开始时间，运动时长在valueType中(The start time and exercise duration are in valueType)
    val sportId: Int,
    /**
     * 基本参数类型(Basic parameter type)
     */
    val valueType: List<SportValueTypeData>,
) : Parcelable
@Parcelize
data class SportValueTypeData(val id: Int, val value: Double): Parcelable
