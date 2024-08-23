package com.sjbt.sdk.sample.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmWidget
import com.base.sdk.entity.apps.WmWidgetType
import com.sjbt.sdk.sample.BuildConfig
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.*
import com.sjbt.sdk.sample.databinding.FragmentCombineBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.utils.showFailed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class CombineFragment : BaseFragment(R.layout.fragment_combine) {

    private val viewBind: FragmentCombineBinding by viewBinding()
    private val viewModel by viewModels<CombineViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemExerciseGoal.setOnClickListener {
            findNavController().navigate(CombineFragmentDirections.toExerciseGoal())
        }

        viewBind.itemUserInfo.setOnClickListener {
            findNavController().navigate(CombineFragmentDirections.toEditUserInfo())
        }



        viewBind.itemTest.visibility = if (BuildConfig.DEBUG) {
            View.VISIBLE
        } else {
            View.GONE
        }




        viewBind.itemTest.setOnClickListener {

//            UNIWatchMate.getDeviceInfo().subscribe { it ->
//                UNIWatchMate.wmLog.logE("测试消息1", "基本信息：" + it)
//            }
//
//            val wmUnitInfo = WmUnitInfo(
//                WmUnitInfo.WeightUnit.LB,
//                WmUnitInfo.TemperatureUnit.FAHRENHEIT,
//
//                WmUnitInfo.TimeFormat.TWENTY_FOUR_HOUR,
//                WmUnitInfo.DistanceUnit.MILE,
//
//                )
//
//            UNIWatchMate.wmSettings.settingUnitInfo.set(wmUnitInfo).subscribe { it ->
//                UNIWatchMate.wmLog.logE("测试消息6", "单位设置成功:" + it)
//            }
//
//            UNIWatchMate.wmSettings.settingAppView.get().subscribe { it ->
//                UNIWatchMate.wmLog.logE("测试消息7", "应用视图获取成功:" + it)
//            }
//
//            UNIWatchMate.wmSync.syncStepData.syncData(0).subscribe {
//                UNIWatchMate.wmLog.logE("测试消息2", "步数数据同步成功:" + it.value.size)
//            }
//
//            UNIWatchMate.wmSync.syncActivityDurationData.syncData(0).subscribe {
//                UNIWatchMate.wmLog.logE("测试消息3", "活动时长数据同步成功:" + it.value.size)
//            }
//
//            UNIWatchMate.wmSync.syncCaloriesData.syncData(0).subscribe {
//                UNIWatchMate.wmLog.logE("测试消息4", "卡路里同步成功:" + it.value.size)
//            }
//
//            UNIWatchMate.wmSync.syncOxygenData.syncData(0).subscribe {
//                UNIWatchMate.wmLog.logE("测试消息5", "血氧同步成功:" + it.value.size)
//            }

//            UNIWatchMate.wmSync.syncDailyActivityDuration.syncData(0).subscribe {
//                UNIWatchMate.wmLog.logE("测试消息8", "每日活动时长同步成功:" + it.value.size)
//            }


            UNIWatchMate.wmApps.appWidget.getWidgetList.subscribe { wmWidgets ->
                UNIWatchMate.wmLog.logE("小部件", "小部件列表:$wmWidgets")
            }

        }



        viewBind.setWidgets.setOnClickListener {
            val wmWidgets = mutableListOf<WmWidget>()

            wmWidgets.add(WmWidget(WmWidgetType.WIDGET_MUSIC))
            wmWidgets.add(WmWidget(WmWidgetType.WIDGET_SPORT))
            wmWidgets.add(WmWidget(WmWidgetType.WIDGET_SLEEP))
            wmWidgets.add(WmWidget(WmWidgetType.WIDGET_TIMER))

            UNIWatchMate.wmApps.appWidget.updateWidgetList(wmWidgets).subscribe { result ->
                UNIWatchMate.wmLog.logE("小部件", "设置小部件结果:$result")
            }
        }

        viewBind.tvVersion.text = getString(R.string.version_app, BuildConfig.VERSION_NAME)
        viewBind.btnSignOut.setOnClickListener {
//            UNIWatchMate.notifyWatchDisconnect()
            UNIWatchMate.disconnect()
            viewModel.signOut()
        }
        lifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    if (it.async is Loading) {
                        promptProgress.showProgress(R.string.account_sign_out_ing)
                    } else {
                        promptProgress.dismiss()
                    }
                }
            }
            launch {
                viewModel.flowEvent.collect {
                    when (it) {
                        is AsyncEvent.OnFail -> promptToast.showFailed(it.error)
                        is AsyncEvent.OnSuccess<*> -> {
                            startActivity(Intent(requireContext(), LaunchActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                }
            }
        }
    }

}

class CombineViewModel : AsyncViewModel<SingleAsyncState<Unit>>(SingleAsyncState()) {

    private val authManager = Injector.getAuthManager()

    fun signOut() {
        suspend {
            //Delay 3 seconds. Simulate the sign out process
            delay(1500)
            authManager.signOut()
            UNIWatchMate.disconnect()
        }.execute(SingleAsyncState<Unit>::async) {
            copy(async = it)
        }
    }
}