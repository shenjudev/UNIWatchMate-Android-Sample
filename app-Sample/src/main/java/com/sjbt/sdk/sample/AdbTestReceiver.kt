package com.sjbt.sdk.sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.ActivityUtils
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.utils.ToastUtil
import timber.log.Timber
import java.io.File

class AdbTestReceiver : BroadcastReceiver() {
    private var mContext: Context? = null
    override fun onReceive(context: Context, intent: Intent?) {
        mContext = context
        intent?.let {
            val dialPath = it.getStringExtra(INSTALL_DIAL_ACTION)
            if (!TextUtils.isEmpty(dialPath)) {
                Timber.i(dialPath)
                    if (ActivityUtils.getTopActivity() != null && ActivityUtils.getTopActivity() is BaseActivity) {
                        (ActivityUtils.getTopActivity() as BaseActivity).adbInstallDial(dialPath!!)
                    }
            }
        }
    }

    companion object {
        private const val INSTALL_DIAL_ACTION = "dial_path"
    }
}