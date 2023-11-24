package com.sjbt.sdk.sample.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
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

        viewBind.itemTest.setOnClickListener {
            UNIWatchMate.getDeviceInfo().subscribe { it->
                UNIWatchMate.wmLog.logE("测试消息", "基本信息：" + it)
            }

            UNIWatchMate.wmSync.syncStepData.syncData(0).subscribe {
                UNIWatchMate.wmLog.logE("测试消息", "步数数据同步成功:"+it.value.size)
            }


        }

        viewBind.tvVersion.text = getString(R.string.version_app,BuildConfig.VERSION_NAME)
        viewBind.btnSignOut.setOnClickListener {
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