package com.sjbt.sdk.sample.ui.device.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.base.sdk.entity.apps.WmLanguage
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.databinding.ItemLanguageListBinding

class LanguageListAdapter() :
    RecyclerView.Adapter<LanguageListAdapter.ItemViewHolder>() {

    var listener: Listener? = null

    var sources: List<WmLanguage>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemLanguageListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val wmLanguage = sources?.get(position) ?: return

        holder.viewBind.itemLanguageGridding.getTitleView().text = wmLanguage.bcp
        holder.viewBind.itemLanguageGridding.getImageView().visibility = if(wmLanguage.curr_lang) View.VISIBLE else View.GONE
        holder.viewBind.itemLanguageGridding.getImageView().setImageResource(R.drawable.ic_baseline_done_24)
        holder.viewBind.itemLanguageGridding.setOnClickListener {
            listener?.onItemSelect(holder.bindingAdapterPosition)
        }

    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    interface Listener {
        fun onItemSelect(position: Int)
    }

    class ItemViewHolder(val viewBind: ItemLanguageListBinding) :
        RecyclerView.ViewHolder(viewBind.root)

}