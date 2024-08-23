package com.sjbt.sdk.sample.base

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.base.sdk.entity.apps.WmConnectState
import com.blankj.utilcode.util.FileUtils
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.di.internal.SingleInstance
import com.sjbt.sdk.sample.dialog.CallBack
import com.sjbt.sdk.sample.dialog.ConfirmDialog
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.ui.device.dial.library.DialLibraryDfuDialogFragment
import com.sjbt.sdk.sample.ui.dialog.LoadingDialog
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.promptProgress
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import java.io.File

abstract class BaseActivity : AppCompatActivity() {
    protected var mConfirmDialog: ConfirmDialog? = null
    protected var mFindPhoneDialog: ConfirmDialog? = null
    private var loading_Dialog: LoadingDialog? = null
    protected var mHandler = Handler(Looper.getMainLooper())
    protected var isFront = false
    protected val promptProgress by promptProgress()

    override fun setRequestedOrientation(requestedorientation: Int) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentorFloating) {
            return
        }
        super.setRequestedOrientation(requestedorientation)
    }

    private val isTranslucentorFloating: Boolean
        get() {
            var isTranslucentorFloating = false
            try {
                val styleableRes = Class.forName("com.android.internal.R\$styleable")
                    .getField("Window")[null] as IntArray
                val typedArray = obtainStyledAttributes(styleableRes)
                val m = ActivityInfo::class.java.getMethod(
                    "isTranslucentOrFloating",
                    TypedArray::class.java
                )
                m.isAccessible = true
                isTranslucentorFloating = m.invoke(null, typedArray) as Boolean
                m.isAccessible = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return isTranslucentorFloating
        }

    fun showConfirmDialogWithCallback(tip: String?, btnName: String?, callBack: CallBack<String>?) {
        runOnUiThread(Runnable {
            if (mConfirmDialog != null && mConfirmDialog!!.isShowing) {
                return@Runnable
            }
            mConfirmDialog = ConfirmDialog(this@BaseActivity, tip, btnName, callBack)
            try {
                mConfirmDialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun hideConfirmDialog() {
        hideDialog(mConfirmDialog)
    }


    fun showFindPhoneDialogWithCallback(
        tip: String?,
        btnName: String?,
        callBack: CallBack<String>?
    ) {
        runOnUiThread(Runnable {
            if (mConfirmDialog != null && mFindPhoneDialog!!.isShowing) {
                return@Runnable
            }
            mFindPhoneDialog = ConfirmDialog(this@BaseActivity, tip, btnName, callBack)
            try {
                mFindPhoneDialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun hideStopFindPhonemDialog() {
        hideDialog(mFindPhoneDialog)
    }

    protected fun hideDialog(dialog: Dialog?) {
        runOnUiThread {
            if (dialog != null && dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }

    fun showLoadingDlg() {
        runOnUiThread(Runnable {
            if (loading_Dialog != null) {
                loading_Dialog!!.dismiss()
                loading_Dialog = null
            }
            loading_Dialog = LoadingDialog(this@BaseActivity)
            if (isFinishing) {
                return@Runnable
            }
            try {
                loading_Dialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun showLoadingDlg(msg: String?) {
        runOnUiThread(Runnable {
            if (loading_Dialog != null) {
                loading_Dialog!!.dismiss()
                loading_Dialog = null
            }
            if (isFinishing) {
                return@Runnable
            }
            loading_Dialog = LoadingDialog(this@BaseActivity, msg)
            try {
                loading_Dialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun hideLoadingDlg() {
        hideDialog(loading_Dialog)
    }

    override fun onDestroy() {
        hideDialog(loading_Dialog)
        hideDialog(mConfirmDialog)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        isFront = false
    }

    fun adbInstallDial(dialPath: String) {
        startLocalUpdate(File(dialPath))
    }

    private fun startLocalUpdate(file: File?) {
        if (file == null || !file.exists()) {
            ToastUtil.showToast(getString(R.string.error_up_file))
            return
        }
        if (file.length() < 100) { //长度小于100
            ToastUtil.showToast(getString(R.string.error_up_file))
            return
        }
        val extension = FileUtils.getFileExtension(file)
        try {
            if (!TextUtils.isEmpty(extension)) {
                if (extension != BTConfig.DIAL) {
                    ToastUtil.showToast(getString(R.string.error_up_file))
                    return
                }
            }
            CacheDataHelper.setTransferring(true)
            startOta(file)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToast(getString(R.string.error_up_file))
            CacheDataHelper.setTransferring(false)
            return
        }
    }

    private fun startOta(dialFile: File) {
        Injector.getApplicationScope().launchWithLog {
            runCatchingWithLog {
                if (SingleInstance.deviceManager.flowConnectorState.value != WmConnectState.BIND_SUCCESS) {
                    ToastUtil.showToast(getString(R.string.device_state_disconnected))
                    return@launchWithLog
                }
                val fileList = mutableListOf<File>()
                dialFile.let { file ->
                    fileList.add(file)
                    val extension: String = FileUtils.getFileExtension(file)
                    if (extension == BTConfig.DIAL) {
                        val dialMock = DialMock(-2, file.absolutePath, -1, "")
                        DialLibraryDfuDialogFragment.newInstance(dialMock)
                            .show(supportFragmentManager, null)
                    }
                }
            }
        }
    }

    companion object {

    }
}