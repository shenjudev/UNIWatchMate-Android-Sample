package com.sjbt.sdk.sample.ui.device.dial

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
import com.sjbt.sdk.sample.databinding.FragmentDialInstalledListBinding
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.LoadingView
import kotlinx.coroutines.launch

class DialInstalledListFragment : BaseFragment(R.layout.fragment_dial_installed_list) {

    private val viewBind: FragmentDialInstalledListBinding by viewBinding()
    private val viewModel: DialInstalledViewModel by viewModels()
    private lateinit var adapter: DialListAdapter
    private val TAG = "DialInstalledListFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.ds_dial_installed)

        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        adapter = DialListAdapter()
        adapter.listener = object : DialListAdapter.Listener {

            override fun onItemDelete(position: Int) {
                UNIWatchMate.wmApps.appDial.deleteDial(adapter.sources!![position])
                    .subscribe { reuslt ->
                        UNIWatchMate.wmLog.logD(
                            TAG,
                            "删除当前表盘：${adapter.sources!![position]} 结果：${reuslt}"
                        )
                        viewModel.requestInstallDials()
                        UNIWatchMate.wmApps.appDial.setDial(adapter.sources!![position])
                            .subscribe { reuslt ->
                                UNIWatchMate.wmLog.logD(
                                    TAG,
                                    "设置当前表盘：${adapter.sources!!.get(position)} 结果：${reuslt}"
                                )
                                if (reuslt){
                                    adapter?.sources!!.forEach {
                                        it.curr = it.id == adapter.sources!![position].id
                                    }
                                    adapter.notifyDataSetChanged()
                                }
                            }

                    }
            }

            override fun onItemCurrDial(position: Int) {
                UNIWatchMate.wmApps.appDial.setDial(adapter.sources!![position])
                    .subscribe { reuslt ->
                        UNIWatchMate.wmLog.logD(
                            TAG,
                            "设置当前表盘：${adapter.sources!![position]} 结果：${reuslt}"
                        )
                       if (reuslt){
                           adapter?.sources!!.forEach {
                               it.curr = it.id == adapter.sources!![position].id
                           }
                           adapter.notifyDataSetChanged()
                       }
                    }
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerView.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestInstallDials()
        }
        viewBind.loadingView.associateViews = arrayOf(viewBind.recyclerView)

        loadDial()

    }


    private fun loadDial(){
        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect { state ->
                    when (state.requestDials) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }
                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }
                        is Success -> {
                            val alarms = state.requestDials()
                            if (alarms == null || alarms.isEmpty()) {
                                viewBind.loadingView.showError(R.string.ds_no_data)
                            } else {
                                viewBind.loadingView.visibility = View.GONE
                            }
                            adapter.sources = alarms
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
                        is DialEvent.DialRemoved -> {
                            promptProgress.dismiss()
                            viewBind.loadingView.visibility = View.GONE
                            adapter.notifyItemRemoved(event.position)
                        }
                        else ->{}
                    }
                }
            }
        }

        UNIWatchMate.wmApps.appDial.observerCurrentDial().subscribe {

            UNIWatchMate.wmLog.logE("当前表盘", "dial.id == ${it.id}")
            adapter.sources?.forEach { dial ->
                dial.curr = dial.id == it.id
                UNIWatchMate.wmLog.logE("表盘遍历", "dial.id == ${it.id}")
            }

            adapter.notifyDataSetChanged()
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (adapter.itemCount <= 0) {
                viewBind.loadingView.showError(R.string.ds_no_data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(adapterDataObserver)
    }

}