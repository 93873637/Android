package com.liz.puretorch.app;

import android.app.Application;
import android.content.Context;

import com.liz.puretorch.logic.DataLogic;

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
        DataLogic.init();
    }

    public static Context getAppContext() {
        return mAppInst;
    }
}
