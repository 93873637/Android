package com.liz.whatsai.app;

import android.app.Application;
import android.content.Context;

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
        super.onCreate();
        mAppInst = this;
        mAppVersion = SysUtils.getAppVersion(this);
        LogUtils.setTag(ComDef.APP_NAME);

        /* for test only
        //WhatsaiMail.sendMail("tom.li@cloudminds.com", "ddaasfess111", "fease111");
        //WhatsaiMail.sendMail("tom.li@cloudminds.com", "ddaasfess222", "fease222", "/sdcard/whatsai/whatsai.xml");
        WhatsaiMail.sendMail("93873637@qq.com", "ddaasfess111", "fease111", "/sdcard/whatsai/whatsai.xml");
        //*/
    }

    public static Context getAppContext() {
        return mAppInst;
    }

    public static void init() {
        DataLogic.init();
    }
}
