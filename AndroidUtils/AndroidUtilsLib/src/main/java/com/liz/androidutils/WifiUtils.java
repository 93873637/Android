package com.liz.androidutils;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

public class WifiUtils {
    // 使用 WifiConfiguration 连接.
    public static void connectByConfig(WifiManager manager, WifiConfiguration config) {
        if (manager == null) {
            return;
        }
        try {
            Method connect = manager.getClass().getDeclaredMethod("connect", WifiConfiguration.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (connect != null) {
                connect.setAccessible(true);
                connect.invoke(manager, config, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 使用 networkId 连接.
    public static void connectByNetworkId(WifiManager manager, int networkId) {
        if (manager == null) {
            return;
        }
        try {
            Method connect = manager.getClass().getDeclaredMethod("connect", int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (connect != null) {
                connect.setAccessible(true);
                connect.invoke(manager, networkId, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 保存网络.
    public static void saveNetworkByConfig(WifiManager manager, WifiConfiguration config) {
        if (manager == null) {
            return;
        }
        try {
            Method save = manager.getClass().getDeclaredMethod("save", WifiConfiguration.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (save != null) {
                save.setAccessible(true);
                save.invoke(manager, config, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 添加网络.
//    public static int addNetwork(WifiManager manager, WifiConfiguration config) {
//        if (manager != null) {
//            manager.addNetwork(config);
//        }
//    }

    // 忘记网络.
    public static void forgetNetwork(WifiManager manager, int networkId) {
        if (manager == null) {
            return;
        }
        try {
            Method forget = manager.getClass().getDeclaredMethod("forget", int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (forget != null) {
                forget.setAccessible(true);
                forget.invoke(manager, networkId, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 禁用网络.
    public static void disableNetwork(WifiManager manager, int netId) {
        if (manager == null) {
            return;
        }
        try {
            Method disable = manager.getClass().getDeclaredMethod("disable", int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (disable != null) {
                disable.setAccessible(true);
                disable.invoke(manager, netId, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//    // 断开连接.
//    public static boolean disconnectNetwork(WifiManager manager) {
//        return manager != null && manager.disconnect();
//    }

    // 禁用短暂网络.
    public static void disableEphemeralNetwork(WifiManager manager, String SSID) {
        if (manager == null || TextUtils.isEmpty(SSID))
            return;
        try {
            Method disableEphemeralNetwork = manager.getClass().getDeclaredMethod("disableEphemeralNetwork", String.class);
            if (disableEphemeralNetwork != null) {
                disableEphemeralNetwork.setAccessible(true);
                disableEphemeralNetwork.invoke(manager, SSID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
