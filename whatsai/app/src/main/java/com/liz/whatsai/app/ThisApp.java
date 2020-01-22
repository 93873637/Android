package com.liz.whatsai.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.SysUtils;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;

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
        LogUtils.setTag(ComDef.APP_NAME);
        LogUtils.d("ThisApp:onCreate: pid = " + android.os.Process.myPid());

        super.onCreate();
        mAppInst = this;
        mAppVersion = SysUtils.getAppVersion(this);

        /* for test only
        //WhatsaiMail.sendMail("tom.li@cloudminds.com", "ddaasfess111", "fease111");
        //WhatsaiMail.sendMail("tom.li@cloudminds.com", "ddaasfess222", "fease222", "/sdcard/whatsai/whatsai.xml");
        WhatsaiMail.sendMail("93873637@qq.com", "ddaasfess111", "fease111", "/sdcard/whatsai/whatsai.xml");
        //*/

        //move to MainActivity
        //DataLogic.init();
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
}
