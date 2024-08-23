package com.sjbt.sdk.sample.ui.device.contacts

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.databinding.FragmentPhoneContactsBinding
import com.sjbt.sdk.sample.model.device.PhoneContact
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.LoadingView
import kotlinx.coroutines.launch
import timber.log.Timber

class PhoneContactsFragment : BaseFragment(R.layout.fragment_phone_contacts) {

    private val viewBind: FragmentPhoneContactsBinding by viewBinding()
    private val viewModel: PhoneContactsViewModel by viewModels()
    private val args: PhoneContactsFragmentArgs by navArgs()

    private lateinit var adapter: PhotoContactsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = PhotoContactsAdapter(args.contactNum)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_contact_select, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) {
                    onBackPressed()
                    return true
                }else if (menuItem.itemId == R.id.menu_contact_select) {
                    val choosePhoneContact = arrayListOf<PhoneContact>()
                    adapter.sources?.let {
                        for (bean in it) {
                            if (bean.checked) {
                                choosePhoneContact.add(bean)
                            }
                        }
                        setFragmentResult(ContactsFragment.PHONE_CONTACTS_SELECT_KEY, Bundle().apply {
                            putParcelableArrayList(ContactsFragment.PHONE_CONTACTS_SELECT, choosePhoneContact)
                        })
                        onBackPressed()
                    }
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

        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestContacts()
        }
        viewBind.loadingView.associateViews = arrayOf(viewBind.recyclerView)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect { state ->
                    when (state.requestPhoneContacts) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }

                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }

                        is Success -> {
                            val contacts = state.requestPhoneContacts()
                            if (contacts.isNullOrEmpty()) {
                                viewBind.loadingView.showError(R.string.tip_current_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                            Timber.i("requestContacts: contacts$contacts ]")
                            adapter.sources = contacts
                            adapter.notifyDataSetChanged()

                        }

                        else -> {}
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect { event ->
                    when (event) {
                        is PhoneContactsEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                            promptProgress.dismiss()
                        }
                        else ->{}
                    }
                }
            }
        }
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
}