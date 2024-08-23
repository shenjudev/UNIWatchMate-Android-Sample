package com.transsion.imagepicker;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.transsion.imagepicker.bean.ImageFolder;
import com.transsion.imagepicker.bean.ImageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：加载手机图片和视频实现类
 * 修订历史：
 * ================================================
 */
public class MediaDataSource implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_ALL = 0;         //加载所有图片和视频
    public static final int LOADER_CATEGORY = 1;    //分类加载图片和视频
    private final String[] MEDIA_PROJECTION = {     //查询图片和视频需要的数据列
            MediaStore.Files.FileColumns.DISPLAY_NAME,   //显示名称  aaa.jpg
            MediaStore.Files.FileColumns.DATA,           //真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Files.FileColumns.SIZE,           //大小，long型  132492
            MediaStore.Files.FileColumns.WIDTH,          //宽度，int型  1920
            MediaStore.Files.FileColumns.HEIGHT,         //高度，int型  1080
            MediaStore.Files.FileColumns.MIME_TYPE,      //类型     image/jpeg
            MediaStore.Files.FileColumns.DATE_ADDED,     //添加的时间，long型  1450518608
            MediaStore.Video.VideoColumns.DURATION,      //时长
    };

    private final String MEDIA_SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
            + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


    private final static String TAG = MediaDataSource.class.getSimpleName();

    private FragmentActivity activity;
    private OnImagesLoadedListener loadedListener;                     //图片和视频加载完成的回调接口
    private ArrayList<ImageFolder> imageFolders = new ArrayList<>();   //所有的图片和视频文件夹

    /**
     * @param activity       用于初始化LoaderManager，需要兼容到2.3
     * @param path           指定扫描的文件夹目录，可以为 null，表示扫描所有图片和视频
     * @param loadedListener 图片和视频加载完成的监听
     */
    public MediaDataSource(FragmentActivity activity, String path, OnImagesLoadedListener loadedListener) {
        this.activity = activity;
        this.loadedListener = loadedListener;
        LoaderManager loaderManager = LoaderManager.getInstance(activity);
        Log.d(TAG, "准备加载相册图片和视频");
        if (path == null) {
            loaderManager.initLoader(LOADER_ALL, null, this);//加载所有的图片和视频
        } else {
            //加载指定目录的图片和视频
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            loaderManager.initLoader(LOADER_CATEGORY, bundle, this);
        }
    }

    public void setLoadedListener(OnImagesLoadedListener loadedListener) {
        this.loadedListener = loadedListener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "创建图片和视频加载器");
        CursorLoader cursorLoader = null;
        //扫描所有图片和视频
        if (id == LOADER_ALL)
            cursorLoader = new CursorLoader(activity, MediaStore.Files.getContentUri("external"), MEDIA_PROJECTION, MEDIA_SELECTION, null, MEDIA_PROJECTION[6] + " DESC");
        //扫描某个图片和视频文件夹
        if (id == LOADER_CATEGORY)
            cursorLoader = new CursorLoader(activity, MediaStore.Files.getContentUri("external"), MEDIA_PROJECTION, MEDIA_PROJECTION[1] + " like '%" + args.getString("path") + "%'", null, MEDIA_PROJECTION[6] + " DESC");

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "图片和视频加载完成");
        if (loadedListener == null) {
            return;
        }
        new Thread(() -> {
            try {
                imageFolders.clear();
                if (data != null && data.getCount() > 0) {
                    data.moveToFirst();
                    ArrayList<ImageItem> allImages = new ArrayList<>();   //所有图片和视频的集合,不分文件夹
                    while (data.moveToNext()) {
                        //查询数据
                        String imageName = data.getString(data.getColumnIndexOrThrow(MEDIA_PROJECTION[0]));
                        String imagePath = data.getString(data.getColumnIndexOrThrow(MEDIA_PROJECTION[1]));

                        if (TextUtils.isEmpty(imagePath)) {
                            continue;
                        }
                        File file = new File(imagePath);
                        if (!file.exists() || file.length() <= 0) {
                            continue;
                        }

                        long imageSize = data.getLong(data.getColumnIndexOrThrow(MEDIA_PROJECTION[2]));
                        int imageWidth = data.getInt(data.getColumnIndexOrThrow(MEDIA_PROJECTION[3]));
                        int imageHeight = data.getInt(data.getColumnIndexOrThrow(MEDIA_PROJECTION[4]));
                        String imageMimeType = data.getString(data.getColumnIndexOrThrow(MEDIA_PROJECTION[5]));
                        long imageAddTime = data.getLong(data.getColumnIndexOrThrow(MEDIA_PROJECTION[6]));
                        long duration = data.getLong(data.getColumnIndexOrThrow(MEDIA_PROJECTION[7]));

                        //封装实体
                        ImageItem imageItem = new ImageItem();
                        imageItem.name = imageName;
                        imageItem.path = imagePath;
                        imageItem.size = imageSize;
                        imageItem.width = imageWidth;
                        imageItem.height = imageHeight;
                        imageItem.mimeType = imageMimeType;
                        imageItem.addTime = imageAddTime;
                        imageItem.duration = duration;
                        imageItem.isVideo = duration > 0;

                        Log.d("MEDIA", "媒体信息：" + imageItem);

                        if (imageItem.mimeType.contains("image/jpeg") || (imageItem.mimeType.contains("video/mp4") && imageItem.path.endsWith(".mp4"))) {
                            allImages.add(imageItem);
                            //根据父路径分类存放图片和视频
                            File imageFile = new File(imagePath);
                            File imageParentFile = imageFile.getParentFile();
                            ImageFolder imageFolder = new ImageFolder();
                            imageFolder.name = imageParentFile.getName();
                            imageFolder.path = imageParentFile.getAbsolutePath();

                            if (!imageFolders.contains(imageFolder)) {
                                ArrayList<ImageItem> images = new ArrayList<>();
                                images.add(imageItem);
                                imageFolder.cover = imageItem;
                                imageFolder.images = images;
                                imageFolders.add(imageFolder);
                            } else {
                                imageFolders.get(imageFolders.indexOf(imageFolder)).images.add(imageItem);
                            }
                        }
                    }
                    //防止没有图片和视频报异常
                    if (data.getCount() > 0 && allImages.size() > 0) {
                        //构造所有图片和视频的集合
                        ImageFolder allImagesFolder = new ImageFolder();
                        allImagesFolder.name = activity.getResources().getString(R.string.ip_all_images);
                        allImagesFolder.path = "/";
                        allImagesFolder.cover = allImages.get(0);
                        allImagesFolder.images = allImages;
                        imageFolders.add(0, allImagesFolder);  //确保第一条是所有图片和视频
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            //回调接口，通知图片和视频数据准备完成
            ImagePicker.getInstance().setImageFolders(imageFolders);
            if (loadedListener != null) {
                loadedListener.onImagesLoaded(imageFolders);
            }
        }).start();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        System.out.println("--------");
    }

    /**
     * 所有图片和视频加载完成的回调接口
     */
    public interface OnImagesLoadedListener {
        void onImagesLoaded(List<ImageFolder> imageFolders);
    }
}
