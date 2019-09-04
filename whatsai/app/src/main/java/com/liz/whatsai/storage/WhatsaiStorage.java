package com.liz.whatsai.storage;

import android.text.TextUtils;
import android.util.Xml;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.ZipUtils;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.logic.Reminder;
import com.liz.whatsai.logic.Task;
import com.liz.whatsai.logic.WhatsaiDir;
import com.liz.whatsai.logic.WhatsaiMail;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        //##@: loadTestData();
        loadData();
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
                DataLogic.local_save();
                DataLogic.cloud_save_period();
            }
        }, ComDef.TASK_LIST_SAVING_DELAY, ComDef.TASK_LIST_SAVING_TIMER);
    }

    private static void loadData() {
        try {
            File f = new File(ComDef.WHATSAI_DATA_FILE);
            if (!f.exists()) {
                LogUtils.i("WhatsaiStorage: whatsai data file \"" + ComDef.WHATSAI_DATA_FILE + "\" not exists");
            }
            else {
                InputStream input = new FileInputStream(f);
                StorageXML.loadFromXML(input, (WhatsaiDir)mRootNode);
            }
        } catch (Exception e) {
            LogUtils.e("WhatsaiStorage: loadData from xml exception: " + e.toString());
        }
    }

    public static void local_save() {
        if (!isDirty()) {
            LogUtils.v("WhatsaiStorage: list data not change for local_save.");
            return;
        }

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
            //###@: StorageXML.saveToXML(output, (WhatsaiDir) mRootNode);
            StorageJSON.saveToJSON(output, (WhatsaiDir) mRootNode);
            output.flush();
            output.close();

            LogUtils.d("WhatsaiStorage: save to local file \"" + ComDef.WHATSAI_DATA_PATH + "\" succeed.");
            setDirty(false);
        } catch (Exception e) {
            LogUtils.e("WhatsaiStorage: save to local file \"" + ComDef.WHATSAI_DATA_PATH + "\" exception: " + e.toString());
        }
    }

    public static void cloud_save() {
        if (ZipUtils.zip(ComDef.MAIL_ATTACH_FILE_PATH, ComDef.WHATSAI_DATA_FILE)) {
            WhatsaiMail.start();
        }
        else {
            LogUtils.e("WhatsaiStorage: cloud_save: zip data file failed");
        }
    }

    protected static void cloud_save_period() {
        long currentTime = System.currentTimeMillis();

        //check if time up to cloud save period
        long diff = currentTime - mRootNode.getSyncTime();
        if (diff < ComDef.CLOUD_SAVE_PERIOD) {
            LogUtils.d("WhatsaiStorage: current diff " + diff + " not up to cloud save period " + ComDef.CLOUD_SAVE_PERIOD);
            return;
        }

        //TODO: improve it as first compress current data file to a temporary zip file
        //####@:
        if (!ZipUtils.zip(ComDef.MAIL_ATTACH_FILE_PATH, ComDef.WHATSAI_DATA_FILE)) {
            LogUtils.e("WhatsaiStorage: cloud_save_period: zip data file failed");
            return;
        }

        //check if zip file changed
        //###@:

        //finally, save to cloud by mail
        WhatsaiMail.start();
    }

    public static void updateSyncTime() {
        mRootNode.setSyncTime(System.currentTimeMillis());
    }

    private static void loadTestData() {
        {
            Task task = new Task();
            task.setName("TaskName0");
            task.setDone(true);
            mRootNode.add(task);
        }
        {
            Task task = new Task();
            task.setName("TaskName1");
            task.setDone(false);
            mRootNode.add(task);
        }
        {
            WhatsaiDir tg = new WhatsaiDir();
            tg.setName("taskgroup0");
            for (int i = 0; i < 30; i++) {
                Task task = new Task();
                task.setName("SubTask" + i);
                task.setDone(i % 2 == 0);
                tg.add(task);
            }
            mRootNode.add(tg);
        }
    }

     /* for test
    protected static void loadTestData() {
        {Task task = new Task(); task.name = "TaskName"; task.setDone(true); mRootNode.add(task);}
        {Task task = new Task(); task.name = "TaskName"; task.setDone(false); mRootNode.add(task);}

        {
            WhatsaiDir tg = new WhatsaiDir();
            tg.name = ComDef.XML_TAG_DIR;
            mRootNode.add(tg);
            for (int i = 0; i < 19; i++) {
                Task task = new Task();
                task.name = "SubTask" + i;
                task.setDone(i % 2 != 0);
                tg.list.add(task);
            }
            {
                WhatsaiDir tg22 = new WhatsaiDir();
                tg22.name = "taskgroup22";
                tg.add(tg22);
                for (int i = 0; i < 5; i++) {
                    Task task = new Task();
                    task.name = "SubTask" + i;
                    task.setDone(i % 2 != 0);
                    tg22.list.add(task);
                }
            }
        }

        {Task task = new Task(); task.name = "TaskName"; task.setDone(false); mRootNode.add(task);}
        {Task task = new Task(); task.name = "TaskName"; task.setDone(true); mRootNode.add(task);}
        {Task task = new Task(); task.name = "TaskName"; task.setDone(true); mRootNode.add(task);}

        {
            WhatsaiDir tg = new WhatsaiDir();
            tg.name = "taskgroup2";
            mRootNode.add(tg);
            for (int i = 0; i < 9; i++) {
                Task task = new Task();
                task.name = "SubTask" + i;
                task.setDone(i % 2 == 0);
                tg.list.add(task);
            }
        }
    }
    //*/
}
