package com.kevin.kfloatview.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothStateReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = true;
    private static final String TAG = "BluetoothStateReceiver";

    private final IntentFilter mBtStateFilter;

    public BluetoothStateReceiver() {

        mBtStateFilter = new IntentFilter();
        //蓝牙适配器
        mBtStateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        mBtStateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mBtStateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mBtStateFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        mBtStateFilter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        mBtStateFilter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mBtStateFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        mBtStateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //设备状态
        mBtStateFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mBtStateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //蓝牙权限请求activity
        mBtStateFilter.addAction(BluetoothActivity.BT_ACTION_LOCATION_PERMISSION_GRANT_STATE);
        mBtStateFilter.addAction(BluetoothActivity.BT_ACTION_ENABLE_BLUETOOTH_STATE);
    }

    public IntentFilter getBtStateFilter() {
        return mBtStateFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: 蓝牙监听广播: action:" + action);
        Log.e(TAG, "onReceive: 蓝牙监听广播: intent:" + intent.toString());
        if (action != null) {
            switch (action) {
                //Todo 蓝牙adapter
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    aConnectionStateChanged(intent);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    aDiscoveryFinished(intent);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    aDiscoveryStarted(intent);
                    break;
                case BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED:
                    aLocalNameChanged(intent);
                    break;
                case BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE:
                    aRequestDiscoverable(intent);
                    break;
                case BluetoothAdapter.ACTION_REQUEST_ENABLE:
                    aRequestEnable(intent);
                    break;
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    aScanModeChanged(intent);
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    aStateChanged(intent);
                    break;
                //Todo 蓝牙device发现的设备
                case BluetoothDevice.ACTION_FOUND:
                    aDevicesFind(intent);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    aBondStateChanged(intent);
                    break;
                //Todo 蓝牙权限请求activity
                case BluetoothActivity.BT_ACTION_LOCATION_PERMISSION_GRANT_STATE:
                    aLocationGrantState(intent);
                    break;
                case BluetoothActivity.BT_ACTION_ENABLE_BLUETOOTH_STATE:
                    aBtEnableState(intent);
                    break;
            }
        }
    }

    private void aBtEnableState(Intent intent) {
        Log.e(TAG, "aBtEnableState: " + intent.getBooleanExtra(BluetoothActivity.EXTRA_NAME, false));
    }

    private void aLocationGrantState(Intent intent) {
        Log.e(TAG, "aLocationGrantState: " + intent.getBooleanExtra(BluetoothActivity.EXTRA_NAME, false));

    }

    private void aBondStateChanged(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
        int previousState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

        Log.e(TAG, "aBondStateChanged: bondState:" + bondState);
        Log.e(TAG, "aBondStateChanged: previousState:" + previousState);

        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            //Toast.makeText(BluetoothService.this, "配对成功:" + device.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void aDevicesFind(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device != null) {
            String devName = device.getName();
            String devMac = device.getAddress();
            if (DEBUG) {
                Log.d(TAG, "aDevicesFind: 设备名字:" + devName);
                Log.d(TAG, "aDevicesFind: 物理地址:" + devMac);
            }
            if (devName != null) {
               /* mSBuilder.append("设备名字:")
                        .append(devName)
                        .append("\n")
                        .append("设备地址:")
                        .append(devMac)
                        .append("\n\n");*/
            }

            //if ("FXNB-868681042704138".equals(devName)) {
            //.if ("Printer_E4BA".equals(devName)) {
            //if ("NP100S31B3".equals(devName)) {
            //Log.e(TAG, "aDevicesFind: devname:length="+devName.length()+" "+"NP100S31B3".length());
            if ("NP100S31B3  ".equals(devName)) {
                //if ("DESKTOP-2D8RTSR".equals(devName)) {
                Log.e(TAG, "aDevicesFind: ----发现指定设备-----");
                Log.e(TAG, "aDevicesFind: 绑定状态:" + device.getBondState());
                /*isScanStandardBluetooth(false);

                mCacheFound = device;*/

                    /*Message msg = Message.obtain();
                    msg.what = MSG_CONNECT_BLUETOOTH_DEVICE;
                    msg.obj = device;
                    mHandler.sendMessageDelayed(msg, 2000);*/
            }
        }
    }

    private void aStateChanged(Intent intent) {

    }

    private void aScanModeChanged(Intent intent) {

    }

    private void aRequestEnable(Intent intent) {
        Log.e(TAG, "aRequestEnable: 请求打开蓝牙");
    }

    private void aRequestDiscoverable(Intent intent) {
    }

    private void aLocalNameChanged(Intent intent) {

    }

    private void aDiscoveryStarted(Intent intent) {

    }

    private void aDiscoveryFinished(Intent intent) {
        //mTvSearchState.setText("搜索结束");
    }

    private void aConnectionStateChanged(Intent intent) {

    }
}
