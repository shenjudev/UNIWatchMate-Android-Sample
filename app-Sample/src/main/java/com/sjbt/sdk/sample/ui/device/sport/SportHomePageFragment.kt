package com.sjbt.sdk.sample.ui.device.sport

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDialHomePageBinding
import com.sjbt.sdk.sample.databinding.FragmentSportHomePageBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding

class SportHomePageFragment : BaseFragment(R.layout.fragment_sport_home_page) {

    private val viewBind: FragmentSportHomePageBinding by viewBinding()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Check which dial feature the device supports

        viewBind.btnSportInstalled.clickTrigger {
            findNavController().navigate(SportHomePageFragmentDirections.toInstalledList())
        }

        viewBind.btnSportTestInstall.clickTrigger {
            findNavController().navigate(SportHomePageFragmentDirections.toSportLibrary())
        }
    }

}