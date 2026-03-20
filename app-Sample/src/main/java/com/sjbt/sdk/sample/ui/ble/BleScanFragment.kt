package com.sjbt.sdk.sample.ui.ble

import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.lensmoo.business.ui.device.OtaDemobleActivity
import androidx.core.util.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.scan.ScanResult
import com.polidea.rxandroidble3.scan.ScanSettings
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentBleScanBinding
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.widget.CustomDividerItemDecoration
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 使用 RxAndroidBle 扫描并展示 BLE 设备列表。
 * @see <a href="https://github.com/dariuszseweryn/RxAndroidBle">RxAndroidBle</a>
 */
class BleScanFragment : BaseFragment(R.layout.fragment_ble_scan) {

    companion object {
        private const val TAG = "BleScan"
        private const val PREFS_NAME = "ble_scan_prefs"
        private const val KEY_CONNECTED_ADDRESS = "connected_device_address"
        private const val KEY_CONNECTED_NAME = "connected_device_name"
        // 2.6.1 APP向设备发送数据 / 2.6.2 设备向APP发送数据

//        private val CHAR_UUID_ERROR = UUID.fromString("0000FFF3-0000-1000-8000-00805F9B34FB")
    }

    private val prefs by lazy { requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    private lateinit var viewBind: FragmentBleScanBinding

    private val rxBleClient: RxBleClient by lazy {
        RxBleClient.create(requireContext())
    }

    private val adapter = BleScanListAdapter { device ->
        val canEnterOta = device.isConnected &&
            (device.connectionStatus == "connected" || device.connectionStatus == null)
        if (canEnterOta) {
            startActivity(Intent(requireContext(), OtaDemobleActivity::class.java).apply {
                putExtra(OtaDemobleActivity.EXTRA_DEVICE_ADDRESS, device.address)
                putExtra(OtaDemobleActivity.EXTRA_DEVICE_NAME, device.name)
            })
        } else {
            connectToDevice(device)
        }
    }

    /** 当前连接订阅，点击新设备或 onStop 时 dispose */
    private var connectionDisposable: Disposable? = null

    /** 已连接成功的设备（GATT 服务与特征均获取到），显示在列表最上面 */
    private var connectedDevice: BleScanDevice? = null

    /** 仅显示厂商 ID 低字节为 0xA0 或 0xC0 的设备（getManufacturerSpecificData 的 key） */
    private val MANUFACTURER_ID_FIRST_BYTES = setOf(0xA0, 0xC0)

    /** 按 mac 去重，保留最新 rssi 的扫描结果 */
    private val deviceMap = ConcurrentHashMap<String, BleScanDevice>()
    private var scanDisposable: Disposable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind = FragmentBleScanBinding.bind(view)
        requireActivity().title = getString(R.string.ble_scan_title)

        viewBind.recyclerBleDevices.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewBind.recyclerBleDevices.addItemDecoration(
                CustomDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )
        viewBind.recyclerBleDevices.adapter = adapter

        // 恢复上次已连接成功的设备，第二次进入时显示在列表顶部
        loadConnectedDeviceFromPrefs()
        refreshDeviceList()

        viewBind.refreshLayout.setOnRefreshListener {
            if (scanDisposable?.isDisposed != true) {
                stopScan()
            }
            deviceMap.clear()
            adapter.submitList(emptyList())
            viewBind.tvEmptyTips.visibility = View.VISIBLE
            viewBind.tvEmptyTips.text = getString(R.string.ble_scan_connect)
            viewBind.recyclerBleDevices.visibility = View.GONE
            startScan()
        }

        PermissionHelper.requestBle(this) { granted ->
            if (granted) {
                tryReconnectIfNeeded()
                startScan()
            } else {
                viewBind.tvEmptyTips.text = "需要蓝牙和定位权限才能搜索设备"
                viewBind.refreshLayout.isRefreshing = false
            }
        }
    }

    /**
     * 若有已保存的已连接设备，直接发起重连（不依赖 observeConnectionStateChanges，
     * 因进程内未连接过时可能不发射，导致重连从不触发）。
     */
    private fun tryReconnectIfNeeded() {
        val device = connectedDevice ?: return
        Log.d(TAG, "BLE 自动重连: ${device.name} [${device.address}]")
        connectedDevice = connectedDevice?.copy(connectionStatus = "connecting")
        refreshDeviceList()
        connectToDevice(device, fromReconnect = true)
    }

    private fun startScan() {
        viewBind.refreshLayout.isRefreshing = true
        viewBind.tvEmptyTips.visibility = View.VISIBLE
        viewBind.tvEmptyTips.text = "正在搜索 BLE 设备…"

        scanDisposable = rxBleClient.scanBleDevices(
                ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build()
        )
                .take(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result: ScanResult ->
                            if (!hasManufacturerIdFirstByte(result, MANUFACTURER_ID_FIRST_BYTES)) return@subscribe

                            logScanRecord(result)
                            val device = result.bleDevice
                            val mac = device.macAddress
                            val name = device.name
                            val rssi = result.rssi
                            deviceMap[mac] = BleScanDevice(mac, name, rssi)
                            refreshDeviceList()
                            viewBind.tvEmptyTips.visibility = if (deviceMap.isEmpty()) View.VISIBLE else View.GONE
                            viewBind.recyclerBleDevices.visibility = if (deviceMap.isEmpty()) View.GONE else View.VISIBLE
                        },
                        { t ->
                            Timber.e(t, "BLE scan error")
                            viewBind.refreshLayout.isRefreshing = false
                            viewBind.tvEmptyTips.text = "扫描出错: ${t.message}"
                        },
                        {
                            viewBind.refreshLayout.isRefreshing = false
                            viewBind.tvEmptyTips.text = if (deviceMap.isEmpty()) "未发现 BLE 设备" else null
                            if (deviceMap.isNotEmpty()) viewBind.tvEmptyTips.visibility = View.GONE
                        }
                )
    }

    private fun stopScan() {
        scanDisposable?.dispose()
        scanDisposable = null
        viewBind.refreshLayout.isRefreshing = false
    }

    /** 连接真正成功时持久化，第二次进入页面时恢复显示 */
    private fun saveConnectedDeviceToPrefs(address: String, name: String?) {
        prefs.edit().putString(KEY_CONNECTED_ADDRESS, address).putString(KEY_CONNECTED_NAME, name ?: "").apply()
    }

    private fun loadConnectedDeviceFromPrefs() {
        val address = prefs.getString(KEY_CONNECTED_ADDRESS, null) ?: return
        if (address.isBlank()) return
        val name = prefs.getString(KEY_CONNECTED_NAME, null).takeIf { !it.isNullOrBlank() }
        connectedDevice = BleScanDevice(address, name, 0, isConnected = true, connectionStatus = "reconnecting")
    }

    /**
     * 刷新列表：已连接设备在最上面，其余为扫描结果按 rssi 排序。
     */
    private fun refreshDeviceList() {
        val scanned = deviceMap.values
            .filter { connectedDevice == null || it.address != connectedDevice!!.address }
            .sortedByDescending { it.rssi }
        val list = (connectedDevice?.let { listOf(it) } ?: emptyList()) + scanned
        adapter.submitList(list)
    }

    /**
     * 检查 GATT 是否包含指定服务和三个特征。
     */
    private fun hasRequiredServiceAndCharacteristics(services: List<BluetoothGattService>): Boolean {
        val service = services.find { it.uuid == BleConnectionHolder.SERVICE_UUID } ?: return false
        val chars = service.characteristics ?: return false
        val charUuids = chars.map { it.uuid }.toSet()
        return charUuids.contains(BleConnectionHolder.CHAR_UUID_APP_TO_DEVICE) &&
                charUuids.contains(BleConnectionHolder.CHAR_UUID_DEVICE_TO_APP)
//                &&
//                charUuids.contains(CHAR_UUID_ERROR)
    }

    /**
     * 点击设备后连接该 BLE 设备；连接后发现 GATT 服务与特征，全部获取到后才视为真正成功并记住设备。
     * @param fromReconnect true 表示由进入页面时的自动重连触发，成功仅更新为「已连接」不跳转 OTA
     */
    private fun connectToDevice(device: BleScanDevice, fromReconnect: Boolean = false) {
        connectionDisposable?.dispose()
        val name = device.name ?: "Unknown"
        val mac = device.address
        if (connectedDevice?.address == mac) {
            connectedDevice = connectedDevice?.copy(connectionStatus = "connecting")
            refreshDeviceList()
        }
        Log.d(TAG, "BLE 开始连接: $name [$mac] fromReconnect=$fromReconnect")
        ToastUtil.showToast("正在连接 $name…")
        val rxBleDevice = rxBleClient.getBleDevice(mac)
        connectionDisposable = rxBleDevice.establishConnection(false)
                .flatMapSingle { connection ->
                    BleConnectionHolder.set(mac, connection)
                    connection.discoverServices()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { rxBleDeviceServices ->
                            val services = rxBleDeviceServices.getBluetoothGattServices()
                            if (hasRequiredServiceAndCharacteristics(services)) {
                                Log.d(TAG, "BLE GATT 服务与特征获取成功: $name [$mac]")
                                connectedDevice = BleScanDevice(mac, name, device.rssi, isConnected = true, connectionStatus = "connected")
                                saveConnectedDeviceToPrefs(mac, name)
                                refreshDeviceList()
                                ToastUtil.showToast("连接成功: $name")
                                if (!fromReconnect) {
                                    startActivity(Intent(requireContext(), OtaDemobleActivity::class.java).apply {
                                        putExtra(OtaDemobleActivity.EXTRA_DEVICE_ADDRESS, mac)
                                        putExtra(OtaDemobleActivity.EXTRA_DEVICE_NAME, name)
                                    })
                                }
                            } else {
                                Log.w(TAG, "BLE GATT 未包含所需服务/特征: $name [$mac]")
                                if (connectedDevice?.address == mac) {
                                    connectedDevice = connectedDevice?.copy(connectionStatus = "failed")
                                    refreshDeviceList()
                                }
                                ToastUtil.showToast("设备无所需 GATT 服务")
                            }
                        },
                        { t ->
                            Log.e(TAG, "BLE 连接/发现服务失败: $name [$mac] ${t.message}", t)
                            if (connectedDevice?.address == mac) {
                                connectedDevice = connectedDevice?.copy(connectionStatus = "failed")
                                refreshDeviceList()
                            }
                            ToastUtil.showToast("连接失败: ${t.message}")
                        }
                )
    }

    /**
     * 使用 getManufacturerSpecificData() 判断是否显示：是否存在厂商 ID 低字节在 allowedFirstBytes 中的厂商数据。
     * 对应 iOS 的 kCBAdvDataManufacturerData。
     */
    private fun hasManufacturerIdFirstByte(result: ScanResult, allowedFirstBytes: Set<Int>): Boolean {
        val scanRecord = result.scanRecord ?: return false
        val manufacturerData = scanRecord.manufacturerSpecificData ?: return false
        for (i in 0 until manufacturerData.size()) {
            val key = manufacturerData.keyAt(i)
            if ((key and 0xFF) in allowedFirstBytes) return true
        }
        return false
    }

    /**
     * 打印 getManufacturerSpecificData() 的 key 及广播数据，便于调试。使用 Log.d 确保在 release 下也能看到。
     */
    private fun logScanRecord(result: ScanResult) {
        val scanRecord = result.scanRecord ?: return
        val bytes = scanRecord.bytes ?: return
        val manufacturerData = scanRecord.manufacturerSpecificData
        val mac = result.bleDevice.macAddress
        val name = result.bleDevice.name ?: ""
        // 广播数据：完整 hex
        val hexBroadcast = bytes.joinToString(" ") { b -> "%02X".format(b.toInt() and 0xFF) }
//        Log.d(TAG, "BLE [$mac] $name rssi=${result.rssi} 广播数据(hex) len=${bytes.size}: $hexBroadcast")
        // getManufacturerSpecificData 的 key（厂商 ID）与 value
        if (manufacturerData != null && manufacturerData.size() > 0) {
            val keys = (0 until manufacturerData.size()).map { manufacturerData.keyAt(it) }
//            Log.d(TAG, "BLE [$mac] getManufacturerSpecificData keys: $keys")
            manufacturerData.forEach { key, value ->
                val valueHex = value.joinToString(" ") { b -> "%02X".format(b.toInt() and 0xFF) }
//                Log.d(TAG, "BLE [$mac] manufacturerData key=0x${"%04X".format(key)} (低字节=0x${"%02X".format(key and 0xFF)}) value=$valueHex")
            }
        } else {
//            Log.d(TAG, "BLE [$mac] getManufacturerSpecificData: null 或 空")
        }
    }

    override fun onStop() {
        super.onStop()
        stopScan()
        // 不要在此处 dispose connectionDisposable：跳转到全屏 OtaDemobleActivity 时
        // Fragment 会 onStop，若此处断开 GATT，OTA 页拿到的 BleConnectionHolder 连接已失效，
        // setupNotification 会报 BleDisconnectedException。连接仅在换设备连接或 onDestroy 时释放。
    }

    override fun onDestroy() {
        connectionDisposable?.dispose()
        connectionDisposable = null
        BleConnectionHolder.clear()
        super.onDestroy()
    }
}
