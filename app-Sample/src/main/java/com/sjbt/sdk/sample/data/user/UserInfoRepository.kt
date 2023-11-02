package com.sjbt.sdk.sample.data.user

import com.sjbt.sdk.sample.base.storage.InternalStorage
import com.sjbt.sdk.sample.db.AppDatabase
import com.sjbt.sdk.sample.entity.UserEntity
import com.sjbt.sdk.sample.model.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

interface UserInfoRepository {

    val flowCurrent: StateFlow<UserInfo?>

    suspend fun getUserInfo(userId: Long): UserInfo?

    suspend fun setUserInfo(userInfo: UserInfo)

}

internal class UserInfoRepositoryImpl constructor(
    applicationScope: CoroutineScope,
    internalStorage: InternalStorage,
    appDatabase: AppDatabase,
) : UserInfoRepository {

    private val userDao = appDatabase.userDao()

    override val flowCurrent: StateFlow<UserInfo?> = internalStorage.flowAuthedUserId.flatMapLatest {
        if (it == null) {
            flowOf(null)
        } else {
            appDatabase.userDao().flowUserInfo(it)
        }
    }.stateIn(applicationScope, SharingStarted.Eagerly, null)

    override suspend fun getUserInfo(userId: Long): UserInfo? {
        return userDao.queryUserInfo(userId)
    }

    override suspend fun setUserInfo(userInfo: UserInfo) {
        if (getUserInfo(userId = userInfo.id) == null) {
            userDao.insert(
                UserEntity(userInfo.id,"name","password", userInfo.height, userInfo.weight, userInfo.sex, userInfo.birthYear,userInfo.birthMonth,userInfo.birthDay)
            )
        }else{
            userDao.updateUserInfo(
                userInfo.id, userInfo.height, userInfo.weight, userInfo.sex, userInfo.birthYear,userInfo.birthMonth,userInfo.birthDay)
        }

    }

}