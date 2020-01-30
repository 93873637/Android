package com.liz.multidialer.logic;

import android.text.TextUtils;

import com.jcraft.jsch.ChannelSftp;
import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.multidialer.net.SFTPManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MultiDialClient {

    private static String mDeviceId = "";
    private static String mServerAddress = "";
    private static int mServerPort = 0;
    private static String mUserName = "";
    private static String mPassword = "";
    private static String mNetworkType = ComDef.DEFAULT_NETWORK_TYPE;
    private static String mServerHome;
    private static int mJpegQuality;
    private static long mHeartbeatTimer;

    protected static void loadSettings() {
        mDeviceId = Settings.readDeviceId();
        mServerAddress = Settings.readServerAddress();
        mServerPort = Settings.readServerPort();
        mUserName = Settings.readUserName();
        mPassword = Settings.readPassword();
        mNetworkType = Settings.readNetworkType();
        mServerHome = Settings.readServerHome();
        mJpegQuality = Settings.readJpegQuality();
        mHeartbeatTimer = Settings.readHeartbeatTimer();
    }

    private static String getListFileString() {
        return ComDef.SFTP_PATH_NUM_WAIT_DATA + "/" + DataLogic.getDeviceId() + "*.txt";
    }

    // NOTE: network operation can't run on main thread
    protected static void fetchTelListFile() {
        new Thread() {
            @Override
            public void run() {
                _fetchTelListFile();
            }
        }.start();
    }

    private static void logVector(Vector<ChannelSftp.LsEntry> vf, String tag) {
        LogUtils.d("------------------------------------------");
        LogUtils.d("Vector(" + tag + "): size = " + vf.size());
        for (int i=0; i<vf.size(); i++) {
            LogUtils.d("#" + i + ": " + vf.get(i).getFilename());
        }
        LogUtils.d("------------------------------------------");
    }

    //
    //do fetch tellist file from server
    //
    private static void _fetchTelListFile() {
        SFTPManager sftpMgr = new SFTPManager(getServerAddress(), getServerPort(), getUserName(), getPassword());
        LogUtils.d("SFTP: connect " + getServerAddress() + ":" + getServerPort() + "...");
        if (!sftpMgr.connect()) {
            LogUtils.d("SFTP: connect failed.");
        } else {
            LogUtils.d("SFTP: connect ok, list file of " + getListFileString() + "...");
            Vector vf = sftpMgr.listFiles(getListFileString());
            if (vf == null) {
                LogUtils.d("SFTP: list files failed.");
                return;
            }
            LogUtils.d("SFTP: list files success, size = " + vf.size());
            if (vf.size() < 1) {
                LogUtils.d("SFTP: list files empty.");
                return;
            }

            //##@: logVector(vf, "Before Sort");
            Collections.sort(vf, new Comparator() {
                public int compare(Object obj1, Object obj2) {
                    return ((ChannelSftp.LsEntry)obj1).getFilename().compareTo(((ChannelSftp.LsEntry)obj2).getFilename());
                }
            });
            //##@: logVector(vf, "After Sort");

            String fileName = ((ChannelSftp.LsEntry)vf.get(0)).getFilename();
            LogUtils.d("SFTP: get tel list file, name = " + fileName + ", download...");
            if (!sftpMgr.downloadFile(FileUtils.formatDirSeparator(ComDef.SFTP_PATH_NUM_WAIT_DATA), fileName,
                    FileUtils.formatDirSeparator(ComDef.DIALER_NUM_DIR), fileName)) {
                LogUtils.d("SFTP: download file failed.");
                return;
            }
            LogUtils.d("SFTP: download file success.");

            String srcFilePath = FileUtils.formatDirSeparator(ComDef.SFTP_PATH_NUM_WAIT_DATA) + fileName;
            String tarFilePath = FileUtils.formatDirSeparator(ComDef.SFTP_PATH_NUM_RUN_DATA) + fileName;
            LogUtils.d("SFTP: mv file " + srcFilePath + " to " + tarFilePath + "...");
            sftpMgr.mv(srcFilePath, tarFilePath);

            DataLogic.onTelListFileUpdate(fileName);
            sftpMgr.disconnect();
        }
    }

    public static void uploadHeartBeatFile(final String fileName) {
        new Thread() {
            @Override
            public void run() {
                _uploadHeartBeatFile(fileName);
            }
        }.start();
    }

    private static void _uploadHeartBeatFile(String fileName) {
        LogUtils.d("_uploadHeartBeatFile: fileName = " + fileName);
        if (TextUtils.isEmpty(fileName)) {
            LogUtils.e("ERROR: _uploadHeartBeatFile: no file name to upload");
            return;
        }

        SFTPManager sftpMgr = new SFTPManager(getServerAddress(), getServerPort(), getUserName(), getPassword());
        DataLogic.showProgress("_uploadHeartBeatFile: SFTP: connect " + getServerAddress() + ":" + getServerPort() + "...");
        if (!sftpMgr.connect()) {
            DataLogic.showProgress("_uploadHeartBeatFile: SFTP: connect failed.");
        } else {
            DataLogic.showProgress("_uploadHeartBeatFile: SFTP: connect ok");

            String remotePath = FileUtils.formatDirSeparator(ComDef.SFTP_PATH_HEARTBEAT);
            String remoteFileName = fileName;
            String localPath = FileUtils.formatDirSeparator(ComDef.DIALER_DIR);
            String localFileName = fileName;

            DataLogic.showProgress("_uploadHeartBeatFile: SFTP: upload file " + fileName + " to " + remotePath + "...");
            if (!sftpMgr.uploadFile(remotePath, remoteFileName, localPath, localFileName)) {
                LogUtils.e("ERROR: _uploadHeartBeatFile: upload failed.");
                DataLogic.setServerConnect(ComDef.SERVER_CONNECT_FAILED);
            }
            else {
                LogUtils.d("_uploadHeartBeatFile: upload success");
                DataLogic.setServerConnect(ComDef.SERVER_CONNECT_OK);
            }

            sftpMgr.disconnect();
        }
    }

    // NOTE: network operation can't run on main thread
    public static void uploadPicData(final String fileName, final String fileNameDone) {
        new Thread() {
            @Override
            public void run() {
                _uploadPicData(fileName, fileNameDone);
                DataLogic.onUploadFinished();
            }
        }.start();
    }

    private static void _uploadPicData(String fileName, String fileNameDone) {
        LogUtils.d("_uploadPicData: fileName = " + fileName);
        if (TextUtils.isEmpty(fileName)) {
            LogUtils.e("ERROR: _uploadPicData: no file name to upload");
            return;
        }
        SFTPManager sftpMgr = new SFTPManager(getServerAddress(), getServerPort(), getUserName(), getPassword());
        DataLogic.showProgress("_uploadPicData: SFTP: connect " + getServerAddress() + ":" + getServerPort() + "...");
        if (!sftpMgr.connect()) {
            DataLogic.showProgress("_uploadPicData: SFTP: connect failed.");
        } else {
            DataLogic.showProgress("_uploadPicData: SFTP: connect ok");

            String remotePath = FileUtils.formatDirSeparator(ComDef.SFTP_PATH_PIC_WAIT_DATA);
            String remoteFileName = fileName;
            String localPath = FileUtils.formatDirSeparator(ComDef.DIALER_PIC_DIR);
            String localFileName = fileName;

            DataLogic.showProgress("_uploadPicData: SFTP: upload file " + fileName + " to " + remotePath + "...");
            if (!sftpMgr.uploadFile(remotePath, remoteFileName, localPath, localFileName)) {
                LogUtils.e("ERROR: _uploadPicData: upload failed.");
                DataLogic.addDaemonTask(fileName, fileNameDone);
            }
            else {
                LogUtils.d("_uploadPicData: upload success");

                //rename remote file name
                String srcFilePath = remotePath + fileName;
                String tarFilePath = remotePath + fileNameDone;
                DataLogic.showProgress("SFTP: rename file " + srcFilePath + " to " + tarFilePath + "...");
                sftpMgr.mv(srcFilePath, tarFilePath);

                //delete local file
                String localFilePath = localPath + localFileName;
                DataLogic.showProgress("MultiDialClient: delete local picture file " + localFilePath);
                FileUtils.removeFile(localFilePath);
            }

            sftpMgr.disconnect();
        }
    }

    // NOTE: network operation can't run on main thread
    protected static void moveRunDataToEnd(String fileName) {
        final String _fileName = fileName;
        new Thread() {
            @Override
            public void run() {
                _moveRunDataToEnd(_fileName);
            }
        }.start();
    }

    public static void _moveRunDataToEnd(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            LogUtils.e("ERROR: _moveRunDataToEnd: no file name to mv");
            return;
        }
        SFTPManager sftpMgr = new SFTPManager(getServerAddress(), getServerPort(), getUserName(), getPassword());
        DataLogic.showProgress("_moveRunDataToEnd: SFTP: connect " + getServerAddress() + ":" + getServerPort() + "...");
        if (!sftpMgr.connect()) {
            DataLogic.showProgress("_moveRunDataToEnd: SFTP: connect failed.");
        } else {
            DataLogic.showProgress("_moveRunDataToEnd: SFTP: connect ok");
            String srcFilePath = FileUtils.formatDirSeparator(ComDef.SFTP_PATH_NUM_RUN_DATA) + fileName;
            String tarFilePath = FileUtils.formatDirSeparator(ComDef.SFTP_PATH_NUM_END_DATA) + fileName;
            DataLogic.showProgress("_moveRunDataToEnd: SFTP: mv file " + srcFilePath + " to " + tarFilePath + "...");
            sftpMgr.mv(srcFilePath, tarFilePath);
            sftpMgr.disconnect();
        }
    }

    public static String getDeviceId() { return mDeviceId;  }
    public static void setDeviceId(String value) { mDeviceId = value; Settings.saveDeviceId(value); }

    public static String getServerAddress() { return mServerAddress; }
    public static void setServerAddress(String value) { mServerAddress = value; Settings.saveServerAddress(value); }

    public static int getServerPort() { return mServerPort; }
    public static String getServerPortInfo() { return mServerPort + ""; }
    public static void setServerPort(int value) { mServerPort = value; Settings.saveServerPort(value); }

    public static String getUserName() { return mUserName; }
    public static void setUserName(String value) { mUserName = value; Settings.saveUserName(value); }

    public static String getPassword() { return mPassword; }
    public static void setPassword(String value) { mPassword = value; Settings.savePassword(value); }

    public static String getNetworkType() { return mNetworkType; }
    public static void setNetworkType(String value) { mNetworkType = value; Settings.saveNetworkType(value); }

    public static String getServerHome() { return mServerHome; }
    public static void setServerHome(String value) { mServerHome = value; Settings.saveServerHome(value); }

    public static int getJpegQuality() { return mJpegQuality; }
    public static String getJpegQualityInfo() { return "" + mJpegQuality; }
    public static void setJpegQuality(int value) { mJpegQuality = value; Settings.saveJpegQuality(value); }

    public static long getHeartbeatTimer() { return mHeartbeatTimer; }
    public static String getHeartbeatTimerInfo() { return "" + mHeartbeatTimer/1000; }
    public static void setHeartbeatTimer(long value) {
        if (value >= ComDef.HEARTBEAT_TIMER_MIN) {
            if (value != mHeartbeatTimer) {
                mHeartbeatTimer = value;
                Settings.saveHeartbeatTimer(value);
                DataLogic.onHeartbeatTimerUpdated();
            }
        }
    }
}
