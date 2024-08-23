package com.sjbt.sdk.sample.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.github.kilnn.tool.dialog.prompt.PromptAutoCancel
import com.github.kilnn.tool.dialog.prompt.PromptDialogFragment
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.*
import com.sjbt.sdk.sample.databinding.FragmentSignUpBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.model.user.UserInfo
import com.sjbt.sdk.sample.ui.MainActivity
import com.sjbt.sdk.sample.ui.dialog.DatePickerDialogFragment
import com.sjbt.sdk.sample.utils.*
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class SignUpFragment : BaseFragment(R.layout.fragment_sign_up),
    PromptDialogFragment.OnPromptListener, DatePickerDialogFragment.Listener {
    private val dateFormat = FormatterUtil.getFormatterYYYYMMMdd()
    private val userBirthday = "user_birthday"
    private val viewBind: FragmentSignUpBinding by viewBinding()
    private val viewModel by viewModels<SignUpViewMode>()
    private var valueDate:Date?=null
    private var info: UserInfo?=null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.btnSignUp.setOnClickListener {
            signUp()
        }
         valueDate = Date(1995 - 1900, 0, 0)
        viewBind.piBirthday.getTextView().text = valueDate?.let { dateFormat.format(it) }
        viewBind.piBirthday.setOnClickListener {
                val calendar = Calendar.getInstance()
                val end = Date()
                val start = DateTimeUtils.getDateBetween(calendar, end, -365*150)
                DatePickerDialogFragment.newInstance(
                    start = start,
                    end = end,
                    value = valueDate,
                    getString(R.string.account_edit_birthday),
                ).show(childFragmentManager, userBirthday)
        }

        viewLifecycle.launchRepeatOnStarted {
            launch {
                viewModel.flowState.collect {
                    if (it.async is Loading) {
                        promptProgress.showProgress(R.string.action_loading)
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
                            promptToast.showSuccess(R.string.account_sign_up_success,
                                intercept = true,
                                promptId = 1)
                        }
                    }
                }
            }
        }
    }

    private fun signUp() {
        val username =
            viewBind.editUsername.text.trim().toString().takeIf { it.isNotEmpty() } ?: return
        val password =
            viewBind.editPassword.text.trim().toString().takeIf { it.isNotEmpty() } ?: return
        val height = viewBind.editHeight.text.trim().toString().toIntOrNull() ?: return
        if (height !in 50..300) {
            promptToast.showInfo(R.string.account_height_error,
                autoCancel = PromptAutoCancel.Duration(2500))
            return
        }
        val weight = viewBind.editWeight.text.trim().toString().toIntOrNull() ?: return
        if (weight !in 20..300) {
            promptToast.showInfo(R.string.account_weight_error)
            return
        }
        val sex = viewBind.rbSexMale.isChecked
        viewModel.signUp(username, password, height, weight, sex, valueDate!!.year+1900,valueDate!!.month+1,valueDate!!.date)
    }

    override fun onPromptCancel(promptId: Int, cancelReason: Int, tag: String?) {
        if (promptId == 1) {
            MainActivity.start(requireContext())
            requireActivity().finish()
        }
    }
    override fun onDialogDatePicker(tag: String?, date: Date) {
        tag?.let { it ->
            when (it) {
                userBirthday->{
                    valueDate=date
                    viewBind.piBirthday.getTextView().text = valueDate?.let { it1 ->
                        dateFormat.format(
                            it1
                        )
                    }
                }
                else->{

                }
            }
        }
    }
}

class SignUpViewMode : AsyncViewModel<SingleAsyncState<Unit>>(SingleAsyncState()) {

    private val authManager = Injector.getAuthManager()

    fun signUp(
        username: String,
        password: String,
        height: Int,
        weight: Int,
        sex: Boolean,
        year: Int,
        month: Int,
        day: Int,
    ) {
        suspend {
            //Delay 3 seconds. Simulate the sign up process
            delay(3000)
            authManager.signUp(username, password, height, weight, sex, year, month, day)
        }.execute(SingleAsyncState<Unit>::async) {
            copy(async = it)
        }
    }
}