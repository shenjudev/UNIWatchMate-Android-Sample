package com.sjbt.sdk.sample.ui.camera;

public interface CameraPreviewListener {

    void transferAllowState(int state, int reason);

    void transferFrameFinish(int result);

}
