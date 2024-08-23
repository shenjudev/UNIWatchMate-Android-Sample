package com.sjbt.sdk.sample.entity

import androidx.room.Entity
import androidx.room.TypeConverters
import com.sjbt.sdk.sample.utils.room.TimeConverter
import java.util.*

@Entity(primaryKeys = ["userId", "time"])
class HeartRateItemEntity(
    val userId: Long,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val heartRate: Int,
)

@Entity(primaryKeys = ["userId", "time"])
class OxygenItemEntity(

    val userId: Long,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val oxygen: Int,
)
