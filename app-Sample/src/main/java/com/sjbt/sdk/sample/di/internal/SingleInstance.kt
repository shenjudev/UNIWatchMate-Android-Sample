package com.sjbt.sdk.sample.di.internal

import android.content.Context
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.data.config.SportGoalRepository
import com.sjbt.sdk.sample.data.config.SportGoalRepositoryImpl
import com.sjbt.sdk.sample.db.AppDatabase
import com.sjbt.sdk.sample.base.storage.InternalStorage
import com.sjbt.sdk.sample.base.storage.InternalStorageImpl
import com.sjbt.sdk.sample.data.auth.AuthManager
import com.sjbt.sdk.sample.data.auth.AuthManagerImpl
import com.sjbt.sdk.sample.data.device.DeviceManager
import com.sjbt.sdk.sample.data.device.DeviceManagerImpl
import com.sjbt.sdk.sample.data.device.SyncDataRepository
import com.sjbt.sdk.sample.data.device.SyncDataRepositoryImpl
import com.sjbt.sdk.sample.data.user.UserInfoRepository
import com.sjbt.sdk.sample.data.user.UserInfoRepositoryImpl

object SingleInstance {

    private val applicationContext: Context = MyApplication.instance

    private val appDatabase: AppDatabase by lazy {
        AppDatabase.build(applicationContext, CoroutinesInstance.ioDispatcher)
    }

     val internalStorage: InternalStorage by lazy {
        InternalStorageImpl(applicationContext, CoroutinesInstance.applicationScope, CoroutinesInstance.applicationIOScope)
    }


    val authManager: AuthManager by lazy {
        AuthManagerImpl(internalStorage, appDatabase)
    }

    val userInfoRepository: UserInfoRepository by lazy {
        UserInfoRepositoryImpl(CoroutinesInstance.applicationScope, internalStorage, appDatabase)
    }

    val syncDataRepository: SyncDataRepository by lazy {
        SyncDataRepositoryImpl(appDatabase, userInfoRepository)
    }

    val deviceManager: DeviceManager by lazy {
        DeviceManagerImpl(
            applicationContext,
            CoroutinesInstance.applicationScope,
            internalStorage,
            userInfoRepository,
            sportGoalRepository,
            syncDataRepository,
                    appDatabase
        )
    }


    val sportGoalRepository: SportGoalRepository by lazy {
        SportGoalRepositoryImpl(
            CoroutinesInstance.applicationScope,
            internalStorage,
            appDatabase
        )
    }
}