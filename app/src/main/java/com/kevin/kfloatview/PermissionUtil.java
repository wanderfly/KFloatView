package com.kevin.kfloatview;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Kevin  2020/9/11
 */
public final class PermissionUtil {
    private PermissionUtil() {
    }

    /**
     * 生成需要检测的权限请求字符串数组
     * 主要用于安装时请求权限
     *
     * @param ctx 上下文
     */
    public static String[] generatePermissionArray(Context ctx) {
        List<String> lists = new ArrayList<>();
        if (!checkReadWrite(ctx)) {
            lists.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            lists.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!checkReadPhoneStatus(ctx)) {
            lists.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!checkLocationCoarseAndFine(ctx)) {
            lists.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            lists.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!checkCamera(ctx)) {
            lists.add(Manifest.permission.CAMERA);
        }
        if (Build.VERSION.SDK_INT > 28) {
            if (!checkForegroundService(ctx))
                lists.add(Manifest.permission.FOREGROUND_SERVICE);
        }
        if (!checkBoot(ctx)) {
            lists.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
        }
        int size = lists.size();
        final String[] permissions = new String[size];
        for (int i = 0; i < size; i++) {
            permissions[i] = lists.get(i);
        }
        return permissions;
    }

    private static boolean checkBase(Context ctx, String permission) {
        return ActivityCompat.checkSelfPermission(ctx.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkRead(Context ctx) {
        return checkBase(ctx, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean checkWrite(Context ctx) {
        return checkBase(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean checkReadWrite(Context ctx) {
        return checkRead(ctx) && checkWrite(ctx);
    }

    public static boolean checkCamera(Context ctx) {
        return checkBase(ctx, Manifest.permission.CAMERA);
    }

    public static boolean checkLocationCoarse(Context ctx) {
        return checkBase(ctx, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static boolean checkLocationFine(Context ctx) {
        return checkBase(ctx, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean checkLocationCoarseAndFine(Context ctx) {
        return checkLocationCoarse(ctx) && checkLocationFine(ctx);
    }

    public static boolean checkReadPhoneStatus(Context ctx) {
        return checkBase(ctx, Manifest.permission.READ_PHONE_STATE);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static boolean checkForegroundService(Context ctx) {
        return checkBase(ctx, Manifest.permission.FOREGROUND_SERVICE);
    }

    public static boolean checkBoot(Context ctx) {
        return checkBase(ctx, Manifest.permission.RECEIVE_BOOT_COMPLETED);
    }


    public static void requestBase(Activity ac, int request, String... permission) {
        ActivityCompat.requestPermissions(ac, permission, request);
    }

    public static void requestRead(Activity ac, int request) {
        requestBase(ac, request, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void requestWrite(Activity ac, int request) {
        requestBase(ac, request, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestReadWrite(Activity ac, int request) {
        requestBase(ac, request, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestCamera(Activity ac, int request) {
        requestBase(ac, request, Manifest.permission.CAMERA);
    }

    public static void requestLocationCoarse(Activity ac, int request) {
        requestBase(ac, request, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static void requestLocationFine(Activity ac, int request) {
        requestBase(ac, request, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * 单独请求该权限并不会弹出权限窗口
     */
    @TargetApi(Build.VERSION_CODES.Q)
    public static void requestLocationBackground(Activity ac, int request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBase(ac, request, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
    }

    public static void requestBoot(Activity ac, int request) {
        requestBase(ac, request, Manifest.permission.RECEIVE_BOOT_COMPLETED);
    }

    public static void requestLocationCoarseAndFine(Activity ac, int request) {
        requestBase(ac, request, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * 单独请求该权限并不会弹出权限窗口
     */
    public static void requestReadPhoneStatus(Activity ac, int request) {
        requestBase(ac, request, Manifest.permission.READ_PHONE_STATE);
    }


    /**
     * 打印动态权限请求结果
     */
    public static void printRequestResult(String TAG, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionsResult: request:" + requestCode + " 授权结果长度:" + grantResults.length);
        int perLen = permissions.length;
        for (int i = 0; i < perLen; i++) {
            Log.e(TAG, "onRequestPermissionsResult: permissions[" + i + "]" + "=" + permissions[i]);
        }
        int grantLen = grantResults.length;
        for (int i = 0; i < grantLen; i++) {
            Log.e(TAG, "onRequestPermissionsResult: grantResults[" + i + "]" + "=" + grantResults[i]);
        }
    }

    /**
     * 检查申请权限组的授权情况,只要有一个没有被授权，则返回false
     */
    public static boolean parseAllResults(@NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }


}
