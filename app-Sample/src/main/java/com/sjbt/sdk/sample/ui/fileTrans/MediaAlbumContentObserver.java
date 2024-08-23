package com.sjbt.sdk.sample.ui.fileTrans;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by noah on 2020/8/21.
 * Email:
 * Manual:
 */

public class MediaAlbumContentObserver extends ContentObserver {

    // 自定义的接口
    private       OnChangeListener onChangeListener;
    private       Uri              mUri;
//    private final Uri              videoUri  = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private final Uri              audioUri  = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//    private final Uri              imageUri  = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private final String           QUERY_URI = "content://media/external";

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public MediaAlbumContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.i("TAG", "onChange: " + uri);
        //发生变化，筛选Uri
        boolean isAdd = false;
        if (uri.toString().equalsIgnoreCase(null != mUri ? mUri.toString() : null)) {     //重复返回
            return;
        }
        if (uri.toString().contains("?")) {    //相机锁定状态回调不处理
            return;
        }
//        if (uri.toString().contains(videoUri.toString())) {
//            isAdd = true;
//            if (!uri.toString().contains(videoUri.toString() + "/")) {   //无文件id不处理
//                return;
//            }
//        }
        if (uri.toString().contains(audioUri.toString())) {
            isAdd = true;
            if (!uri.toString().contains(audioUri.toString() + "/")) {   //无文件id不处理
                return;
            }
        }
        //删除动作 返回的uri为"content://media/external"，并且会返回多次，这里无法判断是返回多次还是连续删除
        if (!isAdd && !uri.toString().equalsIgnoreCase(QUERY_URI)) {
            return;
        }
        if (onChangeListener != null) {
            if (isAdd) {   //由于删除返回的uri是一致的，因此不能避免多次回调
                mUri = uri;
            }
            onChangeListener.onChange(isAdd, uri);
        }
    }


    // 接口的set方法
    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    // 定义接口
    public interface OnChangeListener {
        void onChange(boolean isAdd, Uri uri);
    }

}
