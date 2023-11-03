package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmOxygenData
import com.sjbt.sdk.sample.R
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
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
        return runBlocking { UNIWatchMate.wmSync.syncOxygenData.syncData(date.time).await().value }
    }
}