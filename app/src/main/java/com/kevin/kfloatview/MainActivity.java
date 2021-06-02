package com.kevin.kfloatview;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kevin.kfloatview.bt.BluetoothBinder;
import com.kevin.kfloatview.bt.BluetoothService;
import com.kevin.kfloatview.bt.BluetoothPmActivity;


public class MainActivity extends BluetoothPmActivity {
    private static final String TAG = "MainActivity";

    private TextView mTvState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvState = findViewById(R.id.tv_state);
    }


    public void onClickOpenBt(View view) {
        Log.e(TAG, "onClickOpenBt: ");
        bindBtService();
    }

    public void onClickStopBt(View view) {
        Log.e(TAG, "onClickStopBt: ");
        unBindBtService();
    }

    public void onClickPrintBt(View view) {
        Log.e(TAG, "onClickPrintBt: ");
        printData("akdcjladbjbalwkbvdajbdsjkabvjds");
        printData("cadsbbj");
    }

    public void onClickSecond(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private BtServiceConnected mBtServiceConnected;
    private BluetoothService mBtService;


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


    private class BtServiceConnected implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            BluetoothBinder mBtBinder = (BluetoothBinder) service;
            Log.e(TAG, "onServiceConnected: mBtBinder:" + mBtBinder);
            mBtService = mBtBinder.getBtService();
            mBtService.setCurActivity(MainActivity.this);
            mBtService.showBtStateView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: ");
        }
    }


}