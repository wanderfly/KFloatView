package com.kevin.kfloatview.bt;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.kfloatview.R;
import com.kevin.kfloatview.bt.adapter.BtSettingDeviceAdapter;
import com.kevin.kfloatview.bt.adapter.BtSettingDeviceInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class BluetoothSetActivity extends BluetoothPmActivity implements View.OnClickListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "BluetoothSetActivity";

    private static final String SCAN_STATE_DEFAULT = "点击开始搜索";
    private static final String SCAN_STATE_ING = "搜索设备中...";
    private static final String SCAN_STATE_FINISHED = "搜索完成";

    private BtServiceConnected mBtServiceConnected;
    private BtStateCallBack mBtStateCallback;
    private BluetoothService mBtService;
    private BluetoothBinder mBtBinder;

    private TextView mTvBtScanState;
    private TextView mTvBondedDevs;
    private TextView mTvScanDevs;

    private ImageView mIvLeftAnim;
    private ImageView mIvRightAnim;

    private AnimationDrawable mLeftDrawable;
    private AnimationDrawable mRightDrawable;

    private RecyclerView mRcBondedDevs;
    private RecyclerView mRcScanDevs;

    private BtSettingDeviceAdapter mAdapterBonded;
    private BtSettingDeviceAdapter mAdapterScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_setting);

        mTvBtScanState = findViewById(R.id.tv_bt_setting_scan_state);
        mTvBondedDevs = findViewById(R.id.tv_bt_setting_bonded_devices_title);
        mTvScanDevs = findViewById(R.id.tv_bt_setting_scan_devices_title);
        mTvBtScanState.setText(SCAN_STATE_DEFAULT);

        findViewById(R.id.iv_bt_setting_close).setOnClickListener(this);
        findViewById(R.id.iv_bt_setting_circle).setOnClickListener(this);
        mIvLeftAnim = findViewById(R.id.iv_bt_setting_left_anim);
        mIvRightAnim = findViewById(R.id.iv_bt_setting_right_anim);
        mIvLeftAnim.setVisibility(View.INVISIBLE);
        mIvRightAnim.setVisibility(View.INVISIBLE);

        mLeftDrawable = (AnimationDrawable) mIvLeftAnim.getDrawable();
        mRightDrawable = (AnimationDrawable) mIvRightAnim.getDrawable();

        initRcBondedDevices();
        initRcScanDevices();

        bindBtService();
    }

    private void initRcBondedDevices() {
        mRcBondedDevs = findViewById(R.id.rc_bt_setting_bonded_devices);
        mRcBondedDevs.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRcBondedDevs.setLayoutManager(linearLayoutManager);
        mAdapterBonded = new BtSettingDeviceAdapter(this, new ArrayList<>());
        mRcBondedDevs.setAdapter(mAdapterBonded);
        mAdapterBonded.setOnItemClickListener((view, position) -> {

            Log.e(TAG, "initRcBondedDevices: position:" + position);
        });
    }

    private void initRcScanDevices() {
        mRcScanDevs = findViewById(R.id.rc_bt_setting_scan_devices);
        mRcScanDevs.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRcScanDevs.setLayoutManager(linearLayoutManager);
        mAdapterScan = new BtSettingDeviceAdapter(this, new ArrayList<>());
        mRcScanDevs.setAdapter(mAdapterScan);
        mAdapterScan.setOnItemClickListener(new BtSettingDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.e(TAG, "onItemClick: position:" + position);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasLocationPermission())
            reqLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDiscover();
        mBtService.setBluetoothStateCallback(null);
        unBindBtService();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick: id:" + v.getId());
        switch (v.getId()) {
            case R.id.iv_bt_setting_close:
                finish();
                break;
            case R.id.iv_bt_setting_circle:
                if (Objects.equals(SCAN_STATE_DEFAULT, mTvBtScanState.getText().toString())) {
                    startDiscovery();
                } else if (Objects.equals(SCAN_STATE_ING, mTvBtScanState.getText().toString())) {
                    stopDiscover();
                } else if (Objects.equals(SCAN_STATE_FINISHED, mTvBtScanState.getText().toString())) {
                    startDiscovery();
                }
                break;
        }
    }

    private void bindBtService() {
        Intent intent = new Intent(this, BluetoothService.class);
        if (mBtServiceConnected == null) {
            mBtServiceConnected = new BtServiceConnected();
            Log.e(TAG, "bindBtService: mBtServiceConnected 为null 创建新的连接者" + mBtServiceConnected);
        }
        bindService(intent, mBtServiceConnected, BIND_AUTO_CREATE);

        startService(intent);
    }

    private void unBindBtService() {
        if (mBtServiceConnected != null) {
            Log.e(TAG, "unBindBtService: mBtServiceConnected 不为null 解除绑定");
            unbindService(mBtServiceConnected);
            mBtServiceConnected = null;
        }

        //stopService()
        //Intent intent = new Intent(this, BluetoothService.class);
        //stopService(intent);

    }

    private void printData(String jsonString) {
        if (mBtService != null) {
            mBtService.printData(jsonString);
        }
    }

    private void startDiscovery() {
        Log.e(TAG, "startDiscovery: mBtService:" + mBtService);
        if (mBtService != null) {
            if (hasLocationPermission()) {
                if (mBtService.isBluetoothEnable()) {
                    boolean isOk = mBtService.scanBluetooth(true);
                } else {
                    reqEnableBluetooth();
                }
            } else {
                reqLocationPermission();
            }
        }
    }

    private void stopDiscover() {
        if (mBtService != null) {
            mBtService.scanBluetooth(false);
        }
    }


    private class BtServiceConnected implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mBtBinder = (BluetoothBinder) service;
            Log.e(TAG, "onServiceConnected: mBtBinder:" + mBtBinder);
            mBtService = mBtBinder.getBtService();
            //mBtService.setCurActivity(BluetoothSetActivity.this);
            if (mBtStateCallback == null)
                mBtStateCallback = new BtStateCallBack();
            mBtService.setBluetoothStateCallback(mBtStateCallback);
            //mBtService.showBtStateView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: ");
        }
    }

    private class BtStateCallBack implements IBluetoothStateCallBack {

        @Override
        public void updateBondedDevs(@NotNull ArrayList<BtSettingDeviceInfo> bondedDevs) {
            Log.e(TAG, "updateBondedDevs: --------------bondedDevs:" + bondedDevs);
            mAdapterBonded.updateBondedDevices(bondedDevs);
            String value = "已配对设备(" + bondedDevs.size() + ")";
            mTvBondedDevs.setText(value);
        }

        @Override
        public void updateScanDevs(@NotNull ArrayList<BtSettingDeviceInfo> scanDevs) {
            Log.e(TAG, "updateScanDevs: ---------------scanDevs:" + scanDevs + " hashCode:" + scanDevs.hashCode());
            mAdapterScan.updateBondedDevices(scanDevs);
            String value = "其它设备(" + scanDevs.size() + ")";
            mTvScanDevs.setText(value);
        }

        @Override
        public void otherState(@BtStateCode int stateCode, @Nullable String stateInfo) {
            switch (stateCode) {
                case IBluetoothStateCallBack.BT_CODE_DISCOVERY_START:
                    mIvLeftAnim.setVisibility(View.VISIBLE);
                    mIvRightAnim.setVisibility(View.VISIBLE);
                    mLeftDrawable.start();
                    mRightDrawable.start();
                    mTvBtScanState.setText(SCAN_STATE_ING);
                    break;
                case IBluetoothStateCallBack.BT_CODE_DISCOVERY_FINISHED:
                    mTvBtScanState.setText(SCAN_STATE_FINISHED);
                    mLeftDrawable.stop();
                    mRightDrawable.stop();
                    mIvLeftAnim.setVisibility(View.INVISIBLE);
                    mIvRightAnim.setVisibility(View.INVISIBLE);
                    break;
                case IBluetoothStateCallBack.BT_CODE_LOC_GRANT_SUCCESS:
                    break;
                case IBluetoothStateCallBack.BT_CODE_LOC_GRANT_FAILED:
                    break;
                case IBluetoothStateCallBack.BT_CODE_ENABLE_SUCCESS:
                    break;
                case IBluetoothStateCallBack.BT_CODE_ENABLE_FAILED:
                    break;

            }
        }
    }
}