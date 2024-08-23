package com.sjbt.sdk.sample.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TodayStepData(
    val timestamp: Long,
    val step: Int,
    val distance: Float,//km
    val calories: Float//kilocalorie
)