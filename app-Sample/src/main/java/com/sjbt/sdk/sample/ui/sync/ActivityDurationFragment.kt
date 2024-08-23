package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmActivityDurationData
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.utils.DateTimeUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import java.util.*

class ActivityDurationFragment : DataListFragment<WmActivityDurationData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmActivityDurationData> = object : DataListAdapter.ValueFormat<WmActivityDurationData> {
        override fun format(context: Context, obj: WmActivityDurationData): String {
            return timeFormat.format(obj.timestamp) + "    " +
                    context.getString(R.string.unit_minute_param, obj.duration/60)
        }
    }

    override fun queryData(date: Date): List<WmActivityDurationData>? {
        return runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
            UNIWatchMate.wmSync.syncActivityDurationData.syncData(start.time)
                .awaitFirst().value

        }
    }

}

