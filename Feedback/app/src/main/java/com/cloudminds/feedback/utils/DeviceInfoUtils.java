package com.cloudminds.feedback.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeviceInfoUtils {

    private Context mContext;
    private ActivityManager mActivityManager;

    private static final String PATH_CAPACITY_PERCENT = "/sys/class/power_supply/battery/capacity";
    private static final String PROP_HARDWARE_VERSION = "sys.hw.ver";
    private static final String PROP_IMEI1 = "gsm.imei.sub0";

    private static final String NETWORK_TYPE_UNKNOWN = "unknown";
    private static final String NETWORK_TYPE_WIFI = "WIFI";
    private static final String NETWORK_TYPE_2G = "2G";
    private static final String NETWORK_TYPE_3G = "3G";
    private static final String NETWORK_TYPE_4G = "4G";

    public DeviceInfoUtils(Context context) {
        mContext = context;
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    public String getDeviceModel(){
        return Build.MODEL;
    }

    public String getAndroidVersion(){
        return Build.VERSION.RELEASE;
    }

    public String getBuildVersion() {
        return Build.DISPLAY;
    }

    public String getHardwareVersion() {
        return SysUtils.getSystemProperty(mContext, PROP_HARDWARE_VERSION, "");
    }

    public String getIMEI1() {
        return SysUtils.getSystemProperty(mContext, PROP_IMEI1, "");
    }

    public String getAppVersion(String packageName) {
        String appVersionName = "";
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(packageName, 0);
            appVersionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersionName;
    }

    public int isUserAMonkey() {
        return mActivityManager.isUserAMonkey() ? 1 : 0;
    }

    public String getBatteryLevel() {
        return readNodeInfo(PATH_CAPACITY_PERCENT);
    }

    private String readNodeInfo(String path) {
        BufferedReader br = null;
        String info = "-1";
        try {
            br = new BufferedReader(new FileReader(path));
            info = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return info + "%";
    }

    public String getCpuUsage() {
        Process process = null;
        BufferedReader br = null;
        String info = null;
        try {
            process = Runtime.getRuntime().exec("dumpsys cpuinfo");
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                info = line; //get the last line
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (process != null) {
                    process.destroy();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return info;
    }

    public String getMemoryUsage() {
        MemoryInfo memoryInfo = new MemoryInfo();
        mActivityManager.getMemoryInfo(memoryInfo);
        String availableMemory = Formatter.formatFileSize(mContext, memoryInfo.availMem);
        String totalMemory = Formatter.formatFileSize(mContext, memoryInfo.totalMem);
        return availableMemory + " / " + totalMemory;
    }

    public String getStorageUsage() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long availableBlocks = statFs.getAvailableBlocksLong();
        long totalBlocks = statFs.getBlockCountLong();
        long blockSize = statFs.getBlockSizeLong();
        String availableStorage = Formatter.formatFileSize(mContext, availableBlocks * blockSize);
        String totalStorage = Formatter.formatFileSize(mContext, totalBlocks * blockSize);
        return availableStorage + " / " + totalStorage;
    }

    public String getNetworkType() {
        String type = NETWORK_TYPE_UNKNOWN;
        try {
            ConnectivityManager connectManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectManager != null) {
                NetworkInfo info = connectManager.getActiveNetworkInfo();
                if (info != null) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        type = NETWORK_TYPE_WIFI;
                    } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        int subType = info.getSubtype();
                        switch (subType) {
                            case TelephonyManager.NETWORK_TYPE_IDEN:
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                            case TelephonyManager.NETWORK_TYPE_CDMA:
                            case TelephonyManager.NETWORK_TYPE_EDGE:
                            case TelephonyManager.NETWORK_TYPE_GPRS:
                                type = NETWORK_TYPE_2G;
                                break;
                            case TelephonyManager.NETWORK_TYPE_EHRPD:
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            case TelephonyManager.NETWORK_TYPE_HSDPA:
                            case TelephonyManager.NETWORK_TYPE_HSPAP:
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                                type = NETWORK_TYPE_3G;
                                break;
                            case TelephonyManager.NETWORK_TYPE_LTE:
                                type = NETWORK_TYPE_4G;
                                break;
                            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                                type = NETWORK_TYPE_UNKNOWN;
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }
}
