package com.sjbt.sdk.sample.ui.device.bind

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.isVisible
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentConnectHelpBinding
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.flowLocationServiceState
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import timber.log.Timber

class ConnectHelpFragment : BaseFragment(R.layout.fragment_connect_help) {

    private val viewBind: FragmentConnectHelpBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.layoutPermission.isVisible = !PermissionHelper.hasBle(requireContext())
        viewBind.btnGrantPermission.setOnClickListener {
            PermissionHelper.requestBle(this) { granted ->
                if (granted) {
                    viewBind.layoutPermission.isVisible = false
                }
            }
        }
        viewLifecycle.launchRepeatOnStarted {
            flowLocationServiceState(requireContext()).collect { isEnabled ->
                viewBind.layoutLocationService.isVisible = !isEnabled
            }
        }
        viewBind.btnEnableLocationService.setOnClickListener {
            try {
                requireContext().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }

}