package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmBaseSyncData
import com.base.sdk.entity.data.WmCaloriesData
import com.base.sdk.entity.data.WmHeartRateData
import com.base.sdk.entity.data.WmStepData
import com.base.sdk.entity.data.WmSyncData
import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.entity.HeartRateItemEntity
import com.sjbt.sdk.sample.utils.DateTimeUtils
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.collect
import timber.log.Timber
import java.util.*

class AllDataFragment : DataListFragment<WmSyncData<*>>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmSyncData<*>> =
        object : DataListAdapter.ValueFormat<WmSyncData<*>> {
            override fun format(context: Context, obj: WmSyncData<*>): String {
                return timeFormat.format(obj.timestamp) + "    ${obj}"

            }
        }
    val dataList = mutableListOf<WmSyncData<*>>()

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

    override fun queryData(date: Date): List<WmSyncData<*>>? {
        Timber.i("queryData runBlocking")
        return runBlocking {
            val calendar = Calendar.getInstance()
            val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
            val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
            UNIWatchMate.wmSync.syncAllData.syncData(UNIWatchMate.wmSync.syncAllData.latestSyncTime()).asFlow().catch {
                Timber.e("queryData error=${it.message}")
                ToastUtil.showToast(it.message)
            }
                .collect {
                    dataList.add(it)
                }
            Timber.i("queryData result=${dataList}")
//            result.value
            dataList
        }
    }

}

