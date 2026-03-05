package com.sjbt.sdk.sample.ui.ble

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sjbt.sdk.sample.databinding.ItemScanDeviceBinding

/**
 * BLE 扫描结果项（仅展示，与 UNIWatchMate 绑定无关）
 * @param isConnected 是否为已连接成功的设备（显示在列表顶部）
 * @param connectionStatus 连接状态：reconnecting=重连中，connecting=连接中，connected=已连接，failed=连接失败，null=正常/已连接
 */
data class BleScanDevice(
    val address: String,
    val name: String?,
    val rssi: Int,
    val isConnected: Boolean = false,
    val connectionStatus: String? = null
)

class BleScanListAdapter(
    private val onItemClick: (BleScanDevice) -> Unit
) : ListAdapter<BleScanDevice, BleScanListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScanDeviceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(private val viewBind: ItemScanDeviceBinding) :
        RecyclerView.ViewHolder(viewBind.root) {

        fun bind(item: BleScanDevice, onItemClick: (BleScanDevice) -> Unit) {
            viewBind.tvName.text = item.name?.takeIf { it.isNotBlank() } ?: "Unknown"
            viewBind.tvAddress.text = item.address
            val statusText = when (item.connectionStatus) {
                "reconnecting", "connecting" -> "重连中…"
                "failed" -> "连接失败"
                else -> if (item.isConnected) "已连接" else null
            }
            if (statusText != null) {
                viewBind.tvRssi.text = statusText
                viewBind.signalView.visibility = android.view.View.GONE
            } else {
                viewBind.tvRssi.text = "${item.rssi}"
                viewBind.signalView.visibility = android.view.View.VISIBLE
                viewBind.signalView.setMaxSignal(4)
                viewBind.signalView.setCurrentSignal(rssiToLevel(item.rssi))
                viewBind.signalView.invalidate()
            }
            viewBind.root.setOnClickListener { onItemClick(item) }
        }

        private fun rssiToLevel(rssi: Int): Int = when {
            rssi < -70 -> 1
            rssi < -60 -> 2
            rssi < -50 -> 3
            else -> 4
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<BleScanDevice>() {
        override fun areItemsTheSame(a: BleScanDevice, b: BleScanDevice) =
            a.address == b.address
        override fun areContentsTheSame(a: BleScanDevice, b: BleScanDevice) = a == b
    }
}
