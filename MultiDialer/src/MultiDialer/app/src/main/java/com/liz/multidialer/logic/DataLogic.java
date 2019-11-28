package com.liz.multidialer.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.multidialer.app.ThisApp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * DataLogic:
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class DataLogic {

    private static ArrayList<String> mTelList;
    private static int mCurrentCallIndex = 0;
    private static int mMaxCallNum = ComDef.MAX_CALL_NUM;
    private static long mEndCallDelay = ComDef.DEFAULT_END_CALL_DELAY;
    private static int mCalledNum = 0;
    private static String mPictureDir = null;
    private static boolean mCallRunning = false;

    public static void init() {
        LogUtils.d("DataLogic: init");

        loadTelList();
        genPictureDir();
        mCurrentCallIndex = readCurrentCallIndex();

        LogUtils.d("DataLogic: TEL_LIST_FILE_PATH = " + ComDef.TEL_LIST_FILE_PATH);
        LogUtils.d("DataLogic: DIALER_DIR = " + ComDef.DIALER_DIR);
        LogUtils.d("DataLogic: mCurrentCallIndex = " + mCurrentCallIndex);
    }

    public static void loadTelList() {
        mTelList = FileUtils.readTxtFileLines(ComDef.TEL_LIST_FILE_PATH);
    }

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

    public static int getTelNumber() {
        if (mTelList == null) {
            LogUtils.e("ERROR: getTelNumber: list null");
            return 0;
        }
        else {
            return mTelList.size();
        }
    }

    public static String getCurrentTelNumber() {
        LogUtils.d("getCurrentTelNumber: mCurrentCallIndex=" + mCurrentCallIndex);
        if (mTelList == null) {
            LogUtils.e("ERROR: getTelNumber: list null");
            return "";
        }
        else if (mCurrentCallIndex < 0 || mCurrentCallIndex >= mTelList.size()) {
            LogUtils.e("ERROR: getTelNumber: invalid tel index = " + mCurrentCallIndex);
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
        saveCurrentCallIndex();
    }

    private static int readCurrentCallIndex() {
        SharedPreferences sharedPreferences = ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(ComDef.KEY_CURRENT_CALLED_INDEX, 0);
    }

    private static void saveCurrentCallIndex() {
        SharedPreferences sharedPreferences= ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ComDef.KEY_CURRENT_CALLED_INDEX, mCurrentCallIndex);
        editor.apply();
    }

    public static boolean isCallRunning() {
        return mCallRunning;
    }

    public static boolean startCall() {
        if (mCurrentCallIndex >= mTelList.size()) {
            LogUtils.i("startCall: All call numbers(" + DataLogic.getCurrentCallIndex() + "/" + DataLogic.getTelNumber() + ") have been finished.");
            return false;
        }

        mCallRunning = true;
        LogUtils.i("***startCall: current/total=" + DataLogic.getCurrentCallIndex() + "/" + DataLogic.getTelNumber());
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
        saveCurrentCallIndex();

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
