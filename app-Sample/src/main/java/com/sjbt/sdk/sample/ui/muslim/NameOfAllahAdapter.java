package com.sjbt.sdk.sample.ui.muslim;

import android.content.Context;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sjbt.sdk.sample.R;
import com.sjbt.sdk.sample.model.MuslimAllahInfo;

import java.util.List;

public class NameOfAllahAdapter extends BaseQuickAdapter<MuslimAllahInfo, BaseViewHolder> {

    private Context mContext;
    private List<MuslimAllahInfo> mDataList = null;

    public NameOfAllahAdapter(Context context, List<MuslimAllahInfo> data) {
        super(R.layout.item_name_of_allah, data);
        mContext = context;
        mDataList = data;
    }

    @Override
    protected void convert(BaseViewHolder helper, MuslimAllahInfo item) {
        helper.setText(R.id.tv_id, item.getId() + "");
            helper.setText(R.id.tv_name, item.getEnglishName());

        ToggleButton btn_favorite = helper.getView(R.id.btn_favorite);
        btn_favorite.setChecked(item.isFavorite());
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

}
