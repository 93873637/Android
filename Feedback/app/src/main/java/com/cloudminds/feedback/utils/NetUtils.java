package com.cloudminds.feedback.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * NetUtils.java: network interface utils
 * Created by cloud on 18-4-12.
 */

public class NetUtils {

    //for API larger than 23
    public static Boolean isConnectedOnWifi(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            LogUtils.e("get connectivity manager failed.");
            return false;
        }

        if (connMgr.getActiveNetworkInfo() == null) {
            LogUtils.e("get active network info failed.");
            return false;
        }

        return (connMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static Boolean isConnectedOnMobile(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            LogUtils.e("get connectivity manager failed.");
            return false;
        }

        if (connMgr.getActiveNetworkInfo() == null) {
            LogUtils.e("get active network info failed.");
            return false;
        }

        return (connMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static Boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            LogUtils.e("get connectivity manager failed.");
            return false;
        }

        if (connMgr.getActiveNetworkInfo() == null) {
            LogUtils.e("get active network info failed.");
            return false;
        }

        return connMgr.getActiveNetworkInfo().isAvailable();
    }
}