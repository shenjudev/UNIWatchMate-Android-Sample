package com.sjbt.sdk.sample.ui.fileTrans;

import com.sjbt.sdk.sample.ui.BaseProtocolListener;

public interface CancelTransferFileListener extends BaseProtocolListener {
    void onCancelResult(boolean result);
}