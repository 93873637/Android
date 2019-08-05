package com.liz.androidutils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

@SuppressWarnings("unused")
public class NetUtils {
    public static final String INVALID_ADDR = "";

    public static String getLocalIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager == null) {
            LogUtils.e("ERROR: getLocalIpAddress: get WIFI_SERVICE failed.");
            return INVALID_ADDR;
        }
        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return String.format(Locale.CHINA, "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        }
        catch (Exception e) {
            LogUtils.e("ERROR: get local ip address failed, ex=" + e.toString());
            return INVALID_ADDR;
        }
    }

    public static boolean isWifiConnected(Context context) {
//        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connManager == null) {
//            LogUtils.e("ERROR: isWifiConnected: get WIFI_SERVICE failed.");
//            return false;
//        }
//        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        if (wifiInfo == null) {
//            LogUtils.e("ERROR: isWifiConnected: get wifi info failed.");
//            return false;
//        }
//        return wifiInfo.isConnected();
        return false;
    }
}
