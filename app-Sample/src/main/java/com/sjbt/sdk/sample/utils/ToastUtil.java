package com.sjbt.sdk.sample.utils;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sjbt.sdk.sample.MyApplication;
import com.sjbt.sdk.sample.R;


public class ToastUtil {

    private static long lastClickTime = 0;

    public static void showToast(String text) {
        showToast(text,false);

    }

    public static void showToast(String text, boolean showLong) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        MyApplication.Companion.getMHandler().post(new Runnable() {
            @Override
            public void run() {
                View customview = LayoutInflater.from(MyApplication.Companion.getInstance()).inflate(
                        R.layout.toast_layout, null);
                TextView tvMessage = customview.findViewById(R.id.message);
                tvMessage.setText(text);
                Toast mToast = Toast.makeText(MyApplication.Companion.getInstance(), text, showLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                mToast.setView(customview);
                mToast.setGravity(Gravity.TOP, 0, 0);

                long duration = System.currentTimeMillis() - lastClickTime;
                if (duration > 1000) {
                    mToast.show();
                }

                lastClickTime = System.currentTimeMillis();
            }
        });

    }
}
