package com.sjbt.sdk.sample.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.sjbt.sdk.sample.dialog.InfoDialog
import com.sjbt.sdk.sample.utils.promptToast
import com.sjbt.sdk.sample.utils.promptProgress

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    protected var mInfoDialog: InfoDialog? = null
    protected val promptToast by promptToast()
    protected val promptProgress by promptProgress()

    fun showInfoDialog(tip: String) {
        activity?.runOnUiThread(Runnable {
            if (mInfoDialog != null && mInfoDialog!!.isShowing) {
                mInfoDialog!!.updateTip(tip)
                return@Runnable
            }
            mInfoDialog = InfoDialog(requireContext(), tip)
            try {
                mInfoDialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun hideInfoDialog() {
        activity?.runOnUiThread {
            if (mInfoDialog != null && mInfoDialog!!.isShowing) {
                mInfoDialog?.dismiss()
            }
        }
    }

}