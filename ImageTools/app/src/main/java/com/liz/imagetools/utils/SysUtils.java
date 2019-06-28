package com.liz.imagetools.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

public class SysUtils {

    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e("ERROR: get version failed with ex=" + e.toString());
            e.printStackTrace();
            return "";
        }
    }

    public static String getIMEI(Context context) {
        return SysUtils.getSystemProperty(context, "gsm.imei.sub0", "000000000000000");
    }

    public static int getSystemPropertyInt(Context context, String propName, int defaultValue) {
        String propValue = getSystemProperty(context, propName, "");
        if (TextUtils.isEmpty(propValue)) {
            return  defaultValue;
        }
        else {
            return Integer.parseInt(propValue);
        }
    }

    public static String getSystemProperty(Context context, String propName, String defaultValue) {
        final String PROP_NAME_SYSTEM = "android.os.SystemProperties";
        final String METHOD_GET = "get";

        String propValue = defaultValue;

        try {
            Class<?> SystemProperties = context.getClassLoader().loadClass(PROP_NAME_SYSTEM);
            Method methodGet = SystemProperties.getMethod(METHOD_GET, new Class[]{
                    String.class, String.class
            });
            propValue = (String) methodGet.invoke(null, new Object[]{
                    propName, defaultValue
            });
        } catch (Exception ex) {
            LogUtils.e("SysUtils:getSystemProperty:Exception: " + ex.toString());
        }

        if (propValue != null) {
            return propValue;
        } else {
            return defaultValue;
        }
    }

    public static boolean setSystemProperty(Context context, String propName, String propValue) {
        final String PROP_NAME_SYSTEM = "android.os.SystemProperties";
        final String METHOD_SET = "set";

        try {
            Class<?> SystemProperties = context.getClassLoader().loadClass(PROP_NAME_SYSTEM);
            Method methodSet = SystemProperties.getMethod(METHOD_SET, new Class[]{
                    String.class, String.class
            });
            methodSet.invoke(null, new Object[]{
                    propName, propValue
            });
        } catch (Exception ex) {
            LogUtils.e("SysUtils:setSystemProperty:Exception: " + ex.toString());
            return false;
        }

        return true;
    }
}
