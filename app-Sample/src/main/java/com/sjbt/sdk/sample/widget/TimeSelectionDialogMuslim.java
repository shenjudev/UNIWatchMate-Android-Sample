package com.sjbt.sdk.sample.widget;


import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.bigkoo.pickerview.adapter.NumericWheelAdapter;
import com.contrarywind.view.WheelView;
import com.sjbt.sdk.sample.R;
import com.sjbt.sdk.sample.ui.dialog.BaseDialogFragment;
import com.sjbt.sdk.sample.utils.DateUtil;

import java.util.Arrays;
import java.util.Calendar;

/**
 * author : sj
 * package_name : com.sparkpro.business.dialog
 * class_name : TimeSelectionDialog
 * description : 时间选中弹框
 * time : 2021-12-14 11:04
 */
public class TimeSelectionDialogMuslim extends BaseDialogFragment {

    private static final String KEY_HOUR = "key_hour";
    private static final String KEY_MIN = "key_min";
    private static final String KEY_SHOW_24_FORMAT = "key_show_24_format";

    WheelView mWvImeQuantum;
    WheelView mWvHour;
    WheelView mWvMin;

    /**
     * 默认可见item数量为3
     */
    private int mVisibleCount = 3;
    private String[] mTimeQuantumArr;

    /**
     * 选中的时、分
     */
    private int mSelectedHour, mSelectedMin;
    private boolean mShow24Format;
    private OnTimeSelectedListener mOnTimeSelectedListener;
    private boolean mIsHCyclic;
    private boolean mIsMCyclic;

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_time_selection;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        mWvImeQuantum = view.findViewById(R.id.wv_ime_quantum);
        mWvHour = view.findViewById(R.id.wv_hour);
        mWvMin = view.findViewById(R.id.wv_min);


        AppCompatTextView mTvLeft  = view.findViewById(R.id.tv_left);
        AppCompatTextView mTvRight  = view.findViewById(R.id.tv_right);

        mTvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
            }
        });

        mTvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback();
                dismissAllowingStateLoss();
            }
        });
    }

    /**
     * 不传选中的时间，默认为当前时间
     *
     * @param show24Format
     * @param cancelable
     * @return
     */
    public static TimeSelectionDialogMuslim getInstance(boolean show24Format, boolean cancelable) {
        Calendar data = DateUtil.getCurrentData();
        return getInstance(data.get(Calendar.HOUR_OF_DAY), data.get(Calendar.MINUTE), show24Format, cancelable);
    }

    /**
     * 传入选中的时间按24小时制
     *
     * @param selectedHour 选中的小时
     * @param selectedMin  选中的分钟
     * @param show24Format 是否按24小时制显示
     * @param cancelable   点击外部是否可取消
     * @return
     */
    public static TimeSelectionDialogMuslim getInstance(int selectedHour, int selectedMin, boolean show24Format, boolean cancelable) {
        TimeSelectionDialogMuslim dialog = new TimeSelectionDialogMuslim();
        dialog.setCancelable(cancelable);
        Bundle args = new Bundle();
        args.putInt(KEY_HOUR, selectedHour);
        args.putInt(KEY_MIN, selectedMin);
        args.putBoolean(KEY_SHOW_24_FORMAT, show24Format);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    protected void initData() {
        super.initData();
        mTimeQuantumArr = new String[]{getString(R.string.muslim_time_am), getString(R.string.muslim_time_pm)};
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        mSelectedHour = arguments.getInt(KEY_HOUR);
        mSelectedMin = arguments.getInt(KEY_MIN);
        mShow24Format = arguments.getBoolean(KEY_SHOW_24_FORMAT, true);
    }

    @Override
    protected void initEvent(View view) {
        super.initEvent(view);
        initWheelView();
    }

    /**
     * 初始化WheelView
     */
    private void initWheelView() {
        //上午、下午
        mWvImeQuantum.setVisibility(mShow24Format ? View.GONE : View.VISIBLE);
        if (!mShow24Format) {
            mWvImeQuantum.setCyclic(false);
            mWvImeQuantum.setItemsVisibleCount(mVisibleCount);
            mWvImeQuantum.setAdapter(new ArrayWheelAdapter<>(Arrays.asList(mTimeQuantumArr)));
            mWvImeQuantum.setCurrentItem(mSelectedHour < 12 ? 0 : 1);
        }

        //时
        mWvHour.setCyclic(mIsHCyclic);
        mWvHour.setItemsVisibleCount(mVisibleCount);
        if (mShow24Format) {
            mWvHour.setAdapter(new NumericWheelAdapter(0, 23));
            mWvHour.setCurrentItem(mSelectedHour);
        } else {
            mWvHour.setAdapter(new NumericWheelAdapter(1, 12));
            int position = mSelectedHour % 12;
            mWvHour.setCurrentItem(position == 0 ? mWvHour.getItemsCount() - 1 : position - 1);
        }

        //分
        mWvMin.setCyclic(mIsMCyclic);
        mWvMin.setItemsVisibleCount(mVisibleCount);
        mWvMin.setAdapter(new NumericWheelAdapter(0, 59));
        mWvMin.setCurrentItem(mSelectedMin);
    }


    public TimeSelectionDialogMuslim setCyclic(boolean isHCyclic, boolean isMCyclic) {
        mIsHCyclic = isHCyclic;
        mIsMCyclic = isMCyclic;
        if (mWvHour == null || mWvMin == null) return this;
        mWvHour.setCyclic(isHCyclic);
        mWvMin.setCyclic(isMCyclic);
        return this;
    }



    /**
     * 回调时间
     */
    private void callback() {
        if (mOnTimeSelectedListener != null) {
            int hourPos = mWvHour.getCurrentItem();
            int hour;
            if (mShow24Format) {
                hour = hourPos;
            } else {
                if (mWvImeQuantum.getCurrentItem() == 0) {
                    //上午12点 对应24小时制0：00；下午12点对应24小时制12：00
                    //上午
                    hour = hourPos + 1;
                    if (hour == 12) hour = 0;
                } else {
                    //下午
                    hour = hourPos + 13;
                    //24对应12点
                    if (hour == 24) {
                        hour = 12;
                    }
                }
            }
            mOnTimeSelectedListener.onTimeSelected(hour, mWvMin.getCurrentItem());
        }
    }

    public TimeSelectionDialogMuslim setOnItemSelectedListener(OnTimeSelectedListener onTimeSelectedListener) {
        mOnTimeSelectedListener = onTimeSelectedListener;
        return this;
    }

    public interface OnTimeSelectedListener {

        /**
         * 回调时间，24小时制
         */
        void onTimeSelected(int hour, int minuter);
    }
}
