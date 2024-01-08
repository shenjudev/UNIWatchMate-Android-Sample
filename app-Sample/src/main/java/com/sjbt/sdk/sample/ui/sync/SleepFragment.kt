package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmSleepData
import com.base.sdk.entity.data.WmSleepItem
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.utils.DateTimeUtils
import com.sjbt.sdk.sample.utils.FormatterUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import kotlinx.coroutines.withContext
import java.util.*

class SleepFragment : DataListFragment<WmSleepData>() {

    override val layoutId: Int = R.layout.fragment_sleep

    private lateinit var tvDeepSleep: TextView
    private lateinit var tvLightSleep: TextView
    private lateinit var tvAwakeSleep: TextView
    private lateinit var tvRem: TextView
    private var start: Date?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvDeepSleep = view.findViewById(R.id.tv_deep_sleep)
        tvLightSleep = view.findViewById(R.id.tv_light_sleep)
        tvAwakeSleep = view.findViewById(R.id.tv_awake_sleep)
        tvRem = view.findViewById(R.id.tv_rem)
        super.onViewCreated(view, savedInstanceState)
    }

    override val valueFormat: DataListAdapter.ValueFormat<WmSleepData> =
        object : DataListAdapter.ValueFormat<WmSleepData> {
            override fun format(context: Context, obj: WmSleepData): String {
//                val statusText = when (obj.status) {
//                    WmSleepItem.STATUS_DEEP -> context.getString(R.string.deep_sleep)
//                    WmSleepItem.STATUS_LIGHT -> context.getString(R.string.light_sleep)
//                    WmSleepItem.STATUS_REM -> context.getString(R.string.rapid_eye_movement)
//                    WmSleepItem.STATUS_SOBER -> context.getString(R.string.awake_sleep)
//                    else -> context.getString(R.string.awake_sleep)
//                }
                return "    bedTime=" + timeFormat.format(obj.wmSleepSummary.bedTime) + "    \n${obj}"
            }
        }

    override fun queryData(date: Date): List<WmSleepData>? {
        return runBlocking {
            val calendar = Calendar.getInstance()
            start = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
            val data = UNIWatchMate.wmSync.syncSleepData.syncData(0).awaitFirst().value

            val sleepItemDataList = mutableListOf<WmSleepData>()
//            val sleepItemDatas = mutableListOf<WmSleepItem>()
//            val duration = IntArray(4)
//            data.forEach { wmSleepData ->
//                sleepItemDataList.add(wmSleepData)
//            }
//            withContext(Dispatchers.Main) {
//                tvDeepSleep.text = FormatterUtil.second2Hmm(duration[0])
//                tvLightSleep.text = FormatterUtil.second2Hmm(duration[1])
//                tvRem.text = FormatterUtil.second2Hmm(duration[2])
//                tvAwakeSleep.text = FormatterUtil.second2Hmm(duration[3])
//            }
            data
        }
    }

}