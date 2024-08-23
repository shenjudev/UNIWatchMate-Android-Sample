package com.sjbt.sdk.sample.db

import androidx.room.*
import com.sjbt.sdk.sample.entity.HeartRateItemEntity
import com.sjbt.sdk.sample.entity.StepItemEntity
import com.sjbt.sdk.sample.utils.room.TimeConverter
import java.util.*

@Dao
abstract class SyncDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStep(items: List<StepItemEntity>)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract suspend fun insertSleep(items: List<SleepItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHeartRate(items: List<HeartRateItemEntity>)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract suspend fun insertOxygen(items: List<OxygenItemEntity>)
//
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract suspend fun insertSport(items: List<SportRecordEntity>)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract suspend fun insertGps(items: List<SportGpsEntity>)

    @Query("DELETE FROM StepItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end")
    abstract suspend fun deleteStepBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date)

    @Query("SELECT * FROM StepItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end ORDER BY time ASC")
    abstract suspend fun queryStepBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date): List<StepItemEntity>?

    @Query("SELECT * FROM HeartRateItemEntity WHERE userId=:userId AND time BETWEEN :start AND :end ORDER BY time ASC")
    abstract suspend fun queryHeartRateBetween(userId: Long, @TypeConverters(TimeConverter::class) start: Date, @TypeConverters(TimeConverter::class) end: Date): List<HeartRateItemEntity>?

}