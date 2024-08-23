package com.sjbt.sdk.sample.ui.device.contacts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.settings.WmEmergencyCall
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.databinding.FragmentEmergencyContactsBinding
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewLifecycleScope
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import timber.log.Timber

class EmergencyContactFragment : BaseFragment(R.layout.fragment_emergency_contacts) {

    private val viewBind: FragmentEmergencyContactsBinding by viewBinding()
    private val emergencyModel: EmergencyContactViewModel by viewModels()
    private lateinit var adapter: ContactsAdapter

    private val pickEmergencyContact =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                getContact(uri, true)
            }
        }

    private fun getContact(uri: Uri, emergency: Boolean) {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        val cursor =
            requireContext().contentResolver.query(uri, projection, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val numberIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            var number = cursor.getString(numberIndex)
            val name = cursor.getString(nameIndex)
            Timber.i("select contacts result: [$name , $number]")
            cursor.close()
            if (!name.isNullOrEmpty() && !number.isNullOrEmpty()) {
                number = number.replace(" ".toRegex(), "")
                val newContact = WmContact.create(name, number)
                newContact?.let {
                    if (emergency) {
                        promptProgress.showProgress("")
                        emergencyModel.setEmergencyContact(it)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ContactsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    onBackPressed()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })


        viewBind.loadingView.associateViews = arrayOf(viewBind.llEmergency)

        viewBind.itemEmergencyContactSwitch.getSwitchView()
            ?.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    if (isChecked) {
                        if (emergencyModel.state.requestEmergencyCall()?.emergencyContacts?.isEmpty() == true) {
                            pickEmergencyContact.launch(Intent(Intent.ACTION_PICK).apply {
                                type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                            })
                            viewBind.itemEmergencyContactSwitch.getSwitchView().isChecked = false
                            return@setOnCheckedChangeListener
                        }
                    }
                    promptProgress.showProgress("")
                    emergencyModel.setEmergencyEnbalbe(isChecked)
                }
            }

        viewBind.itemEmergencyContact.setOnClickListener {
            viewLifecycleScope.launchWhenResumed {
                PermissionHelper.requestContacts(this@EmergencyContactFragment) { granted ->
                    if (granted) {
                        pickEmergencyContact.launch(Intent(Intent.ACTION_PICK).apply {
                            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                        })
                    }
                }
            }
        }


        viewLifecycle.launchRepeatOnStarted {

            launch {
                emergencyModel.flowState.collect { state ->
                    when (state.requestEmergencyCall) {
                        is Fail -> {
                            viewBind.llEmergency.setAllChildEnabled(false)
                            promptProgress.dismiss()
                        }

                        is Success -> {
                            val emergencyCall = state.requestEmergencyCall()

                            if (emergencyCall == null) {
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                                viewBind.itemEmergencyContactSwitch.getSwitchView()?.isChecked =
                                    emergencyCall.isEnabled
                                updateEmergencyUi(emergencyCall)
                            }
                            promptProgress.dismiss()
                        }

                        else -> {}
                    }
                }
            }
            launch {
                emergencyModel.flowEvent.collect { event ->
                    when (event) {
                        is EmergencyCallEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                        }

                        is EmergencyCallEvent.setEmergencyContactFail -> {
                            promptToast.showFailed(event.throwable)
                            promptProgress.dismiss()
                        }

                        is EmergencyCallEvent.setEmergencyContactSuccess -> {
                            promptProgress.dismiss()
                            updateEmergencyUi(event.wmEmergencyCall)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun updateEmergencyUi(wmEmergencyCall: WmEmergencyCall) {
        viewBind.itemEmergencyContact.getTitleView()?.text =
            if (wmEmergencyCall.emergencyContacts.isNotEmpty()) {
                wmEmergencyCall.emergencyContacts[0].name
            } else {
                getString(R.string.ds_no_data)
            }

        viewBind.itemEmergencyContact.getTextView()?.text =
            if (wmEmergencyCall.emergencyContacts.isNotEmpty()) {
                wmEmergencyCall.emergencyContacts[0].number
            } else {
                ""
            }
    }

    private fun onBackPressed() {
        findNavController().navigateUp()
    }
}