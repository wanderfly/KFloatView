package com.kevin.kfloatview.bt.adapter

import android.bluetooth.BluetoothDevice

/**
 *@author Kevin  2021/6/1
 */
data class BtSettingDeviceInfo(val btDevice:BluetoothDevice, var linkState: Boolean)