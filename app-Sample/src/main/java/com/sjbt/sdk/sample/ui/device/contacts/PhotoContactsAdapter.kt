package com.sjbt.sdk.sample.ui.device.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.databinding.ItemPhoneContactsListBinding
import com.sjbt.sdk.sample.model.device.PhoneContact
import com.sjbt.sdk.sample.utils.ToastUtil

class PhotoContactsAdapter(val contactNum: Int) : RecyclerView.Adapter<PhotoContactsAdapter.ItemViewHolder>() {

    var sources: ArrayList<PhoneContact>? = null
    var selectCount = 0
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
            if (buttonView.isPressed) {
                if (isChecked) {
                    if (selectCount + contactNum >= 100) {
                        holder.viewBind.checkBox.isChecked = false
                        ToastUtil.showToast(MyApplication.instance.resources.getString(R.string.ds_contacts_tips1))
                        return@setOnCheckedChangeListener
                    }else{
                        selectCount++
                    }
                }else{
                    selectCount--
                }
                item.checked = isChecked
            }

        }
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    class ItemViewHolder(val viewBind: ItemPhoneContactsListBinding) :
        RecyclerView.ViewHolder(viewBind.root)
}