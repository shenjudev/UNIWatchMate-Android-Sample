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
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.bertsir.zbar.Qr.ScanResult
import com.base.sdk.entity.apps.WmContact
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.databinding.FragmentContactsBinding
import com.sjbt.sdk.sample.model.device.PhoneContact
import com.sjbt.sdk.sample.ui.device.bind.DeviceBindFragment
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewLifecycleScope
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.LoadingView
import kotlinx.coroutines.launch
import timber.log.Timber

class ContactsFragment : BaseFragment(R.layout.fragment_contacts) {

    private val viewBind: FragmentContactsBinding by viewBinding()
    private val viewModel: ContactsViewModel by viewModels()

    private lateinit var adapter: ContactsAdapter

//    private val pickContact =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            val uri = result.data?.data
//            if (result.resultCode == Activity.RESULT_OK && uri != null) {
//                getContact(uri)
//            }
//        }

    private fun setContacts(contacts: ArrayList<PhoneContact>) {
        if (contacts.isEmpty()) {
            return
        }
        promptProgress.showProgress("")
        val wmContactList = arrayListOf<WmContact>()
        contacts.forEach {
            it?.let {
                wmContactList.add(WmContact.create(it.name, it.number)!!)
            }
        }
        viewModel.addContacts(wmContactList)
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

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
//        if (BuildConfig.DEBUG) {
        viewBind.itemAddTest100.visibility = View.VISIBLE
//        }
        viewBind.itemAddTest100.setOnClickListener {
            promptProgress.showProgress("")
            testAdd100Contacts()
        }
        adapter.listener = object : ContactsAdapter.Listener {
            override fun onItemDelete(position: Int) {
                promptProgress.showProgress("")
                viewModel.deleteContacts(position)
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestContacts()
        }
        viewBind.loadingView.associateViews = arrayOf(viewBind.recyclerView)

        viewBind.fabAdd.setOnClickListener {
            viewLifecycleScope.launchWhenResumed {
                if ((adapter.sources?.size ?: 0) >= 100) {
                    promptToast.showInfo(R.string.ds_contacts_tips1)
                } else {
                    PermissionHelper.requestContacts(this@ContactsFragment) { granted ->
                        if (granted) {
                            findNavController().navigate(
                                ContactsFragmentDirections.toPhoneContacts(
                                    adapter.sources?.size ?: 0
                                )
                            )
                        }
                    }
                }
            }
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect { state ->
                    when (state.requestContacts) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                            viewBind.fabAdd.hide()
                        }

                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                            viewBind.fabAdd.hide()
                        }

                        is Success -> {
                            val contacts = state.requestContacts()
                            if (contacts.isNullOrEmpty()) {
                                viewBind.loadingView.showError(R.string.tip_current_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                            Timber.i("requestContacts: contacts$contacts ]")
                            adapter.sources = contacts
                            adapter.notifyDataSetChanged()
                            viewBind.fabAdd.show()

                        }

                        else -> {}
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect { event ->
                    when (event) {
                        is ContactsEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                            promptProgress.dismiss()
                        }

                        is ContactsEvent.Inserted -> {
                            adapter.notifyItemInserted(event.pos)
                            promptProgress.dismiss()
                        }

                        is ContactsEvent.UpdateFail -> {
                            promptProgress.dismiss()
                        }

                        is ContactsEvent.Update100Success -> {
                            adapter.notifyDataSetChanged()
                            promptProgress.dismiss()
                        }

                        is ContactsEvent.Removed -> {
                            adapter.notifyItemRemoved(event.position)
                            promptProgress.dismiss()
                        }

                        is ContactsEvent.NavigateUp -> {
                            findNavController().navigateUp()
                        }

                        else ->{}
                    }
                }
            }

        }
        setFragmentResultListener(PHONE_CONTACTS_SELECT_KEY) { requestKey, bundle ->
            if (requestKey == PHONE_CONTACTS_SELECT_KEY) {
                val results =
                    bundle.getParcelableArrayList<PhoneContact>(PHONE_CONTACTS_SELECT) as ArrayList<PhoneContact>?
                results?.let { setContacts(it) }
            }
        }
    }

    private fun testAdd100Contacts() {
        val contacts = mutableListOf<WmContact>()
        val random = java.util.Random()

        val firstNames = listOf(
            "张三",
            "李四",
            "王五",
            "赵六",
            "陈七",
            "刘八",
            "孙九",
            "周十",
            "吴十一",
            "郑十二"
        )
        val lastNames = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九", "十")
        for (indext in 0 until 100) {
            val firstName = firstNames[random.nextInt(firstNames.size)]
            val lastName = lastNames[random.nextInt(lastNames.size)]
            val fullName = "$firstName$lastName"
//            val phoneNumber =
//                "${random.nextInt(900) + 100}${random.nextInt(900) + 100}${random.nextInt(9000) + 1000}$indext"
            val phoneNumber = "$indext"
            val wmContact = WmContact.create(fullName, phoneNumber)
            if (wmContact != null) {
                contacts.add(wmContact)
            }
        }
        viewModel.add100Contacts(contacts)
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (adapter.itemCount <= 0) {
                viewBind.loadingView.showError(R.string.tip_current_no_data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

    private fun onBackPressed() {
        findNavController().navigateUp()
    }

    companion object {
        const val PHONE_CONTACTS_SELECT_KEY = "phone_contacts_select_key"
        const val PHONE_CONTACTS_SELECT = "phone_contacts_select"
    }
}