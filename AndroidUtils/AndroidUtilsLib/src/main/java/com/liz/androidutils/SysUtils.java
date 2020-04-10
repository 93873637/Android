package com.liz.androidutils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

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

    @TargetApi(23)
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

    /**
     *  Checks if external storage is available for read and write
     * */
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     *  Checks if external storage is available to at least read
     * */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static String getAppMemInfo() {
        final int M = 1024*1024;
        Runtime r = Runtime.getRuntime();
        DecimalFormat df = new DecimalFormat("#.0");
        String maxMemory = df.format((double) r.maxMemory() / M);
        String totalMemory = df.format((double) r.totalMemory() / M);
        String freeMemory = df.format((double) r.freeMemory() / M);
        String usedMemory = df.format((double) (r.totalMemory() - r.freeMemory()) / M);
        return maxMemory + " / " + totalMemory + " / " + freeMemory + " / " + usedMemory;
    }

//    ////////////////////////////////////////////////////////////////////////////////////////////////
//    // Wake Lock
//
//    private PowerManager.WakeLock mWakeLock = null;
//    private static final String mWakeLockName = ComDef.APP_NAME + ":wakelocktag";
//
//    /**
//     * acquire wakelock to keep running after screen off
//     */
//    private synchronized void acquireWakeLock() {
//        if (mWakeLock == null) {
//            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//            if (pm == null) {
//                LogUtils.te2("get power service failed");
//            }
//            else {
//                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, mWakeLockName);
//                if (null != mWakeLock) {
//                    LogUtils.trace();
//                    mWakeLock.acquire();
//                }
//            }
//        }
//    }
//
//    private synchronized void releaseWakeLock() {
//        if (null != mWakeLock) {
//            LogUtils.trace();
//            mWakeLock.release();
//            mWakeLock = null;
//        }
//    }
//
//    // Wake Lock
//    ////////////////////////////////////////////////////////////////////////////////////////////////
}
