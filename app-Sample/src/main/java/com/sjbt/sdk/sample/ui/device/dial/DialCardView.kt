package com.sjbt.sdk.sample.ui.device.dial

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import com.github.kilnn.tool.ui.DisplayUtil

class DialCardView : CardView {

    private lateinit var contentView: View

    private var aspectRatio = 1.0f //宽高比

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        cardElevation = DisplayUtil.dip2px(context, 1F).toFloat()
        val padding = DisplayUtil.dip2px(context, 2F)
        setContentPadding(padding, padding, padding, padding)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!this::contentView.isInitialized) {
            contentView = getChildAt(0)
        }
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val lp = contentView.layoutParams as LayoutParams
        val imgWidth = widthSize - contentPaddingLeft - contentPaddingRight - lp.leftMargin - lp.rightMargin
        val imgHeight = (imgWidth / aspectRatio).toInt()
        val heightSize = imgHeight + contentPaddingTop + contentPaddingBottom + lp.topMargin + lp.bottomMargin
        setMeasuredDimension(widthSize, heightSize)
    }

}
