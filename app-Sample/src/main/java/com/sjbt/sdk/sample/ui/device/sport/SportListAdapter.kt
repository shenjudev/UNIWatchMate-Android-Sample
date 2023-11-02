package com.sjbt.sdk.sample.ui.device.sport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.base.sdk.entity.apps.WmSport
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.databinding.ItemSportInstalledBinding

class SportListAdapter(val viewModel: SportInstalledViewModel) :
    RecyclerView.Adapter<SportListAdapter.ItemViewHolder>() {

    var listener: Listener? = null

    var sources: List<WmSport>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemSportInstalledBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val sport = sources?.get(position) ?: return
        val name = viewModel.getNameById(sport.id)
        holder.viewBind.tvSportId.text = "${sport.id} $name"
        holder.viewBind.imgDelete.visibility=if(sport.buildIn||sources!!.size<2) View.GONE else View.VISIBLE
        holder.viewBind.tvDialBuiltIn.visibility=if(sport.buildIn) View.VISIBLE else View.GONE
        holder.viewBind.imgDelete.setOnClickListener {
            listener?.onItemDelete(holder.bindingAdapterPosition)
        }

    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    interface Listener {
        fun onItemDelete(position: Int)
    }

    class ItemViewHolder(val viewBind: ItemSportInstalledBinding) :
        RecyclerView.ViewHolder(viewBind.root)

}