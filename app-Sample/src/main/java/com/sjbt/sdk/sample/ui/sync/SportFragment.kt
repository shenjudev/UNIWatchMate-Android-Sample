package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmSportSummaryData
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ResourceUtils
import com.sjbt.sdk.sample.entity.SportSummaryEntity
import com.sjbt.sdk.sample.model.LocalSportLibrary
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SportFragment : DataListFragment<WmSportSummaryData>(),
    DataListAdapter.Listener<WmSportSummaryData> {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private var localSportLibrary: LocalSportLibrary? = null

    override val valueFormat: DataListAdapter.ValueFormat<WmSportSummaryData> =
        object : DataListAdapter.ValueFormat<WmSportSummaryData> {
            override fun format(context: Context, obj: WmSportSummaryData): String {
                return dateTimeFormat.format(obj.timestamp) + "    " + sportTypeText(obj.sportId) + "  " + obj.valueType
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDate.isVisible = false
        adapter.listener = this
    }

    override fun queryData(date: Date): List<WmSportSummaryData>? {

//        val result =runBlocking {
//            if (localSportLibrary != null) {
//                val sportsData = ResourceUtils.readAssets2String("sports_data.json")
//                localSportLibrary =
//                    GsonUtils.fromJson<LocalSportLibrary>(sportsData, LocalSportLibrary::class.java)
//            }
//
//           UNIWatchMate.wmSync.syncSportSummaryData.syncData(date.time).await()
//        }
        val result = arrayListOf<WmSportSummaryData>()
        if (result is ArrayList) {
            result.add(WmSportSummaryData(System.currentTimeMillis(),9, mutableListOf()))
        }
        return result
    }

    //TODO Only part game types are listed here.
    // For more types, please refer to https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/05.Sync-Data#game
    private fun sportTypeText(type: Int): String {
        return getSportName(type)
    }

    private fun getSportName(sportId: Int): String {
        localSportLibrary?.let {
            it.sports.forEach { localSport ->
                if (sportId == localSport.id) {
                    return it.getNameById(sportId) + it.getTypeById(sportId)
                }
            }
        }
        return sportId.toString()
    }

    override fun onItemClick(item: WmSportSummaryData) {
        val test = SportSummaryEntity(item.timestamp, item.sportId, mutableListOf())
        findNavController().navigate(SportFragmentDirections.toSportDetail(test))
    }

}