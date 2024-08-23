package com.sjbt.sdk.sample.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import com.sjbt.sdk.sample.R;

public class LoadingDialog extends AppCompatDialog {

    private Context mContext;
    private String msg = "";
    private TextView tvMessage;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.CustomDialogTrans);
        mContext = context;
    }

    public LoadingDialog(@NonNull Context context, String msg) {
        super(context, R.style.CustomDialogTrans);
        mContext = context;
        this.msg = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View customview = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_loading, null);
        tvMessage = (TextView) customview.findViewById(R.id.tv_message);
        if (!TextUtils.isEmpty(msg)) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(msg);
        } else {
            tvMessage.setVisibility(View.GONE);
        }

        setContentView(customview);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    public void updateMsgText(String msg) {

        if (isShowing() && tvMessage != null) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(msg);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //常亮
    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//取消常亮
    }
}
