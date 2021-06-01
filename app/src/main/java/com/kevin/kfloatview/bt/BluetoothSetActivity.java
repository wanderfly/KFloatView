package com.kevin.kfloatview.bt;


import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kevin.kfloatview.R;
import com.kevin.kfloatview.bt.adapter.BtSettingDeviceAdapter;
import com.kevin.kfloatview.bt.adapter.BtSettingDeviceInfo;

import java.util.ArrayList;

public class BluetoothSetActivity extends BluetoothPmActivity implements View.OnClickListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "BluetoothSetActivity";


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

        findViewById(R.id.iv_bt_setting_close).setOnClickListener(this);
        mIvLeftAnim = findViewById(R.id.iv_bt_setting_left_anim);
        mIvRightAnim = findViewById(R.id.iv_bt_setting_right_anim);

        mLeftDrawable = (AnimationDrawable) mIvLeftAnim.getDrawable();
        mRightDrawable = (AnimationDrawable) mIvRightAnim.getDrawable();

        initRcBondedDevices();
        initRcScanDevices();

    }

    private void initRcBondedDevices() {
        ArrayList<BtSettingDeviceInfo> lists = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            BtSettingDeviceInfo info = new BtSettingDeviceInfo("打印机设备" + (i + 1), i == 5);
            lists.add(info);
        }

        mRcBondedDevs = findViewById(R.id.rc_bt_setting_bonded_devices);
        mRcBondedDevs.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRcBondedDevs.setLayoutManager(linearLayoutManager);
        mAdapterBonded = new BtSettingDeviceAdapter(this, lists);
        mRcBondedDevs.setAdapter(mAdapterBonded);
        mAdapterBonded.setOnItemClickListener((view, position) -> {

            Log.e(TAG, "initRcBondedDevices: position:" + position);
        });
    }

    private void initRcScanDevices() {
        ArrayList<BtSettingDeviceInfo> lists = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            BtSettingDeviceInfo info = new BtSettingDeviceInfo("打印机设备" + (11 + i), false);
            lists.add(info);
        }

        mRcScanDevs = findViewById(R.id.rc_bt_setting_scan_devices);
        mRcScanDevs.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRcScanDevs.setLayoutManager(linearLayoutManager);
        mAdapterScan = new BtSettingDeviceAdapter(this, lists);
        mRcScanDevs.setAdapter(mAdapterScan);
        mAdapterScan.setOnItemClickListener(new BtSettingDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.e(TAG, "onItemClick: position:" + position);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        mLeftDrawable.start();
        mRightDrawable.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLeftDrawable.stop();
        mRightDrawable.stop();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_bt_setting_close:
                finish();
                break;
        }

    }
}