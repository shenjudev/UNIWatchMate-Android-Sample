package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmSleepItem
import com.sjbt.sdk.sample.R
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import java.util.*

class SleepFragment : DataListFragment<WmSleepItem>() {

    override val layoutId: Int = R.layout.fragment_sleep

    private lateinit var tvDeepSleep: TextView
    private lateinit var tvLightSleep: TextView
    private lateinit var tvAwakeSleep: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvDeepSleep = view.findViewById(R.id.tv_deep_sleep)
        tvLightSleep = view.findViewById(R.id.tv_light_sleep)
        tvAwakeSleep = view.findViewById(R.id.tv_awake_sleep)
        super.onViewCreated(view, savedInstanceState)
    }

    override val valueFormat: DataListAdapter.ValueFormat<WmSleepItem> = object : DataListAdapter.ValueFormat<WmSleepItem> {
        override fun format(context: Context, obj: WmSleepItem): String {
            val statusText = when (obj.status) {
                WmSleepItem.STATUS_DEEP -> context.getString(R.string.deep_sleep)
                WmSleepItem.STATUS_LIGHT -> context.getString(R.string.light_sleep)
                else -> context.getString(R.string.awake_sleep)
            }
//            return statusText + "    " + timeFormat.format(obj.) + " ->  "
            return statusText + "    "
        }
    }

    override fun queryData(date: Date): List<WmSleepItem>? {
        val data = runBlocking { UNIWatchMate.wmSync.syncSleepData.syncData(date.time).await().value }
        val sleepItemDatas = mutableListOf<WmSleepItem>()
        data.forEach {
//            sleepItemDatas.addAll(it.items)
        }

//        tvDeepSleep.text = FormatterUtil.second2Hmm(duration[0])
//        tvLightSleep.text = FormatterUtil.second2Hmm(duration[1])
//        tvAwakeSleep.text = FormatterUtil.second2Hmm(duration[2])
        return sleepItemDatas
    }

}