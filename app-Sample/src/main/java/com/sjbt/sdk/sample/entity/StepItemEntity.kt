package com.sjbt.sdk.sample.entity

import androidx.room.Entity
import androidx.room.TypeConverters
import com.sjbt.sdk.sample.utils.room.TimeConverter
import java.util.*

@Entity(primaryKeys = ["userId", "time"])
class StepItemEntity(
    val userId: Long,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val step: Int,
    val distance: Float,
    val calories: Float,
)