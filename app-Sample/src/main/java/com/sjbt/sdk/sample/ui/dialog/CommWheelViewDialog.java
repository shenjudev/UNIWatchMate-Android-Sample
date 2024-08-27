package com.sjbt.sdk.sample.ui.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.widget.AppCompatTextView;
import com.github.kilnn.wheelview.OnWheelClickedListener;
import com.github.kilnn.wheelview.WheelView;
import com.github.kilnn.wheelview.adapters.ArrayWheelAdapter;
import com.sjbt.sdk.sample.R;
import java.util.ArrayList;

/**
 * author : sj
 * package_name : com.sparkpro.business.dialog
 * class_name : GoalSettingDialog
 * description : 通用滚动选择弹框
 * time : 2021-11-04 11:36
 */
public class CommWheelViewDialog extends BaseDialogFragment {

    private static final String KEY_CURRENT = "key_current";
    private static final String KEY_DATA = "key_data";

    private WheelView mWheelView;
    private AppCompatTextView mTvRight, mTvLeft;
    private OnItemSelectListener mOnItemSelectListener;
    private String mCurrent;

    /**
     * 默认可见item数量为3
     */
    private int mVisibleCount = 3;
    private ArrayList<String> mDataList;

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_string_datas;
    }

    public CommWheelViewDialog setTvRightTxt(String mTvRightText) {
        if (!TextUtils.isEmpty(mTvRightText)) {
            this.mTvRight.setText(mTvRightText);
        }
        return this;
    }

    public CommWheelViewDialog setTvLeftTxt(String mTvLeftText) {
        if (!TextUtils.isEmpty(mTvLeftText)) {
            this.mTvLeft.setText(mTvLeftText);
        }
        return this;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        mWheelView = view.findViewById(R.id.wv_goal);
        mTvRight = view.findViewById(R.id.tv_right);
        mTvLeft = view.findViewById(R.id.tv_left);
        mTvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemSelectListener != null) {
                    mOnItemSelectListener.onItemSelected(mCurrent);
                    dismiss();
                }
            }
        });

        mTvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
            }
        });
    }

    /**
     * @param current    当前值
     * @param cancelable 点击外部是否可取消
     * @return
     */
    public static CommWheelViewDialog getInstance(ArrayList<String> data, String current, boolean cancelable) {
        CommWheelViewDialog dialog = new CommWheelViewDialog();
        dialog.setCancelable(cancelable);
        Bundle args = new Bundle();
        args.putString(KEY_CURRENT, current);
        args.putSerializable(KEY_DATA, data);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    protected void initData() {
        super.initData();
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        mCurrent = arguments.getString(KEY_CURRENT);
        mDataList = arguments.getStringArrayList(KEY_DATA);
    }

    @Override
    protected void initEvent(View view) {
        super.initEvent(view);
        initWheelView();
    }

    /**
     * 设置可见item数量
     *
     * @param count
     * @return
     */
    public CommWheelViewDialog setVisibleCount(int count) {
        mVisibleCount = count;
        return this;
    }

    /**
     * 初始化WheelView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initWheelView() {
        mWheelView.setCyclic(false);
        mWheelView.setVisibleItems(mVisibleCount);
        mWheelView.setViewAdapter(new ArrayWheelAdapter<>(getContext(),mDataList.toArray()));
        int current = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            String s = mDataList.get(i);
            if (s.equals(mCurrent)) {
                current = i;
                break;
            }
        }
        mWheelView.setCurrentItem(current);
        mWheelView.addClickingListener(new OnWheelClickedListener() {
            @Override

            public void onItemClicked(WheelView wheel, int itemIndex) {
                mCurrent = mDataList.get(itemIndex);
                //LogUtil.d("mCurrent -- "+mCurrent);
                mTvRight.setEnabled(true);
            }
        });

        //处理滑动延迟onItemSelected问题
        mWheelView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //LogUtil.d("setOnTouchListener event -- "+event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTvRight.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                        //>500ms左右才会回调onItemSelected
                        mWheelView.getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTvRight.setEnabled(true);
                            }
                        }, 1000);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 设置是否循环
     *
     * @param isSyclic
     * @return
     */
    public CommWheelViewDialog setCyclic(boolean isSyclic) {
        if (mWheelView != null)
            mWheelView.setCyclic(isSyclic);
        return this;
    }


    /**
     * 设置监听
     *
     * @param listener
     * @return
     */
    public CommWheelViewDialog setOnItemSelectListener(OnItemSelectListener listener) {
        mOnItemSelectListener = listener;
        return this;
    }

    /**
     * 选择监听
     */
    public interface OnItemSelectListener {

        /**
         * 选中目标
         *
         * @param data
         */
        void onItemSelected(String data);
    }
}
