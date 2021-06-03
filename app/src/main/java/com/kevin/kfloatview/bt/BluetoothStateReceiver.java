package com.kevin.kfloatview.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


import com.kevin.kfloatview.bt.adapter.BtSettingDeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    private final BluetoothService mBtService;
    private IBluetoothStateCallBack mCallBack;

    public BluetoothStateReceiver(BluetoothService bluetoothService) {

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

        mBtService = bluetoothService;
    }

    public void setCallBack(IBluetoothStateCallBack callBack) {
        this.mCallBack = callBack;
    }

    public IntentFilter getBtStateFilter() {
        return mBtStateFilter;
    }

    public void sendStateCode(@IBluetoothStateCallBack.BtStateCode int stateCode) {
        if (mCallBack != null)
            mCallBack.otherState(stateCode, null);
    }

    public void sendStateCode(@IBluetoothStateCallBack.BtStateCode int stateCode, BluetoothDevice bluetoothDevice) {
        if (mCallBack != null)
            mCallBack.otherState(stateCode, bluetoothDevice);
    }

    /**
     * 过滤掉发现设备的map中已经绑定的设备
     */
    private void filterBondedDevsInFindMap() {
        Set<BluetoothDevice> set = mBtService.getBondedDevs();
        if (set != null) {
            int proSize = mMapCacheFindDevs.size();
            for (BluetoothDevice dev : set) {
                mMapCacheFindDevs.remove(dev.getName());
            }
            int afterSize = mMapCacheFindDevs.size();
            if (proSize - afterSize >= 1)
                sendFindDevsInHashMap();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        /*if (DEBUG) {
            Log.e(TAG, "onReceive: 蓝牙监听广播: action:" + action);
            Log.e(TAG, "onReceive: 蓝牙监听广播: intent:" + intent.toString());
        }*/
        if (action != null) {
            switch (action) {
                //Todo 蓝牙adapter
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                case BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED:
                    break; //上面为暂未处理action
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    aConnectionStateChanged(intent);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.e(TAG, "onReceive: ACTION_DISCOVERY_FINISHED");
                    sendStateCode(IBluetoothStateCallBack.BT_CODE_DISCOVERY_FINISHED);
                    //filterBondedDevsInFindMap();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.e(TAG, "onReceive: ACTION_DISCOVERY_STARTED");
                    mCacheFindDevs.clear();
                    mMapCacheFindDevs.clear();
                    sendStateCode(IBluetoothStateCallBack.BT_CODE_DISCOVERY_START);
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

    private void aConnectionStateChanged(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        int curState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
        int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);

        if (DEBUG) {
            Log.e(TAG, "aConnectionStateChanged: 设备名字:" + device.getName());
            Log.e(TAG, "aConnectionStateChanged: previousState:" + getConnectValue(previousState));
            Log.e(TAG, "aConnectionStateChanged: curState:" + getConnectValue(curState));
        }
    }

    private void aBtEnableState(Intent intent) {
        boolean isOk = intent.getBooleanExtra(BluetoothPmActivity.EXTRA_NAME, false);
        Log.e(TAG, "aBtEnableState: " + isOk);
        sendStateCode(isOk ? IBluetoothStateCallBack.BT_CODE_ENABLE_SUCCESS : IBluetoothStateCallBack.BT_CODE_ENABLE_FAILED);
    }

    private void aLocationGrantState(Intent intent) {
        boolean isOk = intent.getBooleanExtra(BluetoothPmActivity.EXTRA_NAME, false);
        Log.e(TAG, "aLocationGrantState: " + isOk);
        sendStateCode(isOk ? IBluetoothStateCallBack.BT_CODE_LOC_GRANT_SUCCESS : IBluetoothStateCallBack.BT_CODE_LOC_GRANT_FAILED);
    }

    private void aBondStateChanged(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        int curState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
        int previousState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

        if (DEBUG) {
            Log.e(TAG, "aBondStateChanged: 设备名字:" + device.getName());
            Log.e(TAG, "aBondStateChanged: previousState:" + getBondValue(previousState));
            Log.e(TAG, "aBondStateChanged: curState:" + getBondValue(curState));
        }

        if (previousState == BluetoothDevice.BOND_BONDING && curState == BluetoothDevice.BOND_BONDED) {
            if (mMapCacheFindDevs.remove(device.getName()) != null) //将已绑定的设备从发现的设备集合中移除
                sendFindDevsInHashMap();
            sendStateCode(IBluetoothStateCallBack.BT_CODE_BONDED);
            mBtService.sendCurBondedDevs();
            Log.e(TAG, "aBondStateChanged: 有设备绑定成功+++++");
        } else if (previousState == BluetoothDevice.BOND_BONDED && curState == BluetoothDevice.BOND_NONE) {
            sendStateCode(IBluetoothStateCallBack.BT_CODE_UN_BONDED);
            mBtService.sendCurBondedDevs();
            Log.e(TAG, "aBondStateChanged: 有设备解绑成功-----");
        }
    }

    private String getBondValue(int state) {
        switch (state) {
            case BluetoothDevice.BOND_NONE:
                return "没有绑定";
            case BluetoothDevice.BOND_BONDING:
                return "绑定中...";
            case BluetoothDevice.BOND_BONDED:
                return "绑定完成";
        }
        return "未知状态";
    }

    private String getConnectValue(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_CONNECTED:
                return "连接成功";
            case BluetoothAdapter.STATE_CONNECTING:
                return "连接中...";
            case BluetoothAdapter.STATE_DISCONNECTED:
                return "已断开";
            case BluetoothAdapter.STATE_DISCONNECTING:
                return "断开中...";
        }
        return "未知状态";
    }


    /**
     * 将hashMap中扫描到的设备，通过接口回调
     */
    private void sendFindDevsInHashMap() {
        if (mCallBack != null && mMapCacheFindDevs.size() > 0) {
            mCacheFindDevs.clear();
            Set<Map.Entry<String, BtSettingDeviceInfo>> sets = mMapCacheFindDevs.entrySet();
            for (Map.Entry<String, BtSettingDeviceInfo> set : sets) {
                mCacheFindDevs.add(set.getValue());
            }
            mCallBack.updateScanDevs(mCacheFindDevs);
        }
    }

    private void aDevicesFind(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //Log.e(TAG, "aDevicesFind: device:" + device);
        if (device != null) {
            String findName = device.getName();
            String findMac = device.getAddress();
            if (DEBUG) {
                Log.d(TAG, "aDevicesFind: 设备名字:" + findName);
                Log.d(TAG, "aDevicesFind: 物理地址:" + findMac);
            }
            if (findName != null) {
                int proSize = mMapCacheFindDevs.size();
                Set<BluetoothDevice> set = mBtService.getBondedDevs();
                if (set != null) {
                    for (BluetoothDevice bondedDev : set) {
                        if (!Objects.equals(bondedDev.getName(), findName)) //只将不在已经绑定的集合中的新设备添加到发现的设备集合中
                            mMapCacheFindDevs.put(findName, new BtSettingDeviceInfo(device, false));//通过HashMap去重
                    }
                }
                int afterSize = mMapCacheFindDevs.size();
                if (afterSize - proSize >= 1)//说明有新的设备发现
                    sendFindDevsInHashMap();
                filterBondedDevsInFindMap();
                if (mMapCacheFindDevs.size() >= DEFAULT_SCAN_DEV_NUMBER)
                    mBtService.scanBluetooth(false);
            }
        }
    }
}
