package com.sjbt.sdk.sample.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.android.material.color.MaterialColors

class ProgressDotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val dotCount = 5
    private val dotSize: Int
    private val dotPadding: Int

    private val loadingColor: Int
    private val failedColor: Int
    private val successColor: Int

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

    private var state: Int = 0

    fun setLoading() {
        state = 0
        progressIndex = 1
        invalidate()
    }

    fun setFailed() {
        state = 1
        invalidate()
    }

    fun setSuccess() {
        state = 2
        invalidate()
    }

    private var progressIndex = 1

    init {
        setWillNotDraw(false)
        dotSize = SizeUtils.dp2px( 8f)
        dotPadding = SizeUtils.dp2px( 5f)
        loadingColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimaryVariant)
        failedColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorError)
        successColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary)
        paint.color = failedColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(dotSize * dotCount + dotPadding * (dotCount + 1), dotSize + dotPadding * 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val dotHalfSize = dotSize / 2.0f
        // o o o o o
        if (state == 1 || state == 2) {
            paint.color = if (state == 1) {
                failedColor
            } else {
                successColor
            }
            for (i in 1..dotCount) {
                canvas.drawCircle(
                    dotPadding * i + dotSize * (i - 1) + dotHalfSize,
                    dotPadding + dotHalfSize,
                    dotHalfSize,
                    paint
                )
            }
        } else {
            val activeIndex = progressIndex % (dotCount + 1)
            for (i in 1..dotCount) {
                paint.color = if (i == activeIndex) {
                    successColor
                } else {
                    loadingColor
                }
                canvas.drawCircle(
                    dotPadding * i + dotSize * (i - 1) + dotHalfSize,
                    dotPadding + dotHalfSize,
                    dotHalfSize,
                    paint
                )
            }
            progressIndex++
            postInvalidateDelayed(200)
        }
    }

}