package com.sjbt.sdk.sample.dialog

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.sjbt.sdk.sample.R

/**
 * 用于操作确认弹窗(For operation confirmation popup)
 */
class ConfirmDialog : BaseDialog {
    private var mTip: String?
    private var mBtnName: String? = null
    private var mContext: Context
    private var mCallBack: CallBack<String>? = null

    constructor(context: Context, tip: String) : super(context) {
        mContext = context
        mTip = tip
    }

    constructor(context: Context, tip: String, btnName: String?) : super(context) {
        mContext = context
        mTip = tip
        mBtnName = btnName
    }

    constructor(
        context: Context,
        tip: String?,
        btnName: String?,
        callBack: CallBack<String>?
    ) : super(context) {
        mContext = context
        mTip = tip
        mBtnName = btnName
        mCallBack = callBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = View.inflate(mContext, R.layout.dialog_confirm, null)
        setContentView(view)
        val mBtnOk = findViewById<TextView>(R.id.btn_ok)
        val mTvTip = findViewById<TextView>(R.id.tv_tip)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setWindowParam(Gravity.CENTER, 0f, BaseDialog.Companion.ANIM_TYPE_NONE)
        if (!TextUtils.isEmpty(mTip)) {
            mTvTip!!.text = mTip
        }
        if (!TextUtils.isEmpty(mBtnName)) {
            mBtnOk!!.text = mBtnName
        }
        mBtnOk!!.setOnClickListener {
            if (mCallBack != null) {
                mCallBack!!.callBack(mTvTip!!.text.toString())
            }
            dismiss()
        }
    }
}