package com.sjbt.sdk.spp.bt;

import android.bluetooth.BluetoothDevice;

import com.sjbt.sdk.entity.MsgBean;

public interface BtStateListener {
        int ON_SOCKET_CLOSE = 0;
        int CONNECTED = 1;
        int MSG = 2;
        int TIME_OUT = 3;
        int BUSY = 4;

        void socketNotify(int state, Object obj);

        void socketNotifyError(MsgBean msgBean);

        void onConnectFailed(BluetoothDevice device, String msg);
    }