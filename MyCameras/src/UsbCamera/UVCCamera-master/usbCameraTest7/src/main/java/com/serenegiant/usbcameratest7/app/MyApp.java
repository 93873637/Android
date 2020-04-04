package com.serenegiant.usbcameratest7.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.serenegiant.usbcameratest7.logic.ComDef;
import com.serenegiant.usbcameratest7.utils.LogUtils;

/**
 * MyApp.java
 * Created by liz on 18-1-8.
 */

@SuppressWarnings("unused")
public class MyApp extends Application {
    private static MyApp mAppInst;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.setTag(ComDef.APP_NAME);
    }

    public static Context getAppContext() {
        return mAppInst;
    }

    public static void exitApp() {
        int pid = android.os.Process.myPid();
        LogUtils.d("exitApp, pid = " + pid);
        android.os.Process.killProcess(pid);
    }

    @Override
    public void onTerminate() {
        LogUtils.d("onTerminate");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        LogUtils.d("onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        LogUtils.d("onTrimMemory");
        super.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtils.d("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }
}
