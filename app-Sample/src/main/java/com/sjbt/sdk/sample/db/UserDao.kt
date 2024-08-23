package com.sjbt.sdk.sample.db

import androidx.room.*
import com.sjbt.sdk.sample.entity.UserEntity
import com.sjbt.sdk.sample.model.user.UserInfo
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao {
    @Query("SELECT * FROM UserEntity WHERE name=:name ")
    abstract suspend fun queryUserByName(name: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(userEntity: UserEntity)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM UserEntity WHERE id=:userId")
    internal abstract fun flowUserInfo(userId: Long): Flow<UserInfo>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM UserEntity WHERE id=:userId")
    abstract suspend fun queryUserInfo(userId: Long): UserInfo

    @Query("UPDATE UserEntity SET height=:height,weight=:weight,sex=:sex,birthYear=:birthYear,birthMonth=:birthMonth,birthDay=:birthDay WHERE id=:userId")
    abstract suspend fun updateUserInfo(
        userId: Long,
        height: Int,
        weight: Int,
        sex: Boolean,
        birthYear: Int,
        birthMonth: Int,
        birthDay: Int,
    )

    @Query("SELECT COUNT(*) FROM UserEntity WHERE id=:userId")
    protected abstract suspend fun queryUserCount(userId: Long): Int

    suspend fun isUserExist(userId: Long): Boolean {
        return queryUserCount(userId) > 0
    }
}