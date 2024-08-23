package com.sjbt.sdk.sample.ui.combine

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentEditUserInfoBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.model.user.UserInfo
import com.sjbt.sdk.sample.model.user.toSdkUser
import com.sjbt.sdk.sample.ui.dialog.DatePickerDialogFragment
import com.sjbt.sdk.sample.utils.DateTimeUtils
import com.sjbt.sdk.sample.utils.FormatterUtil
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.viewLifecycleScope
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.util.Calendar
import java.util.Date


class EditUserInfoFragment : BaseFragment(R.layout.fragment_edit_user_info),
    DatePickerDialogFragment.Listener {

    private val TAG = "EditUserInfoFragment"
    private val viewBind: FragmentEditUserInfoBinding by viewBinding()
    private val userInfoRepository = Injector.getUserInfoRepository()
    private val authedUserId = Injector.requireAuthedUserId()
    private var info: UserInfo? = null
    private val userBirthday = "user_birthday"
    private var valueDate: Date? = null
    private val dateFormat = FormatterUtil.getFormatterYYYYMMMdd()
    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleScope.launchWhenStarted {
            info = userInfoRepository.getUserInfo(authedUserId)
            if (info == null) {
                Timber.d( "user info is null")
                return@launchWhenStarted
            }

            lifecycleScope.launchWhenStarted {
                UNIWatchMate.wmSettings.settingPersonalInfo.get().subscribe{ it->
                    UNIWatchMate.wmLog.logE("个人消息",""+it)
                }
            }

            valueDate = Date(info!!.birthYear - 1900, info!!.birthMonth - 1, info!!.birthDay)
            viewBind.editHeight.setText(info!!.height.toString())
            viewBind.editWeight.setText(info!!.weight.toString())
            viewBind.tvName.text = info!!.name
            viewBind.rgSex.check(
                if (info!!.sex) {
                    R.id.rb_sex_male
                } else {
                    R.id.rb_sex_female
                }
            )
            viewBind.piBirthday.getTextView().text = valueDate?.let { dateFormat.format(it) }
        }
        viewBind.btnSave.setOnClickListener {
            save()
        }
        viewBind.piBirthday.setOnClickListener {
            info?.let {
                val calendar = Calendar.getInstance()
                val end = Date()
                val start = DateTimeUtils.getDateBetween(calendar, end, -365 * 150)
                DatePickerDialogFragment.newInstance(
                    start = start,
                    end = end,
                    value = valueDate,
                    getString(R.string.account_edit_birthday),
                ).show(childFragmentManager, userBirthday)
            }
        }

    }

    private fun save() {
//        promptProgress.showProgress(R.string.action_loading)
        val height = viewBind.editHeight.text.trim().toString().toIntOrNull() ?: return
        if (height !in 50..300) {
            promptToast.showInfo(getString(R.string.account_height_error))
            return
        }
        val weight = viewBind.editWeight.text.trim().toString().toIntOrNull() ?: return
        if (weight !in 20..300) {
            promptToast.showInfo(R.string.account_weight_error)
            return
        }
        val sex = viewBind.rbSexMale.isChecked
//        val age = viewBind.editAge.text.trim().toString().toIntOrNull() ?: return
//        if (age !in 1..150) {
//            promptToast.showInfo(R.string.account_age_error)
//            return
//        }
        lifecycleScope.launchWhenStarted {
            info?.let { it ->
                it.birthYear = valueDate!!.year + 1900
                it.birthMonth = valueDate!!.month + 1
                it.birthDay = valueDate!!.date
                it.sex = sex
                it.weight = weight
                it.height = height
                userInfoRepository.setUserInfo(
                    it
                )
                if (deviceManager.flowConnectorState.value == WmConnectState.BIND_SUCCESS) {
                    val userInfo =
                        UNIWatchMate.wmSettings.settingPersonalInfo.set(it.toSdkUser()).doOnError {
                            ToastUtil.showToast(it.message)
                        }.await()
                    Timber.i( "userInfo save $userInfo")
                }
//                viewLifecycle.launchRepeatOnStarted {
//                    promptProgress.dismiss()
//                }
                findNavController().popBackStack()
            }
        }
    }

    override fun onDialogDatePicker(tag: String?, date: Date) {
        tag?.let { it ->
            when (it) {
                userBirthday -> {
                    valueDate = date
                    viewBind.piBirthday.getTextView()?.text = dateFormat.format(valueDate)
                }

                else -> {

                }
            }
        }
    }
}