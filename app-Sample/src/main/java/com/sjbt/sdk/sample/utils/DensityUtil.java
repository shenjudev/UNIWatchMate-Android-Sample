package com.sjbt.sdk.sample.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;

/**
 * author : ym
 * package_name : com.transsion.basic.utils
 * class_name : DensityUtil
 * description : 屏幕密度单位转换工具类
 * time : 2021-10-21 10:02
 */
public class DensityUtil {

    /**
     * 获取屏幕的宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * dip转px
     *
     * @param dpValue
     * @return
     */
    public static int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     *
     * @deprecated 推荐使用不需要context的{@link #px2dip(float)}
     */
    @Deprecated
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * px转dpi
     *
     * @param pxValue px的值
     * @return 返回转换后的dip的值
     */
    public static int px2dip(float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * @param spValue
     * @param fontScale (DisplayMetrics类中的scaledDensity属性)
     * @return
     */
    public static int sp2pix(float spValue, float fontScale) {
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * sp转px
     *
     * @param spValue sp的值
     * @return 返回转换为像素后的值
     */
    public static int sp2px(float spValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale);
    }

    /**
     * px转sp
     *
     * @param context
     * @param pxValue
     * @return
     * @deprecated 推荐使用不传入context的{@link #px2sp(float)}
     */
    @Deprecated
    public static int px2sp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * px转s
     *
     * @param pxValue px的值
     * @return 返回转换后的px的值
     */
    public static int px2sp(float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 测量文字，x表示宽，y表示高
     *
     * @param paint
     * @param text
     * @return
     */
    public static Point measureText(Paint paint, String text) {
        if (paint == null || TextUtils.isEmpty(text)) {
            return new Point();
        }
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return new Point(rect.width(), rect.height());
    }
}
