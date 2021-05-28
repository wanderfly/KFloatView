package com.kevin.kfloatview.bt;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;

/**
 *
 */
public class BlueToothUtil {

    public static boolean createBond(Class<?> cls, BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = cls.getMethod("createBond");
        return (Boolean) createBondMethod.invoke(btDevice);
    }


    public static boolean removeBond(Class<?> cls, BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = cls.getMethod("removeBond");
        return (Boolean) createBondMethod.invoke(btDevice);
    }


    public static boolean cancelBondProcess(Class<?> cls, BluetoothDevice device) throws Exception {
        Method createBondMethod = cls.getMethod("cancelBondProcess");
        return (Boolean) createBondMethod.invoke(device);
    }


    public static boolean cancelPairingUserInput(Class<?> cls, BluetoothDevice device) throws Exception {
        Method createBondMethod = cls.getMethod("cancelPairingUserInput");
        return (Boolean) createBondMethod.invoke(device);
    }

}
