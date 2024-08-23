package com.sjbt.sdk.sample.ui.device.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.base.sdk.entity.apps.WmAlarm
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.databinding.ItemAlarmListBinding
import com.sjbt.sdk.sample.utils.FormatterUtil

class AlarmListAdapter() : RecyclerView.Adapter<AlarmListAdapter.ItemViewHolder>() {

    var listener: Listener? = null

    var sources: List<WmAlarm>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemAlarmListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val alarm = sources?.get(position) ?: return
        val context = holder.itemView.context

        if (AlarmHelper.is24HourFormat(context)) {
            holder.viewBind.tvAmPm.visibility = View.GONE
            holder.viewBind.tvTime.text = FormatterUtil.hmm(alarm.hour, alarm.minute)
        } else {
            holder.viewBind.tvAmPm.visibility = View.VISIBLE
            var hour = alarm.hour
            if (hour < 12) { //AM
                holder.viewBind.tvAmPm.setText(R.string.ds_alarm_am)
                if (hour == 0) {
                    hour = 12
                }
            } else {//PM
                holder.viewBind.tvAmPm.setText(R.string.ds_alarm_pm)
                if (hour > 12) {
                    hour -= 12
                }
            }
            holder.viewBind.tvTime.text = FormatterUtil.hmm(hour, alarm.minute)
        }
        holder.viewBind.tvLabel.text = alarm.alarmName
        holder.viewBind.tvRepeat.text = AlarmHelper.repeatToSimpleStr(alarm.repeatOptions,context)
        holder.viewBind.switchIsEnabled.setOnCheckedChangeListener(null)
        holder.viewBind.switchIsEnabled.isChecked = alarm.isOn
        holder.viewBind.switchIsEnabled.setOnCheckedChangeListener { button, isChecked -> //Copy the array, excluding the alarm to be deleted
            if (button.isPressed) {
                listener?.onItemModified(holder.bindingAdapterPosition, AlarmHelper.newAlarm(alarm).apply {
                    isOn = isChecked
                })
            }
        }
        holder.viewBind.imgDelete.setOnClickListener {
            listener?.onItemDelete(holder.bindingAdapterPosition)
        }
        holder.viewBind.layoutContent.setOnClickListener {
            listener?.onItemClick(holder.bindingAdapterPosition, alarm)
        }
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    interface Listener {
        fun onItemModified(position: Int, alarmModified: WmAlarm)
        fun onItemClick(position: Int, alarm: WmAlarm)
        fun onItemDelete(position: Int)
    }

    class ItemViewHolder(val viewBind: ItemAlarmListBinding) :
        RecyclerView.ViewHolder(viewBind.root)

}