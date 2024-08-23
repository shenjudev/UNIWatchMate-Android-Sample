package com.sjbt.sdk.sample.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
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
import kotlinx.coroutines.reactive.asFlow

/**
 * ***Description**
 * Show how to sync data, observer sync state, save sync data
 */
class SyncFragment : BaseFragment(R.layout.fragment_sync) {

    private val viewBind: FragmentSyncBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewBind.refreshLayout.setOnRefreshListener {
//            deviceManager.syncData()
//        }

        viewBind.itemStep.clickTrigger(block = blockClick)
        viewBind.itemSleep.clickTrigger(block = blockClick)
        viewBind.itemCalories.clickTrigger(block = blockClick)
        viewBind.itemActivityDuration.clickTrigger(block = blockClick)
        viewBind.itemSleep.clickTrigger(block = blockClick)
        viewBind.itemHeartRate.clickTrigger(block = blockClick)
        viewBind.itemOxygen.clickTrigger(block = blockClick)
        viewBind.itemActivityDistance.clickTrigger(block = blockClick)
        viewBind.itemSport.clickTrigger(block = blockClick)
        viewBind.itemAll.clickTrigger(block = blockClick)
        viewBind.itemDailyActivityDuration.clickTrigger(block = blockClick)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    viewBind.refreshLayout.setAllChildEnabled(it)
                }
            }

//            launch {
//                UNIWatchMate.wmApps.appSport.getSupportSportList.toFlowable().asFlow().collect {
//                    LogUtils.d("getSupportSportList = ${GsonUtils.toJson(it)}")
//                }
//            }
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

            viewBind.itemCalories -> {
                findNavController().navigate(SyncFragmentDirections.toCalories())
            }

            viewBind.itemActivityDistance -> {
                findNavController().navigate(SyncFragmentDirections.toDistance())
            }

            viewBind.itemActivityDuration -> {
                findNavController().navigate(SyncFragmentDirections.toActivityDuration())
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
//                val tag:String? = null
//                Log.e("CRASH", tag!!)
                findNavController().navigate(SyncFragmentDirections.toSport())
            }

            viewBind.itemAll -> {
                findNavController().navigate(SyncFragmentDirections.toAllData())
            }
            viewBind.itemDailyActivityDuration -> {
                findNavController().navigate(SyncFragmentDirections.toDailyActivityDurationData())
            }

        }
    }
}
