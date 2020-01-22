package com.liz.androidutils;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.lang.reflect.Method;

/**
 * Created by liz on 2018/3/2.
 */

@SuppressWarnings("unused")
public class SysUtils {

    public static String getAppVersion(Context context) {
        String ver = "";
        try {
            PackageManager manager = context.getPackageManager();
            String packageName = context.getPackageName();
            LogUtils.d("packageName=" + packageName);
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            ver = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e("getPackageInfo exception: " + e.toString());
            e.printStackTrace();
        }
        return ver;
    }

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

    public static Bitmap capture(Activity activity) {
        activity.getWindow().getDecorView().setDrawingCacheEnabled(true);
        return activity.getWindow().getDecorView().getDrawingCache();
    }

    public static final String ERR_MSG_OK = "OK";

    public static String supportFingerprint(Context context) {
        String errMsg = ERR_MSG_OK;

        /*
        //unnecessary for our project's minimum version is 23
        if (Build.VERSION.SDK_INT < 23) {
            Toast.makeText(context, "您的系统版本过低，不支持指纹功能", Toast.LENGTH_SHORT).show();
            return false;
        }
        */

        KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
        FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);

        if (!fingerprintManager.isHardwareDetected()) {
            errMsg = "No fingerprint hardware detected";
        } else if (!keyguardManager.isKeyguardSecure()) {
            errMsg = "Keyguard Secure not set, please add one fingerprint first";
        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
            errMsg = "No enrolled fingerprint, please add one at least";
        }

        return errMsg;
    }

    public static boolean checkRootExecutable() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("SysUtils", "checkRootExecutable exception: " + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable () {
        String state = Environment. getExternalStorageState ();
        if ( Environment . MEDIA_MOUNTED . equals ( state )) {
            return true ;
        }
        return false ;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable () {
        String state = Environment . getExternalStorageState ();
        if ( Environment . MEDIA_MOUNTED . equals ( state ) ||
                Environment . MEDIA_MOUNTED_READ_ONLY . equals ( state )) {
            return true ;
        }
        return false ;
    }
}
