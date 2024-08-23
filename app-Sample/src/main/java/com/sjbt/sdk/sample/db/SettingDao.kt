package com.sjbt.sdk.sample.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sjbt.sdk.sample.entity.DeviceBindEntity
import com.sjbt.sdk.sample.entity.SportGoalEntity
import com.sjbt.sdk.sample.entity.UnitInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SettingDao {
    @Query("SELECT * FROM DeviceBindEntity WHERE userId=:userId")
    abstract fun flowDeviceBind(userId: Long): Flow<DeviceBindEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDeviceBind(vararg deviceBind: DeviceBindEntity)

    @Query("DELETE FROM DeviceBindEntity WHERE userId=:userId")
    abstract suspend fun clearDeviceBind(userId: Long)


    @Query("SELECT * FROM SportGoalEntity WHERE userId=:userId")
    abstract fun flowExerciseGoal(userId: Long): Flow<SportGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExerciseGoal(vararg configs: SportGoalEntity)

//    @Query("SELECT * FROM SportGoalEntity WHERE userId=:userId")
//    abstract fun flowExerciseGoal(): Flow<UnitInfoEntity?>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract suspend fun insertExerciseGoal(vararg configs: SportGoalEntity)
}