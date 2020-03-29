package com.liz.testcamera.utils;

import android.content.Context;

import java.lang.reflect.Method;

/**
 * SysUtils.java
 * Created by liz on 2018/3/2.
 */

public class SysUtils {

    public static String getIMEI(Context context) {
        return SysUtils.getSystemProperty(context, "gsm.imei.sub0", "000000000000000");
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

    //##@:TODO: add common funtions later
//
//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//
//    // Storage Permissions
//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
//
//    public static void checkAndRequestPermissions(Activity activity, final @NonNull String[] permissions) {
//
//        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    activity,
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//            );
//        }
//    }
}
