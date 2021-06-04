package com.kevin.kfloatview.bt;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kevin.kfloatview.bt.adapter.BtSettingDeviceInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * @author Kevin  2021/6/2
 */
public interface IBluetoothStateCallBack {
    int BT_CODE_DISCOVERY_START = 0;     //蓝牙扫描 开始
    int BT_CODE_DISCOVERY_FINISHED = 1;  //蓝牙扫描 结束
    int BT_CODE_LOC_GRANT_SUCCESS = 2;   //蓝牙定位 授权成功
    int BT_CODE_LOC_GRANT_FAILED = 3;    //蓝牙定位 授权失败
    int BT_CODE_ENABLE_SUCCESS = 4;      //请求打开蓝牙 授权成功
    int BT_CODE_ENABLE_FAILED = 5;       //请求打开蓝牙 授权失败
    int BT_CODE_BONDED_SUCCESS = 6;      //绑定设备成功
    int BT_CODE_BONDED_FAILED = 7;       //绑定设备失败
    int BT_CODE_UN_BONDED = 8;           //解绑设备完成
    int BT_CODE_DEV_CONNECT_START = 9;   //连接 --> 开始
    int BT_CODE_DEV_CONNECT_SUCCESS = 10;//连接 --> 成功
    int BT_CODE_DEV_CONNECT_FAILED = 11; //连接 --> 失败

    @IntDef({BT_CODE_DISCOVERY_START,
            BT_CODE_DISCOVERY_FINISHED,
            BT_CODE_LOC_GRANT_SUCCESS,
            BT_CODE_LOC_GRANT_FAILED,
            BT_CODE_ENABLE_SUCCESS,
            BT_CODE_ENABLE_FAILED,
            BT_CODE_BONDED_SUCCESS,
            BT_CODE_BONDED_FAILED,
            BT_CODE_UN_BONDED,
            BT_CODE_DEV_CONNECT_START,
            BT_CODE_DEV_CONNECT_SUCCESS,
            BT_CODE_DEV_CONNECT_FAILED})
    @Retention(RetentionPolicy.SOURCE)
    @interface BtStateCode {

    }

    @UiThread
    void updateBondedDevs(@NonNull ArrayList<BtSettingDeviceInfo> bondedDevs);

    @UiThread
    void updateScanDevs(@NonNull ArrayList<BtSettingDeviceInfo> scanDevs);

    @UiThread
    void otherState(@BtStateCode int stateCode, @Nullable BluetoothDevice bluetoothDevice);
}
