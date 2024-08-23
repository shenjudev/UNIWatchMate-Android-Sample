package com.sjbt.sdk.sample.utils;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.transsion.imagepicker.loader.ImageLoader;

import java.io.File;

/**
 * author : ym
 * package_name : com.transsion.imagepicker.loader
 * class_name : GlideImageLoader
 * description :
 * time : 2021-12-29 10:35
 */
public class GlideImageLoader implements ImageLoader {
    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
        Glide.with(activity.getApplicationContext())
                //设置错误图片
                .load(Uri.fromFile(new File(path)))
                //缓存全尺寸
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    @Override
    public void displayImagePreview(Activity activity, String path, ImageView imageView, int width, int height) {
        Glide.with(activity.getApplicationContext())
                //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .load(Uri.fromFile(new File(path)))
                //缓存全尺寸
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /*@Override
    public void displayImage(Activity activity, Uri uri, ImageView imageView, int width, int height) {
        Glide.with(activity)                             //配置上下文
                .load(uri)       //设置错误图片
                .diskCacheStrategy(DiskCacheStrategy.RESULT)//缓存全尺寸
                .into(imageView);

    }

    @Override
    public void displayImagePreview(Activity activity, Uri uri, ImageView imageView, int width, int height) {
        Glide.with(activity)                             //配置上下文
                .load(uri)//设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)//缓存全尺寸
                .into(imageView);

    }*/

    @Override
    public void clearMemoryCache() {

    }
}
