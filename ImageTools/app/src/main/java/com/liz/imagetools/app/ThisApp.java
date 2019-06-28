package com.liz.imagetools.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.liz.imagetools.logic.ComDef;
import com.liz.imagetools.logic.DataLogic;
import com.liz.imagetools.ui.MainActivity;
import com.liz.imagetools.utils.LogUtils;
import com.liz.imagetools.utils.SysUtils;

/**
 * ThisApp.java
 * Created by liz on 18-1-8.
 */

@SuppressWarnings("unused")
public class ThisApp extends Application {

    private static ThisApp mAppInst;
    private static Activity mMainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInst = this;
        mMainActivity = null;
        LogUtils.setTag(ComDef.APP_NAME);
        LogUtils.d("ThisApp:onCreate: mAppVersion=" + SysUtils.getVersionName(this));
        DataLogic.init(getAppContext());
    }

    public static Context getAppContext() {
        return mAppInst;
    }

    public static void exitApp() {
        int pid = android.os.Process.myPid();
        LogUtils.d("exitApp, pid=" + pid);
        android.os.Process.killProcess(pid);
    }

    public static void setMainActivity(Activity mainActivity) {
        mMainActivity = mainActivity;
    }

    public static Activity getMainActivity() {
        return mMainActivity;
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
