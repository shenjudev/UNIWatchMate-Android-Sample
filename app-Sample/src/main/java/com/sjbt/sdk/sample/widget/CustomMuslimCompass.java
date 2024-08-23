package com.sjbt.sdk.sample.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.sjbt.sdk.sample.R;


public class CustomMuslimCompass extends View {
    private Bitmap compassBgIcon;
    private Bitmap qiblaHouseIcon;
    private Bitmap pointerIcon;
    private float qiblaHouseAngle; // 角度
    private Paint paint;

    public CustomMuslimCompass(Context context) {
        super(context);
        init(null);
    }

    public CustomMuslimCompass(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomMuslimCompass(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        qiblaHouseAngle = 0; // 初始角度为0

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CustomIconView,
                    0, 0);

            try {
                int compassBgIconResId = a.getResourceId(R.styleable.CustomIconView_compassBgIcon, 0);
                int qiblaHouseIconResId = a.getResourceId(R.styleable.CustomIconView_qiblaHouseIcon, 0);
                int pointerIconResId = a.getResourceId(R.styleable.CustomIconView_pointerIcon, 0);
                qiblaHouseAngle = a.getFloat(R.styleable.CustomIconView_qiblaHouseAngle, 0);

                if (compassBgIconResId != 0) {
                    compassBgIcon = BitmapFactory.decodeResource(getResources(), compassBgIconResId);
                }

                if (qiblaHouseIconResId != 0) {
                    qiblaHouseIcon = BitmapFactory.decodeResource(getResources(), qiblaHouseIconResId);
                }

                if (pointerIconResId != 0) {
                    pointerIcon = BitmapFactory.decodeResource(getResources(), pointerIconResId);
                }
            } finally {
                a.recycle();
            }
        }
    }

    public void setCompassBgIcon(Bitmap bitmap) {
        this.compassBgIcon = bitmap;
        invalidate();
    }

    public void setQiblaHouseIcon(Bitmap bitmap) {
        this.qiblaHouseIcon = bitmap;
        invalidate();
    }

    public void setPointerIcon(Bitmap bitmap) {
        this.pointerIcon = bitmap;
        invalidate();
    }

    public void setQiblaHouseAngle(float angle) {
        this.qiblaHouseAngle = angle;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (compassBgIcon == null || qiblaHouseIcon == null || pointerIcon == null) {
            return;
        }

        // 计算大图标的绘制范围
        float bigRadius = Math.min(getWidth(), getHeight()) / 2.0f - qiblaHouseIcon.getWidth() / 2.0f;

        RectF bigIconRect = new RectF(getWidth() / 2.0f - bigRadius, getHeight() / 2.0f - bigRadius,
                getWidth() / 2.0f + bigRadius, getHeight() / 2.0f + bigRadius);

        // 绘制大图标
        canvas.drawBitmap(compassBgIcon, null, bigIconRect, paint);

        // 计算小图标的中心点坐标
        float smallCenterX = getWidth() / 2.0f + bigRadius * (float) Math.cos(Math.toRadians(qiblaHouseAngle));
        float smallCenterY = getHeight() / 2.0f + bigRadius * (float) Math.sin(Math.toRadians(qiblaHouseAngle));

        // 创建Matrix用于旋转小图标
        Matrix matrix = new Matrix();
        matrix.postTranslate(-qiblaHouseIcon.getWidth() / 2, -qiblaHouseIcon.getHeight() / 2); // 将小图标中心点移到原点
        matrix.postRotate(qiblaHouseAngle + 90); // 旋转小图标，使其垂直于大图标的切线
        matrix.postTranslate(smallCenterX, smallCenterY); // 将小图标移动到正确的位置

        // 绘制小图标
        canvas.drawBitmap(qiblaHouseIcon, matrix, paint);

        // 绘制箭头图标
        matrix.reset();
        matrix.postTranslate(-pointerIcon.getWidth() / 2, -pointerIcon.getHeight() / 2); // 将箭头图标中心点移到原点
        matrix.postRotate(qiblaHouseAngle + 90); // 旋转箭头图标，使其指向小图标
        matrix.postTranslate(getWidth() / 2.0f, getHeight() / 2.0f); // 将箭头图标移动到圆心

        canvas.drawBitmap(pointerIcon, matrix, paint);
    }
}
