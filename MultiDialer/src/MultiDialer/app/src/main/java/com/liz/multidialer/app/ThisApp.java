package com.liz.multidialer.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.liz.androidutils.LogUtils;
import com.liz.multidialer.logic.ComDef;
import com.liz.multidialer.logic.DataLogic;

/**
 * ThisApp.java
 * Created by liz on 18-1-8.
 */

@SuppressWarnings("unused")
public class ThisApp extends Application {
    private static ThisApp mAppInst;
    public static String mAppVersion = "";
    public static String mAppVersionShow = "";  //text show in log

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInst = this;
        mAppVersion = "";
        mAppVersionShow = "";

        LogUtils.setTag(ComDef.APP_NAME);
        DataLogic.init();
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtils.d("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }
}
