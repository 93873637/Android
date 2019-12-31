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
import com.liz.androidutils.ZipUtils;
import com.liz.multidialer.app.ThisApp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private static boolean mPaused = false;

    public static void init() {
        LogUtils.d("DataLogic: init");
        if (createDirs()) {
            LogUtils.e("ERROR: create directories failed.");
        }
        loadSettings();
        if (!loadTelList()) {
            MultiDialClient.fetchTelListFile();
        }

        // ##@: test
//        new Thread() {
//            @Override
//            public void run() {
//                SFTPManager sftpMgr = new SFTPManager(getServerAddress(), getServerPort(), getUserName(), getPassword());
//                sftpMgr.connect();
//                sftpMgr.exec("mv /home/shandong1/PUB_SPACE/NUM_DATA/WAIT_DATA/M01_000001.txt /home/shandong1/PUB_SPACE/NUM_DATA/RUN_DATA/M01_000001.txt");
//            }
//        }.start();
    }


    private static boolean createDirs() {
        if (!FileUtils.touchDir(ComDef.DIALER_DIR)) {
            LogUtils.e("ERROR: create dir \"" + ComDef.DIALER_DIR + "\" failed.");
            return false;
        }
        if (!FileUtils.touchDir(ComDef.DIALER_NUM_DIR)) {
            LogUtils.e("ERROR: create dir \"" + ComDef.DIALER_NUM_DIR + "\" failed.");
            return false;
        }
        if (!FileUtils.touchDir(ComDef.DIALER_PIC_DIR)) {
            LogUtils.e("ERROR: create dir \"" + ComDef.DIALER_PIC_DIR + "\" failed.");
            return false;
        }
        return true;
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

        if (!genPicturePathForTelList(mTelListFileName)) {
            showProgress("ERROR: loadTelList: generate picture path for \"" + mTelListFileName + "\" failed.");
            return false;
        }

        showProgress("loadTelList: get picture path \"" + mPicturePath + "\"");

        //init call index when load success
        mCalledNum = 0;
        mCurrentCallIndex = 0;

        showProgress("loadTelList: size = " + mTelList.size());
        return true;
    }

    public static void clearTelListFile() {
        setTelListFileName("");
        if (mTelList != null) {
            mTelList.clear();
        }
        setCalledNum(0);
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
        return ComDef.DIALER_NUM_DIR + "/" + mTelListFileName;
    }

    private static boolean genPicturePathForTelList(String telListFileName) {
        String strDateTime = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
        String picPath = ComDef.DIALER_PIC_DIR + "/" + FileUtils.getFileNeatName(telListFileName) + "_" + strDateTime;
        if (FileUtils.touchDir(picPath)) {
            mPicturePath = picPath;
            showProgress("generate picture path \"" + mPicturePath + "\"");
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

    public static String getTelListNumInfo() {
        return "" + getTelListNum();
    }

    public static String getCalledNumInfo() {
        return "" + getCalledNum();
    }

    public static String getFloatingButtonInfo() {
        if (!mWorking) {
            return "NOT WORKING";
        }
        else {
            if (!mDialing) {
                return "NOT DIALING";
            }
            else {
                String dialInfo = "DIALING "
                        + (getCurrentCallIndex() + 1) + "/" + getTelListNum() + ": "
                        + getCurrentTelNumber()
                        + "\n";
                if (mPaused) {
                    dialInfo += "已暂停，点击继续";
                }
                else {
                    dialInfo += "拨号中，点击暂停";
                }
                return dialInfo;
            }
        }
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

    public static boolean isDialing() {
        return mDialing;
    }

    public static boolean isPaused() {
        return mPaused;
    }

    public static void startWorking(Context context) {
        mWorking = true;
        startWorkingTimer(context);
    }

    public static void stopWorking() {
        mWorking = false;
        stopWorkingTimer();
        mDialing = false;
        mPaused = false;
    }

    public static void switchPauseOrContinue(Context context) {
        if (!mWorking) {
            LogUtils.d("switchPauseOrContinue: NOT Working");
            return;
        }
        if (!mDialing) {
            LogUtils.d("switchPauseOrContinue: NOT Dialing");
            return;
        }
        showProgress("switchPauseOrContinue: mPaused = " + mPaused + " -> " + !mPaused);
        mPaused = !mPaused;
        if (!mPaused) {
            startLoopCallOnNum(context);
        }
    }

    private static void checkDialing(Context context) {
        LogUtils.d("DataLogic: checkDialing: mDialing = " + mDialing);
        if (!mDialing) {
            if (canDial()) {
                mDialing = true;
                startLoopCallOnNum(context);
            } else {
                MultiDialClient.fetchTelListFile();
            }
        }
    }

    //loopCallOnNum has handler, here using Main looper
    private static void startLoopCallOnNum(final Context context) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                loopCallOnNum(context);
            }
        });
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
            LogUtils.e("ERROR: canDial: mTelList null");
            return false;
        }

        if (mTelList.size() == 0) {
            LogUtils.e("ERROR: canDial: mTelList empty");
            return false;
        }

        if (TextUtils.isEmpty(mPicturePath)) {
            LogUtils.e("ERROR: canDial: mPicturePath empty");
            return false;
        }

        if (mCurrentCallIndex >= mTelList.size()) {
            LogUtils.i("***canDial: All call numbers(" + mCurrentCallIndex + "/" + mTelList.size() + ") have been finished.");
            return false;
        }

        LogUtils.i("***canDial: yes");
        return true;
    }

    private static void loopCallOnNum(final Context context) {
        LogUtils.d("loopCallOnNum: DataLogic.getCurrentCallIndex()=" + DataLogic.getCurrentCallIndex());
        //LogUtils.d("loopCallOnNum: DataLogic.getCurrentCallIndex()=" + DataLogic.getCurrentCallIndex() + ", ThreadId=" + android.os.Process.myTid());

        if (mPaused) {
            LogUtils.d("loopCallOnNum: Paused");
            return;
        }

        String strTel = DataLogic.getCurrentTelNumber();
        if (!TelUtils.isValidTelNumber(strTel)) {
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Invalid Tel Number = \"" + strTel + "\"");
            callNextNumber(context);
            return;
        }

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
                    callNextNumber(context);
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

    private static void callNextNumber(final Context context) {
        if (DataLogic.toNextCall()) {
            loopCallOnNum(context);
        }
        else {
            showProgress("callNextNumber: No next call.");
            onAllCallFinished();
        }
    }

    private static void onAllCallFinished() {
        LogUtils.i("onAllCallFinished");
        String strDateTime = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));

        //zip pic data and upload
        String zipFileName = FileUtils.getFileNeatName(mTelListFileName) + "_" + strDateTime + ".zip";
        String zipFileNameDone = FileUtils.getFileNeatName(mTelListFileName) + "_" + strDateTime + "_done.zip";
        String zipFilePath = ComDef.DIALER_PIC_DIR + "/" + zipFileName;
        LogUtils.d("onAllCallFinished: mPicturePath = " + mPicturePath);
        LogUtils.d("onAllCallFinished: zipFilePath = " + zipFilePath);

        if (!ZipUtils.zip(zipFilePath, mPicturePath, true)) {
            LogUtils.e("ERROR: onAllCallFinished: zip pic failed");
            onUploadFinished();
        }
        else {
            MultiDialClient.uploadPicData(zipFileName, zipFileNameDone);
        }
    }

    public static void onUploadFinished() {
        MultiDialClient.moveRunDataToEnd(mTelListFileName);
        mDialing = false;
        mPaused = false;
    }

    private static boolean toNextCall() {
        LogUtils.d("toNextCall: mCurrentCallIndex=" + mCurrentCallIndex);

        if (!mWorking) {
            LogUtils.i("toNextCall: Working stopped.");
            return false;
        }

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
            LogUtils.d("toNextCall: mCurrentCallIndex(" + mCurrentCallIndex + ") up to tel list size(" + mTelList.size() + ")");
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
        String jpgFileName = mPicturePath + "/" + getCurrentTelNumber() + "_" + TimeUtils.getFileTime(false) + ".jpg";

        int ret = ImageUtils.saveImage2JPGFile(img, jpgFileName, DataLogic.getJpegQuality());
        if (ret < 0) {
            LogUtils.e("save screen image to " + jpgFileName + " failed with error " + ret);
        } else {
            LogUtils.i("screen image saved to " + jpgFileName);
        }
    }
}
