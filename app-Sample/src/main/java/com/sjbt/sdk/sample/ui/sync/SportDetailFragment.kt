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
import com.sjbt.sdk.sample.utils.getSerializableCompat
import com.sjbt.sdk.sample.utils.getSportLibrary
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import java.text.SimpleDateFormat
import java.util.*

class SportDetailFragment : DataListFragment<WmSportSummaryData>() {
    override val layoutId: Int = R.layout.fragment_sport_detail
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val localSportLibrary by lazy {
        getSportLibrary()
    }
    var tvDataSport: TextView? = null
    private var sportSummaryEntity: WmSportSummaryData? = null

    override val valueFormat: DataListAdapter.ValueFormat<WmSportSummaryData> =
        object : DataListAdapter.ValueFormat<WmSportSummaryData> {
            override fun format(context: Context, obj: WmSportSummaryData): String {
                return dateTimeFormat.format(obj.timestamp) + "    " + sportTypeText(obj.sportId) + "  " +  obj
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sportSummaryEntity =
            arguments?.getSerializableCompat<WmSportSummaryData>("wmSportSummaryData")
        tvDataSport = view.findViewById(R.id.tv_data_sport)
        btnDate.isVisible = false
        sportSummaryEntity?.let {
            tvDataSport?.text = "${sportTypeText(sportSummaryEntity!!.sportId)}\n${sportSummaryEntity}"
        }
    }

    override fun queryData(date: Date): List<WmSportSummaryData>? {
        return runBlocking {
            //detail里面获取每10秒的数据
            mutableListOf()
        }
    }

    //TODO Only part game types are listed here.
    // For more types, please refer to https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/05.Sync-Data#game
    private fun sportTypeText(type: Int): String {
        return getSportName(type)
    }

    private fun getSportName(sportId: Int): String {
        localSportLibrary?.let {
            return it.getNameById(sportId) +"   "+ it.getTypeById(sportId)
        }
        return ""
    }

}