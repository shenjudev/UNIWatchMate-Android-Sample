package com.sjbt.sdk.sample.ui.device

import android.os.Bundle
import android.view.View
import com.base.api.UNIWatchMate
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceBinding
import com.sjbt.sdk.sample.databinding.FragmentDeviceInfoBinding
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

class DeviceInfoFragment : BaseFragment(R.layout.fragment_device_info) {

    private val viewBind: FragmentDeviceInfoBinding by viewBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                val it=  UNIWatchMate.getDeviceInfo().await()
                CacheDataHelper.setCurrentDeviceInfo(it)
                viewBind.itemDeviceInfo.text = "mac=${it.macAddress}\n" +
                        "bluetoothName=${it.bluetoothName}\n" +
                        "deviceName=${it.deviceName}\n" +
                        "deviceId=${it.deviceId}\n" +
                        "version=${it.version}\n"+
                        "cw=${it.cw}\n"+
                        "ch=${it.ch}\n"+
                        "lang=${it.lang}\n"+
                        "screen=${it.screen}\n"+
                        "dialAbility=${it.dialAbility}\n"+
                        "model=${it.model}"
            }

        }
    }

}