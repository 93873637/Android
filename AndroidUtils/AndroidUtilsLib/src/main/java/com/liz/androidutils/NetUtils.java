package com.liz.androidutils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager == null) {
            LogUtils.e("ERROR: isWifiConnected: get WIFI_SERVICE failed.");
            return false;
        }
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo == null) {
            LogUtils.e("ERROR: isWifiConnected: get wifi info failed.");
            return false;
        }
        return wifiInfo.isConnected();
    }

    public static String getEthernetIP() {
        return getClientIP("eth0");
    }

    public static String getWifiIP() {
        return getClientIP("wlan0");
    }

    public static String getClientIP(String interfaceName) {
        List<String> ipList = getIPList(interfaceName);
        if (ipList != null && ipList.size() > 0) {
            return ipList.get(0);
        }
        else {
            return "";
        }
    }

    public static List<String> getIPList(String interfaceName) {
        try {
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface.getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                if (networkInterface != null
                        && TextUtils.equals(networkInterface.getDisplayName(), interfaceName)) {
                    return getIPList(networkInterface);
                }
            }
        } catch (Exception e) {
            LogUtils.te2("getIPList exception, e = " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getIPList(NetworkInterface networkInterface) {
        ArrayList<String> ipList = new ArrayList<>();
        Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
        while (enumIpAddr.hasMoreElements()) {
            InetAddress inetAddress = enumIpAddr.nextElement();
            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                ipList.add(inetAddress.getHostAddress());
            }
        }
        return ipList;
    }
}
