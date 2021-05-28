package com.kevin.kfloatview;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Kevin  2021/5/28
 * 蓝牙权限请求activity
 */
public class BtActivity extends AppCompatActivity {
    private static final String TAG = "BtActivity";
    private static final boolean DEBUG = true;
    private static final int REQUEST_ENABLE_BLUETOOTH = 10;
    private static final int REQUEST_LOCATION_PERMISSION = 11;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (DEBUG) {
            Log.d(TAG, "onActivityResult: requestCode:" + requestCode + " resultCode:" + resultCode);
            Log.d(TAG, "onActivityResult: data:" + (data == null ? "null" : data.toString()));
        }
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
          /*  mTvData.setText((resultCode == RESULT_OK ? "打开蓝牙成功" : "打开蓝牙失败"));
            mTvData.setClickable(true);*/

            /*if (resultCode == RESULT_OK) {
                scanBleDevice();
            }*/
        }
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (PermissionUtil.parseAllResults(grantResults)) {
                //checkLocationStatus();
            } else {
                ToastUtil.show("你已经禁用定位权限");
            }

        }
    }

    public boolean hasLocationPermission() {
        return PermissionUtil.checkLocationCoarseAndFine(this);
    }

    public void reqLocationPermission() {
        PermissionUtil.requestLocationCoarseAndFine(this, REQUEST_LOCATION_PERMISSION);
    }

    public void reqEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
    }
}
