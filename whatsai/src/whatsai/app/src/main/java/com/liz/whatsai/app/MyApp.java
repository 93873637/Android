package com.liz.whatsai.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.PowerManager;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.SysUtils;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.WSListenService;
import com.liz.whatsai.ui.WSNotifier;

/**
 * MyApp.java
 * Created by liz on 18-1-8.
 */

@SuppressWarnings("unused")
public class MyApp extends Application {
    private static MyApp mAppInst;

    public static String mAppVersion = "";
    public static String mAppVersionShow = "";  //text show in log

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtils.setTag(ComDef.APP_NAME);
        if (ComDef.DEBUG) {
            LogUtils.setLevel(LogUtils.LOG_LEVEL_V);
        }
        LogUtils.trace();

        mAppInst = this;
        mAppVersion = SysUtils.getAppVersion(this);

        DataLogic.init();
        WSListenService.startService(MyApp.getAppContext());
        WSNotifier.onCreate(this);
        //###@: acquireWakeLock();
    }

    public static Context getAppContext() {
        return mAppInst;
    }

    public static void exitApp() {
        DataLogic.release();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Wake Lock

    private PowerManager.WakeLock mWakeLock = null;
    private static final String mWakeLockName = ComDef.APP_NAME + ":wakelocktag";

    /**
     * acquire wakelock to keep running after screen off
     */
    private synchronized void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (pm == null) {
                LogUtils.te2("get power service failed");
            }
            else {
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, mWakeLockName);
                if (null != mWakeLock) {
                    LogUtils.trace();
                    mWakeLock.acquire();
                }
            }
        }
    }

    private synchronized void releaseWakeLock() {
        if (null != mWakeLock) {
            LogUtils.trace();
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    // Wake Lock
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
