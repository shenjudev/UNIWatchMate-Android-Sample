package com.sjbt.sdk.sample.ui.device.language

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.api.UNIWatchMate
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.databinding.FragmentLanguageListBinding
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.LoadingView
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

class LanguageListFragment : BaseFragment(R.layout.fragment_language_list) {

    private val viewBind: FragmentLanguageListBinding by viewBinding()
    private val viewModel: LanguagelInstalledViewModel by viewModels()
    private lateinit var adapter: LanguageListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        adapter = LanguageListAdapter()
        adapter.listener = object : LanguageListAdapter.Listener {
            override fun onItemSelect(position: Int) {
                viewModel.setLanguage(position)
            }
        }

        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestLanguages()
        }
        viewBind.loadingView.associateViews = arrayOf(viewBind.recyclerView)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect { state ->
                    when (state.requestLanguages) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }

                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }

                        is Success -> {
                            val wmLanguages = state.requestLanguages()
                            Timber.d( "wmLanguages $wmLanguages")
                            if (wmLanguages.isNullOrEmpty()) {
                                viewBind.loadingView.showError(R.string.ds_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                            adapter.sources = wmLanguages
                            adapter.notifyDataSetChanged()

                        }

                        else -> {}
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect { event ->
                    when (event) {
                        is DialEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                        }

                        is DialEvent.LanguageSet -> {
                            adapter.notifyDataSetChanged()
                        }

                    }
                }
            }
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (adapter.itemCount <= 0) {
                viewBind.loadingView.showError(R.string.ds_alarm_no_data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

}