package com.sjbt.sdk.sample.dialog

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.GsonUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.kilnn.tool.widget.item.PreferenceItem
import com.google.gson.reflect.TypeToken
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.Constant
import com.sjbt.sdk.sample.model.WeatherCode

class WeatherCodeTestDialog(
    val mContext: Context, val callBack: CallBack<WeatherCode>,
) : BaseDialog(mContext) {
    private val datas = mutableListOf<WeatherCode>()
    private val mAdapter by lazy {
        WeatherCodeAdapter(datas)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = View.inflate(mContext, R.layout.dialog_weather_test, null)
        setContentView(view)
        val type = object : TypeToken<ArrayList<WeatherCode>>() {}.type
        val weatherCodes =
            GsonUtils.fromJson<ArrayList<WeatherCode>>(Constant.WeatherCodeName, type)
        datas.addAll(weatherCodes)
        val recycleViewWeather = findViewById<RecyclerView>(R.id.recycle_view_weather)
        setWindowParam(Gravity.CENTER, 0f, ANIM_TYPE_NONE)
        recycleViewWeather?.layoutManager = LinearLayoutManager(context)
        recycleViewWeather?.adapter = mAdapter
        mAdapter.setOnItemClickListener { adapter, view, position ->
            run {
                callBack.callBack(datas[position])
                dismiss()
            }
        }
    }

    inner class WeatherCodeAdapter : BaseQuickAdapter<WeatherCode, BaseViewHolder> {
        var cnNow = false
        constructor(data: List<WeatherCode>) : super(R.layout.item_preference_test_list, data) {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.content.res.Resources.getSystem().configuration.locales.get(0)
            } else {
                android.content.res.Resources.getSystem().configuration.locale
            }
            if (locale.country.toLowerCase().contains("cn")||locale.language.toLowerCase().contains("cn")) {
                cnNow = true
            }
        }

        override fun convert(baseViewHolder: BaseViewHolder, weatherCode: WeatherCode) {
            val pItem = baseViewHolder.getView<PreferenceItem>(R.id.item_preference_text)
            pItem.getTitleView().text = weatherCode.name[if (cnNow) "chinese" else "english"]
        }
    }
}
