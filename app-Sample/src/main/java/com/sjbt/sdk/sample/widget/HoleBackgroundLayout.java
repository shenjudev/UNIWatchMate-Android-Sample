package com.sjbt.sdk.sample.widget;

import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sjbt.sdk.sample.R;


/**
 * * 能够局部透明的layout，也就是将background处理成带洞洞的效果 <br/>
 *  * 当然了，形状要你自己指定，目前想不到好的思路自动处理各种形状，有的话就直接完善了 <br/>
 *  * 根据个crop_image_cover_view_hole子View的位置，确定透明区域 <br/>
 * ————————————————
 *
 * created by luozhaoliang .
 * e-mail : luozhaoliang@shenjugroup.com
 * time   : 2021/06/16
 */
public class HoleBackgroundLayout extends FrameLayout {

    private HoleDrawable background;

    public HoleBackgroundLayout(@NonNull Context context) {
        super(context);
        initView(context, null, 0);
    }

    public HoleBackgroundLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public HoleBackgroundLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        background = new HoleDrawable(getBackground());
        setBackground(background);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        resetBackgroundHoleArea();
    }

    public void resetBackgroundHoleArea() {
        Path path = null;
        // 以crop_image_cover_view_hole子View为范围构造需要透明显示的区域
        View v0 = findViewById(R.id.crop_image_cover_view_hole);
        if (v0 != null) {
            path = new Path();
            // 矩形透明区域
            path.addRect(v0.getLeft(), v0.getTop(), v0.getRight(), v0.getBottom(), Path.Direction.CW);
        }
        if (path != null) {
            background.setSrcPath(path);
        }
    }
}
