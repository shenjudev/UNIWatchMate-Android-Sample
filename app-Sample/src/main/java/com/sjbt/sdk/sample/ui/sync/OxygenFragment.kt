package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmOxygenData
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.utils.DateTimeUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import java.util.*

class OxygenFragment : DataListFragment<WmOxygenData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmOxygenData> =
        object : DataListAdapter.ValueFormat<WmOxygenData> {
            override fun format(context: Context, obj: WmOxygenData): String {
                return timeFormat.format(obj.timestamp) + "    " +
                        context.getString(R.string.percent_param, obj.oxygen)
            }
        }

    override fun queryData(date: Date): List<WmOxygenData>? {
        return runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
            UNIWatchMate.wmSync.syncOxygenData.syncData(System.currentTimeMillis()-1000*60*60).awaitFirst().value }
    }
}