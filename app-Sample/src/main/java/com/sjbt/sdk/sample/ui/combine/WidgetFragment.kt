package com.sjbt.sdk.sample.ui.combine

import android.os.Bundle
import android.view.View
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmWidget
import com.base.sdk.entity.apps.WmWidgetType
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentWidgetBinding
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding


class WidgetFragment : BaseFragment(R.layout.fragment_widget) {

    private val viewBind: FragmentWidgetBinding by viewBinding()

    private var completedWidgetAdapter: WidgetAdapter? = null
    private var pendingWidgetAdapter: WidgetAdapter? = null
    private var widgetList: MutableList<WmWidget> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        completedWidgetAdapter = WidgetAdapter(
            requireContext(),
            R.layout.item_hide_widget,
            listOf(),
            false,
            object : WidgetAdapter.OnItemClicked {
                override fun onClick(position: Int, isAdd: Boolean) {
                    if (completedWidgetAdapter?.data!!.size == 1) {
                        ToastUtil.showToast(getString(R.string.widget_kepp_one))
                        return
                    }
                    val removeItem = completedWidgetAdapter?.data!![position]
                    completedWidgetAdapter?.data!!.remove(removeItem)
                    UNIWatchMate.wmApps.appWidget.updateWidgetList(completedWidgetAdapter?.data as List<WmWidget>)
                        .subscribe { result ->
                            completedWidgetAdapter?.notifyDataSetChanged()
                            pendingWidgetAdapter?.data!!.add(removeItem)
                            pendingWidgetAdapter?.data!!.sortWith(compareBy { it.type.id })
                            pendingWidgetAdapter?.notifyDataSetChanged()
                        }
                }
            })

        viewBind.showRecycle.adapter = completedWidgetAdapter

        widgetList = mutableListOf()

        widgetList.add(WmWidget(WmWidgetType.WIDGET_MUSIC))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_ACTIVITY_RECORD))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_HEART_RATE))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_BLOOD_OXYGEN))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_BREATH_TRAIN))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_SPORT))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_NOTIFY_MSG))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_ALARM))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_PHONE))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_SLEEP))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_WEATHER))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_FIND_PHONE))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_CALCULATOR))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_REMOTE_CAMERA))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_STOP_WATCH))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_TIMER))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_FLASH_LIGHT))
        widgetList.add(WmWidget(WmWidgetType.WIDGET_SETTING))



        UNIWatchMate.wmApps.appWidget.getWidgetList.subscribe { wmWidgets ->
            completedWidgetAdapter?.setNewData(wmWidgets)
            for (haveItem in wmWidgets) {
                for (pendingItem in widgetList) {
                    if (haveItem.type == pendingItem.type) {
                        widgetList.remove(pendingItem)
                        break
                    }
                }
            }
            initPendingWidgetAdapter()
        }
    }

    private fun initPendingWidgetAdapter() {
        pendingWidgetAdapter = WidgetAdapter(
            requireContext(),
            R.layout.item_hide_widget,
            widgetList,
            true,
            object : WidgetAdapter.OnItemClicked {
                override fun onClick(position: Int, isAdd: Boolean) {
                    if ((completedWidgetAdapter?.data!![0].type == WmWidgetType.WIDGET_MUSIC && completedWidgetAdapter?.data!!.size > 3)
                        || ((completedWidgetAdapter?.data!![0].type != WmWidgetType.WIDGET_MUSIC) && completedWidgetAdapter?.data!!.size > 8)
                    ) {
                        ToastUtil.showToast(getString(R.string.widget_limit))
                    } else {
                        completedWidgetAdapter?.data!!.add(pendingWidgetAdapter?.data!![position])
                        UNIWatchMate.wmApps.appWidget.updateWidgetList(completedWidgetAdapter?.data as List<WmWidget>)
                            .subscribe { result ->
                                completedWidgetAdapter?.notifyDataSetChanged()
                            }
                        pendingWidgetAdapter?.data!!.remove(pendingWidgetAdapter?.data!![position])
                        pendingWidgetAdapter?.notifyDataSetChanged()
                    }
                }
            })

        viewBind.settingRecycle.adapter = pendingWidgetAdapter
    }

}