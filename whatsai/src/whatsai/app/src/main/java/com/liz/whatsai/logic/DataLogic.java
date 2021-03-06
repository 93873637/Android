package com.liz.whatsai.logic;

import android.content.Context;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.app.MyApp;
import com.liz.whatsai.storage.WhatsaiStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * DataLogic:
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class DataLogic extends WhatsaiStorage {
    private static Node mActiveNode = null;

    private static Map<String, Node> mAlarmMap = new HashMap<String, Node>();
    private static int mInitState = ComDef.INIT_STATUS_LOADING;

    public static void init() {
        LogUtils.trace();
        if (mInitState == ComDef.INIT_STATUS_OK) {
            LogUtils.ti("DataLogic already initialized");
        }
        else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initThread();
                }
            }).start();
        }
    }

    public static void release() {
        WhatsaiStorage.releaseStorage();
    }

    private static void initThread() {
        LogUtils.trace();
        mInitState = ComDef.INIT_STATUS_LOADING;
        if (!WhatsaiStorage.initStorage()) {
            LogUtils.e("DataLogic: WhatsaiStorage init failed.");
            mInitState = ComDef.INIT_STATUS_FAILED;
        }
        else {
            mActiveNode = getRootNode();
            WSAudio.init();
            startAlarmTimer();
            mInitState = ComDef.INIT_STATUS_OK;
        }
    }

    public static int getInitStatus() {
        return mInitState;
    }

    private static void startAlarmTimer() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                DataLogic.checkAlarm(MyApp.getAppContext());
            }
        }, ComDef.TASK_LIST_ALARM_DELAY, ComDef.TASK_LIST_ALARM_TIMER);
    }

    static void registerAlarm(Node node) {
        if (node.isRemindValid()) {
            mAlarmMap.put(node.getRemindTime(), node);
        }
    }

    public static Node getNodeByAlarmTime(String remindTime) {
        return mAlarmMap.get(remindTime);
    }

    private static void checkAlarm(Context context) {
        SimpleDateFormat format = new SimpleDateFormat("HHmmss");
        String strDateTime = format.format(new Date(System.currentTimeMillis()));
        Node node = mAlarmMap.get(strDateTime);
        if (node != null) {
            //###@:
//            Intent activityIntent = new Intent(context, AlarmActivity.class);
//            activityIntent.putExtra(ComDef.ALARM_TAG, node.getRemindTime());
//            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(activityIntent);
        }
    }

    public static void createNode(String name, int type) {
        switch (type) {
            case ComDef.NODE_TYPE_DIR:
                DataLogic.createDir(name);
                break;
            case ComDef.NODE_TYPE_TASK:
                DataLogic.createTask(name);
                break;
            case ComDef.NODE_TYPE_TASKGROUP:
                DataLogic.createTaskGroup(name);
                break;
            case ComDef.NODE_TYPE_TEXT:
                DataLogic.createText(name);
                break;
            default:
                DataLogic.createFile(name);
                break;
        }
    }

    private static void createFile(String name) {
        Node node = new WSFile(name);
        mActiveNode.add(node);
        setDirty();
    }

    private static void createDir(String name) {
        Node node = new WSDir(name);
        mActiveNode.add(node);
        setDirty();
    }

    private static void createText(String name) {
        Node node = new WSText(name);
        mActiveNode.add(node);
        setDirty();
    }

    private static void createTask(String name) {
        Node node = new Task(name);
        mActiveNode.add(node);
        setDirty();
    }

    private static void createTaskGroup(String name) {
        Node node = new TaskGroup(name);
        mActiveNode.add(node);
        setDirty();
    }

    public static void delTask(int pos) {
        mActiveNode.remove(pos);
        setDirty();
    }

    public static void setActiveNode(Node node) {
        mActiveNode = node;
    }

    public static Node get(int pos) {
        return mActiveNode.get(pos);
    }

    public static List<Node> getDataList() {
        if (mActiveNode == null) {
            LogUtils.e("ERROR: active node null");
            return null;
        }
        else {
            return mActiveNode.getList();
        }
    }

    public static String getPath() {
        return mActiveNode.getPath();
    }

    public static boolean isRootActive() {
        return (mActiveNode.getParent() == null);
    }

    public static void goUpperNode() {
        mActiveNode = mActiveNode.getParent();
    }
}
