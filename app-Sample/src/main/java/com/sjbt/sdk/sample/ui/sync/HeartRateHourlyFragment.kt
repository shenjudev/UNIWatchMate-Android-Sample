package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmHeartRateData
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.utils.DateTimeUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import timber.log.Timber
import java.util.*

class HeartRateHourlyFragment : DataListFragment<WmHeartRateData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmHeartRateData> =
        object : DataListAdapter.ValueFormat<WmHeartRateData> {
            override fun format(context: Context, obj: WmHeartRateData): String {
                return timeFormat.format(obj.timestamp) + "    " +
                        context.getString(R.string.unit_bmp_unit, obj.avgHeartRate) + "  min=${obj.minHeartRate} max=${obj.maxHeartRate}"  +"  ${obj.maxHeartRate}"
            }
        }

    override fun queryData(date: Date): List<WmHeartRateData>? {
        return runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
            val bean =  UNIWatchMate.wmSync.syncHeartRateData.syncData(start.time)
                .awaitFirst()
            Timber.d("type="+bean.type.name)
            bean.value

        }
    }

}

