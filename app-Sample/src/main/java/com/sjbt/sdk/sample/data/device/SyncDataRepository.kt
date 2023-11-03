package com.sjbt.sdk.sample.data.device

import com.base.sdk.entity.data.WmHeartRateData
import com.base.sdk.entity.data.WmStepData
import com.github.kilnn.tool.util.roundHalfUp3
import com.sjbt.sdk.sample.data.user.UserInfoRepository
import com.sjbt.sdk.sample.db.AppDatabase
import com.sjbt.sdk.sample.entity.HeartRateItemEntity
import com.sjbt.sdk.sample.entity.StepItemEntity
import com.sjbt.sdk.sample.entity.TodayStepData
import com.sjbt.sdk.sample.model.user.getStepLength
import com.sjbt.sdk.sample.model.user.getWeight
import com.sjbt.sdk.sample.utils.DateTimeUtils
import com.sjbt.sdk.sample.utils.km2Calories
import com.sjbt.sdk.sample.utils.step2Km
import java.util.Calendar
import java.util.Date

interface SyncDataRepository {
    suspend fun saveStep(userId: Long, data: List<WmStepData>?, isSupportStepExtra: Boolean)

    //    suspend fun saveTodayStep(userId: Long, data: WmStepData?)
    suspend fun saveHeartRate(userId: Long, data: List<WmHeartRateData>?)

    suspend fun queryStep(userId: Long, date: Date): List<StepItemEntity>?
//
//    //    suspend fun queryTodayStep(userId: Long): TodayStepData?
//    suspend fun queryHeartRate(userId: Long, date: Date): List<HeartRateItemEntity>?
}

internal class SyncDataRepositoryImpl(
    appDatabase: AppDatabase,
    private val userInfoRepository: UserInfoRepository,
) : SyncDataRepository {

    //        private val stringTypedDao = appDatabase.stringTypedDao()
    private val syncDao = appDatabase.syncDataDao()
    override suspend fun saveStep(
        userId: Long,
        data: List<WmStepData>?,
        isSupportStepExtra: Boolean,
    ) {
//        if (data.isNullOrEmpty()) return
//        return if (isSupportStepExtra) {
//            syncDao.insertStep(
//                data.map { StepItemEntity(userId, Date(it.timestamp), it.step, 0f, 0f) }
//            )
//        } else {
//            val userInfo = userInfoRepository.flowCurrent.value
//            val stepLength = userInfo.getStepLength()
//            val weight = userInfo.getWeight()
//            syncDao.insertStep(
//                data.map {
//                    val distance = step2Km(it.step, stepLength).roundHalfUp3()
//                    val calories = km2Calories(distance, weight).roundHalfUp3()
//
//                    StepItemEntity(userId, Date(it.timestamp), it.step, distance, calories)
//                }
//            )
//        }
    }

    override suspend fun saveHeartRate(userId: Long, data: List<WmHeartRateData>?) {
//        if (data.isNullOrEmpty()) return
//        syncDao.insertHeartRate(
//            data.map { HeartRateItemEntity(userId, Date(
//                it.timestamp), it.avgHeartRate) }
//        )
    }
    //    override suspend fun saveTodayStep(userId: Long, data: FcTodayTotalData?) {
//        if (data == null) {
//            //Clear today step data
//            val today = Date()
//            val calendar = Calendar.getInstance()
//            val start: Date = DateTimeUtils.getDayStartTime(calendar, today)
//            val end: Date = DateTimeUtils.getDayEndTime(calendar, today)
//            syncDao.deleteStepBetween(userId, start, end)
//            stringTypedDao.setTodayStepData(userId, null)
//        } else {
//            stringTypedDao.setTodayStepData(
//                userId, TodayStepData(
//                    data.timestamp,
//                    data.step,
//                    data.distance / 1000.0f,// FcTodayTotalData.distance unit is meters
//                    data.calorie / 1000.0f,// FcTodayTotalData.calorie unit is calorie, not kilocalorie
//                )
//            )
//        }
//    }
    override suspend fun queryStep(userId: Long, date: Date): List<StepItemEntity>? {
        val calendar = Calendar.getInstance()
        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
        val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
        return syncDao.queryStepBetween(userId, start, end)
    }

//    override suspend fun queryTodayStep(userId: Long): TodayStepData? {
//        return stringTypedDao.getTodayStepData(userId)
//    }

//    override suspend fun queryHeartRate(userId: Long, date: Date): List<HeartRateItemEntity>? {
//        val calendar = Calendar.getInstance()
//        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
//        val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
//        return syncDao.queryHeartRateBetween(userId, start, end)
//    }

}