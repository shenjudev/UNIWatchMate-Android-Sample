package com.sjbt.sdk.sample.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;

public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    public interface ScanListener {
        public void onScanFinish(String path, Uri uri);
    }

    private MediaScannerConnection mMs;
    private File mFile;
    private String mType;
    private ScanListener listener;

    public SingleMediaScanner(Context context, String path, String mimeType, ScanListener l) {
        listener = l;
        mFile = new File(path);
        mType = mimeType;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        try {
            if (mFile.isDirectory()) {
                File[] array = mFile.listFiles();
                if (array != null) {
                    for (int i = 0; i < array.length; i++) {
                        File f = array[i];
                        if (f.isFile()) {// FILE TYPE
                            String name = f.getName();
                            mMs.scanFile(mFile.getAbsolutePath(), mType);
                        } else {// FOLDER TYPE
                            onMediaScannerConnected();
                        }
                    }
                }
            } else {
//            if (mFile.getName().contains(".")) {
                mMs.scanFile(mFile.getAbsolutePath(), mType);
//            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMs.disconnect();
        listener.onScanFinish(path, uri);
    }

}
