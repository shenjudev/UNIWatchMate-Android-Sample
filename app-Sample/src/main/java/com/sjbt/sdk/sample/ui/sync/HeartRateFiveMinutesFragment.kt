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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
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
            UNIWatchMate.wmSync.syncRealtimeRateData.syncData(System.currentTimeMillis() - 1000 * 60 * 60 * 24)
                .await()
        }
    }

}

