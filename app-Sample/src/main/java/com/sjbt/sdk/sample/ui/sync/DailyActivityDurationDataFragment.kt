package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmDailyActivityDurationData
import com.sjbt.sdk.sample.utils.DateTimeUtils
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.awaitFirst
import timber.log.Timber
import java.util.*

class DailyActivityDurationDataFragment : DataListFragment<WmDailyActivityDurationData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmDailyActivityDurationData> =
        object : DataListAdapter.ValueFormat<WmDailyActivityDurationData> {
            override fun format(context: Context, obj: WmDailyActivityDurationData): String {
                return timeFormat.format(obj.timestamp) + "    $obj"
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.launchRepeatOnStarted {
            launch {
//               UNIWatchMate.wmSync.syncAllData.observeSyncData.asFlow().collect{
//                   Timber.i("observeSyncData result=${it}")
//               }
            }
        }
    }

    override fun queryData(date: Date): List<WmDailyActivityDurationData>? {
        Timber.i("queryData runBlocking")
        return runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
            val result =
                UNIWatchMate.wmSync.syncDailyActivityDuration.syncData(start.time).awaitFirst()
            Timber.i("queryData result=$result")
            result.value
//            dataList
        }
    }

}

