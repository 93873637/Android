package com.liz.whatsai.storage;

import android.app.Activity;
import android.text.TextUtils;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.androidutils.ZipUtils;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.logic.Task;
import com.liz.whatsai.logic.WSDir;
import com.liz.whatsai.logic.WSMail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * WhatsaiStorage:
 * Created by liz on 2019/2/15.
 */

@SuppressWarnings("unused")
public class WhatsaiStorage {

    private static Node mRootNode = null;
    private static boolean mDirty = false;
    private static long mLastSyncTime = 0;
    private static String mLastSyncFile = "";

    public static boolean initStorage() {
        mRootNode = new WSDir();
        mRootNode.setName(ComDef.APP_NAME);
        if (!buildDirs()) {
            LogUtils.e("WhatsaiStorage: buildDirs failed.");
            return false;
        }

        StorageJSON.loadData((WSDir)mRootNode);
        DataLogic.clearDirty();  //not dirty for load data

        WSMail.setWhatsaiMailCallback(new WSMail.WhatsaiMailCallback() {
            @Override
            public void onSendMailSuccess(String fileAbsolute) {
                // remove old file
                if (!TextUtils.isEmpty(mLastSyncFile)) {
                    LogUtils.d("WhatsaiStorage: onSendMailSuccess: remove old sync file = " + mLastSyncFile);
                    FileUtils.removeFile(mLastSyncFile);
                }
                mLastSyncTime = System.currentTimeMillis();
                mLastSyncFile = fileAbsolute;
                LogUtils.d("WhatsaiStorage: onSendMailSuccess: mLastSyncFile = " + mLastSyncFile + ", mLastSyncTime = " + mLastSyncTime);
            }
        });

        startLocalSaveTimer();
        startCloudSaveTimer();
        return true;
    }

    public static void releaseStorage() {
        stopLocalSaveTimer();
        stopCloudSaveTimer();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Local Save Timer
    private static Timer mLocalSaveTimer = null;
    private static void startLocalSaveTimer() {
        mLocalSaveTimer = new Timer();
        mLocalSaveTimer.schedule(new TimerTask() {
            public void run() {
                onLocalSaveTimer();
            }
        }, ComDef.LOCAL_SAVE_DELAY, ComDef.LOCAL_SAVE_TIMER);
    }
    private static void stopLocalSaveTimer() {
        if (mLocalSaveTimer != null) {
            mLocalSaveTimer.cancel();
            mLocalSaveTimer = null;
        }
    }
    // Local Save Timer
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Cloud Save Timer
    private static Timer mCloudSaveTimer = null;
    private static void startCloudSaveTimer() {
        mCloudSaveTimer = new Timer();
        mCloudSaveTimer.schedule(new TimerTask() {
            public void run() {
                onCloudSaveTimer();
            }
        }, ComDef.CLOUD_SAVE_DELAY, ComDef.CLOUD_SAVE_TIMER);
    }
    private static void stopCloudSaveTimer() {
        if (mCloudSaveTimer != null) {
            mCloudSaveTimer.cancel();
            mCloudSaveTimer = null;
        }
    }
    // Cloud Save Timer
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static boolean buildDirs() {
        if (!FileUtils.touchDir(ComDef.WHATSAI_HOME)) {
            LogUtils.e("WhatsaiStorage: Touch dir of " + ComDef.WHATSAI_HOME + " failed.");
            return false;
        }
        if (!FileUtils.touchDir(ComDef.WHATSAI_DATA_DIR)) {
            LogUtils.e("WhatsaiStorage: Touch dir of " + ComDef.WHATSAI_DATA_DIR + " failed.");
            return false;
        }
        if (!FileUtils.touchDir(ComDef.WHATSAI_AUDIO_DIR)) {
            LogUtils.e("WhatsaiStorage: Touch dir of " + ComDef.WHATSAI_AUDIO_DIR + " failed.");
            return false;
        }
        if (!FileUtils.touchDir(ComDef.WHATSAI_AUDIO_TEMPLATE_DIR)) {
            LogUtils.e("WhatsaiStorage: Touch dir of " + ComDef.WHATSAI_AUDIO_TEMPLATE_DIR + " failed.");
            return false;
        }
        if (!FileUtils.touchDir(ComDef.WHATSAI_CACHE_DIR)) {
            LogUtils.e("WhatsaiStorage: Touch dir of " + ComDef.WHATSAI_CACHE_DIR + " failed.");
            return false;
        }
        return true;
    }

    public static Node getRootNode() {
        return mRootNode;
    }

    public static void setDirty() {
        mDirty = true;
    }

    public static void clearDirty() {
        mDirty = false;
    }

    private static boolean isDirty() {
        return mDirty;
    }

    public static void onLocalSaveTimer() {
        LogUtils.d("WhatsaiStorage: onLocalSaveTimer: E...");

        if (!isDirty()) {
            LogUtils.d("WhatsaiStorage: onLocalSaveTimer: data not change.");
            return;
        }

        if (mRootNode.isEmpty()) {
            LogUtils.d("WhatsaiStorage: onLocalSaveTimer: root node empty");
            return;
        }

        LogUtils.d("WhatsaiStorage: onLocalSaveTimer: local_save...");
        local_save();
    }

    //
    // Cloud save including:
    // 1. local save
    // 2. check modification
    // 3. upload if modified
    //
    // Cloud save must satisfy two conditions:
    // 1. time is up
    // 2. data not empty
    // 3. data changed
    //
    private static void onCloudSaveTimer() {
        if (!isDirty()) {
            LogUtils.d("WhatsaiStorage: onCloudSaveTimer: data not change.");
            return;
        }

        if (mRootNode.isEmpty()) {
            LogUtils.d("WhatsaiStorage: onCloudSaveTimer: root node empty");
            return;
        }

        // check if time up to cloud save period
        LogUtils.d("WhatsaiStorage: onCloudSaveTimer: mLastSyncTime = " +  mLastSyncTime);
        long diff = System.currentTimeMillis() - mLastSyncTime;
        if (diff < ComDef.CLOUD_SAVE_TIMER) {
            LogUtils.i("WhatsaiStorage: onCloudSaveTimer: current time diff " + diff
                    + " not up to cloud save time " + ComDef.CLOUD_SAVE_TIMER);
            return;
        }

        // anyway, local save first
        LogUtils.d("WhatsaiStorage: onCloudSaveTimer: local save...");
        local_save();

        // generate a new zip file
        String zipFileAbsolute = genCloudSaveFileAbsolute();
        if (!ZipUtils.zipFileAbsolutes(zipFileAbsolute, ComDef.WHATSAI_DATA_FILE, ComDef.WHATSAI_DATA_DIR)) {
            LogUtils.e("WhatsaiStorage: onCloudSaveTimer: zip cloud file failed");
            return;
        }

        // check if upload file changed
        if (TextUtils.isEmpty(mLastSyncFile)) {
            LogUtils.i("WhatsaiStorage: onCloudSaveTimer: last sync file empty");
        }
        else {
            try {
                if (FileUtils.sameFile(zipFileAbsolute, mLastSyncFile)) {
                    LogUtils.i("WhatsaiStorage: onCloudSaveTimer: upload file not changed");
                    return;
                }
            } catch (Exception e) {
                LogUtils.e("WhatsaiStorage: onCloudSaveTimer: compare same file exception: " + e.toString());
                // think as not same, continue cloud save
            }
        }

        // finally, cloud save required
        LogUtils.d("WhatsaiStorage: onCloudSaveTimer: upload new cloud file \"" + zipFileAbsolute + "\"...");
        WSMail.start(null, zipFileAbsolute);
    }

    //
    // no check, direct save to cloud
    //
    public static void local_save() {
        saveToFile((WSDir)mRootNode, ComDef.WHATSAI_DATA_FILE);
    }

    //
    // no check, direct save to cloud
    //
    public static void cloud_save(Activity activity) {
        LogUtils.d("WhatsaiStorage: cloud save: E...");

        // local save first
        local_save();

        //prepare file to upload
        String zipFileAbsolute = genCloudSaveFileAbsolute();
        if (!ZipUtils.zipFileAbsolutes(zipFileAbsolute, ComDef.WHATSAI_DATA_FILE, ComDef.WHATSAI_DATA_DIR)) {
            LogUtils.e("WhatsaiStorage: cloud_save: zip cloud file failed");
            return;
        }

        //finally, save file to cloud
        WSMail.start(activity, zipFileAbsolute);
    }

    private static String genCloudSaveFileAbsolute() {
        return ComDef.WHATSAI_CACHE_DIR + "/"
                + ComDef.CLOUD_FILE_NAME_PREFIX + "_"
                + TimeUtils.getFileTime(false)
                + ComDef.CLOUD_FILE_NAME_SUFFIX;
    }

    private static void saveToFile(WSDir dir, String fileAbsolute) {
        LogUtils.d("WhatsaiStorage: saveToFile: \"" + fileAbsolute + "\"...");
        try {
            File f = new File(fileAbsolute);
            if (!f.exists()) {
                if (!f.createNewFile()) {
                    LogUtils.e("WhatsaiStorage: create whatsai file \"" + fileAbsolute + "\" failed.");
                    return;
                }
            }
            OutputStream output = new FileOutputStream(f);
            StorageJSON.saveToJSON(output, dir);
            output.flush();
            output.close();
            LogUtils.d("WhatsaiStorage: save to local file \"" + fileAbsolute + "\" succeed.");
            clearDirty();
        } catch (Exception e) {
            LogUtils.e("WhatsaiStorage: save to local file \"" + fileAbsolute + "\" exception: " + e.toString());
        }
    }

    ///* for test
    protected static Node loadTestData() {
        WSDir rootNode = new WSDir();
        rootNode.setName("whatsai");

        {Task task = new Task(); task.setName("TaskName0"); task.setDone(true); rootNode.add(task);}
        {Task task = new Task(); task.setName("TaskName1"); task.setDone(false); rootNode.add(task);}

        {
            WSDir tg = new WSDir();
            tg.setName("TaskGroup1");
            rootNode.add(tg);
            for (int i = 0; i < 2; i++) {
                Task task = new Task();
                task.setName("SubTask1" + i);
                task.setDone(i % 2 != 0);
                tg.add(task);
            }
            {
                WSDir tg11 = new WSDir();
                tg11.setName("taskgroup11");
                tg.add(tg11);
                for (int i = 0; i < 3; i++) {
                    Task task = new Task();
                    task.setName("SubTask11" + i);
                    task.setDone(i % 2 != 0);
                    tg11.add(task);
                }
            }
        }

        {Task task = new Task(); task.setName("TaskName2"); task.setDone(false); rootNode.add(task);}
        {Task task = new Task(); task.setName("TaskName3"); task.setDone(true); rootNode.add(task);}
        {Task task = new Task(); task.setName("TaskName4"); task.setDone(true); rootNode.add(task);}

        {
            WSDir tg = new WSDir();
            tg.setName("TaskGroup2");
            rootNode.add(tg);
            for (int i = 0; i < 29; i++) {
                Task task = new Task();
                task.setName("SubTask2" + i);
                task.setDone(i % 2 == 0);
                tg.add(task);
            }
        }

        return rootNode;
    }
    //*/
}
