package com.sjbt.sdk.sample.ui.device.dial

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDialHomePageBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding

class DialHomePageFragment : BaseFragment(R.layout.fragment_dial_home_page) {

    private val viewBind: FragmentDialHomePageBinding by viewBinding()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Check which dial feature the device supports

        viewBind.btnDialInstalled.clickTrigger {
            findNavController().navigate(DialHomePageFragmentDirections.toInstalledList())
        }

        viewBind.btnDialTestInstall.clickTrigger {
            findNavController().navigate(DialHomePageFragmentDirections.toDialLibrary())
        }

        viewBind.btnDialCustom.clickTrigger {
            findNavController().navigate(DialHomePageFragmentDirections.toDiyDialFragment())
        }

    }

}