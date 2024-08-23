package com.sjbt.sdk.sample.ui.muslim;

import android.content.Context;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sjbt.sdk.sample.R;
import com.sjbt.sdk.sample.utils.DateTimeUtil;
import java.util.Calendar;
import java.util.List;

public class RepeatAdapter extends BaseQuickAdapter<DateTimeUtil.WeekDay, BaseViewHolder> {

    private Context mContext;

    public RepeatAdapter(Context context, List<DateTimeUtil.WeekDay> data) {
        super(R.layout.item_repeat, data);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, DateTimeUtil.WeekDay item) {
        TextView tv_item_alarm_repeat = helper.getView(R.id.tv_item_alarm_repeat);
        tv_item_alarm_repeat.setText(item.week);
        tv_item_alarm_repeat.setSelected(item.isSelected);
    }

    public boolean[] getSelectedItems() {
        boolean[] booleans = new boolean[7];
        List<DateTimeUtil.WeekDay> data = getData();
        for (int i = 0; i < data.size(); i++) {
            DateTimeUtil.WeekDay weekDay = data.get(i);
            if (weekDay.index== Calendar.SUNDAY){
                booleans[6]=weekDay.isSelected;
            }else {
                booleans[weekDay.index-2]=weekDay.isSelected;
            }
        }
        return booleans;
    }
}
