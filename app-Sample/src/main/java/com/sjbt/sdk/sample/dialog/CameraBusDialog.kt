package com.sjbt.sdk.sample.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sjbt.sdk.sample.R

/**
 * 权限提醒
 */
class CameraBusDialog(
    private val mContext: Context,
    private val type: Int,
    private val mCallBack: CallBack<Int>?
) : BaseDialog(
    mContext
), View.OnClickListener {
    private var mBtnCancel: TextView? = null
    private var mBtnOk: TextView? = null
    private var mTvTip: TextView? = null
    private var ivCamera: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = View.inflate(mContext, R.layout.dialog_camera, null)
        setContentView(view)
        mBtnCancel = findViewById(R.id.btn_cancel)
        mBtnOk = findViewById(R.id.btn_ok)
        mTvTip = findViewById(R.id.tv_tip)
        ivCamera = findViewById(R.id.iv_camera_icon)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setWindowParam(Gravity.CENTER, 0f, BaseDialog.Companion.ANIM_TYPE_NONE)
        mBtnCancel!!.setOnClickListener(this)
        mBtnOk!!.setOnClickListener(this)
        if (type == TIP_TYPE_OPEN_CAMERA) {
            mBtnOk!!.visibility = View.VISIBLE
            mBtnCancel!!.visibility = View.GONE
            mBtnOk!!.text = mContext.getString(R.string.sure)
            mTvTip!!.text = mContext.getString(R.string.camera_open_tip)
            ivCamera!!.setImageResource(R.mipmap.biu_icon_open_camera)
        } else if (type == TIP_TYPE_OPEN_CAMERA_PERMISSION) {
            mBtnOk!!.visibility = View.VISIBLE
            mBtnCancel!!.text = mContext.getString(R.string.cancel)
            mBtnOk!!.text = mContext.getString(R.string.open_camera_permission)
            mTvTip!!.text = mContext.getString(R.string.camera_permission_reject)
            ivCamera!!.setImageResource(R.mipmap.biu_icon_no_camera_permission)
        } else if (type == TIP_TYPE_OPEN_STORAGE) {
            mBtnOk!!.visibility = View.VISIBLE
            mBtnCancel!!.text = mContext.getString(R.string.cancel)
            mBtnOk!!.text = mContext.getString(R.string.open_camera_permission)
            mTvTip!!.text = mContext.getString(R.string.open_storage_permission_tip)
            ivCamera!!.setImageResource(R.mipmap.biu_icon_no_camera_permission)
        } else if (type == TIP_TYPE_OPEN_LOCATION) {
            mBtnOk!!.visibility = View.VISIBLE
            mBtnCancel!!.text = mContext.getString(R.string.cancel)
            mBtnOk!!.text = mContext.getString(R.string.open_location_permission)
            mTvTip!!.text = mContext.getString(R.string.open_location_permission_tip)
            ivCamera!!.setImageResource(R.mipmap.biu_icon_location_tip)
        }
    }

    override fun onClick(view: View) {
        if (view == mBtnCancel) {
            dismiss()
        } else if (view == mBtnOk) {
            dismiss()
            mCallBack?.callBack(type)
        }
    }

    companion object {
        const val TIP_TYPE_OPEN_CAMERA = 1
        const val TIP_TYPE_OPEN_CAMERA_PERMISSION = 2
        const val TIP_TYPE_OPEN_STORAGE = 3
        const val TIP_TYPE_OPEN_LOCATION = 4
    }
}