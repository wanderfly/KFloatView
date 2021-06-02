package com.kevin.kfloatview.bt;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kevin.kfloatview.PermissionUtil;

/**
 * @author Kevin  2021/5/28
 * 蓝牙权限请求activity  (BluetoothPermissionActivity)
 * 所需权限如下:
 * <p>
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * //没有定位权限无法搜索周边设备
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * <p/>
 */
public abstract class BluetoothPmActivity extends AppCompatActivity {
    private static final String TAG = BluetoothPmActivity.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1000;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    public static final String BT_ACTION_LOCATION_PERMISSION_GRANT_STATE = "action_location_permission_grant_state";
    public static final String BT_ACTION_ENABLE_BLUETOOTH_STATE = "action_enable_bluetooth_state";
    public static final String EXTRA_NAME = "extra_name";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) {
            Log.d(TAG, "onActivityResult: requestCode:" + requestCode + " resultCode:" + resultCode);
            Log.d(TAG, "onActivityResult: data:" + (data == null ? "null" : data.toString()));
        }
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                sendStateBroad(BT_ACTION_ENABLE_BLUETOOTH_STATE, true);
            } else if (resultCode == RESULT_CANCELED) {
                sendStateBroad(BT_ACTION_ENABLE_BLUETOOTH_STATE, false);
            }
        }
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION)
            sendStateBroad(BT_ACTION_LOCATION_PERMISSION_GRANT_STATE, PermissionUtil.parseAllResults(grantResults));
    }

    private void sendStateBroad(@NonNull String action, boolean state) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(EXTRA_NAME, state);
        sendBroadcast(intent);
    }

    protected boolean hasLocationPermission() {
        return PermissionUtil.checkLocationCoarseAndFine(this);
    }

    protected void reqLocationPermission() {
        PermissionUtil.requestLocationCoarseAndFine(this, REQUEST_LOCATION_PERMISSION);
    }

    protected void reqEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
    }
}
