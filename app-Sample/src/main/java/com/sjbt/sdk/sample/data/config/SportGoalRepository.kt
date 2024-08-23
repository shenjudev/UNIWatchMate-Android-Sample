package com.sjbt.sdk.sample.data.config

import com.base.sdk.entity.settings.WmSportGoal
import com.sjbt.sdk.sample.db.AppDatabase
import com.sjbt.sdk.sample.entity.SportGoalEntity
import com.sjbt.sdk.sample.entity.toModel
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.base.storage.InternalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface SportGoalRepository {
    /**
     * Flow current exercise goal config.
     *
     * If there is currently an Authentic user and there is a local config, then return it.
     *
     * Otherwise, return a default config.
     */
    val flowCurrent: StateFlow<WmSportGoal>

    /**
     * @param userId
     * @param config
     */
    fun modify(userId: Long, config: WmSportGoal)

}

internal class SportGoalRepositoryImpl constructor(
    private val applicationScope: CoroutineScope,
    internalStorage: InternalStorage,
    private val appDatabase: AppDatabase,
) : SportGoalRepository {

    override val flowCurrent: StateFlow<WmSportGoal> =
        internalStorage.flowAuthedUserId.flatMapLatest {
            if (it == null) {
                flowOf(null)
            } else {
                appDatabase.settingDao().flowExerciseGoal(it)
            }
        }.map {
            it.toModel()
        }.stateIn(applicationScope, SharingStarted.Eagerly, WmSportGoal(0, 0, 0, 0))


    override fun modify(userId: Long, config: WmSportGoal) {
        applicationScope.launchWithLog {
            val entity = SportGoalEntity(
                userId = userId,
                step = config.steps,
                distance = config.distance,
                calorie = config.calories,
                activityMinutes = config.activityDuration
            )
            appDatabase.settingDao().insertExerciseGoal(entity)
        }
    }

}