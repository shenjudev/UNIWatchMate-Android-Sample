package com.sjbt.sdk.sample.ui.combine

import android.content.Context
import android.widget.TextView
import com.base.sdk.entity.apps.WmWidget
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sjbt.sdk.sample.R

class WidgetAdapter(mContext: Context, mIds: Int, mList: List<WmWidget>, isAdd: Boolean,onItemClicked: OnItemClicked) :
    BaseQuickAdapter<WmWidget, BaseViewHolder>(mIds, mList) {
    val isAdd = isAdd
    private val onItemClicked = onItemClicked
    private val mContext = mContext
    override fun convert(helper: BaseViewHolder, item: WmWidget?) {
        val tvSett = helper.getView<TextView>(R.id.tv_widget_sett)
        if (isAdd) {
            tvSett.setBackgroundColor(mContext.resources.getColor(R.color.color_34c759))
            tvSett.text = "+"
        } else {
            if (data.size == 1){
                tvSett.setBackgroundColor(mContext.resources.getColor(R.color.color_8e8e93))
            }else {
                tvSett.setBackgroundColor(mContext.resources.getColor(R.color.color_ff453a))
            }
            tvSett.text = "-"
        }
        helper.getView<TextView>(R.id.tv_widget_name).text = item?.type?.name

        tvSett.setOnClickListener {
            onItemClicked.onClick(helper.adapterPosition, isAdd)
        }
    }

    interface OnItemClicked{
        fun onClick(position:Int,isAdd:Boolean)
    }

}