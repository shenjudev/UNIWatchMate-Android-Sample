package com.sjbt.sdk.sample.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.IntegerRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.obsez.android.lib.filechooser.internals.UiUtil;
import com.sjbt.sdk.sample.R;


/**
 * author : sj
 * package_name : com.sparkpro.business.widget
 * class_name : CommItemView
 * description : 通用item控件
 * time : 2021-11-03 16:42
 */
public class CommItemView extends LinearLayout {

    AppCompatImageView mIvLeft;
    AppCompatTextView mTvTitle;
    AppCompatTextView tv_sub_title;
    AppCompatTextView mTvData;
    AppCompatTextView mTvUnit;
    AppCompatImageView mIvRight;
    ConstraintLayout mLayoutRoot;
    ViewGroup mLayoutContent;
    View mViewDivider;

    /**
     * ICON
     */
    private Drawable mIcon;

    /**
     * icon大小
     */
    private int mIconSize;

    /**
     * 尺寸
     */
    private float mTitleSize, mDataSize, mUnitSize;

    /**
     * 颜色
     */
    private int mBackColor, mTitleColor, mDataColor, mUnitColor;

    /**
     * 是否显示箭头
     */
    private boolean mShowArrow;
    private String mTitle, mData, mUnit;
    private String mSubTitle;
    private int mMinHeight, mPaddingStart, mPaddingEnd;
    private boolean mShowBottomDivider;

    public CommItemView(Context context) {
        this(context, null);
    }

    public CommItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_comm_item, this, true);
        initAttrs(context, attrs);
        initView();
    }

    /**
     * 初始化属性
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CommItemView);
        mBackColor = ta.getColor(R.styleable.CommItemView_backColor, context.getColor(R.color.white));
        mIcon = ta.getDrawable(R.styleable.CommItemView_icon);
        mIconSize = ta.getDimensionPixelSize(R.styleable.CommItemView_ic_size, context.getResources().getDimensionPixelSize(R.dimen.dp_20));
        mTitleSize = ta.getDimension(R.styleable.CommItemView_title_size, context.getResources().getDimension(R.dimen.sp_18));
        mTitleColor = ta.getColor(R.styleable.CommItemView_title_color, context.getColor(R.color.black));
        mDataSize = ta.getDimension(R.styleable.CommItemView_data_size, context.getResources().getDimension(R.dimen.sp_18));
        mDataColor = ta.getColor(R.styleable.CommItemView_data_color, context.getColor(R.color.black));
        mUnitSize = ta.getDimension(R.styleable.CommItemView_unit_size, context.getResources().getDimension(R.dimen.text_size_tip));
        mUnitColor = ta.getColor(R.styleable.CommItemView_unit_color, context.getColor(R.color.black));
        mShowArrow = ta.getBoolean(R.styleable.CommItemView_show_right_arrow, true);
        mTitle = ta.getString(R.styleable.CommItemView_title);
        mData = ta.getString(R.styleable.CommItemView_data);
        mUnit = ta.getString(R.styleable.CommItemView_unit);
        mMinHeight = ta.getDimensionPixelSize(R.styleable.CommItemView_min_height, context.getResources().getDimensionPixelSize(R.dimen.dp_40));
        mShowBottomDivider = ta.getBoolean(R.styleable.CommItemView_show_bottom_divider, false);
        mPaddingStart = ta.getDimensionPixelSize(R.styleable.CommItemView_padding_start, context.getResources().getDimensionPixelSize(R.dimen.dp_16));
        mPaddingEnd = ta.getDimensionPixelSize(R.styleable.CommItemView_padding_end, context.getResources().getDimensionPixelSize(R.dimen.dp_16));
        ta.recycle();
    }

    /**
     * 初始化控件
     */
    private void initView() {


        mIvLeft = findViewById(R.id.iv_left);
        mTvTitle = findViewById(R.id.tv_title);
        tv_sub_title = findViewById(R.id.tv_sub_title);
        mTvData = findViewById(R.id.tv_data);
        mTvUnit = findViewById(R.id.tv_unit);
        mIvRight = findViewById(R.id.iv_right);
        mLayoutRoot = findViewById(R.id.layout_root);
        mLayoutContent = findViewById(R.id.layout_content);
        mViewDivider = findViewById(R.id.view_divider);

        mLayoutRoot.setBackgroundTintList(ColorStateList.valueOf(mBackColor));
        ViewGroup.LayoutParams layoutParams = mIvLeft.getLayoutParams();
        layoutParams.width = mIconSize;
        layoutParams.height = mIconSize;
        mIvLeft.setLayoutParams(layoutParams);

        mIvLeft.setVisibility(mIcon == null ? GONE : VISIBLE);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mViewDivider.getLayoutParams();
        if (mIcon == null){
            params.setMarginStart(UiUtil.dip2pxInt(18f));
        }else {
            params.setMarginStart(UiUtil.dip2pxInt(45f));
        }
        mViewDivider.setLayoutParams(params);

        mIvLeft.setImageDrawable(mIcon);

        setTitle(mTitle);
        setData(mData);
        setUnit(mUnit);
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
        mTvTitle.setTextColor(mTitleColor);
        mTvData.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDataSize);
        mTvData.setTextColor(mDataColor);
        mTvUnit.setTextSize(TypedValue.COMPLEX_UNIT_PX, mUnitSize);
        mTvUnit.setTextColor(mUnitColor);
        mIvRight.setVisibility(mShowArrow ? VISIBLE : GONE);
        mLayoutRoot.setMinHeight(mMinHeight);
        mViewDivider.setVisibility(mShowBottomDivider ? VISIBLE : GONE);
        mLayoutContent.setPadding(mPaddingStart, 0, mPaddingEnd, 0);
    }

    public Drawable getIcon() {
        return mIvLeft.getDrawable();
    }

    public CommItemView setIcon(Drawable icon) {
        mIcon = icon;
        mIvLeft.setVisibility(mIcon == null ? GONE : VISIBLE);
        mIvLeft.setImageDrawable(mIcon);
        return this;
    }

    @SuppressLint("ResourceType")
    public CommItemView setIcon(@IntegerRes int res) {
        mIvLeft.setVisibility(res == 0 ? GONE : VISIBLE);
        mIvLeft.setImageResource(res);
        return this;
    }

    public CommItemView setIconSize(int iconSize) {
        mIconSize = iconSize;
        ViewGroup.LayoutParams layoutParams = mIvLeft.getLayoutParams();
        layoutParams.width = mIconSize;
        layoutParams.height = mIconSize;
        mIvLeft.setLayoutParams(layoutParams);
        return this;
    }

    public CommItemView setTitleSize(float titleSize) {
        mTitleSize = titleSize;
        mTvTitle.setTextSize(mTitleSize);
        return this;
    }

    public CommItemView setDataSize(float dataSize) {
        mDataSize = dataSize;
        mTvData.setTextSize(mDataSize);
        return this;
    }

    public CommItemView setUnitSize(float unitSize) {
        mUnitSize = unitSize;
        mTvUnit.setTextSize(mUnitSize);
        return this;
    }

    public CommItemView setTitleColor(int titleColor) {
        mTitleColor = titleColor;
        mTvTitle.setTextColor(mTitleColor);
        return this;
    }

    public CommItemView setDataColor(int dataColor) {
        mDataColor = dataColor;
        mTvData.setTextColor(mDataColor);
        return this;
    }

    public CommItemView setUnitColor(int unitColor) {
        mUnitColor = unitColor;
        mTvUnit.setTextColor(mUnitColor);
        return this;
    }

    public boolean isShowArrow() {
        return mShowArrow;
    }

    public CommItemView setShowArrow(boolean showArrow) {
        mShowArrow = showArrow;
        mIvRight.setVisibility(mShowArrow ? VISIBLE : GONE);
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public CommItemView setTitle(String title) {
        mTitle = title;
        mTvTitle.setText(mTitle);
        return this;
    }

    public CommItemView setSubTitle(String subTitle) {
        mSubTitle = subTitle;
        tv_sub_title.setText(mSubTitle);
        tv_sub_title.setVisibility(TextUtils.isEmpty(subTitle) ? GONE : VISIBLE);
        return this;
    }

    public String getData() {
        return mData;
    }

    public CommItemView setData(String data) {
        mData = data;
        mTvData.setVisibility(TextUtils.isEmpty(mData) ? INVISIBLE : VISIBLE);
        mTvData.setText(mData);
        return this;
    }

    public AppCompatTextView getDataView() {
        return mTvData;
    }

    public String getUnit() {
        return mUnit;
    }

    public CommItemView setUnit(String unit) {
        mUnit = unit;
        mTvUnit.setVisibility(TextUtils.isEmpty(mUnit) ? GONE : VISIBLE);
        mTvUnit.setText(mUnit);
        return this;
    }

    public CommItemView setShowBottomDivider(boolean showBottomDivider) {
        mShowBottomDivider = showBottomDivider;
        mViewDivider.setVisibility(showBottomDivider ? VISIBLE : GONE);
        return this;
    }

    public CommItemView setDataAndUnitVisibility(int visibility) {
        mTvData.setVisibility(visibility);
        mTvUnit.setVisibility(visibility);
        return this;
    }

    public CommItemView setIconVisibility(int visibility) {
        mIvLeft.setVisibility(visibility);
        return this;
    }

    public AppCompatImageView getLeftImageview() {
        return mIvLeft;
    }

    public AppCompatTextView getTvData() {
        return mTvData;
    }

}
