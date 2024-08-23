package com.sjbt.sdk.sample.ui.device.sport

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.sdk.entity.apps.WmSport
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.databinding.FragmentSportInstalledListBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.LoadingView
import kotlinx.coroutines.launch

class SportInstalledListFragment : BaseFragment(R.layout.fragment_sport_installed_list) {

    private val viewBind: FragmentSportInstalledListBinding by viewBinding()
    private val viewModel: SportInstalledViewModel by viewModels()
    private val applicationScope = Injector.getApplicationScope()
    private lateinit var adapter: SportListAdapter
    private val dragAdapter by lazy {
        SportDragAdapter(buildInDatas,viewModel)
    }
    private var buildInDatas: MutableList<WmSport> = mutableListOf()
    private var installDatas: MutableList<WmSport> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.recyclerViewDrag.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerViewDrag.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        val mItemDragAndSwipeCallback = ItemDragAndSwipeCallback(dragAdapter)
        val mItemTouchHelper = ItemTouchHelper(mItemDragAndSwipeCallback)
        mItemTouchHelper.attachToRecyclerView(viewBind.recyclerViewDrag)

        dragAdapter.enableDragItem(mItemTouchHelper)
        dragAdapter.setOnItemDragListener(listener)
        viewBind.recyclerViewDrag.adapter = dragAdapter

        viewBind.recyclerViewInstalled.layoutManager = LinearLayoutManager(requireContext())
        viewBind.recyclerViewInstalled.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        adapter = SportListAdapter(viewModel)
        adapter.listener = object : SportListAdapter.Listener {

            override fun onItemDelete(position: Int) {
                if (adapter.sources?.get(position)?.buildIn != true) {
                    promptProgress.showProgress(getString(R.string.action_deling))
                    viewModel.deleteSport(position)
                } else {
                    promptToast.showFailed(getString(R.string.tip_inner_sport_del_error))
                }
            }
        }
        adapter.registerAdapterDataObserver(adapterDataObserver)
        viewBind.recyclerViewInstalled.adapter = adapter

        viewBind.loadingView.listener = LoadingView.Listener {
            viewModel.requestInstallSports()
        }
        viewBind.loadingView.associateViews =
            arrayOf(viewBind.recyclerViewInstalled, viewBind.recyclerViewDrag)


        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect { state ->
                    when (state.requestSports) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }

                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }

                        is Success -> {
                            val sportsMap = state.requestSports()
                            if (sportsMap == null || sportsMap.size()==0) {
                                viewBind.loadingView.showError(R.string.ds_no_data)
                            } else {

                                buildInDatas.clear()
                                buildInDatas.addAll(sportsMap[0])
                                dragAdapter.notifyDataSetChanged()

                                if (sportsMap.size() > 1) {
                                    installDatas.clear()
                                    installDatas.addAll(sportsMap[1])
                                    adapter.sources = installDatas
                                    adapter.notifyDataSetChanged()
                                }
                                viewBind.loadingView.visibility = View.GONE
                            }
                        }
                        else -> {}
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect { event ->
                    when (event) {
                        is SportEvent.RequestFail -> {
                            promptProgress.dismiss()
                        }
                        is SportEvent.SportUpdateFail -> {
                            promptProgress.dismiss()
                            promptToast.showFailed(event.msg)
                            adapter.notifyDataSetChanged()
                        }
                        is SportEvent.SportRemoved -> {
                            promptProgress.dismiss()
                            if (installDatas.size > event.position) {
                                installDatas.removeAt(event.position)
                            }
                            viewBind.loadingView.visibility = View.GONE
                            adapter.notifyDataSetChanged()
                        }

                        is SportEvent.SportSortSuccess -> {
                            promptProgress.dismiss()
                            adapter.notifyDataSetChanged()
                        }

                        else ->{}
                    }
                }
            }
        }
    }
    private var fromOnStart = 0
    var listener: OnItemDragListener = object : OnItemDragListener {
        override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
            Log.d(TAG, "drag start pos=$pos")
            fromOnStart = pos
            val holder = viewHolder as BaseViewHolder?
            context?.apply {
                holder?.itemView?.setBackgroundColor(resources.getColor(R.color.color_25000000))
            }
        }

        override fun onItemDragMoving(
            source: RecyclerView.ViewHolder,
            from: Int,
            target: RecyclerView.ViewHolder,
            to: Int,
        ) {
            Log.d(
                TAG,
                "move from: " + from + " to: " + to
            )

        }

        override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
            Log.d(
                TAG,
                "drag end pos=$pos"
            )
            val holder = viewHolder as BaseViewHolder?
            context?.apply {
                holder?.itemView?.setBackgroundColor(resources.getColor(R.color.white))
            }
            val remove = buildInDatas.removeAt(fromOnStart)
            buildInDatas.add(pos,remove)
            promptProgress.showProgress("")
            applicationScope.launch {
                viewModel.sortFixedSportList(fromOnStart,pos)
            }
            //                holder.setTextColor(R.id.tv, Color.BLACK);
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

    companion object {
        const val TAG = "SportInstalledList"
    }
}