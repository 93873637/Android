package com.liz.multidialer.logic;

import com.jcraft.jsch.ChannelSftp;
import com.liz.multidialer.net.SFTPManager;

import java.util.Vector;

public class MultiDialClient {

    private static String mDeviceId = "";
    private static String mServerAddress = "";
    private static int mServerPort = 0;
    private static String mUserName = "";
    private static String mPassword = "";
    private static String mNetworkType = ComDef.DEFAULT_NETWORK_TYPE;

    protected static void loadSettings() {
        mDeviceId = Settings.readDeviceId();
        mServerAddress = Settings.readServerAddress();
        mServerPort = Settings.readServerPort();
        mUserName = Settings.readUserName();
        mPassword = Settings.readPassword();
        mNetworkType = Settings.readNetworkType();
    }

    protected static void fetchTelListFile() {
        // NOTE: network operation can't run on main thread
        new Thread() {
            @Override
            public void run() {
                _fetchTelListFile();
            }
        }.start();
    }

    private static void _fetchTelListFile() {
        //fetch tellist file from server
        SFTPManager sftp = new SFTPManager(getServerAddress(), getServerPort(), getUserName(), getPassword());
        DataLogic.showProgress("sftp connect " + getServerAddress() + ":" + getServerPort() + "...");
        if (!sftp.connect()) {
            DataLogic.showProgress("sftp connect failed.");
        } else {
            DataLogic.showProgress("sftp connect ok");
            Vector vf = sftp.listFiles("/home/shandong1/PUB_SPACE/NUM_DATA/WAIT_DATA/M01*.txt");
            if (vf == null) {
                DataLogic.showProgress("sftp list files failed.");
                return;
            }
            DataLogic.showProgress("sftp list files success, size = " + vf.size());
            if (vf.size() < 1) {
                DataLogic.showProgress("sftp list files empty.");
                return;
            }
            String fileName = ((ChannelSftp.LsEntry)vf.get(0)).getFilename();
            DataLogic.showProgress("sftp get tel list file, name = " + fileName + ", download...");
            if (!sftp.downloadFile("/home/shandong1/PUB_SPACE/NUM_DATA/WAIT_DATA/", fileName,
                    ComDef.DIALER_DIR + "/", fileName)) {
                DataLogic.showProgress("sftp download file failed.");
                return;
            }
            DataLogic.showProgress("sftp download file success.");
            DataLogic.setTelListFileName(fileName);
            DataLogic.loadTelList();
        }
    }

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
