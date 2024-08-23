package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmUnitInfo
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentUnitConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await


class UnitConfigFragment : BaseFragment(R.layout.fragment_unit_config),
    CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentUnitConfigBinding by viewBinding()

//  private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: WmUnitInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.observeConnectState.asFlow().collect {
                    viewBind.layoutContent.setAllChildEnabled(it.equals(WmConnectState.BIND_SUCCESS))
                }
            }
            launch {
                UNIWatchMate.wmSettings.settingUnitInfo.get().toFlowable().asFlow().collect{
                    config = it
                    updateUI()
                }
            }

            launch {
                UNIWatchMate.wmSettings.settingUnitInfo.observeChange().asFlow().collect {
                    config = it
                    updateUI()
                }
            }
        }

        viewBind.itemTimeFormat12Hour.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemDistanceUnitImperial.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemTemperatureUnitFahrenheit.getSwitchView().setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            when (buttonView) {
                viewBind.itemTimeFormat12Hour.getSwitchView() -> {
                    config?.let {
                        it.timeFormat =
                            if (isChecked) WmUnitInfo.TimeFormat.TWELVE_HOUR else WmUnitInfo.TimeFormat.TWENTY_FOUR_HOUR
                    }
                }
                viewBind.itemDistanceUnitImperial.getSwitchView() -> {
                    config?.let {
                        it.distanceUnit =
                            if (isChecked) WmUnitInfo.DistanceUnit.KM else WmUnitInfo.DistanceUnit.MILE
                    }
                }
                viewBind.itemTemperatureUnitFahrenheit.getSwitchView() -> {
                    config?.let {
                        it.temperatureUnit =
                            if (isChecked) WmUnitInfo.TemperatureUnit.CELSIUS else WmUnitInfo.TemperatureUnit.FAHRENHEIT
                    }
                }

                else -> {
                    throw IllegalArgumentException()
                }
            }
            config?.saveConfig()
        }
    }

    private fun WmUnitInfo.saveConfig() {
        applicationScope.launchWithLog {
            config?.let {
                UNIWatchMate.wmSettings.settingUnitInfo.set(it).await()
            }
        }
        updateUI()
    }

    private fun updateUI() {
        config?.let {
            viewBind.itemTimeFormat12Hour.getSwitchView().isChecked =
                it.timeFormat == WmUnitInfo.TimeFormat.TWELVE_HOUR
            viewBind.itemDistanceUnitImperial.getSwitchView().isChecked =
                it.distanceUnit == WmUnitInfo.DistanceUnit.KM
            viewBind.itemTemperatureUnitFahrenheit.getSwitchView().isChecked =
                it.temperatureUnit == WmUnitInfo.TemperatureUnit.CELSIUS
        }

    }
}