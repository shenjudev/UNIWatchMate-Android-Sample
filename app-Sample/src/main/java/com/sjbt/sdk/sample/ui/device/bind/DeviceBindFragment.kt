package com.sjbt.sdk.sample.ui.device.bind

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.bertsir.zbar.Qr.ScanResult
import com.base.api.UNIWatchMate
import com.base.sdk.entity.BindType
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.common.WmTimeUnit
import com.github.kilnn.tool.dialog.prompt.PromptDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sjbt.sdk.sample.BuildConfig
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.DeviceManager
import com.sjbt.sdk.sample.databinding.FragmentDeviceBindBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.model.ScanStringParse
import com.sjbt.sdk.sample.ui.bind.DeviceConnectDialogFragment
import com.sjbt.sdk.sample.utils.*
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.CustomDividerItemDecoration
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import timber.log.Timber

/**
 * Scan and bind device.
 */
class DeviceBindFragment : BaseFragment(R.layout.fragment_device_bind),
    PromptDialogFragment.OnPromptListener, DeviceConnectDialogFragment.Listener {
    private /*const*/ val promptBindSuccessId = 1
    private var startSearch = false
    private val viewBind: FragmentDeviceBindBinding by viewBinding()
    private val applicationScope = Injector.getApplicationScope()
    private val userInfoRepository = Injector.getUserInfoRepository()
    private val bluetoothManager by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    /**
     * Avoid repeated requests for permissions at the same time
     */
    private var isRequestingPermission: Boolean = false

    private val deviceManager: DeviceManager = Injector.getDeviceManager()

    private val searchDevicesAdapter: SearchDevicesAdapter = SearchDevicesAdapter().apply {
        listener = object : SearchDevicesAdapter.Listener {
            override fun onItemClick(device: BlueToothDevice) {
                tryingBindSearchDevice(device)
            }

            override fun onItemSizeChanged(oldSize: Int, newSize: Int) {
                val animator = viewBind.layoutTips.animate()
                if (oldSize == 0 && newSize > 0) {
                    animator.cancel()
                    animator.setDuration(1000).alpha(0.1f).start()
                } else if (oldSize > 0 && newSize == 0) {
                    animator.cancel()
                    animator.setDuration(500).alpha(0.5f).start()
                }
            }
        }
    }

    private var bluetoothSnackbar: Snackbar? = null

    private fun tryingBindSearchDevice(device: BlueToothDevice) {
        this::class.simpleName?.let { Timber.i("address=${device.address} name=${device.name}") }
        val userInfo = userInfoRepository.flowCurrent.value
        userInfo?.let {
            val wmDevice = UNIWatchMate.connectBtDevice(
                WmBindInfo(
                    it.id.toString(),
                    it.name,
                    device.address,
                    BindType.DISCOVERY,
                    device.deviceType,
                    device.mode
                )
            )
            wmDevice?.let { wmDevice ->
                deviceManager.bind(
                    device.address, if (wmDevice.name.isNullOrEmpty()) {
                        UNKNOWN_DEVICE_NAME
                    } else {
                        wmDevice.name!!
                    }, device.mode
                )

                DeviceConnectDialogFragment().show(childFragmentManager, null)
            } ?: {

            }
        }
    }

    private fun isLegalMacAddress(address: String?): Boolean {
        return !TextUtils.isEmpty(address)
    }

    private fun tryingBindScanDevice(scanContent: String) {
        applicationScope.launchWithLog {
            runCatchingWithLog {
                this::class.simpleName?.let { Timber.tag(it).i("scanContent=$scanContent") }
                val userInfo = userInfoRepository.flowCurrent.value ?: return@launchWithLog
                val scanResult = parseScanContent(scanContent)
                val bindInfo = WmBindInfo(
                    userInfo.id.toString(),
                    userInfo.name,
                    scanResult.schemeMacAddress,
                    BindType.SCAN_QR,
                    scanResult.projectName,
                    WmDeviceModel.SJ_WATCH
                )
                bindInfo.randomCode = scanResult.randomCode
//                  deviceManager.delDevice()
                val wmDevice = UNIWatchMate.connectBtDevice(
                    bindInfo
                )
                if (wmDevice != null && wmDevice.mode != WmDeviceModel.NOT_REG) {
                    Timber.i("device=$wmDevice")
                    deviceManager.bind(
                        wmDevice.address!!, if (wmDevice.name.isNullOrEmpty()) {
                            UNKNOWN_DEVICE_NAME
                        } else {
                            wmDevice.name!!
                        }, wmDevice!!.mode
                    )
                    DeviceConnectDialogFragment().show(childFragmentManager, null)
                } else {
                    ToastUtil.showToast(getString(R.string.device_scan_tips_error))
                }
            }.onFailure {
                ToastUtil.showToast(it.message)
            }
        }
    }

    private fun parseScanContent(scanContent: String): ScanStringParse {
        val urlParams = scanContent.split("?")
        var model = WmDeviceModel.NOT_REG
        var randomCode = ""
        var projectName = ""
        var schemeMacAddress = ""
        if (urlParams.isNotEmpty() && urlParams.size >= 2) {
            val params = urlParams[1].split("&")
            model = WmDeviceModel.NOT_REG

            if (params.isNotEmpty() && params.size >= 3) {
                schemeMacAddress = params[0]
                projectName = params[1]
                randomCode = params[2]
                model = if ("OSW-802N" == projectName) {
                    WmDeviceModel.SJ_WATCH
                } else {
                    WmDeviceModel.NOT_REG
                }
            }
        }
        return ScanStringParse(randomCode, model, projectName, schemeMacAddress)
    }

    override fun onPromptCancel(promptId: Int, cancelReason: Int, tag: String?) {
        if (promptId == promptBindSuccessId) {
            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            if (!isRequestingPermission) {
                isRequestingPermission = true
                PermissionHelper.requestBle(this@DeviceBindFragment) {
                    isRequestingPermission = false
                }
            }
        }
        if (BuildConfig.DEBUG) {
//            viewBind.editFilter.visibility=View.VISIBLE
        }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_device_bind, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.menu_qr_code_scanner) {
                    PermissionHelper.requestAppCamera(this@DeviceBindFragment) {
                        findNavController().navigate(DeviceBindFragmentDirections.toCustomQr())
                    }
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)

//        viewLifecycle.addObserver(scannerHelper)
        viewBind.refreshLayout.setOnRefreshListener {
            //Clear data when using pull to refresh. This is a different strategy than fabScan click event
            if (!startSearch) {
                searchDevicesAdapter.clearItems()
                startDiscover()
            }
        }

        viewBind.scanDevicesRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        viewBind.scanDevicesRecyclerView.addItemDecoration(
            CustomDividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )
        viewBind.scanDevicesRecyclerView.adapter = searchDevicesAdapter

        viewBind.fabScan.setOnClickListener {
            if (!startSearch) {
                searchDevicesAdapter.clearItems()
                startDiscover()
                viewBind.refreshLayout.isRefreshing = true
            }
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowConnectorState.collect {
                    if (it == WmConnectState.BIND_SUCCESS) {
                        /**
                         * Show bind success, and exit in [onPromptCancel]
                         */
                        promptToast.showSuccess(
                            R.string.device_bind_success,
                            intercept = true,
                            promptId = promptBindSuccessId
                        )
                        toggleBluetoothAlert(false)
                    } else {
                        toggleBluetoothAlert(false)
                    }
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                launch {
                    flowLocationServiceState(requireContext()).collect { isEnabled ->
                        viewBind.layoutLocationService.isVisible = !isEnabled
//                        viewBind.recyclerDivider.isVisible = !isEnabled
                    }
                }
            } else {
                viewBind.layoutLocationService.isVisible = false
            }
        }
        viewBind.btnEnableLocationService.setOnClickListener {
            try {
                requireContext().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
        setFragmentResultListener(DEVICE_QR_CODE) { requestKey, bundle ->
            if (requestKey == DEVICE_QR_CODE) {
                val scanResult: ScanResult = bundle.getSerializable(EXTRA_SCAN_RESULT) as ScanResult
                scanResult?.let {
                    if (!it.getContent().isNullOrEmpty()) {
                        tryingBindScanDevice(it.content)
                    }
                }
            }
        }
    }

    private fun startDiscover() {
        startSearch = true
//       val filterTag= viewBind.editFilter.text.trim().toString()
        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.startDiscovery(
                    30000,
                    WmTimeUnit.MILLISECONDS,
                    WmDeviceModel.SJ_WATCH,
                    "oraimo"
                )?.asFlow()
                    ?.catch {
                        this::class.simpleName?.let { tag ->
                            Timber.e("startDiscovery error ${it.message}")
                        }
                        ToastUtil.showToast(it.message, true)
                    }?.onCompletion {
                        this::class.simpleName?.let { tag ->
                            Timber.e("startDiscover onCompletion")
                        }
                        viewBind.refreshLayout.isRefreshing = false
                        startSearch = false
                    }?.collect {
                        this::class.simpleName?.let { it1 -> Timber.tag(it1).i(it.toString()) }
                        searchDevicesAdapter.newScanResult(it, WmDeviceModel.SJ_WATCH, "OSW-802N")
                    }
            }
        }
    }

    private fun toggleBluetoothAlert(show: Boolean) {
        if (show) {
            val snackbar =
                bluetoothSnackbar ?: createBluetoothSnackbar().also { bluetoothSnackbar = it }
            if (!snackbar.isShownOrQueued) {
                snackbar.show()
            }
        } else {
            bluetoothSnackbar?.dismiss()
        }
    }

    private fun createBluetoothSnackbar(): Snackbar {
        val snackbar = Snackbar.make(
            viewBind.root,
            R.string.device_state_bt_disabled,
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction(R.string.action_turn_on) {
            PermissionHelper.requestBle(this) { granted ->
                if (granted) {
                    requireContext().startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
            }
        }
        return snackbar
    }

    override fun onStop() {
        super.onStop()
        bluetoothSnackbar?.dismiss()
    }

    override fun navToConnectHelp() {
        findNavController().navigate(DeviceBindFragmentDirections.toConnectHelp())
    }

    override fun navToBgRunSettings() {
        findNavController().navigate(DeviceBindFragmentDirections.toBgRunSettings())
    }

    companion object {
        const val DEVICE_QR_CODE = "device_qr_code"
        const val EXTRA_ADDRESS = "address"
        const val EXTRA_SCAN_RESULT = "scan_result"
        const val EXTRA_NAME = "name"
        const val UNKNOWN_DEVICE_NAME = "Unknown"
    }
}

class ScanErrorDelayDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.device_scan_tips_error)
            .setMessage(R.string.device_scan_tips_delay)
            .setPositiveButton(R.string.tip_i_know, null)
            .create()
    }
}

class ScanErrorRestartDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.device_scan_tips_error)
            .setMessage(R.string.device_scan_tips_restart)
            .setPositiveButton(R.string.tip_i_know, null)
            .create()
    }
}