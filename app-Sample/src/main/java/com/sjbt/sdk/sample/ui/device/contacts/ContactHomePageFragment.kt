package com.sjbt.sdk.sample.ui.device.contacts

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentContactHomePageBinding
import com.sjbt.sdk.sample.databinding.FragmentContactsBinding
import com.sjbt.sdk.sample.databinding.FragmentDialHomePageBinding
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding

class ContactHomePageFragment : BaseFragment(R.layout.fragment_contact_home_page) {

    private val viewBind: FragmentContactHomePageBinding by viewBinding()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.btnContact.clickTrigger {
            findNavController().navigate(ContactHomePageFragmentDirections.toContacts())
        }

        viewBind.btnEmergencyContact.clickTrigger {
            findNavController().navigate(ContactHomePageFragmentDirections.toEmergencyContacts())
        }
    }

}