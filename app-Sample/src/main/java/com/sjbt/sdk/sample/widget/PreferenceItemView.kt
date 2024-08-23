package com.sjbt.sdk.sample.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.sjbt.sdk.sample.R

class PreferenceItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private var name = ""
    private var showLine = false
    private var showSwitch = false

    private val tvName: TextView
    private val tvContent: TextView
    private val lineBottom: View
    private val ivRightArrow: ImageView
    private val switchRight: SwitchCompat

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.PreferenceItemView
        )
        name = a.getString(R.styleable.PreferenceItemView_name) ?: ""
        showLine = a.getBoolean(R.styleable.PreferenceItemView_showLine, true)
        showSwitch = a.getBoolean(R.styleable.PreferenceItemView_showSwitch, false)

        LayoutInflater.from(context).inflate(R.layout.item_preference, this)
        ivRightArrow = findViewById(R.id.iv_right_arrow)
        tvName = findViewById(R.id.tv_name)
        tvContent = findViewById(R.id.tv_content)
        lineBottom = findViewById(R.id.line_bottom)
        switchRight = findViewById(R.id.switch_right)
        if (showSwitch) {
            switchRight.visibility = View.VISIBLE
            ivRightArrow.visibility = View.GONE
        }
        tvName.text = name
        lineBottom.visibility = if (showLine) View.VISIBLE else View.INVISIBLE
    }

    public fun getTextView(): TextView {
        return tvContent
    }
    public fun getSwitchCompat(): SwitchCompat {
        return switchRight
    }
}