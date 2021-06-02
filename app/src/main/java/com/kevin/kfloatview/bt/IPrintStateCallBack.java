package com.kevin.kfloatview.bt;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

/**
 * @author Kevin  2021/6/2
 */
public interface IPrintStateCallBack {
    @UiThread
    void updatePrintState(@NonNull String jsonString);
}
