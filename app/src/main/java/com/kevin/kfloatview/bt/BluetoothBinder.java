package com.kevin.kfloatview.bt;

import android.os.Binder;

import androidx.annotation.NonNull;


public class BluetoothBinder extends Binder {
    @NonNull
    public BluetoothService btService;

    public BluetoothBinder(@NonNull BluetoothService service) {
        btService = service;
    }

    @NonNull
    public BluetoothService getBtService() {
        return btService;
    }
}
