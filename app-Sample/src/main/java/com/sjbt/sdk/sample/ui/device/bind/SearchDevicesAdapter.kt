package com.sjbt.sdk.sample.ui.device.bind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.common.WmDiscoverDevice
import com.sjbt.sdk.sample.databinding.ItemScanDeviceBinding
import kotlin.math.abs

class SearchDevicesAdapter : RecyclerView.Adapter<SearchDevicesAdapter.DeviceViewHolder>() {

    private val sorter = SortedList(BlueToothDevice::class.java, object : SortedListAdapterCallback<BlueToothDevice>(this) {
        override fun compare(o1: BlueToothDevice, o2: BlueToothDevice): Int {
            return o2.rssi.compareTo(o1.rssi)
        }

        override fun areContentsTheSame(oldItem: BlueToothDevice, newItem: BlueToothDevice): Boolean {
            return oldItem.name == newItem.name && oldItem.rssi == newItem.rssi
        }

        override fun areItemsTheSame(item1: BlueToothDevice, item2: BlueToothDevice): Boolean {
            return item1.address == item2.address
        }
    })

    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            ItemScanDeviceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(sorter[position])
        holder.viewBind.root.setOnClickListener {
            listener?.onItemClick(sorter[holder.bindingAdapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return sorter.size()
    }

    fun newScanResult(result: WmDiscoverDevice, sjWatch: WmDeviceModel,deviceType:String) {
        /**
         * ToNote:The data in [SortedList] is sorted, so the [SortedList.indexOf] method uses binary search to improve efficiency.
         * Unfortunately, this only works if the primary keys match the sort keys.That is, the [SortedListAdapterCallback.areItemsTheSame] method and [SortedListAdapterCallback.compare] need to maintain consistency.
         * We use [BlueToothDevice.address] as primary key. And [BlueToothDevice.rssi] as sort key. So never use [SortedList.indexOf] to find a item.
         */
        var existIndex = SortedList.INVALID_POSITION
        for (i in 0 until sorter.size()) {
            if (result.device.address == sorter[i].address) {
                existIndex = i
                break
            }
        }
        val exist = if (existIndex != SortedList.INVALID_POSITION) {
            sorter.get(existIndex)
        } else {
            null
        }
        if (exist != null) {
            //If it exists, then update the rssi and the name that may change
            //ToNote:In rare cases, the name may change
            val nameChanged = exist.name != result.device.name && !result.device.name.isNullOrEmpty()
            //ToNote:Not updated when the rssi difference is small. This is to avoid frequent drawing of View when there are a large number of devices around
            val rssiChanged = abs(exist.rssi - 10) > 5
            if (nameChanged || rssiChanged) {
                exist.name = result.device.name
                exist.rssi = result.rss.toInt()
                exist.mode = sjWatch
                exist.deviceType = deviceType
                sorter.recalculatePositionOfItemAt(existIndex)
            }
        } else {
            val oldSize = sorter.size()
            //If it does not exist, then add
            sorter.add(BlueToothDevice(result.device.address, result.device.name, result.rss.toInt(),sjWatch,deviceType))
            listener?.onItemSizeChanged(oldSize, oldSize + 1)
        }
    }

    fun clearItems() {
        val oldSize = sorter.size()
        sorter.clear()
        listener?.onItemSizeChanged(oldSize, 0)
    }

    interface Listener {
        fun onItemClick(device: BlueToothDevice)
        fun onItemSizeChanged(oldSize: Int, newSize: Int)
    }

    class DeviceViewHolder(val viewBind: ItemScanDeviceBinding) : RecyclerView.ViewHolder(viewBind.root) {
        fun bind(result: BlueToothDevice) {
            viewBind.tvName.text = if (result.name.isNullOrEmpty()) {
                DeviceBindFragment.UNKNOWN_DEVICE_NAME
            } else {
                result.name
            }
            viewBind.tvAddress.text = result.address
            viewBind.tvRssi.text = "${result.rssi}"
            viewBind.signalView.setMaxSignal(4)
            viewBind.signalView.setCurrentSignal(getRssiLevel(result.rssi))
            viewBind.signalView.invalidate()
        }

        private fun getRssiLevel(rssi: Int): Int {
            return when {
                rssi < -70 -> 1
                rssi < -60 -> 2
                rssi < -50 -> 3
                else -> 4
            }
        }
    }

}

class BlueToothDevice(
    val address: String,
    var name: String?,
    var rssi: Int,
    var mode: WmDeviceModel,
    var deviceType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlueToothDevice

        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

    override fun toString(): String {
        return "ScanDevice(address='$address', name=$name, rssi=$rssi, mode=$mode)"
    }

}