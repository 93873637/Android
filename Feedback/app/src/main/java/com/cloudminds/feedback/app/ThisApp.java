package com.cloudminds.feedback.app;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.cloudminds.feedback.logic.ComDef;
import com.cloudminds.feedback.logic.LogConfig;
import com.cloudminds.feedback.logic.Settings;
import com.cloudminds.feedback.utils.ComUtils;
import com.cloudminds.feedback.utils.LogUtils;

import net.gotev.uploadservice.UploadService;

/**
 * Created by liz on 18-1-8.
 */

public class ThisApp extends Application {
    private static ThisApp mAppInst;

    public static String mAppVersion = "";
    public static String mAppVersionShow = "";  //text show in log

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInst = this;

        UploadService.NAMESPACE = ComDef.APPLICATION_ID;
        mAppVersion = ComUtils.getAppVersion(this);
        mAppVersionShow = "***Feedback " + mAppVersion;
        LogUtils.i("ThisApp.onCreate: " + mAppVersionShow);

        LogConfig.init();
        String user = Settings.getUserType(getApplicationContext());
        if(TextUtils.isEmpty(user)) {
            Settings.setUserType(getApplicationContext(), Settings.NORMAL_USER);
        }
        /*
        //check build type for init, and do something for user version
        //because current user version not integrated into system, if current build type is user, the app must installed by user self manually.
        String buildType = SysUtils.getSystemProperty(this, ComDef.PROP_BUILD_TYPE, ComDef.PROP_BUILD_TYPE_DEFAULT);
        if (buildType == ComDef.PROP_BUILD_TYPE_USER) {
            //since user self installed, we start open user experience and start logging
            SysUtils.getSystemProperty
        }
        //*/
    }

    public static Context getAppContext() {
        return mAppInst;
    }
}
