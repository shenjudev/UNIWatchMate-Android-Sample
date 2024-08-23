package com.sjbt.sdk.sample.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.base.sdk.entity.settings.WmSportGoal

@Entity
data class SportGoalEntity(
    /**
     * userId
     */
    @PrimaryKey
    val userId: Long,

    /**
     * steps
     */
    val step: Int,

    /**
     * distance
     */
    val distance: Int,

    /**
     * calorie
     */
    val calorie: Int,

    /**
     * activity minutes
     */
    val activityMinutes: Short,
)

fun SportGoalEntity?.toModel(): WmSportGoal {
    return if (this == null) {
        WmSportGoal(0, 0, 0, 0)
    } else {
        WmSportGoal(
            step,
            calorie,
            distance,
            activityMinutes
        )
    }
}