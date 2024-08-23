package com.sjbt.sdk.sample.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.sjbt.sdk.sample.R;

public abstract class BaseTitleActivity<V extends ViewBinding> extends BaseActivity {

    protected RelativeLayout layoutTitle;
    protected ImageView ivBack, ivRight2, ivRight;
    protected TextView tvTitle, tvRight;
    private FrameLayout contentContainer;
    protected V binding;
    public LinearLayout topView;

    @NonNull
    protected abstract V getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = DataBindingUtil.setContentView(this, getLayoutId());
        binding = getBinding(LayoutInflater.from(this), null);

        ViewGroup viewGroup = findViewById(android.R.id.content);
        viewGroup.removeAllViews();

        LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.layout_title_container, null, false);

        viewGroup.addView(titleLayout);
        topView = titleLayout.findViewById(R.id.top_view);
        contentContainer = titleLayout.findViewById(R.id.layoutContainer);
        layoutTitle = titleLayout.findViewById(R.id.layoutTitle);
        ivBack = titleLayout.findViewById(R.id.iv_back);
        ivRight = titleLayout.findViewById(R.id.iv_right);
        ivRight2 = titleLayout.findViewById(R.id.iv_right_2);
        tvTitle = titleLayout.findViewById(R.id.tv_title);
        tvRight = titleLayout.findViewById(R.id.tv_right);

        if (binding != null) {
            setContentView(binding.getRoot());
        } else {
            setContentView(LayoutInflater.from(this).inflate(getLayoutId(), contentContainer, false));
        }

        ivBack.setOnClickListener(v -> finishActivity());

        init();
    }

    protected void finishActivity() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    protected void hideTitle() {
        layoutTitle.setVisibility(View.GONE);
    }

    @Override
    public void setContentView(View view) {
        contentContainer.addView(view);
    }

    protected void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
    }

    protected void showHideRightBtn(boolean show) {
        tvRight.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    protected void setRightBtn(String text, View.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            tvRight.setText(text);
            showHideRightBtn(true);
        }

        tvRight.setOnClickListener(listener);
    }

    public abstract int getLayoutId();

    public abstract void init();




}
