package com.kevin.kfloatview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kevin.kfloatview.bt.BluetoothService;

public class MainActivity extends BtActivity {
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
        printData("akdcjladbjbalwkbvdajbdsjkabvjds");
        printData("cadsbbj");
    }


    private BtServiceConnected mBtServiceConnected;
    private BtPrintCallback mBtPrintCallback;
    private BluetoothService mBtService;
    private BluetoothService.BluetoothBinder mBtBinder;


    private void bindBtService() {
        Intent intent = new Intent(this, BluetoothService.class);
        if (mBtServiceConnected == null)
            mBtServiceConnected = new BtServiceConnected();
        bindService(intent, mBtServiceConnected, BIND_AUTO_CREATE);
    }

    private void unBindBtService() {
        if (mBtServiceConnected != null) {
            unbindService(mBtServiceConnected);
            mBtServiceConnected = null;
        }

    }

    private void printData(String jsonString) {
        if (mBtService != null) {
            mBtService.printData(jsonString);
        }
    }


    private class BtServiceConnected implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected: ");
            mBtBinder = (BluetoothService.BluetoothBinder) service;
            mBtBinder.setCurActivity(MainActivity.this);
            mBtService = mBtBinder.getBtService();
            if (mBtPrintCallback == null)
                mBtPrintCallback = new BtPrintCallback();
            mBtService.setPrintCallback(mBtPrintCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBtBinder.releaseCurActivity();
            Log.e(TAG, "onServiceDisconnected: ");
        }
    }

    private class BtPrintCallback implements BluetoothService.PrintCallback {

        @Override
        public void stateChange(String data) {
            Log.e(TAG, "stateChange: " + data);
            mTvState.setText(data);
        }
    }


}