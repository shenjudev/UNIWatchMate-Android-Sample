package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.apps.WmDial
import com.base.sdk.entity.apps.WmValueTypeData
import com.base.sdk.entity.data.WmSportSummaryData
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.TimeUtils
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.AsyncViewModel
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.SingleAsyncState
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.di.internal.CoroutinesInstance.applicationScope
import com.sjbt.sdk.sample.entity.SportSummaryEntity
import com.sjbt.sdk.sample.model.LocalSportLibrary
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.ui.device.contacts.PhoneContactsEvent
import com.sjbt.sdk.sample.ui.device.contacts.PhoneContactsState
import com.sjbt.sdk.sample.ui.device.dial.library.PushParamsAndPackets
import com.sjbt.sdk.sample.utils.DateTimeUtils
import com.sjbt.sdk.sample.utils.getSportLibrary
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewLifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SportFragment : DataListFragment<WmSportSummaryData>(),
    DataListAdapter.Listener<WmSportSummaryData> {
    val calendar = Calendar.getInstance()
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val viewModel: SportLibraryViewModel by viewModels()
    override val valueFormat: DataListAdapter.ValueFormat<WmSportSummaryData> =
        object : DataListAdapter.ValueFormat<WmSportSummaryData> {
            override fun format(context: Context, obj: WmSportSummaryData): String {
                return dateTimeFormat.format(obj.startTime) + "    " + sportTypeText(obj.sportId)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.listener = this
    }

    override fun queryData(date: Date): List<WmSportSummaryData>? {
        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
        val end: Date = DateTimeUtils.getDayEndTime(calendar, date)
        val result = runBlocking {
            viewModel.requestSports(date)
//            UNIWatchMate.wmSync.syncSportSummaryData.syncData(start.time).await()
        }
        return result
    }

    //TODO Only part game types are listed here.
    // For more types, please refer to https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/05.Sync-Data#game
    private fun sportTypeText(type: Int): String {
        return getSportName(type)
    }

    private fun getSportName(sportId: Int): String {
        getSportLibrary().sports.forEach { localSport ->
            if (sportId == localSport.id) {
                return getSportLibrary().getNameById(sportId) + "   "+getSportLibrary().getTypeById(
                    sportId
                )
            }
        }
        return sportId.toString()
    }

    override fun onItemClick(item: WmSportSummaryData) {
        val test = SportSummaryEntity(item.timestamp, item.sportId, mutableListOf())
        findNavController().navigate(SportFragmentDirections.toSportDetail(item))
    }

}

data class SportsState(
    val getports: Async<ArrayList<WmSportSummaryData>> = Uninitialized,
)

sealed class SportsEvent {
    class RequestFail(val throwable: Throwable) : SportsEvent()
    object NavigateUp : SportsEvent()
}

class SportLibraryViewModel(
) : StateEventViewModel<SportsState, SportsEvent>(SportsState()) {
    private var oldData = ""
    val calendar = Calendar.getInstance()
    suspend fun requestSports(date: Date): ArrayList<WmSportSummaryData> {
        val start: Date = DateTimeUtils.getDayStartTime(calendar, date)
        val startDate =  TimeUtils.date2String(start)
        if (state.getports() != null && !state.getports()!!.isEmpty()&&startDate == oldData) {
        } else {
            oldData = startDate
            val result =    UNIWatchMate.wmSync.syncSportSummaryData.syncData(start.time).awaitFirst().value
            LogUtils.d("requestSports result = ${GsonUtils.toJson(result)}")
            state.copy(getports = Success(ArrayList(result))).newState()
//            runCatchingWithLog {
//
//            }.onSuccess {
//                state.copy(getports = Success(ArrayList(it))).newState()
//            }.onFailure {
//                SportsEvent.RequestFail(it).newEvent()
//            }
        }
        return state.getports() ?: arrayListOf()
    }
}