package com.sjbt.sdk.sample.ui.device.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sjbt.sdk.sample.databinding.ItemContactsListBinding
import com.sjbt.sdk.sample.databinding.ItemPhoneContactsListBinding
import com.sjbt.sdk.sample.model.device.PhoneContact

class PhotoContactsAdapter : RecyclerView.Adapter<PhotoContactsAdapter.ItemViewHolder>() {

    var sources: ArrayList<PhoneContact>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemPhoneContactsListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val items = this.sources ?: return
        val item = items[position]
        holder.viewBind.tvName.text = item.name
        holder.viewBind.tvNumber.text = item.number
        holder.viewBind.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            item.checked = isChecked
        }
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    class ItemViewHolder(val viewBind: ItemPhoneContactsListBinding) :
        RecyclerView.ViewHolder(viewBind.root)
}