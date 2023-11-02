package com.sjbt.sdk.sample.dialog

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.sjbt.sdk.sample.R


class InfoDialog : BaseDialog {
    private var mTip: String?
    private var mContext: Context
    private var mTvTip: TextView? = null

    constructor(context: Context, tip: String) : super(context) {
        mContext = context
        mTip = tip
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = View.inflate(mContext, R.layout.dialog_info, null)
        setContentView(view)
        mTvTip = findViewById(R.id.tv_tip)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setWindowParam(Gravity.CENTER, 0f, BaseDialog.Companion.ANIM_TYPE_NONE)
        if (!TextUtils.isEmpty(mTip)) {
            mTvTip!!.text = mTip
        }
    }

    fun updateTip(tip: String) {
        mTvTip?.text = tip
    }
}
