package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmSoundAndHaptic
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentSoundTouchConfigBinding
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

class SoundTouchFeedbackConfigFragment : BaseFragment(R.layout.fragment_sound_touch_config),
    CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentSoundTouchConfigBinding by viewBinding()

    //    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: WmSoundAndHaptic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.wmSettings.settingSoundAndHaptic.get().toFlowable().asFlow()
                    .collect {
                        config = it
                        updateUI()
                    }
            }
            launch {
                UNIWatchMate.observeConnectState.asFlow().collect {
                    viewBind.layoutContent.setAllChildEnabled(it.equals(WmConnectState.BIND_SUCCESS))
                    updateUI()
                }
            }
            launch {
                UNIWatchMate.wmSettings.settingSoundAndHaptic.observeChange().asFlow().collect {
                    config = it
                    updateUI()
                }
            }
        }

        viewBind.itemIsCrownHasTactileFeedback.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemIsMute.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemIsNotificationTouch.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemIsSystemHasTactileFeedback.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemRingWhenCallComeIn.getSwitchView().setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            when (buttonView) {
                viewBind.itemIsCrownHasTactileFeedback.getSwitchView() -> {
                    config?.let {
                        it.isCrownHapticFeedback = isChecked
                    }
                }

                viewBind.itemIsMute.getSwitchView() -> {
                    config?.let {
                        it.isMuted = isChecked
                    }
                }

                viewBind.itemIsNotificationTouch.getSwitchView() -> {
                    config?.let {
                        it.isNotificationHaptic = isChecked
                    }
                }

                viewBind.itemIsSystemHasTactileFeedback.getSwitchView() -> {
                    config?.let {
                        it.isSystemHapticFeedback = isChecked
                    }
                }

                viewBind.itemRingWhenCallComeIn.getSwitchView() -> {
                    config?.let {
                        it.isRingtoneEnabled = isChecked
                    }
                }

                else -> {
                    throw IllegalArgumentException()
                }
            }
            config?.saveConfig()
        }
    }

    private fun WmSoundAndHaptic.saveConfig() {
        applicationScope.launchWithLog {
            config?.let {
                UNIWatchMate.wmSettings.settingSoundAndHaptic.set(it).await()
            }
        }
        updateUI()
    }

    private fun updateUI() {
        config?.let {
            viewBind.itemRingWhenCallComeIn.getSwitchView().isChecked =
                it.isRingtoneEnabled
            viewBind.itemIsSystemHasTactileFeedback.getSwitchView().isChecked =
                it.isSystemHapticFeedback
            viewBind.itemIsCrownHasTactileFeedback.getSwitchView().isChecked =
                it.isCrownHapticFeedback
            viewBind.itemIsMute.getSwitchView().isChecked =
                it.isMuted
            viewBind.itemIsNotificationTouch.getSwitchView().isChecked =
                it.isNotificationHaptic
        }

    }
}