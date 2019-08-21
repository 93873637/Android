package com.liz.whatsai.logic;

import android.content.Context;
import android.content.Intent;

import com.liz.whatsai.app.ThisApp;

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
public class DataLogic extends Storage {
    private static Node mActiveNode = null;

    private static Map<String, Node> mAlarmMap = new HashMap<String, Node>();

    public static void init() {
        Storage.init();
        mActiveNode = getRootNode();
        startAlarmTimer();
    }

    private static void startAlarmTimer() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                DataLogic.checkAlarm(ThisApp.getAppContext());
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

    public static void addTask(String name) {
        Node node = new Task(name);
        mActiveNode.add(node);
        setDirty(true);
    }

    public static void addTaskGroup(String name) {
        Node node = new TaskGroup(name);
        mActiveNode.add(node);
        setDirty(true);
    }

    public static void delTask(int pos) {
        mActiveNode.remove(pos);
        setDirty(true);
    }

    public static void setActiveNode(Node node) {
        mActiveNode = node;
    }

    public static Node get(int pos) {
        return mActiveNode.get(pos);
    }

    public static List<Node> getDataList() {
        return mActiveNode.getList();
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
