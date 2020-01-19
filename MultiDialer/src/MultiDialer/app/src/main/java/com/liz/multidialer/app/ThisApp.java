package com.liz.multidialer.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.liz.androidutils.LogUtils;
import com.liz.multidialer.logic.ComDef;
import com.liz.multidialer.logic.DataLogic;

import java.util.ArrayList;

/**
 * ThisApp.java
 * Created by liz on 18-1-8.
 */

@SuppressWarnings("unused")
public class ThisApp extends Application {
    private static ThisApp mAppInst;
    public static String mAppVersion = "";
    public static String mAppVersionShow = "";  //text show in log

    private ArrayList<Activity> mActivityList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInst = this;
        mAppVersion = "";
        mAppVersionShow = "";

        LogUtils.setTag(ComDef.APP_NAME);
        LogUtils.setLogDir(ComDef.DIALER_LOG_DIR);
        LogUtils.setSaveToFile(true);
        LogUtils.d("ThisApp: onCreate, pid = " + android.os.Process.myPid());

        DataLogic.init();
        Thread.setDefaultUncaughtExceptionHandler(new UnCeHandler(this));
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

    /**
     * Activity关闭时，删除Activity列表中的Activity对象
     **/
    public void removeActivity(Activity a){
        mActivityList.remove(a);
    }

    /**
     * 向Activity列表中添加Activity对象
     **/
    public void addActivity(Activity a){
        mActivityList.add(a);
    }

    /**
     * 关闭Activity列表中的所有Activity
     **/
    public void finishActivity(){
        for (Activity activity : mActivityList) {
            if (null != activity) {
                activity.finish();
            }
        }
    }
}
