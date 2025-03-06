package com.sjbt.sdk.sample.ui.device

import android.os.Bundle
import android.view.View
import com.base.api.UNIWatchMate
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentCustomMessageBinding
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.utils.BtUtils

class CustomMessageFragment : BaseFragment(R.layout.fragment_custom_message) {

    private val viewBind: FragmentCustomMessageBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        UNIWatchMate.observeCustomDataFromDevice.subscribe {
            viewBind.tvReceivedData.text = "收到消息：" + BtUtils.bytesToHexString(it)
            UNIWatchMate.sendCustomDataResponse(byteArrayOf(1)).subscribe ({

            },{

            })
        }

        viewBind.btnSend.setOnClickListener {
            val message = viewBind.etInput.text.toString()
            if (message.isEmpty()) {
                ToastUtil.showToast("请输入要发送的数据")
                return@setOnClickListener
            }

            if (message.length % 2 != 0) {
                ToastUtil.showToast("请输入正确的16进制数据")
                return@setOnClickListener
            }

            // 发送数据并等待回复
            UNIWatchMate.sendCustomDataWithResponse(1, BtUtils.hexStringToByteArray(message))
                .subscribe({ response ->
                    // 显示接收到的数据
                    viewBind.tvReceivedData.text = "收到消息：" + BtUtils.bytesToHexString(response)
                }, { error ->
                    ToastUtil.showToast("发送失败: ${error.message}")
                })
        }

        viewBind.btnSendNoReply.setOnClickListener {
            val message = viewBind.etInput.text.toString()
            if (message.isEmpty()) {
                ToastUtil.showToast("请输入要发送的数据")
                return@setOnClickListener
            }

            if (message.length % 2 != 0) {
                ToastUtil.showToast("请输入正确的16进制数据")
                return@setOnClickListener
            }

            // 发送数据不等待回复
            UNIWatchMate.sendCustomDataNoResponse(1, BtUtils.hexStringToByteArray(message))
                .subscribe({
                    ToastUtil.showToast("发送成功")
                }, { error ->
                    ToastUtil.showToast("发送失败: ${error.message}")
                })
        }
    }
} 