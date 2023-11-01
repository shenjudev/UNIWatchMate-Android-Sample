package com.sjbt.sdk.sample.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.databinding.FragmentSyncBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/05.Sync-Data
 *
 * ***Description**
 * Show how to sync data, observer sync state, save sync data
 *
 * **Usage**
 * 1. [SyncFragment]
 * Observer sync state and display available data types
 *
 * 2. [DeviceManager]
 * Execute [FcDataFeature.syncData] and emit [FcDataFeature.observerSyncState]
 *
 * 3. [SyncDataRepository]
 * Save sync data
 *
 * 4. [StepFragment]
 * Display step data
 *
 * 5. [SleepFragment]
 * Display sleep data
 *
 * 6. [HeartRateFragment]
 * Display heart rate data
 *
 * 7. [OxygenFragment]
 * Display oxygen data
 *
 * 8. [BloodPressureFragment]
 * Display blood pressure data
 *
 * 9. [TemperatureFragment]
 * Display temperature data
 *
 * 10. [PressureFragment]
 * Display pressure data
 *
 * 11. [EcgFragment]
 * Display ECG data
 *
 * 12. [SportFragment]
 * Display sport data
 *
 * 13. [GameFragment]
 * Display game data
 */
class SyncFragment : BaseFragment(R.layout.fragment_sync) {

    private val viewBind: FragmentSyncBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.refreshLayout.setOnRefreshListener {
            deviceManager.syncData()
        }
        viewBind.refreshLayout.setOnRefreshListener {
        }
        viewBind.itemStep.clickTrigger(block = blockClick)
        viewBind.itemSleep.clickTrigger(block = blockClick)
        viewBind.itemHeartRate.clickTrigger(block = blockClick)
        viewBind.itemOxygen.clickTrigger(block = blockClick)
        viewBind.itemSport.clickTrigger(block = blockClick)

        viewLifecycle.launchRepeatOnStarted {
//            launch {
//                deviceManager.flowSyncState.collect { state ->
//                    if (state == null || state == FcSyncState.SUCCESS) {//refresh none or success
//                        viewBind.refreshLayout.isRefreshing = false
//                        viewBind.tvRefreshState.setText(R.string.sync_state_idle)
//                    } else if (state < 0) {//refresh fail
//                        viewBind.refreshLayout.isRefreshing = false
//                        viewBind.tvRefreshState.setText(R.string.sync_state_idle)
//                    } else {//refresh progress
//                        viewBind.refreshLayout.isRefreshing = true
//                        viewBind.tvRefreshState.text = getString(R.string.sync_state_process, state)
//                    }
//                }
//            }
//            launch {
//                deviceManager.flowSyncEvent.collectLatest {
//                    when (it) {
//                        DeviceManager.SyncEvent.SUCCESS -> {
//                            promptToast.showSuccess(R.string.sync_data_success)
//                        }
//                        DeviceManager.SyncEvent.FAIL_DISCONNECT -> {
//                            promptToast.showFailed(R.string.device_state_disconnected)
//                        }
//                        DeviceManager.SyncEvent.FAIL -> {
//                            promptToast.showFailed(R.string.sync_data_failed)
//                        }
//                        else -> {
//                            promptToast.dismiss()
//                        }
//                    }
//                }
//            }
//        }
        }
    }

    val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStep -> {
                findNavController().navigate(SyncFragmentDirections.toStep())
            }

            viewBind.itemSleep -> {
              findNavController().navigate(SyncFragmentDirections.toSleep())
            }

            viewBind.itemHeartRate -> {
                findNavController().navigate(SyncFragmentDirections.toHeartRateHomePage())
            }

            viewBind.itemOxygen -> {
                findNavController().navigate(SyncFragmentDirections.toOxygen())
            }

            viewBind.itemSport -> {
                findNavController().navigate(SyncFragmentDirections.toSport())
            }
        }
    }
}
