package com.sjbt.sdk.sample.ui.device.dial.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.sjbt.sdk.sample.databinding.ItemDialLibraryBinding
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.utils.glideShowMipmapImage

class DialLibraryAdapter : RecyclerView.Adapter<DialLibraryAdapter.DialLibraryViewHolder>() {

    //Create a Shape by default. To avoid error

    var listener: Listener? = null

    var items: List<DialMock>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialLibraryViewHolder {
        return DialLibraryViewHolder(
            ItemDialLibraryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DialLibraryViewHolder, position: Int) {
        val items = this.items ?: return
        val item = items[position]
        holder.viewBind.tvStatus.visibility=if(item.installed==1) View.VISIBLE else View.GONE
        holder.itemView.clickTrigger {
            listener?.onItemClick(item)
        }
        glideShowMipmapImage(holder.viewBind.imgView, item.dialCoverRes)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class DialLibraryViewHolder(val viewBind: ItemDialLibraryBinding) : RecyclerView.ViewHolder(viewBind.root)

    interface Listener {
        fun onItemClick(packet: DialMock)
    }
}