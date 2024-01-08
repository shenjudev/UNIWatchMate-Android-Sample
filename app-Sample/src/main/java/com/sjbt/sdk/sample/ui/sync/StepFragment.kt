package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmHeartRateData
import com.base.sdk.entity.data.WmStepData
import com.base.sdk.entity.data.WmSyncData
import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.entity.HeartRateItemEntity
import com.sjbt.sdk.sample.utils.DateTimeUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import java.util.*
import kotlin.collections.ArrayList

class StepFragment : DataListFragment<WmStepData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmStepData> = object : DataListAdapter.ValueFormat<WmStepData> {
        override fun format(context: Context, obj: WmStepData): String {
            return timeFormat.format(obj.timestamp) + "    " +
                    context.getString(R.string.unit_step_param, obj.step)
        }
    }

    override fun queryData(date: Date): List<WmStepData>? {

        val result=
        runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
            UNIWatchMate.wmSync.syncStepData.syncData(start.time)
                .awaitFirst()
        }
//        WmSyncData<WmStepData>
//        if (result is ArrayList) {
//            result.add(WmStepData(System.currentTimeMillis(),10000,1223))
//        }
        return result.value
    }

}

