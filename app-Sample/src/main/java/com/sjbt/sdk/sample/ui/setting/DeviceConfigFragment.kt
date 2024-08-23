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
        viewBind.itemUnitConfig.setOnClickListener(blockClick)
        viewBind.itemSedentary.setOnClickListener(blockClick)
        viewBind.itemDrinkWater.setOnClickListener(blockClick)
        viewBind.itemTurnWristLighting.setOnClickListener(blockClick)
        viewBind.itemSleep.setOnClickListener(blockClick)
        viewBind.itemSoundTouchFeedback.setOnClickListener(blockClick)
        viewBind.itemAppView.setOnClickListener(blockClick)
        viewBind.itemLanguage.setOnClickListener(blockClick)
        viewBind.itemHeartRate.setOnClickListener(blockClick)

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

            viewBind.itemUnitConfig -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toUnitConfig())
            }

            viewBind.itemSoundTouchFeedback -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toSoundTouchFeedback())
            }

            viewBind.itemAppView -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toAppView())
            }

            viewBind.itemTurnWristLighting -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toTurnWristLightingConfig())
            }

            viewBind.itemLanguage -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toLanguage())
            }

            viewBind.itemSedentary -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toSedentary())
            }

            viewBind.itemDrinkWater -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toDrinkWaterConfig())
            }

            viewBind.itemHeartRate -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toHeartRate())
            }

            viewBind.itemSleep -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toSleepConfig())
            }

        }
    }

}