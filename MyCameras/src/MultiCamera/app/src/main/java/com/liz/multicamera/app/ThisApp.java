package com.liz.multicamera.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.liz.androidutils.LogUtils;
import com.liz.multicamera.logic.ComDef;
import com.liz.multicamera.logic.DataLogic;

/**
 * ThisApp.java
 * Created by liz on 18-1-8.
 */

@SuppressWarnings("unused")
public class ThisApp extends Application {
    ////////////////////////////////////////////////////////////////////////////////////////
    //Interfaces

    public static String mAppVersion = "";
    public static String mAppVersionShow = "";  //text show in log

    public static Context getAppContext() {
        return mAppInst;
    }
    public static Handler getMainHandler() {
        return mAppInst.mMainHandler;
    }

    public static void exit() {
        mAppInst.mMainHandler.removeCallbacksAndMessages(null);
        mAppInst.onTerminate();
    }

    //Interfaces
    ////////////////////////////////////////////////////////////////////////////////////////

    private static ThisApp mAppInst;
    private final Handler mMainHandler = new MainHandler();

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInst = this;
        LogUtils.setTag(ComDef.APP_NAME);
        DataLogic.init();
    }

    @Override
    public void onTerminate() {
        LogUtils.d("onTerminate");
        super.onTerminate();
        System.exit(0);
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
     * This Handler is used to post message back onto the main thread of the
     * application
     */
    class MainHandler extends Handler {
        public MainHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            LogUtils.d("MainHandler.handleMessage: msg=" + msg.toString());
            switch (msg.what) {

            }
        }
    }
}
