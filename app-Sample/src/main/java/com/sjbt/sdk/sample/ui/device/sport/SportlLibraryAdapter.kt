package com.sjbt.sdk.sample.ui.device.sport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.base.sdk.entity.apps.WmSport
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.databinding.ItemDialLibraryBinding
import com.sjbt.sdk.sample.databinding.ItemSportLibraryBinding
import com.sjbt.sdk.sample.model.LocalSportLibrary
import java.util.Locale

class SportlLibraryAdapter : RecyclerView.Adapter<SportlLibraryAdapter.DialLibraryViewHolder>() {

    //Create a Shape by default. To avoid error

    var listener: Listener? = null

    var items: List<LocalSportLibrary.LocalSport>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialLibraryViewHolder {
        return DialLibraryViewHolder(
            ItemSportLibraryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DialLibraryViewHolder, position: Int) {
        val items = this.items ?: return
        val item = items[position]
        holder.viewBind.tvName.text=getName(item.names)
        holder.viewBind.tvStatus.visibility=if(item.installed) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            listener?.onItemClick(item,position)
        }
    }

    private fun getName(names: HashMap<String, String>): String {
        val locale = MyApplication.instance.resources.configuration.locale;
        val language = locale.language;
        if (names.contains(language)) {
            return names[language]?:""
        }
        val iterator=  names.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            if (entity.key.lowercase() == language.lowercase()||entity.key.lowercase() == locale.toLanguageTag().lowercase()) {
                return entity.value
            }
        }
        return names["en"]?:""
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class DialLibraryViewHolder(val viewBind: ItemSportLibraryBinding) : RecyclerView.ViewHolder(viewBind.root)

    interface Listener {
        fun onItemClick(packet: LocalSportLibrary.LocalSport,pos:Int)
    }
}