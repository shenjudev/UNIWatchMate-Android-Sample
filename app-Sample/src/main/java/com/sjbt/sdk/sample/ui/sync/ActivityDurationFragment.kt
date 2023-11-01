package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmActivityData
import com.base.sdk.entity.data.WmHeartRateData
import com.base.sdk.entity.data.WmStepData
import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.entity.HeartRateItemEntity
import com.sjbt.sdk.sample.utils.DateTimeUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import java.util.*

class ActivityDurationFragment : DataListFragment<WmActivityData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmActivityData> = object : DataListAdapter.ValueFormat<WmActivityData> {
        override fun format(context: Context, obj: WmActivityData): String {
            return timeFormat.format(obj.timestamp) + "    " +
                    context.getString(R.string.unit_minute_param, obj.duration/60)
        }
    }

    override fun queryData(date: Date): List<WmActivityData>? {
        return runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
            UNIWatchMate.wmSync.syncActivityData.syncData(start.time)
                .await()

        }
    }

}

