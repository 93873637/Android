package com.liz.whatsai.storage;

import android.app.Activity;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.ZipUtils;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.logic.Task;
import com.liz.whatsai.logic.WhatsaiDir;
import com.liz.whatsai.logic.WhatsaiMail;

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

    public static boolean init() {
        mRootNode = new WhatsaiDir();
        mRootNode.setName(ComDef.APP_NAME);
        if (!buildDirs()) {
            LogUtils.e("WhatsaiStorage: buildDirs failed.");
            return false;
        }

        /*
        //##@: for test only
        //StorageJSON.test();
        mRootNode = loadTestData();
        local_save();
        //*/

        //StorageXML.loadData((WhatsaiDir)mRootNode);
        StorageJSON.loadData((WhatsaiDir)mRootNode);
        startSavingTimer();
        return true;
    }

    private static boolean buildDirs() {
        if (!FileUtils.touchDir(ComDef.WHATSAI_HOME)) {
            LogUtils.e("WhatsaiStorage: Touch dir of " + ComDef.WHATSAI_HOME + " failed.");
            return false;
        }
        if (!FileUtils.touchDir(ComDef.WHATSAI_DATA_DIR)) {
            LogUtils.e("WhatsaiStorage: Touch dir of " + ComDef.WHATSAI_DATA_DIR + " failed.");
            return false;
        }
        if (!FileUtils.touchDir(ComDef.WHATSAI_AUDIO_DATA_PATH)) {
            LogUtils.e("WhatsaiStorage: Touch dir of " + ComDef.WHATSAI_AUDIO_DATA_PATH + " failed.");
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

    private static void startSavingTimer() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                local_save_period();
                cloud_save_period();
            }
        }, ComDef.WHATSAI_SAVING_DELAY, ComDef.WHATSAI_SAVING_TIMER);
    }

    private static void local_save_period() {
        if (isDirty()) {
            local_save();
        }
        else {
            LogUtils.v("WhatsaiStorage: list data not change for local save.");
        }
    }

    private static void cloud_save_period() {
        if (cloud_save_required()) {
            cloud_save(null);
        } else {
            LogUtils.d("WhatsaiStorage: not need cloud save.");
        }
    }

    //
    //Two Conditions:
    //1. time up to sync
    //2. data file changed
    //
    private static boolean cloud_save_required() {
        LogUtils.d("WhatsaiStorage: cloud_save_required: sync_time = " +  mRootNode.getSyncTime());

        // check if time up to cloud save period
        long diff = System.currentTimeMillis() - mRootNode.getSyncTime();
        if (diff < ComDef.CLOUD_SAVE_PERIOD) {
            LogUtils.d("WhatsaiStorage: cloud_save_required: current diff " + diff
                    + " not up to cloud save period " + ComDef.CLOUD_SAVE_PERIOD + ", check failed");
            return false;
        }

        // anyway, update local save file
        saveToFile((WhatsaiDir)mRootNode, ComDef.WHATSAI_DATA_FILE_TEMP);

        // check if data file changed
        try {
            if (FileUtils.sameFile(ComDef.WHATSAI_DATA_FILE_SYNC, ComDef.WHATSAI_DATA_FILE_TEMP)) {
                LogUtils.i("WhatsaiStorage: cloud_save_required: data file not changed, check failed");
                return false;
            }
        } catch (Exception e) {
            LogUtils.e("WhatsaiStorage: cloud_save_required: compare same file exception: " + e.toString());
            // think as not same
        }

        // finally, cloud save required
        LogUtils.d("WhatsaiStorage: cloud_save_required: check pass");
        return true;
    }

    public static void local_save() {
        saveToFile((WhatsaiDir)mRootNode, ComDef.WHATSAI_DATA_FILE);
    }

    private static void saveToFile(WhatsaiDir dir, String fileAbsolute) {
        try {
            File f = new File(fileAbsolute);
            if (!f.exists()) {
                if (!f.createNewFile()) {
                    LogUtils.e("WhatsaiStorage: create whatsai file \"" + fileAbsolute + "\" failed.");
                    return;
                }
            }
            OutputStream output = new FileOutputStream(f);
            //##@: StorageXML.saveToXML(output, (WhatsaiDir) mRootNode);
            StorageJSON.saveToJSON(output, dir);
            output.flush();
            output.close();
            LogUtils.d("WhatsaiStorage: save to local file \"" + fileAbsolute + "\" succeed.");
            clearDirty();
        } catch (Exception e) {
            LogUtils.e("WhatsaiStorage: save to local file \"" + fileAbsolute + "\" exception: " + e.toString());
        }
    }

    public static void cloud_save(Activity activity) {
        LogUtils.d("WhatsaiStorage: cloud save: E...");

        //update sync time of root node
        mRootNode.setSyncTime(System.currentTimeMillis());

        //update local save file
        saveToFile((WhatsaiDir)mRootNode, ComDef.WHATSAI_DATA_FILE_SYNC);

        //zip local file to cloud file
        if (!ZipUtils.zip(ComDef.CLOUD_FILE_PATH, ComDef.WHATSAI_DATA_FILE_SYNC)) {
            LogUtils.e("WhatsaiStorage: cloud_save: zip cloud file failed");
            return;
        }

        //finally, save file to cloud
        LogUtils.i("WhatsaiStorage: cloud_save: sync_time = " + mRootNode.getSyncTime());
        WhatsaiMail.start(activity);
    }

    ///* for test
    protected static Node loadTestData() {
        WhatsaiDir rootNode = new WhatsaiDir();
        rootNode.setName("whatsai");

        {Task task = new Task(); task.setName("TaskName0"); task.setDone(true); rootNode.add(task);}
        {Task task = new Task(); task.setName("TaskName1"); task.setDone(false); rootNode.add(task);}

        {
            WhatsaiDir tg = new WhatsaiDir();
            tg.setName("TaskGroup1");
            rootNode.add(tg);
            for (int i = 0; i < 2; i++) {
                Task task = new Task();
                task.setName("SubTask1" + i);
                task.setDone(i % 2 != 0);
                tg.add(task);
            }
            {
                WhatsaiDir tg11 = new WhatsaiDir();
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
            WhatsaiDir tg = new WhatsaiDir();
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
