package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmCaloriesData
import com.base.sdk.entity.data.WmHeartRateData
import com.base.sdk.entity.data.WmStepData
import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.entity.HeartRateItemEntity
import com.sjbt.sdk.sample.utils.DateTimeUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import java.util.*

class CalorieFragment : DataListFragment<WmCaloriesData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmCaloriesData> = object : DataListAdapter.ValueFormat<WmCaloriesData> {
        override fun format(context: Context, obj: WmCaloriesData): String {
            return timeFormat.format(obj.timestamp) + "    " +
                    context.getString(R.string.unit_k_calories_param, (obj.calorie/1000).toString())
        }
    }

    override fun queryData(date: Date): List<WmCaloriesData>? {
        return runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
            UNIWatchMate.wmSync.syncCaloriesData.syncData(start.time)
                .await()

        }
    }

}

