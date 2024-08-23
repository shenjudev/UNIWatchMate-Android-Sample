package com.sjbt.sdk.sample.ui.bind

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.base.sdk.entity.apps.WmConnectState
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.AsyncEvent
import com.sjbt.sdk.sample.base.AsyncViewModel
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.SingleAsyncState
import com.sjbt.sdk.sample.data.device.DeviceManager
import com.sjbt.sdk.sample.databinding.DialogDeviceConnectBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.sjbt.sdk.sample.utils.promptToast
import com.sjbt.sdk.sample.utils.promptProgress
import com.sjbt.sdk.sample.utils.showFailed
import kotlinx.coroutines.delay

class DeviceConnectDialogFragment : AppCompatDialogFragment() {

    interface Listener {
        fun navToConnectHelp()
        fun navToBgRunSettings()
    }

    private val promptToast by promptToast()
    private val promptProgress by promptProgress()

    private val deviceManager: DeviceManager = Injector.getDeviceManager()

    private var _viewBind: DialogDeviceConnectBinding? = null
    private val viewBind get() = _viewBind!!
    private val viewModel by viewModels<DeviceConnectViewMode>()

    private var timberJob: Job? = null
    private var wmConnectState: WmConnectState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            //Required permissions
            PermissionHelper.requestBleConnect(this@DeviceConnectDialogFragment) { granted ->
                if (!granted) {
                    deviceManager.cancelBind()
                    dismiss()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBind = DialogDeviceConnectBinding.inflate(layoutInflater)

        _viewBind?.ivBack!!.setOnClickListener {
            dismiss()
        }


        lifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    if (it.async is Loading) {
                        promptProgress.showProgress(R.string.tip_please_wait)
                    } else {
                        promptProgress.dismiss()
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is AsyncEvent.OnFail -> promptToast.showFailed(it.error)
                        is AsyncEvent.OnSuccess<*> -> {
                            //Unbind success and dismiss
                            dismiss()
                        }
                    }
                }
            }
            launch {
                deviceManager.flowDevice?.collect {
                    if (it != null) {
                        viewBind.tvName.text = it.name
                        viewBind.tvAddress.text = it.address
                        isCancelable = !it.isTryingBind
                        if (it.isTryingBind) {
                            viewBind.btnUnbind.setText(R.string.device_cancel_bind)
                            viewBind.btnUnbind.setOnClickListener {
                                //Cancel bind and exit
                                deviceManager.cancelBind()
                                dismissAllowingStateLoss()
                            }
                        } else {
                            viewBind.btnUnbind.setText(R.string.device_unbind)
                            viewBind.btnUnbind.setOnClickListener {
                                lifecycleScope.launchWhenResumed {
                                    UnbindConfirmDialogFragment().showNow(
                                        childFragmentManager,
                                        null
                                    )
                                }
                            }
                        }
                    }
                }
            }
            launch {
                deviceManager.flowConnectorState.collect {
                    timberJob?.cancel()
                    wmConnectState = it
                    when (it) {

                        WmConnectState.DISCONNECTED -> {
                            viewBind.tvState.setText(R.string.device_state_disconnected)
                            viewBind.progressDotView.setFailed()
                            showDisconnectedReason()
                        }

                        WmConnectState.CONNECTING -> {
                            timberJob = lifecycleScope.launch {
                                val seconds = 10
                                viewBind.tvState.text = getString(R.string.device_state_connecting) + " ${seconds}s"
                                if (seconds > 0) {
                                    repeat(seconds) { times ->
                                        delay(1000)
                                        viewBind.tvState.text =getString(R.string.device_state_connecting) +  " ${seconds - times - 1}s"
                                    }
                                }
                            }
                            viewBind.progressDotView.setFailed()
                            showConnectingTips()
                        }
                        WmConnectState.CONNECTED -> {
                            viewBind.tvState.setText(getString(R.string.device_state_connected) + getString(
                                R.string.binding
                            ))
                            viewBind.progressDotView.setLoading()
                            showConnectingTips()
                        }
                        WmConnectState.BIND_SUCCESS -> {
                            viewBind.tvState.setText(R.string.device_state_verified)
                            viewBind.progressDotView.setSuccess()
                            showBgRunSettings()
                        }

                        else -> {

                        }
                    }
                }
            }
        }
        extraNormalColor = viewBind.tvExtraMsg.textColors
        extraErrorColor =
            MaterialColors.getColor(viewBind.root, com.google.android.material.R.attr.colorError)
        viewBind.tvUnableToConnect.setOnClickListener {
            (parentFragment as? Listener)?.navToConnectHelp()
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setView(viewBind.root)
            .create()
    }

    private lateinit var extraNormalColor: ColorStateList
    private var extraErrorColor: Int = 0

    private fun showBtDisabled() {
        viewBind.layoutConnecting.isVisible = false
        viewBind.layoutAction.isVisible = true

        viewBind.tvExtraMsg.setTextColor(extraErrorColor)
        viewBind.tvExtraMsg.setText(R.string.device_state_bt_disabled)
        viewBind.btnAction.setText(R.string.action_turn_on)
        viewBind.btnAction.setOnClickListener {
            requireContext().startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun showDisconnectedReason() {
        viewBind.layoutConnecting.isVisible = false
        viewBind.layoutAction.isVisible = false
    }

    private fun showConnectingTips() {
        viewBind.layoutConnecting.isVisible = true
        viewBind.layoutAction.isVisible = false
    }

    private fun showBgRunSettings() {
        viewBind.layoutConnecting.isVisible = false
        viewBind.layoutAction.isVisible = true

        viewBind.tvExtraMsg.setTextColor(extraNormalColor)
        viewBind.tvExtraMsg.setText(R.string.device_connect_bg_run_settings)
        viewBind.btnAction.setText(R.string.action_to_set)
        viewBind.btnAction.setOnClickListener {
            (parentFragment as? Listener)?.navToBgRunSettings()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBind = null
    }

    override fun onStop() {
        super.onStop()
        timberJob?.cancel()
    }

    companion object {
        private const val TAG = "DeviceConnectDialog"
    }
}

class DeviceConnectViewMode : AsyncViewModel<SingleAsyncState<Unit>>(SingleAsyncState()) {

    private val deviceManager: DeviceManager = Injector.getDeviceManager()

    fun unbind() {
        suspend {
            deviceManager.reset()
        }.execute(SingleAsyncState<Unit>::async)
        {
            copy(async = it)
        }
    }

}

class UnbindConfirmDialogFragment : AppCompatDialogFragment() {

    private val viewModel by viewModels<DeviceConnectViewMode>({ requireParentFragment() })

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.tip_prompt)
            .setMessage(R.string.device_unbind_confirm_msg)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
//
                viewModel.unbind()
            }
        return builder.create()
    }
}