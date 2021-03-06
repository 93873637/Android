package com.liz.noannoy.app;

import android.app.Application;
import android.content.Context;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.SysUtils;
import com.liz.noannoy.logic.ComDef;
import com.liz.noannoy.logic.DataLogic;

/**
 * ThisApp.java
 * Created by liz on 18-1-8.
 */

@SuppressWarnings("unused")
public class ThisApp extends Application {
    private static ThisApp mAppInst;
    public static String mAppVersion = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInst = this;
        mAppVersion = SysUtils.getAppVersion(this);
        LogUtils.setTag(ComDef.APP_NAME);
        DataLogic.init();
        LogUtils.d("ThisApp: onCreate, version=" + mAppVersion);
    }

    public static Context getAppContext() {
        return mAppInst;
    }

    public static void exitApp() {
        int pid = android.os.Process.myPid();
        LogUtils.d("exitApp, pid=" + pid);
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
}
