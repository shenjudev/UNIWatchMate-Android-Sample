package com.sjbt.sdk.sample.ui.dialog;



import static com.sjbt.sdk.sample.base.Config.CLICK_CANCEL;
import static com.sjbt.sdk.sample.base.Config.CLICK_RETRY;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;

import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar;
import com.blankj.utilcode.util.NumberUtils;
import com.sjbt.sdk.sample.R;
import com.sjbt.sdk.sample.dialog.BaseDialog;
import com.sjbt.sdk.sample.dialog.CallBack;
import com.sjbt.sdk.sample.utils.CacheDataHelper;

/**
 * 修改名称弹窗
 */
public class SendMusicDialog extends BaseDialog {

    private View rootView;
    private TextView tv_title_name, tvMusicCount, tvMusicName, tv_think_again, tv_sure, tvSendingCount, tvFinish, tvFinishTip, tvTip, tv_cancel, tv_send_again;
    private ImageView ivClose, ivStatus;
    private TextRoundCornerProgressBar pbProgress;

    private Group sendingGroup, closeGroup, finishGroup;

    private boolean isSending, isClosing = false;
    private Context mContext;
    private int fileCount;
    private CallBack<Integer> callBack;

    public SendMusicDialog(@NonNull Context context, CallBack<Integer> callBack) {
        super(context);
        mContext = context;
        this.callBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = View.inflate(mContext, R.layout.dialog_send_music, null);
        setContentView(rootView);

        tv_title_name = rootView.findViewById(R.id.tv_title_name);
        tvMusicCount = rootView.findViewById(R.id.tvMusicCount);
        tvMusicName = rootView.findViewById(R.id.tvMusicName);
        tv_think_again = rootView.findViewById(R.id.tv_think_again);
        tv_sure = rootView.findViewById(R.id.tv_sure);
        tvSendingCount = rootView.findViewById(R.id.tvSendingCount);
        ivClose = rootView.findViewById(R.id.ivClose);
        pbProgress = rootView.findViewById(R.id.pb_progress);

        tvFinish = rootView.findViewById(R.id.tvFinish);
        ivStatus = rootView.findViewById(R.id.ivStatus);
        tvFinishTip = rootView.findViewById(R.id.tvFinishTip);
        tvTip = rootView.findViewById(R.id.tvTip);
        tv_cancel = rootView.findViewById(R.id.tv_cancel);
        tv_send_again = rootView.findViewById(R.id.tv_send_again);

        sendingGroup = rootView.findViewById(R.id.sendingGroup);
        closeGroup = rootView.findViewById(R.id.closeGroup);
        finishGroup = rootView.findViewById(R.id.finishGroup);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setWindowParam(Gravity.BOTTOM, 1, ANIM_TYPE_BOTTOM_ENTER);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClosing = true;

                if (isSending) {
                    tv_title_name.setText(R.string.in_send);
                    tvSendingCount.setText(String.format(mContext.getString(R.string.sending_count), fileCount));
                    showSending(View.VISIBLE, View.GONE);
                } else {
                    dismiss();
                }
            }
        });

        tv_think_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClosing = false;
                tv_title_name.setText(R.string.sending);
                showSending(View.GONE, View.VISIBLE);
            }
        });

        tv_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.callBack(CLICK_CANCEL);
                dismiss();
                isClosing = true;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetDialog();
                    }
                }, 500);

            }
        });
    }

    public void resetDialog() {

        if (pbProgress != null) {
            isClosing = false;
            isSending = false;
            pbProgress.setProgress(0);
            fileCount = 0;
        }

    }

    public void updateProgress(String fileName, int sended_count, int total_count, double progress, boolean cancelSend) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
//                if (!isShowing() && CacheDataHelper.INSTANCE.getCurrentDeiceBean().isBtConnect && !cancelSend && !isClosing) {
                if (!isShowing() &&  !cancelSend && !isClosing) {
                    show();
                }

//                LogUtils.logBlueTooth("rootView:" + rootView);
//                LogUtils.logBlueTooth("finishGroup:" + finishGroup);
//                LogUtils.logBlueTooth("closeGroup:" + closeGroup);
//                LogUtils.logBlueTooth("sendingGroup:" + sendingGroup);

                isSending = progress < 100;

                if (rootView == null) {
                    return;
                } else {
                    if (finishGroup == null) {
                        tv_title_name = rootView.findViewById(R.id.tv_title_name);
                        tvMusicCount = rootView.findViewById(R.id.tvMusicCount);
                        tvMusicName = rootView.findViewById(R.id.tvMusicName);
                        tv_think_again = rootView.findViewById(R.id.tv_think_again);
                        tv_sure = rootView.findViewById(R.id.tv_sure);
                        tvSendingCount = rootView.findViewById(R.id.tvSendingCount);
                        ivClose = rootView.findViewById(R.id.ivClose);
                        pbProgress = rootView.findViewById(R.id.pb_progress);

                        tvFinish = rootView.findViewById(R.id.tvFinish);
                        ivStatus = rootView.findViewById(R.id.ivStatus);
                        tvFinishTip = rootView.findViewById(R.id.tvFinishTip);
                        tvTip = rootView.findViewById(R.id.tvTip);
                        tv_cancel = rootView.findViewById(R.id.tv_cancel);
                        tv_send_again = rootView.findViewById(R.id.tv_send_again);

                        sendingGroup = rootView.findViewById(R.id.sendingGroup);
                        closeGroup = rootView.findViewById(R.id.closeGroup);
                        finishGroup = rootView.findViewById(R.id.finishGroup);
                    }
                }

                finishGroup.setVisibility(View.GONE);
                closeGroup.setVisibility(View.GONE);
                sendingGroup.setVisibility(View.VISIBLE);
                tvFinish.setVisibility(View.GONE);
                ivClose.setVisibility(View.VISIBLE);
                tv_send_again.setVisibility(View.GONE);
                tv_cancel.setVisibility(View.GONE);

                if (!isClosing) {
                    showSending(View.GONE, View.VISIBLE);
                } else {
                    showSending(View.VISIBLE, View.GONE);
                }

                fileCount = total_count;

                tvMusicName.setText(fileName);
                tvMusicCount.setText(String.format(mContext.getString(R.string.music_send_count), sended_count, total_count));
                pbProgress.setProgressText(NumberUtils.format(progress, 1) + "%");
                pbProgress.setProgress((int) progress);
            }
        });
    }

    @Override
    public void show() {
        super.show();
        isClosing = false;
    }

    public boolean isSending() {
        return isSending;
    }

    private void showSending(int gone, int visible) {
        if (isSending) {
            closeGroup.setVisibility(gone);
            sendingGroup.setVisibility(visible);
        }
    }

    public void showFinish(int sendCount, int totalCount) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                show();
                isSending = false;
                CacheDataHelper.INSTANCE.setTransferring(false);
                finishGroup.setVisibility(View.VISIBLE);
                closeGroup.setVisibility(View.GONE);
                sendingGroup.setVisibility(View.GONE);
                pbProgress.setProgressText(0 + "%");
                pbProgress.setProgress(0);
                tvTip.setVisibility(View.VISIBLE);

                tv_title_name.setVisibility(View.INVISIBLE);
                tv_send_again.setVisibility(View.GONE);
                tv_cancel.setVisibility(View.GONE);

                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        callBack.callBack(CLICK_CANCEL);
                    }
                });

                tv_send_again.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callBack.callBack(CLICK_RETRY);
                    }
                });

                if (sendCount >= totalCount) {

                    tvFinish.setVisibility(View.VISIBLE);
                    tvFinishTip.setVisibility(View.VISIBLE);
                    tvFinishTip.setText(R.string.all_send_success);

                    tvFinishTip.setTextColor(Color.parseColor("#34C759"));
                    ivStatus.setImageResource(R.mipmap.biu_icon_success);
                    tvFinish.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismiss();
                        }
                    });
                    tvTip.setText(String.format(mContext.getString(R.string.send_all), totalCount));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                        }
                    }, 3000);

                } else if (sendCount <= 0) {
                    ivClose.setVisibility(View.INVISIBLE);
                    ivStatus.setImageResource(R.mipmap.biu_icon_fail);
                    tv_send_again.setVisibility(View.VISIBLE);
                    tv_cancel.setVisibility(View.VISIBLE);

                    tvFinishTip.setText(R.string.send_fail);
                    tvFinishTip.setTextColor(Color.WHITE);
                    tvTip.setText(String.format(mContext.getString(R.string.send_fail_all), totalCount));
                    tvFinish.setVisibility(View.GONE);
                } else if (sendCount < totalCount) {
                    ivStatus.setImageResource(R.mipmap.biu_icon_error);
                    tv_send_again.setVisibility(View.VISIBLE);
                    tv_send_again.setBackgroundResource(R.drawable.shape_bg_retry);
                    tv_send_again.setTextColor(mContext.getResources().getColor(R.color.color_ff9500));
                    tv_cancel.setVisibility(View.VISIBLE);
                    tvFinishTip.setText(R.string.send_finish);
                    tvFinishTip.setTextColor(mContext.getResources().getColor(R.color.color_ff9500));
                    tvTip.setText(String.format(mContext.getString(R.string.send_fail_count_tip), sendCount, totalCount - sendCount));
                    tvFinish.setVisibility(View.GONE);
                }
            }
        });
    }
}
