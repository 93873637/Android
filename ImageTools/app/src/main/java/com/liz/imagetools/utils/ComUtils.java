package com.liz.imagetools.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * Created by liz on 2018/1/29.
 */

public class ComUtils {

    public static String getAppVersion(Context context) {
        String ver = "";
        try {
            PackageManager manager = context.getPackageManager();
            String packageName = context.getPackageName();
            LogUtils.d("packageName=" + packageName);
            PackageInfo info = manager.getPackageInfo(packageName,0);
            ver = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e("getPackageInfo exception: " + e.toString());
            e.printStackTrace();
        }
        return ver;
    }
}
