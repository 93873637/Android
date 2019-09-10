package com.liz.whatsai.storage;

import android.app.Activity;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.ZipUtils;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
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
    private static boolean mListDirty = false;

    public static void init() {
        mRootNode = new WhatsaiDir();
        mRootNode.setName(ComDef.APP_NAME);

        /*
        //##@: for test only
        //StorageJSON.test();
        mRootNode = loadTestData();
        local_save();
        //*/

        //StorageXML.loadData((WhatsaiDir)mRootNode);
        StorageJSON.loadData((WhatsaiDir)mRootNode);
        startSavingTimer();
    }

    public static Node getRootNode() {
        return mRootNode;
    }

    public static void setDirty(boolean dirty) {
        mListDirty = dirty;
    }

    private static boolean isDirty() {
        return mListDirty;
    }

    private static void startSavingTimer() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                if (isDirty()) {
                    local_save();
                }
                else {
                    LogUtils.v("WhatsaiStorage: list data not change for local save.");
                }
                cloud_save_period();
            }
        }, ComDef.WHATSAI_SAVING_DELAY, ComDef.WHATSAI_SAVING_TIMER);
    }

    public static void local_save() {
        try {
            File path = new File(ComDef.WHATSAI_DATA_PATH);
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    LogUtils.e("WhatsaiStorage: make dir of whatsai path \"" + ComDef.WHATSAI_DATA_PATH + "\" failed.");
                    return;
                }
            }
            File f = new File(ComDef.WHATSAI_DATA_FILE);
            if (!f.exists()) {
                if (!f.createNewFile()) {
                    LogUtils.e("WhatsaiStorage: create whatsai file \"" + ComDef.WHATSAI_DATA_FILE + "\" failed.");
                    return;
                }
            }
            OutputStream output = new FileOutputStream(f);
            //##@: StorageXML.saveToXML(output, (WhatsaiDir) mRootNode);
            StorageJSON.saveToJSON(output, (WhatsaiDir) mRootNode);
            output.flush();
            output.close();

            LogUtils.d("WhatsaiStorage: save to local file \"" + ComDef.WHATSAI_DATA_PATH + "\" succeed.");
            setDirty(false);
        } catch (Exception e) {
            LogUtils.e("WhatsaiStorage: save to local file \"" + ComDef.WHATSAI_DATA_PATH + "\" exception: " + e.toString());
        }
    }

    public static void cloud_save(Activity activity) {
        if (ZipUtils.zip(ComDef.MAIL_ATTACH_FILE_PATH, ComDef.WHATSAI_DATA_FILE)) {
            WhatsaiMail.start(activity);
        }
        else {
            LogUtils.e("WhatsaiStorage: cloud_save: zip data file failed");
        }
    }

    private static void cloud_save_period() {
        long currentTime = System.currentTimeMillis();

        //check if time up to cloud save period
        long diff = currentTime - mRootNode.getSyncTime();
        if (diff < ComDef.CLOUD_SAVE_PERIOD) {
            LogUtils.d("WhatsaiStorage: current diff " + diff + " not up to cloud save period " + ComDef.CLOUD_SAVE_PERIOD);
            return;
        }
        else {
            mRootNode.setSyncTime(currentTime);
        }

        //TODO: improve it as first compress current data file to a temporary zip file
        //###@:
        if (!ZipUtils.zip(ComDef.MAIL_ATTACH_FILE_PATH, ComDef.WHATSAI_DATA_FILE)) {
            LogUtils.e("WhatsaiStorage: cloud_save_period: zip data file failed");
            return;
        }

        //check if zip file changed
        //###@:

        //finally, save to cloud by mail
        WhatsaiMail.start(null);
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
