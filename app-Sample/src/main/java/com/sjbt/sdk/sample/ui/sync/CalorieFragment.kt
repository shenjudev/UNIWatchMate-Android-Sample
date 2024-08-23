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
import kotlinx.coroutines.rx3.awaitFirst
import timber.log.Timber
import java.util.*

class CalorieFragment : DataListFragment<WmCaloriesData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmCaloriesData> = object : DataListAdapter.ValueFormat<WmCaloriesData> {
        override fun format(context: Context, obj: WmCaloriesData): String {
            return timeFormat.format(obj.timestamp) + "    " +
                    context.getString(
                        R.string.unit_calories_param,
                        (obj.calorie.toString()) + "  ${obj.calorie}"
                    )

        }
    }

    override fun queryData(date: Date): List<WmCaloriesData>? {
        return runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
           val result = UNIWatchMate.wmSync.syncCaloriesData.syncData(start.time)
                .awaitFirst()
            Timber.i("queryData result=${result}")
            result.value
        }
    }

}

