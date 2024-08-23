package com.sjbt.sdk.sample.ui.device.dial.diyDial

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import android.widget.FrameLayout.LayoutParams
import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.obsez.android.lib.filechooser.internals.UiUtil
import com.sjbt.sdk.sample.R

class DiyDialTimeShowTypeAdapter(
    mContext: Context,
    mIds: Int,
    mList: MutableList<Int>,
    deviceW: Int,
    deviceH: Int
) : BaseQuickAdapter<Int, BaseViewHolder>(mIds, mList) {
    private val deviceW = deviceW
    private val deviceH = deviceH

    private var croppedBitmap: Bitmap? = null
    private var currSelectColor = "#ffffff"
    override fun convert(helper: BaseViewHolder, item: Int?) {
        val cardView = helper.getView<CardView>(R.id.card_view)
        val ivTimeShowType = helper.getView<ImageView>(R.id.iv_time_show_type)
        val params = LayoutParams(deviceW, deviceH)
        params.setMargins(UiUtil.dip2px(4),0,UiUtil.dip2px(4),0)
        cardView.layoutParams = params
        ivTimeShowType.setImageResource(item!!)
    }

    override fun convertPayloads(helper: BaseViewHolder, item: Int?, payloads: MutableList<Any>) {
        super.convertPayloads(helper, item, payloads)
        if (payloads != null){
            when (payloads[0]) {
                1 -> {
                    val ivBg = helper.getView<ImageView>(R.id.iv_bg)
                    ivBg.setImageBitmap(croppedBitmap)
                }
                2-> {
                    val ivTimeShowType = helper.getView<ImageView>(R.id.iv_time_show_type)
                    ivTimeShowType.setColorFilter(Color.parseColor(currSelectColor))
                }
            }
        }
    }

    fun setBitmap(croppedBitmap: Bitmap?) {
        this.croppedBitmap = croppedBitmap
        notifyItemChanged(0,1)
        notifyItemChanged(1,1)
        notifyItemChanged(2,1)
        notifyItemChanged(3,1)
    }

    /**
     *
     */
    fun setCurrColor(currColorStr: String) {
        currSelectColor = currColorStr
        notifyItemChanged(0,2)
        notifyItemChanged(1,2)
        notifyItemChanged(2,2)
        notifyItemChanged(3,2)
    }
}