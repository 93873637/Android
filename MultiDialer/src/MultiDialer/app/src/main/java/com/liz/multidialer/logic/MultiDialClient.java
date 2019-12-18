package com.liz.multidialer.logic;

public class MultiDialClient {

    private static String mDeviceId = "";
    private static String mServerAddress = "";
    private static String mUserName = "";
    private static String mPassword = "";
    private static String mNetworkType = ComDef.DEFAULT_NETWORK_TYPE;

    public static void loadSettings() {
        mDeviceId = Settings.readDeviceId();
        mServerAddress = Settings.readServerAddress();
        mUserName = Settings.readUserName();
        mPassword = Settings.readPassword();
        mNetworkType = Settings.readNetworkType();
    }

    public static String getDeviceId() { return mDeviceId;  }
    public static void setDeviceId(String value) { mDeviceId = value; Settings.saveDeviceId(value); }

    public static String getServerAddress() { return mServerAddress; }
    public static void setServerAddress(String value) { mServerAddress = value; Settings.saveServerAddress(value); }

    public static String getUserName() { return mUserName; }
    public static void setUserName(String value) { mUserName = value; Settings.saveUserName(value); }

    public static String getPassword() { return mPassword; }
    public static void setPassword(String value) { mPassword = value; Settings.savePassword(value); }

    public static String getNetworkType() { return mNetworkType; }
    public static void setNetworkType(String value) { mNetworkType = value; Settings.saveNetworkType(value); }
}
