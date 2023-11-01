package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentContactHomePageBinding
import com.sjbt.sdk.sample.databinding.FragmentContactsBinding
import com.sjbt.sdk.sample.databinding.FragmentDialHomePageBinding
import com.sjbt.sdk.sample.databinding.FragmentHeartrateHomePageBinding
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding

class HeartRateHomePageFragment : BaseFragment(R.layout.fragment_heartrate_home_page) {

    private val viewBind: FragmentHeartrateHomePageBinding by viewBinding()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Check which dial feature the device supports

        viewBind.btnHourlyData.clickTrigger {
            findNavController().navigate(HeartRateHomePageFragmentDirections.toHourHeartRateFragment())
        }

        viewBind.btnEvery5Minutes.clickTrigger {
            findNavController().navigate(HeartRateHomePageFragmentDirections.to5MinutesHeartRateFragment())
        }
    }

}