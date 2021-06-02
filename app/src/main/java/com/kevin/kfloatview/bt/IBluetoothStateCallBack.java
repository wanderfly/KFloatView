package com.kevin.kfloatview.bt;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kevin.kfloatview.bt.adapter.BtSettingDeviceInfo;

import java.util.ArrayList;

/**
 * @author Kevin  2021/6/2
 */
public interface IBluetoothStateCallBack {
    int BT_CODE_DISCOVERY_START = 0;   //蓝牙扫描开始
    int BT_CODE_DISCOVERY_FINISHED = 1;//蓝牙扫描结束
    int BT_CODE_LOC_GRANT_SUCCESS = 2; //蓝牙定位授权成功
    int BT_CODE_LOC_GRANT_FAILED = 3;  //蓝牙定位授权失败
    int BT_CODE_ENABLE_SUCCESS = 4;    //请求打开蓝牙 授权成功
    int BT_CODE_ENABLE_FAILED = 5;     //请求打开蓝牙 授权失败

    @IntDef({BT_CODE_DISCOVERY_START,
            BT_CODE_DISCOVERY_FINISHED,
            BT_CODE_LOC_GRANT_SUCCESS,
            BT_CODE_LOC_GRANT_FAILED,
            BT_CODE_ENABLE_SUCCESS,
            BT_CODE_ENABLE_FAILED})
    @interface BtStateCode {

    }

    @UiThread
    void updateBondedDevs(@NonNull ArrayList<BtSettingDeviceInfo> bondedDevs);

    @UiThread
    void updateScanDevs(@NonNull ArrayList<BtSettingDeviceInfo> scanDevs);

    @UiThread
    void otherState(@BtStateCode int stateCode, @Nullable String stateInfo);
}
