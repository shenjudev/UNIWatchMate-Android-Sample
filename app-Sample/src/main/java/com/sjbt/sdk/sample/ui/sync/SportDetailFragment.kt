package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmSportSummaryData
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ResourceUtils
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.entity.SportSummaryEntity
import com.sjbt.sdk.sample.model.LocalSportLibrary
import com.sjbt.sdk.sample.utils.getParcelableCompat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import java.text.SimpleDateFormat
import java.util.*

class SportDetailFragment : DataListFragment<WmSportSummaryData>() {
    override val layoutId: Int = R.layout.fragment_sport_detail
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    var localSportLibrary: LocalSportLibrary? = null
    var tvDataSport: TextView? = null
    private var sportSummaryEntity: SportSummaryEntity? = null

    override val valueFormat: DataListAdapter.ValueFormat<WmSportSummaryData> =
        object : DataListAdapter.ValueFormat<WmSportSummaryData> {
            override fun format(context: Context, obj: WmSportSummaryData): String {
                return dateTimeFormat.format(obj.timestamp) + "    " + sportTypeText(obj.sportId) + "  " + obj.valueType
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sportSummaryEntity =
            arguments?.getParcelableCompat<SportSummaryEntity>("wmSportSummaryData")
        tvDataSport = view.findViewById(R.id.tv_data_sport)
        btnDate.isVisible = false
        tvDataSport?.text = "${sportSummaryEntity?.sportId}"
    }

    override fun queryData(date: Date): List<WmSportSummaryData>? {
        return runBlocking {
            if (localSportLibrary != null) {
                val sportsData = ResourceUtils.readAssets2String("sports_data.json")
                localSportLibrary =
                    GsonUtils.fromJson<LocalSportLibrary>(sportsData, LocalSportLibrary::class.java)
            }
            //detail里面获取每10秒的数据
            UNIWatchMate.wmSync.syncSportSummaryData.syncData(date.time).await()
        }
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
        return ""
    }

}