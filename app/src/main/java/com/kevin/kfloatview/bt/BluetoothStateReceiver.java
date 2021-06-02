package com.kevin.kfloatview.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


import com.kevin.kfloatview.bt.adapter.BtSettingDeviceInfo;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BluetoothStateReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = true;
    private static final String TAG = BluetoothStateReceiver.class.getSimpleName();
    private static final int DEFAULT_SCAN_DEV_NUMBER = 16;//默认搜索蓝牙设备数(去重后)

    private final ConcurrentLinkedQueue<Runnable> mConQueue = new ConcurrentLinkedQueue<>();
    private final IntentFilter mBtStateFilter;
    private final ArrayList<BtSettingDeviceInfo> mCacheFindDevs = new ArrayList<>();
    private final HashMap<String, BtSettingDeviceInfo> mMapCacheFindDevs = new HashMap<>(DEFAULT_SCAN_DEV_NUMBER);

    private final BluetoothAdapter mAdapter;
    private IBluetoothStateCallBack mCallBack;

    public BluetoothStateReceiver(BluetoothAdapter adapter) {

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
        mBtStateFilter.addAction(BluetoothPmActivity.BT_ACTION_LOCATION_PERMISSION_GRANT_STATE);
        mBtStateFilter.addAction(BluetoothPmActivity.BT_ACTION_ENABLE_BLUETOOTH_STATE);

       /* mConQueue.add();
        mConQueue.poll()*/

        mAdapter = adapter;
    }

    public void setCallBack(IBluetoothStateCallBack callBack) {
        this.mCallBack = callBack;
    }

    public IntentFilter getBtStateFilter() {
        return mBtStateFilter;
    }

    private void sendStatueCode(@IBluetoothStateCallBack.BtStateCode int stateCode) {
        if (mCallBack != null)
            mCallBack.otherState(stateCode, "");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "onReceive: 蓝牙监听广播: action:" + action);
        Log.e(TAG, "onReceive: 蓝牙监听广播: intent:" + intent.toString());
        if (action != null) {
            switch (action) {
                //Todo 蓝牙adapter
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                case BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED:
                    break; //上面为暂未处理action
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    //sendFindDevsInHashMap();
                    sendStatueCode(IBluetoothStateCallBack.BT_CODE_DISCOVERY_FINISHED);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    mCacheFindDevs.clear();
                    mMapCacheFindDevs.clear();
                    mCacheCount = 0;
                    sendStatueCode(IBluetoothStateCallBack.BT_CODE_DISCOVERY_START);
                    break;
                case BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE:
                    Log.e(TAG, "onReceive: 请求自身蓝牙可被发现");
                    break;
                case BluetoothAdapter.ACTION_REQUEST_ENABLE:
                    Log.e(TAG, "aRequestEnable: 请求打开蓝牙");
                    break;
                //Todo 蓝牙device发现的设备
                case BluetoothDevice.ACTION_FOUND:
                    aDevicesFind(intent);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    aBondStateChanged(intent);
                    break;
                //Todo 蓝牙权限请求activity
                case BluetoothPmActivity.BT_ACTION_LOCATION_PERMISSION_GRANT_STATE:
                    aLocationGrantState(intent);
                    break;
                case BluetoothPmActivity.BT_ACTION_ENABLE_BLUETOOTH_STATE:
                    aBtEnableState(intent);
                    break;
            }
        }
    }

    private void aBtEnableState(Intent intent) {
        boolean isOk = intent.getBooleanExtra(BluetoothPmActivity.EXTRA_NAME, false);
        Log.e(TAG, "aBtEnableState: " + isOk);
        sendStatueCode(isOk ? IBluetoothStateCallBack.BT_CODE_ENABLE_SUCCESS : IBluetoothStateCallBack.BT_CODE_ENABLE_FAILED);
    }

    private void aLocationGrantState(Intent intent) {
        boolean isOk = intent.getBooleanExtra(BluetoothPmActivity.EXTRA_NAME, false);
        Log.e(TAG, "aLocationGrantState: " + isOk);
        sendStatueCode(isOk ? IBluetoothStateCallBack.BT_CODE_LOC_GRANT_SUCCESS : IBluetoothStateCallBack.BT_CODE_LOC_GRANT_FAILED);
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

    /**
     * 生成小于DEFAULT_SCAN_DEV_NUMBER的随机整数
     */
    private int generateRandom() {
        return (int) (Math.random() * DEFAULT_SCAN_DEV_NUMBER);
    }

    private int mCacheCount;

    /**
     * 将hashMap中扫描到的设备，通过接口回调
     */
    private void sendFindDevsInHashMap() {
        //if (mCallBack != null && mMapCacheFindDevs.size() <= generateRandom()) {
        if (mCallBack != null && mMapCacheFindDevs.size() > 0) {
            mCacheFindDevs.clear();
            Set<Map.Entry<String, BtSettingDeviceInfo>> sets = mMapCacheFindDevs.entrySet();
            for (Map.Entry<String, BtSettingDeviceInfo> set : sets) {
                mCacheFindDevs.add(set.getValue());
            }
            mCallBack.updateScanDevs(mCacheFindDevs);
            ++mCacheCount;
            Log.e("Kevin", "aDevicesFind: 接口回调次数:" + mCacheCount);
        }
    }

    private void aDevicesFind(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.e(TAG, "aDevicesFind: device:" + device);
        if (device != null) {
            String devName = device.getName();
            String devMac = device.getAddress();
            if (DEBUG) {
                Log.d(TAG, "aDevicesFind: 设备名字:" + devName);
                Log.d(TAG, "aDevicesFind: 物理地址:" + devMac);
            }
            if (devName != null) {
                Log.e(TAG, "aDevicesFind: mCacheFindDevs:" + mCacheFindDevs + "hashCode:" + mCacheFindDevs.hashCode());
                //通过HashMap去重复
                int proSize = mMapCacheFindDevs.size();
                mMapCacheFindDevs.put(devName, new BtSettingDeviceInfo(device, false));
                int afterSize = mMapCacheFindDevs.size();
                if (afterSize - proSize >= 1)//说明有新的设备发现
                    sendFindDevsInHashMap();
                if (mMapCacheFindDevs.size() >= DEFAULT_SCAN_DEV_NUMBER)
                    mAdapter.cancelDiscovery();
            }

            //if ("FXNB-868681042704138".equals(devName)) {
            //.if ("Printer_E4BA".equals(devName)) {
            //if ("NP100S31B3".equals(devName)) {
            //Log.e(TAG, "aDevicesFind: devname:length="+devName.length()+" "+"NP100S31B3".length());
            if ("NP100S31B3  ".equals(devName)) {
                //if ("DESKTOP-2D8RTSR".equals(devName)) {
                Log.e(TAG, "aDevicesFind: ----发现指定设备-----");
                Log.e(TAG, "aDevicesFind: 绑定状态:" + device.getBondState());
            }
        }
    }
}
