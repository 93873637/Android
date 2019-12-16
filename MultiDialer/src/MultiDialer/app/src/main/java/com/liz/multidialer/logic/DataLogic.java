package com.liz.multidialer.logic;

import android.media.Image;
import android.text.TextUtils;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.multidialer.app.ThisApp;
import com.liz.multidialer.net.SFTPUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * DataLogic:
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class DataLogic {

    private static String mTelListFile = "";

    private static ArrayList<String> mTelList;
    private static int mCurrentCallIndex = 0;
    private static int mMaxCallNum = ComDef.MAX_CALL_NUM;
    private static long mEndCallDelay = ComDef.DEFAULT_END_CALL_DELAY;
    private static int mCalledNum = 0;
    private static String mPictureDir = null;
    private static boolean mCallRunning = false;

    private static String mDeviceId = "";
    private static String mServerAddress = "";
    private static String mUserName = "";
    private static String mPassword = "";
    private static String mNetworkType = ComDef.DEFAULT_NETWORK_TYPE;

    public static void init() {
        LogUtils.d("DataLogic: init");

        loadSettings();
        // loadTelList();
        //LogUtils.d("DataLogic: DIALER_DIR = " + ComDef.DIALER_DIR);

        genPictureDir();
        mCurrentCallIndex = Settings.readCurrentCallIndex();

        LogUtils.d("DataLogic: TEL_LIST_FILE_PATH = " + ComDef.TEL_LIST_FILE_PATH);
        LogUtils.d("DataLogic: mCurrentCallIndex = " + mCurrentCallIndex);


        new Thread() {
            @Override
            public void run() {

                SFTPUtils sftp = new SFTPUtils("192.168.1.4", "liz","jujube***");
                sftp.connect();
            }
        }.start();

    }

    public static void loadTelList() {
        mTelList = FileUtils.readTxtFileLines(ComDef.TEL_LIST_FILE_PATH);
    }

    public static void loadSettings() {
        mDeviceId = Settings.readDeviceId();
        mServerAddress = Settings.readServerAddress();
        mUserName = Settings.readUserName();
        mPassword = Settings.readPassword();
        mNetworkType = Settings.readNetworkType();
    }

    public static String getDeviceId() { return mDeviceId;  }
    public static void setDeviceId(String value) { mDeviceId = value; Settings.saveDeviceId(value); }

    public static String getServerAddress() {
        return mServerAddress;
    }
    public static void setServerAddress(String value) {
        mServerAddress = value;
        Settings.saveServerAddress(value);
    }

    public static String getUserName() {
        return mUserName;
    }
    public static void setUserName(String value) {
        mUserName = value;
        Settings.saveUserName(value);
    }

    public static String getPassword() {
        return mPassword;
    }
    public static void setPassword(String value) {
        mPassword = value;
        Settings.savePassword(value);
    }

    public static String getNetworkType() {
        return mNetworkType;
    }
    public static void setNetworkType(String value) {
        mNetworkType = value;
        Settings.saveNetworkType(value);
    }

    public static String getTelListFile() { return mTelListFile; }
    public static void setTelListFile(String value) { mTelListFile = value; Settings.saveFileListFile(value); }

    public static int getMaxCallNum() {
        return mMaxCallNum;
    }
    public static void setMaxCallNum(int maxCallNum) {
        mMaxCallNum = maxCallNum;
    }

    public static long getEndCallDelay() {
        return mEndCallDelay;
    }
    public static void setEndCallDelay(long endCallDelay) {
        mEndCallDelay = endCallDelay;
    }

    public static String getTelListFileInfo() {
        if (TextUtils.isEmpty(mTelListFile)) {
            return "未知";
        }
        else {
            return mTelListFile;
        }
    }

    public static String getTelListInfo() {
        String telListInfo;
        if (mTelList == null) {
            telListInfo = "ERROR: 没有号码列表文件: " + ComDef.TEL_LIST_FILE_PATH;
        }
        else if (mTelList.size() == 0) {
            telListInfo = "ERROR: 号码列表为空";
        }
        else {
            telListInfo = "电话号码列表总数(" + ComDef.TEL_LIST_FILE_PATH
                    + "):  <font color='#FF0000'>" + mTelList.size() + "</font>";
        }
        return telListInfo;
    }

    public static boolean initCheck() {
        if (mTelList == null) {
            LogUtils.e("ERROR: mTelList is null");
            return false;
        }

        if (mTelList.size() == 0) {
            LogUtils.e("ERROR: mTelList empty");
            return false;
        }

        if (mPictureDir == null) {
            LogUtils.e("ERROR: No picture dir");
            return false;
        }

        return true;
    }

    private static void genPictureDir() {
        String strTimeDir = new SimpleDateFormat("yyMMdd.HHmmss").format(new java.util.Date());
        mPictureDir = ComDef.DIALER_DIR + "/" + strTimeDir;
        if (!FileUtils.touchDir(mPictureDir)) {
            LogUtils.e("ERROR: create picture dir " + mPictureDir + " failed.");
            mPictureDir = null;
        }
    }

    public static String getTelListNumInfo() {
        return "" + getTelListNum();
    }

    public static int getTelListNum() {
        if (mTelList == null) {
            LogUtils.e("ERROR: getTelListNum: list null");
            return 0;
        }
        else {
            return mTelList.size();
        }
    }

    public static String getCurrentTelNumber() {
        LogUtils.d("getCurrentTelNumber: mCurrentCallIndex=" + mCurrentCallIndex);
        if (mTelList == null) {
            LogUtils.e("ERROR: getTelListNum: list null");
            return "";
        }
        else if (mCurrentCallIndex < 0 || mCurrentCallIndex >= mTelList.size()) {
            LogUtils.e("ERROR: getTelListNum: invalid tel index = " + mCurrentCallIndex);
            return "";
        }
        else {
            return mTelList.get(mCurrentCallIndex);
        }
    }

    public static int getCalledNum() {
        return mCalledNum;
    }

    public static void setCalledNum(int calledNum) {
        mCalledNum = calledNum;
    }

    public static int getCurrentCallIndex() {
        return mCurrentCallIndex;
    }

    public static void resetCalledIndex() {
        mCurrentCallIndex = 0;
        Settings.saveCurrentCallIndex(mCurrentCallIndex);
    }

    public static boolean isCallRunning() {
        return mCallRunning;
    }

    public static boolean startCall() {
        if (mCurrentCallIndex >= mTelList.size()) {
            LogUtils.i("startCall: All call numbers(" + DataLogic.getCurrentCallIndex() + "/" + DataLogic.getTelListNum() + ") have been finished.");
            return false;
        }

        mCallRunning = true;
        LogUtils.i("***startCall: current/total=" + DataLogic.getCurrentCallIndex() + "/" + DataLogic.getTelListNum());
        return true;
    }

    public static boolean toNextCall() {
        LogUtils.d("toNextCall: mCurrentCallIndex=" + mCurrentCallIndex);

        mCalledNum ++;
        LogUtils.d("toNextCall: mCalledNum=" + mCalledNum);
        if (mCalledNum >= mMaxCallNum) {
            LogUtils.d("toNextCall: mCalledNum(" + mCalledNum + ") up to mMaxCallNum(" + mMaxCallNum + "), exit app...");
            ThisApp.exitApp();
            return false;
        }

        mCurrentCallIndex ++;
        Settings.saveCurrentCallIndex(mCurrentCallIndex);

        if (mCurrentCallIndex >= mTelList.size()) {
            LogUtils.i("toNextCall: All Calls Finished.");
            mCallRunning = false;
            return false;
        }

        if (!mCallRunning) {
            LogUtils.i("toNextCall: Calls not running.");
            return false;
        }

        return true;
    }

    public static boolean isCallFinished() {
        if (mTelList == null) {
            return true;
        }
        else {
            return mCurrentCallIndex >= mTelList.size();
        }
    }

    public static void stopCall() {
        mCallRunning = false;
    }

    public static void saveCaptureImage(Image img) {
        LogUtils.d("saveCaptureImage: E...");
        String jpgFileName = mPictureDir + "/" + getCurrentTelNumber() + "_" + TimeUtils.getFileTime() + ".jpg";

        int ret = ImageUtils.saveImage2JPGFile(img, jpgFileName);
        if (ret < 0) {
            LogUtils.e("save screen image to " + jpgFileName + " failed with error " + ret);
        } else {
            LogUtils.i("screen image saved to " + jpgFileName);
        }
    }
}
