package com.liz.multidialer.logic;

import com.liz.multidialer.net.SFTPManager;

public class MultiDialClient {

    private static String mDeviceId = "";
    private static String mServerAddress = "";
    private static int mServerPort = 0;
    private static String mUserName = "";
    private static String mPassword = "";
    private static String mNetworkType = ComDef.DEFAULT_NETWORK_TYPE;

    protected static String mTelListFile;

    protected static void loadSettings() {
        mDeviceId = Settings.readDeviceId();
        mServerAddress = Settings.readServerAddress();
        mServerPort = Settings.readServerPort();
        mUserName = Settings.readUserName();
        mPassword = Settings.readPassword();
        mNetworkType = Settings.readNetworkType();
        mTelListFile = Settings.readFileListFile();
    }

    protected static void fetchTelListFile() {
        //fetch tellist file from server
        new Thread() {
            @Override
            public void run() {
                SFTPManager sftp = new SFTPManager(getServerAddress(), getServerPort(), getUserName(),getPassword());
                sftp.connect();
                //####@:
            }
        }.start();
    }

    public static String getTelListFile() { return mTelListFile; }
    public static void setTelListFile(String value) { mTelListFile = value; Settings.saveFileListFile(value); }

    public static String getDeviceId() { return mDeviceId;  }
    public static void setDeviceId(String value) { mDeviceId = value; Settings.saveDeviceId(value); }

    public static String getServerAddress() { return mServerAddress; }
    public static void setServerAddress(String value) { mServerAddress = value; Settings.saveServerAddress(value); }

    public static int getServerPort() { return mServerPort; }
    public static void setServerPort(int value) { mServerPort = value; Settings.saveServerPort(value); }

    public static String getUserName() { return mUserName; }
    public static void setUserName(String value) { mUserName = value; Settings.saveUserName(value); }

    public static String getPassword() { return mPassword; }
    public static void setPassword(String value) { mPassword = value; Settings.savePassword(value); }

    public static String getNetworkType() { return mNetworkType; }
    public static void setNetworkType(String value) { mNetworkType = value; Settings.saveNetworkType(value); }
}
