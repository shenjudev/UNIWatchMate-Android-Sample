package com.sjbt.sdk.sample.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.base.api.UNIWatchMate;
import com.blankj.utilcode.util.AppUtils;
import com.sjbt.sdk.sample.MyApplication;
import com.sjbt.sdk.sample.base.Config;
import com.sjbt.sdk.sample.ui.fileTrans.LocalFileBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取本地音乐文件列表
 */
public class AudioUtils {

    private static final long MAX_LEN = 20 * 1024 * 1024;
    private static final String TAG  = "AudioUtils";

    /**
     * 获取sd卡所有的音乐文件
     *
     * @return
     * @throws Exception
     */
    public static ArrayList<LocalFileBean> initLocalSongs(Context context) {

        ArrayList<LocalFileBean> localFileBeans = new ArrayList<LocalFileBean>();

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID},
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
//                new String[]{"audio/mpeg", "audio/x-ms-wma"},
                new String[]{"audio/mpeg"},//限制只有mp3

                null);

        if (cursor.moveToFirst()) {
            LocalFileBean localFileBean = null;
            do {
                localFileBean = new LocalFileBean();

                localFileBean.setMediaId(cursor.getString(0));
                // 文件名
                localFileBean.setFileName(cursor.getString(1));
                // 歌曲名
                localFileBean.setTitle(cursor.getString(2));
                // 时长
                localFileBean.setDuration(cursor.getInt(3));

                // 歌手名
                String singerName = cursor.getString(4);
                if (!TextUtils.isEmpty(singerName) && !singerName.equals("<unknown>")) {
                    localFileBean.setSinger(cursor.getString(4));
                }

                // 专辑名
                localFileBean.setAlbum(cursor.getString(5));
                // 年代
                if (cursor.getString(6) != null) {
                    localFileBean.setYear(cursor.getString(6));
                }

                // 歌曲格式
                if ("audio/mpeg".equals(cursor.getString(7).trim())) {
                    localFileBean.setType(Config.FILE_TYPE_MP3);
                }
//                else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
//                    localFileBean.setType("wma");
//                }

                // 文件大小
                if (cursor.getString(8) != null) {
                    int fileLen = cursor.getInt(8);
                    if (fileLen <= MAX_LEN && fileLen > 10) {
                        localFileBean.setSize(formatFileSize(fileLen));
                    } else {
                        continue;
                    }
                }
                // 文件路径
                if (cursor.getString(9) != null) {
                    localFileBean.setFileUrl(cursor.getString(9));
                }

//                LogUtils.logBlueTooth("文件路径:"+localFileBean.getFileUrl());

                try {
                    Long album_id = cursor.getLong(10);
                    localFileBean.setAlbumId(album_id);
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                }

                if (localFileBean.getFileName().contains(".mp3")) {
                    localFileBeans.add(localFileBean);
                }


            } while (cursor.moveToNext());

            cursor.close();
        }
        return localFileBeans;
    }

    /**
     * 扫描全局文件变化
     *
     * @param context
     */
    public static void scanMusicFiles(Context context) {
        MediaScannerConnection.scanFile(context, new String[]{Environment
                .getExternalStorageDirectory().getAbsolutePath()}, null, null);
    }

    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }

    private static final String AUTHORITY = AppUtils.getAppPackageName() + ".provider"; // 替换为你的 FileProvider 的 authority

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void readTxtFiles(Context context) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        Uri documentUri = DocumentsContract.buildRootUri(AUTHORITY, DocumentsContract.getTreeDocumentId(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()))));

        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(intent);
    }

    public static File getDownloadDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static List<LocalFileBean> readTxtFilesInDownloads(Context context) {
        List<LocalFileBean> localFileBeans = new ArrayList<>();
        File downloadDirectory = getDownloadDirectory();
        if (downloadDirectory != null) {
            File[] files = downloadDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".txt")) {
                        Uri fileUri = FileProvider.getUriForFile(context, AUTHORITY, file);
                        LocalFileBean localFileBean = new LocalFileBean();

                        String name = file.getName(); // 获取文件名，不包含扩展名
                        String path = file.getPath();  // 获取文件实际路径

                        localFileBean.setFileName(name);
                        localFileBean.setFileUrl(path);
                        localFileBean.setType(Config.FILE_TYPE_TXT);

                        float size = file.length();
                        if (size <= MAX_LEN && size > 0) {
                            localFileBean.setSize((size + "").substring(0, 4) + "M");
                            localFileBeans.add(localFileBean);
                        }
                    }
                }
            }
        }

        return localFileBeans;
    }

    private void getTxtFilesByCur(Context context) {
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME
        };

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";  // 过滤条件：只获取 MIME 类型为 text/plain 的文件
        String[] selectionArgs = new String[]{"text/plain"};

        Cursor cursor = MyApplication.Companion.getInstance().getContentResolver().query(uri, projection, selection, selectionArgs, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {

                int dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                String filePath = cursor.getString(dataIndex);
                UNIWatchMate.INSTANCE.getWmLog().logD(TAG, "scan filePath:" + filePath);
            }

            cursor.close();
        }
    }

    public static List<LocalFileBean> getTxtFiles(Context context) {
        List<LocalFileBean> localFileBeans = new ArrayList<>();
        String[] columns = new String[]{
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE
        };
        Uri uri = MediaStore.Files.getContentUri("external");
//        String selection = "(" + MediaStore.Files.FileColumns.MIME_TYPE + "=='text/plain')";

        String selection = MediaStore.Audio.Media.MIME_TYPE + "=? or "
                + MediaStore.Audio.Media.MIME_TYPE + "=?";

        String mimeType = "text/plain";
        String[] selectionArgs = new String[]{"%" + mimeType + "%"};

        Cursor c = MyApplication.Companion.getInstance().getContentResolver().query(uri, columns, selection, selectionArgs, MediaStore.Files.FileColumns.SIZE + " DESC");
        if (c == null) return localFileBeans;

        if (c.moveToFirst()) {
            int dataIndex = c.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int titleIndex = c.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
            int dataSize = c.getColumnIndex(MediaStore.Files.FileColumns.SIZE);

            do {
                String name = c.getString(titleIndex); // 获取文件名，不包含扩展名
                String path = c.getString(dataIndex);  // 获取文件实际路径
                UNIWatchMate.INSTANCE.getWmLog().logD(TAG,"读取文件名称：" + name + " > " + path);

                LocalFileBean localFileBean = new LocalFileBean();

                localFileBean.setFileName(name);
                localFileBean.setFileUrl(path);
                localFileBean.setType(Config.FILE_TYPE_TXT);

                if (dataSize <= MAX_LEN && dataSize > 0) {
                    localFileBean.setSize(formatFileSize(dataSize));
                    localFileBeans.add(localFileBean);
                }
            } while (c.moveToNext()); // 循环获取文件
        }
        c.close();

        return localFileBeans;

    }

    public static List<LocalFileBean> getLocalDwonloadTxtFiles() {
        List<LocalFileBean> localFileBeans = new ArrayList<>();
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        List<File> txtFiles = findTxtFiles(root);
        for (File file : txtFiles) {
            if (!file.canRead()) continue;

            LocalFileBean localFileBean = new LocalFileBean();

            String name = file.getName(); // 获取文件名，不包含扩展名
            String path = file.getPath();  // 获取文件实际路径
            localFileBean.setFileName(name);
            localFileBean.setFileUrl(path);
            localFileBean.setType(Config.FILE_TYPE_TXT);
            long dataSize = file.length();

            if (dataSize <= MAX_LEN && dataSize > 0) {
                localFileBean.setSize(formatFileSize(file.length()));
                localFileBeans.add(localFileBean);
            }

        }

        return localFileBeans;

    }

    private static List<File> findTxtFiles(File dir) {
        List<File> txtFiles = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    txtFiles.addAll(findTxtFiles(file));
                } else {
                    String filename = file.getName().toLowerCase();
                    if (filename.endsWith(".txt")) {
                        txtFiles.add(file);
                    }
                }
            }
        }
        return txtFiles;
    }

    public static String formatFileSize(long size) {
        // 定义文件大小单位
        String[] units = {"bytes", "Kb", "MB"};

        int unitIndex = 0;
        double fileSize = size;

        // 根据文件大小进行单位转换
        while (fileSize > 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        // 格式化文件大小，保留一位小数
        return String.format("%.1f %s", fileSize, units[unitIndex]);
    }

}