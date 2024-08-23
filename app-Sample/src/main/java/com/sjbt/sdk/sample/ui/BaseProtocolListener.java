package com.sjbt.sdk.sample.ui;


import com.sjbt.sdk.entity.MsgBean;

public interface BaseProtocolListener {
    void onTimeOut(MsgBean msgBean);
}
