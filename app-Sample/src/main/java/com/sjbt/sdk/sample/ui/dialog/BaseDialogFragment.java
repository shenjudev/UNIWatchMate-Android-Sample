package com.sjbt.sdk.sample.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.obsez.android.lib.filechooser.internals.UiUtil;
import com.sjbt.sdk.sample.R;
import com.sjbt.sdk.sample.utils.DensityUtil;


/**
 * author : sj
 * package_name : com.transsion.basic.fragment
 * class_name : BaseDialogFragment
 * description : DialogFragment基类
 * time : 2021-10-21 09:25
 */
public abstract class BaseDialogFragment extends DialogFragment {

    private OnDismissListener mOnDismissListener;
    private OnShowingListener mOnShowingListener;

    @Nullable
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.color.transparent);
            // 定义Dialog动画
            WindowManager.LayoutParams attributes = getDialog().getWindow().getAttributes();
            attributes.windowAnimations = getWindowAnimations();
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        if (getLayoutResId() == 0) {
            return super.onCreateView(inflater, container, savedInstanceState);
        } else {
            return inflater.inflate(getLayoutResId(), container, false);
        }
    }

    protected abstract @LayoutRes
    int getLayoutResId();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDialogStyle(view);
        initView(view);
        initData();
        initEvent(view);
    }

    /**
     * 初始化dialog style
     */
    protected void initDialogStyle(@NonNull View view) {
        try {
            ((ViewGroup) view.getRootView()).getChildAt(0).setBackgroundResource(R.drawable.shape_comm_dialog_bg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getDialog() == null || getDialog().getWindow() == null) {
            return;
        }
        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(DensityUtil.dip2px(10), DensityUtil.dip2px(25), DensityUtil.dip2px(10), DensityUtil.dip2px(25));// 宽度持平
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM; // 紧贴底部
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;// 宽度持平
        window.setAttributes(lp);
    }

    protected void initData() {
    }

    protected void initView(View view) {
    }

    protected void initEvent(View view) {
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOnShowingListener != null) {
            mOnShowingListener.onShowing();
        }
        //如果用户手动点击Home键或者电源键,再回来的时候,需要清除这个FLAG,否则用户点击屏幕外部Dialog不会消失
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    /**
     * 定义Dialog动画，默认为底部弹出
     */
    protected @StyleRes
    int getWindowAnimations() {
        return R.style.Dialog_Anim_Bottom;
    }

    /**
     * 显示
     *
     * @param fm
     */
    public void show(@NonNull FragmentManager fm) {
        try {
            if (fm.isDestroyed()) {
                return;
            }
            //避免重复显示Dialog
            String tag = getClass().getSimpleName();
            // we do not show it twice
            if (fm.findFragmentByTag(tag) == null) {
                try {
                    super.show(fm, tag);
                } catch (Exception e) {
                    fm.beginTransaction().add(this, tag).commitAllowingStateLoss();
                }
            }
            fm.executePendingTransactions();
            if (getDialog() != null && getDialog().getWindow() != null) {
                // Make the dialogs window focusable again.
                getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show(FragmentManager fm, String tag) {
        try {
            if (fm.isDestroyed()) {
                return;
            }
            if (fm.findFragmentByTag(tag) != null) {
                return;
            }
            try {
                super.show(fm, tag);
            } catch (Exception e) {
                fm.beginTransaction().add(this, tag).commitAllowingStateLoss();
            }
            fm.executePendingTransactions();
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().getDecorView().setSystemUiVisibility(
                        getActivity().getWindow().getDecorView().getSystemUiVisibility());

                // Make the dialogs window focusable again.
                getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否显示
     *
     * @return
     */
    public boolean isShowing() {
        Dialog dialog = this.getDialog();
        return dialog != null && dialog.isShowing();
    }

    @Override
    public void dismiss() {
        dismissAllowingStateLoss();
    }

    @Override
    public void dismissAllowingStateLoss() {
        if (getActivity() == null) {
            return;
        }
        super.dismissAllowingStateLoss();
    }

    /**
     * 设置弹框消失监听
     *
     * @param listener
     */
    public BaseDialogFragment setOnDismissListener(OnDismissListener listener) {
        mOnDismissListener = listener;
        return this;
    }

    /**
     * 设置弹框显示监听
     *
     * @param listener
     */
    public BaseDialogFragment setOnShowingListener(OnShowingListener listener) {
        this.mOnShowingListener = listener;
        return this;
    }

    /**
     * 弹框消失监听接口
     */
    public interface OnDismissListener {
        void onDismiss();
    }

    /**
     * 弹框显示监听接口
     */
    public interface OnShowingListener {
        void onShowing();
    }
}
