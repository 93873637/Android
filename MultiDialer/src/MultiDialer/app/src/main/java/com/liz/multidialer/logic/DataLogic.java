package com.liz.multidialer.logic;

import android.content.Context;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TelUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.multidialer.app.ThisApp;
import com.liz.multidialer.ui.FloatingButtonService;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * DataLogic:
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class DataLogic extends MultiDialClient {

    private static ArrayList<String> mTelList = null;
    private static String mTelListFileName = "";
    private static int mCurrentCallIndex = 0;
    private static String mPicturePath = "";
    private static int mMaxCallNum = ComDef.MAX_CALL_NUM;
    private static long mEndCallDelay = ComDef.DEFAULT_END_CALL_DELAY;
    private static int mCalledNum = 0;
    private static boolean mWorking = false;
    private static boolean mDialing = false;

    public static void init() {
        showProgress("DataLogic: init");
        loadSettings();
        if (!loadTelList()) {
            MultiDialClient.fetchTelListFile();
        }
    }

    protected static void loadSettings() {
        MultiDialClient.loadSettings();
        mTelListFileName = Settings.readFileListFile();
        mCurrentCallIndex = Settings.readCurrentCallIndex();
    }

    public static boolean loadTelList() {
        if (!checkTelListFile()) {
            showProgress("ERROR: loadTelList: check tel list file failed.");
            return false;
        }

        String filePath = getTelListFilePath();
        mTelList = FileUtils.readTxtFileLines(filePath);
        if (!checkTelList()) {
            showProgress("ERROR: loadTelList: read tel list from file \"" + filePath + "\" failed.");
            return false;
        }

        if (!genPicturePath()) {
            showProgress("ERROR: loadTelList: generate picture path for \"" + mTelListFileName + "\" failed.");
            return false;
        }

        //init call index when load success
        mCurrentCallIndex = 0;

        showProgress("loadTelList: size = " + mTelList.size());
        return true;
    }

    private static boolean checkTelListFile() {
        if (TextUtils.isEmpty(mTelListFileName)) {
            DataLogic.showProgress("ERROR: TelList file name empty");
            return false;
        }
        String filePath = getTelListFilePath();
        File f = new File(filePath);
        if (!f.exists()) {
            DataLogic.showProgress("ERROR: TelListFile " + filePath + " not exist");
            return false;
        }
        return true;
    }

    private static String getTelListFilePath() {
        return ComDef.DIALER_DIR + "/" + mTelListFileName;
    }

    private static boolean genPicturePath() {
        String picPath = ComDef.DIALER_PIC_DIR + "/" + FileUtils.getFileNeatName(mTelListFileName);
        if (FileUtils.touchDir(picPath)) {
            mPicturePath = picPath;
            return true;
        }
        else {
            mPicturePath = null;
            return false;
        }
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

    public static String getTelListFileInfo() {
        if (TextUtils.isEmpty(mTelListFileName)) {
            return "未知";
        }
        else {
            return mTelListFileName;
        }
    }

    public static String getTelListFileName() { return mTelListFileName; }
    public static void setTelListFileName(String value) { mTelListFileName = value; Settings.saveFileListFile(value); }

    public static int getMaxCallNum() {
        return mMaxCallNum;
    }
    public static void setMaxCallNum(int maxCallNum) {
        mMaxCallNum = maxCallNum;
    }

    private static long getEndCallDelay() {
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

//    private static void genPictureDir() {
//        String strTimeDir = new SimpleDateFormat("yyMMdd.HHmmss").format(new java.util.Date());
//        mPictureDir = ComDef.DIALER_DIR + "/" + strTimeDir;
//        if (!FileUtils.touchDir(mPictureDir)) {
//            LogUtils.e("ERROR: create picture dir " + mPictureDir + " failed.");
//            mPictureDir = null;
//        }
//    }

    public static String getTelListNumInfo() {
        return "" + getTelListNum();
    }

    public static String getCalledNumInfo() {
        return "" + getCalledNum();
    }

    public static String getProgressInfo() {
        return "正在拨打 "
                + (getCurrentCallIndex() + 1)
                + "/" + getTelListNum() + ": "
                + getCurrentTelNumber() + "\n"
                + "点击停止";
    }

    private static int getTelListNum() {
        if (mTelList == null) {
            LogUtils.e("ERROR: getTelListNum: list null");
            return 0;
        }
        else {
            return mTelList.size();
        }
    }

    private static String getCurrentTelNumber() {
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

    private static int getCalledNum() {
        return mCalledNum;
    }

    public static void setCalledNum(int calledNum) {
        mCalledNum = calledNum;
    }

    private static int getCurrentCallIndex() {
        return mCurrentCallIndex;
    }

    public static void resetCalledIndex() {
        mCurrentCallIndex = 0;
        Settings.saveCurrentCallIndex(mCurrentCallIndex);
    }

    public static boolean isWorking() {
        return mWorking;
    }

    public static void startWorking(Context context) {
        mWorking = true;
        startWorkingTimer(context);
    }

    public static void stopWorking() {
        mWorking = false;
        stopWorkingTimer();
    }

    private static void checkDialing(final Context context) {
        LogUtils.d("DataLogic: checkDialing: mDialing = " + mDialing);
        if (!mDialing) {
            if (canDial()) {
                mDialing = true;
                //loopCallOnNum(context);
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loopCallOnNum(context);
                    }
                });
            } else {
                MultiDialClient.fetchTelListFile();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Daemon Timer

    private static final long WORKING_TIMER_DELAY = 1000L;  //unit by ms
    private static final long WORKING_TIMER_PERIOD = 5000L;  //unit by ms

    private static Timer mWorkingTimer = null;

    private static void startWorkingTimer(final Context context) {
        showProgress("startWorkingTimer");
        mWorkingTimer = new Timer();
        mWorkingTimer.schedule(new TimerTask() {
            public void run() {
                LogUtils.d("DataLogic: startWorkingTimer: ThreadId = " + android.os.Process.myTid());
                DataLogic.checkDialing(context);
            }
        }, WORKING_TIMER_DELAY, WORKING_TIMER_PERIOD);
    }

    private static void stopWorkingTimer() {
        showProgress("stopWorkingTimer");
        if (mWorkingTimer != null) {
            mWorkingTimer.cancel();
            mWorkingTimer = null;
        }
    }

    // Daemon Timer
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static boolean canDial() {
        if (mTelList == null) {
            showProgress("ERROR: canDial: mTelList null");
            return false;
        }

        if (mTelList.size() == 0) {
            showProgress("ERROR: canDial: mTelList empty");
            return false;
        }

        if (TextUtils.isEmpty(mPicturePath)) {
            showProgress("ERROR: canDial: mPicturePath empty");
            return false;
        }

        if (mCurrentCallIndex >= mTelList.size()) {
            showProgress("***canDial: All call numbers(" + mCurrentCallIndex + "/" + mTelList.size() + ") have been finished.");
            return false;
        }

        showProgress("***canDial: yes");
        return true;
    }

    private static void loopCallOnNum(final Context context) {
        LogUtils.d("loopCallOnNum: DataLogic.getCurrentCallIndex()=" + DataLogic.getCurrentCallIndex());
        //LogUtils.d("loopCallOnNum: DataLogic.getCurrentCallIndex()=" + DataLogic.getCurrentCallIndex() + ", ThreadId=" + android.os.Process.myTid());

        String strTel = DataLogic.getCurrentTelNumber();
        if (!TelUtils.isValidTelNumber(strTel)) {
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Invalid Tel Number = \"" + strTel + "\"");
            callNextNum(context);
            return;
        }

        //if (mCallState != TelephonyManager.CALL_STATE_IDLE) {
        if (TelUtils.isCalling(context)) {
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Last call not ended, try end it and call again...");
            TelUtils.endCall(context);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    loopCallOnNum(context);
                }
            }, ComDef.WAIT_CALL_IDLE_DELAY);
            return;
        }

        try {
            String ret = TelUtils.startCall(context, strTel);
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Start Call, Tel = " + strTel + ", " + ret);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    ScreenCapture.captureOnce();
                }
            }, getScreenCaptureDelay());

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    String retEndCall = TelUtils.endCall(context);
                    showProgress("End Call: " + retEndCall);
                }
            }, DataLogic.getEndCallDelay());

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    callNextNum(context);
                }
            }, getCallNextDelay());

        } catch (Exception e) {
            //Toast.makeText(context, "loopCallOnNum Exception: " + e.toString(), Toast.LENGTH_SHORT).show();
            showProgress("loopCallOnNum Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static long getScreenCaptureDelay() {
        long delay = DataLogic.getEndCallDelay() - ComDef.CAPTURE_SCREEN_OFFSET;
        if (delay < 0) {
            return 0;
        }
        else {
            return delay;
        }
    }

    private static long getCallNextDelay() {
        return DataLogic.getEndCallDelay() + ComDef.CALL_NEXT_OFFSET;
    }

    private static void callNextNum(final Context context) {
        if (DataLogic.toNextCall()) {
            loopCallOnNum(context);
        }
        else {
            showProgress("callNextNum: No next call.");
            FloatingButtonService.showFloatingButton(false);
        }
    }

    private static boolean toNextCall() {
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
            mWorking = false;
            return false;
        }

        if (!mWorking) {
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

    protected static void saveCaptureImage(Image img) {
        LogUtils.d("saveCaptureImage: E...");
        String jpgFileName = mPicturePath + "/" + getCurrentTelNumber() + "_" + TimeUtils.getFileTime() + ".jpg";

        int ret = ImageUtils.saveImage2JPGFile(img, jpgFileName);
        if (ret < 0) {
            LogUtils.e("save screen image to " + jpgFileName + " failed with error " + ret);
        } else {
            LogUtils.i("screen image saved to " + jpgFileName);
        }
    }
}
