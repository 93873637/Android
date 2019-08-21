package com.liz.whatsai.logic;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.liz.whatsai.utils.LogUtils;

import java.util.List;

/**
 * Node.java
 * Created by admin on 2018/8/30.
 */

@SuppressWarnings("unused")
public abstract class Node implements Comparable<Node> {
    private Node parent = null;
    private String name = "";
    public String detail = "";

    private String remindString;
    int remindType;
    private RemindTime remindTime;

    Node() {
        this.name = "";
        this.detail = "";
        remindString = "";
        remindType = ComDef.REMIND_TYPE_INVALID;
        remindTime = new RemindTime();
    }

    Node(String name) {
        this.name = name;
        this.detail = "";
        remindString = "";
        remindType = ComDef.REMIND_TYPE_INVALID;
        remindTime = new RemindTime();
    }

    public void setParent(Node node) {
        this.parent = node;
    }

    public Node getParent() {
        return parent;
    }

    public void incTaskNumber(int num) {
        //TODO: Override this method
    }

    public void decTaskNumber(int num) {
        //TODO: Override this method
    }

    public int getTaskNumber() {
        //TODO: Override this method
        return 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (TextUtils.isEmpty(name)) {
            this.name = "";
        }
        else {
            this.name = name;
        }
    }

    public String getNameEx() {
        if (isRemindValid()) {
            return name + "(" + this.getRemindString() + ")";
        }
        return name;
    }

    public String getRemindString() {
        return remindString;
    }

    public void setRemindString(String remindString) {
        if (TextUtils.equals(remindString, this.remindString)) {
            LogUtils.v("remind not change");
            return;
        }

        if (TextUtils.isEmpty(remindString)) {
            this.remindString = "";
            this.remindTime = null;
            return;
        }

        this.remindString = remindString;
        parseRemind(remindString);
    }

    String getRemindTime() {
        if (isRemindValid()) {
            return remindTime.timeFormatString();
        }
        else {
            return "";
        }
    }

    @Override
    public int compareTo(@NonNull Node node) {
        if (!this.isRemindValid() && !node.isRemindValid()) {
            return compareName(node.name);
        }

        if (this.isRemindValid() && !node.isRemindValid()) {
            return 1;
        }

        if (!this.isRemindValid() && node.isRemindValid()) {
            return -1;
        }

        //sort by remind time: this.isRemindValid && node.isRemindValid
        int comp = this.remindTime.compareTo(node.remindTime);
        return (comp == 0) ? compareName(node.name) : comp;
    }

    private int compareName(String name) {
        return this.name.charAt(0) - name.charAt(0);
    }

    private void parseRemind(String remind) {
        String[] words = remind.split(" ");

        LogUtils.d("Remind:parseRemind: get words, len = " + words.length);
        if (words.length < 1) {
            LogUtils.d("Remind:parseRemind: no words.");
            return;
        }

        //format as hh:mm
        String[] timeStrings = words[0].split(":");
        if (timeStrings.length < 1) {
            LogUtils.e("Remind:parseRemind: unknown remind type.");
            remindType = ComDef.REMIND_TYPE_INVALID;
            return;
        }

        try {
            int hh = Integer.parseInt(timeStrings[0]);
            if (hh < 0 || hh > 23) {
                LogUtils.e("Remind:parseRemind: invalid hh=" + hh);
                return;
            }
            remindTime.hour = hh;

            int mm = Integer.parseInt(timeStrings[1]);
            if (mm < 0 || mm > 59) {
                LogUtils.e("Remind:parseRemind: invalid mm=" + mm);
                return;
            }
            remindTime.minute = mm;

            if (timeStrings.length > 2) {
                int ss = Integer.parseInt(timeStrings[2]);
                if (ss >= 0 && ss <= 59) {
                    remindTime.second = ss;
                }
            }
        }
        catch(NumberFormatException ex){
            LogUtils.e("Remind:parseRemind: NumberFormatException.");
            remindType = ComDef.REMIND_TYPE_INVALID;
            return;
        }

        LogUtils.d("Remind:parseRemind: get remind time at " + remindTime.hour + ":" + remindTime.minute + ":" + remindTime.second);
        remindType = ComDef.REMIND_TYPE_DAILY_TIME;
    }

    public boolean isRemindValid() {
        return (remindType != ComDef.REMIND_TYPE_INVALID);
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isTask() {
        return this.getType() == ComDef.NODE_TYPE_FILE;
    }

    public boolean isDir() {
        return this.getType() == ComDef.NODE_TYPE_DIR;
    }

    public List<Node> getList() {
        return null;
    }

    public int getType() {
        //TODO: Override this method
        return ComDef.NODE_TYPE_UNKNOWN;
    }

    public void add(Node node) {
        //TODO: Override this method
    }

    public void remove(int pos) {
        //TODO: Override this method
    }

    public Node get(int pos) {
        //TODO: Override this method
        return null;
    }

    public String getPath() {
        Node node = this;
        StringBuilder path = new StringBuilder(node.getName());
        while(node.parent != null) {
            path.insert(0, node.parent.name + "/");
            node = node.parent;
        }
        return path.toString();
    }

    public boolean isDone() {
        //TODO: Override this method
        return false;
    }

    public void setDone(boolean done) {
        //TODO: Override this method
    }

    public void reverseDone() {
        //TODO: Override this method
    }
}
