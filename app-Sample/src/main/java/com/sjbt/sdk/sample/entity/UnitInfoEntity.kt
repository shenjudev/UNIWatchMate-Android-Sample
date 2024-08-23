package com.sjbt.sdk.sample.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UnitInfoEntity(
    @PrimaryKey
    val userId: Long,

    val length: Boolean,

    val temperature: Boolean,

    val time: Boolean,

)

//fun UnitInfo?.toModel(): WmSportGoal {
//    return if (this == null) {
//        WmSportGoal(0, 0, 0, 0)
//    } else {
//        WmSportGoal(
//            step,
//            distance,
//            calorie,
//            activityMinutes
//        )
//    }
//}