package com.sjbt.sdk.sample.di

import com.sjbt.sdk.sample.data.config.SportGoalRepository
import com.sjbt.sdk.sample.di.internal.SingleInstance
import com.sjbt.sdk.sample.base.storage.InternalStorage
import com.sjbt.sdk.sample.data.auth.AuthManager
import com.sjbt.sdk.sample.data.device.DeviceManager
import com.sjbt.sdk.sample.data.device.SyncDataRepository
import com.sjbt.sdk.sample.di.internal.CoroutinesInstance
import com.sjbt.sdk.sample.data.user.UserInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Because some developers may not use dagger or hilt.
 * In order to reduce their learning cost, this sample uses manual injection of dependencies
 */
object Injector {

        fun getAuthManager(): AuthManager {
        return SingleInstance.authManager
    }
    fun requireAuthedUserId(): Long {
        return SingleInstance.authManager.getAuthedUserIdOrNull()!!
    }
    fun getInternalStorage(): InternalStorage {
        return SingleInstance.internalStorage
    }


    fun getDeviceManager(): DeviceManager {
        return SingleInstance.deviceManager
    }

    fun getUserInfoRepository(): UserInfoRepository {
        return SingleInstance.userInfoRepository
    }

    fun getApplicationScope(): CoroutineScope {
        return CoroutinesInstance.applicationScope
    }


    fun getExerciseGoalRepository(): SportGoalRepository {
        return SingleInstance.sportGoalRepository
    }

    fun getSyncDataRepository(): SyncDataRepository {
        return SingleInstance.syncDataRepository
    }
}