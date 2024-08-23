package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmHeartRateData
import com.base.sdk.entity.data.WmRealtimeRateData
import com.github.kilnn.tool.widget.item.PreferenceItem
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.utils.DateTimeUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import timber.log.Timber
import java.util.*

class HeartRateFiveMinutesFragment : DataListFragment<WmRealtimeRateData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmRealtimeRateData> =
        object : DataListAdapter.ValueFormat<WmRealtimeRateData> {
            override fun format(context: Context, obj: WmRealtimeRateData): String {
                return timeFormat.format(obj.timestamp) + "    " +
                        context.getString(R.string.unit_bmp_unit, obj.rate)
            }
        }

    override fun queryData(date: Date): List<WmRealtimeRateData>? {
        return runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)

                val bean =    UNIWatchMate.wmSync.syncRealtimeRateData.syncData(start.time)
                .awaitFirst()
            Timber.d("type="+bean.type.name)
            bean.value
        }
    }

}

