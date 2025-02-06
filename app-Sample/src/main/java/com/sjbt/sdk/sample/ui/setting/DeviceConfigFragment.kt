package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceConfigBinding
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

class DeviceConfigFragment : BaseFragment(R.layout.fragment_device_config) {

    private val viewBind: FragmentDeviceConfigBinding by viewBinding()
    //    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemLanguage.setOnClickListener(blockClick)

        viewLifecycle.launchRepeatOnStarted {
                launch {
                    UNIWatchMate.observeConnectState.asFlow().collect {
                        viewBind.layoutContent.setAllChildEnabled(it.equals(WmConnectState.BIND_SUCCESS))
                    }
                }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemLanguage -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toLanguage())
            }
        }
    }

}