package com.sjbt.sdk.sample.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import com.blankj.utilcode.util.ScreenUtils
import com.sjbt.sdk.sample.R


abstract class BaseDialog : AppCompatDialog {
    constructor(context: Context) : super(context, R.style.CustomDialogTrans)
    constructor(context: Context, fullscreen: Boolean) : super(
        context,
        android.R.style.Theme_Black_NoTitleBar_Fullscreen
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * @param gravity   Gravity.TOP ...
     * @param widthRate
     */
    protected fun setWindowParam(gravity: Int, widthRate: Float, animType: Int) {
        val window = window
        val params = window!!.attributes
        val width = ScreenUtils.getScreenWidth()
        params.width =
            if (widthRate == 0f) (width * SCREEN_RATE).toInt() else (width * widthRate).toInt()
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.gravity = gravity
        when (animType) {
            ANIM_TYPE_NONE -> {}
            ANIM_TYPE_TOP_ENTER -> window.setWindowAnimations(R.style.dialog_animation_top_enter)
            ANIM_TYPE_BOTTOM_ENTER -> window.setWindowAnimations(R.style.dialog_animation_bottom_enter)
        }
        window.attributes = params
    }

    companion object {
        private const val SCREEN_RATE = 0.85f
        const val ANIM_TYPE_NONE = 0
        const val ANIM_TYPE_TOP_ENTER = 1
        const val ANIM_TYPE_BOTTOM_ENTER = 2
    }
}