package com.kevin.kfloatview;

import android.app.Application;

/**
 * @author Kevin  2021/5/28
 */
public class MyApplication extends Application {
    public static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }

    public static MyApplication getMyApplication() {
        return myApplication;
    }
}
