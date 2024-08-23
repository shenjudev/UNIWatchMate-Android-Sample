package com.sjbt.sdk.sample.ui.fileTrans;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface TransferFileListener {

    void transferFail(int reason, @NotNull String msg);

    void transferProcess(String name, int sendingIndex, int totalCount, float process);

    void transferStepFinish(int sendingIndex, File file);
    void transferAllFinish(int sendingIndex, int totalCount);
}
