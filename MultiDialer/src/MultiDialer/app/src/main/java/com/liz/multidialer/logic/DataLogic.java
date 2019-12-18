package com.liz.multidialer.logic;

import android.media.Image;
import android.text.TextUtils;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.multidialer.app.ThisApp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * DataLogic:
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class DataLogic extends MultiDialClient {

    private static ArrayList<String> mTelList;
    private static int mCurrentCallIndex = 0;
    private static int mMaxCallNum = ComDef.MAX_CALL_NUM;
    private static long mEndCallDelay = ComDef.DEFAULT_END_CALL_DELAY;
    private static int mCalledNum = 0;
    private static String mPictureDir = null;
    private static boolean mCallRunning = false;

    public static void init() {
        showProgress("DataLogic: init");

        loadSettings();
        genPictureDir();

        if (!loadTelList()) {
            MultiDialClient.fetchTelListFile();
        }
    }

    protected static void loadSettings() {
        MultiDialClient.loadSettings();
        mCurrentCallIndex = Settings.readCurrentCallIndex();
    }

    private static boolean loadTelList() {
        if (!checkTelListFile()) {
            showProgress("ERROR: loadTelList: check tel list file failed.");
            return false;
        }

        mTelList = FileUtils.readTxtFileLines(mTelListFile);
        if (!checkTelList()) {
            showProgress("ERROR: loadTelList: check tel list failed.");
            return false;
        }

        //load success, init call index
        mCurrentCallIndex = 0;

        showProgress("loadTelList: size = " + mTelList.size());
        return true;
    }

    private static boolean checkTelListFile() {
        if (TextUtils.isEmpty(mTelListFile)) {
            showProgress("ERROR: TelListFile Empty");
            return false;
        }
        File f = new File(mTelListFile);
        if (!f.exists()) {
            showProgress("ERROR: TelListFile " + mTelListFile + " not exist");
            return false;
        }
        return true;
    }

    private static boolean checkTelList() {
        if (mTelList == null) {
            showProgress("ERROR: mTelList null");
            return false;
        }
        if (mTelList.size() == 0) {
            showProgress("ERROR: mTelList empty");
            return false;
        }
        return true;
    }

    public static String getTelListFileInfo() {
        if (TextUtils.isEmpty(mTelListFile)) {
            return "未知";
        }
        else {
            return mTelListFile;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // UI Progress Info Callback

    public interface ShowProgressCallback {
        void onShowProgress(String msg);
    }

    private static ShowProgressCallback mProgressCallback;
    public static void setProgressCallback(ShowProgressCallback callback) {
        mProgressCallback = callback;
    }

    public static void showProgress(String msg) {
        if (mProgressCallback != null) {
            mProgressCallback.onShowProgress(msg);
        }
    }

    // UI Progress Info Callback
    ///////////////////////////////////////////////////////////////////////////////////////////////

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
        if (mTelList == null) {
            showProgress("ERROR: mTelList null");
            return false;
        }

        if (mTelList.size() == 0) {
            showProgress("ERROR: mTelList empty");
            return false;
        }

        if (mPictureDir == null) {
            showProgress("ERROR: Picture Dir null");
            return false;
        }

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
