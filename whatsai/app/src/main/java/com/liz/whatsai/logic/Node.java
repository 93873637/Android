package com.liz.whatsai.logic;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.liz.androidutils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Node.java
 * Created by admin on 2018/8/30.
 */

@SuppressWarnings("unused, WeakerAccess")
public abstract class Node implements Comparable<Node> {

    private String name;
    private String detail;
    private Node parent;
    private String remind_string;
    private int remind_type;
    private RemindTime remind_time;
    private List<Node> list;
    private long sync_time;  //time for sync to cloud

    Node() {
        init();
    }

    Node(String name) {
        init();
        this.name = name;
    }

    private void init() {
        parent = null;
        name = "";
        detail = "";
        remind_string = "";
        remind_type = ComDef.REMIND_TYPE_INVALID;
        remind_time = new RemindTime();
        list = new ArrayList<>();
        sync_time = 0;
    }

    public String getName() {
        return name;
    }

    public String getNameEx() {
        if (isRemindValid()) {
            return name + "(" + this.getRemindString() + ")";
        }
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

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        if (TextUtils.isEmpty(detail)) {
            this.detail = "";
        }
        else {
            this.detail = detail;
        }
    }

    public void setParent(Node node) {
        this.parent = node;
    }

    public Node getParent() {
        return parent;
    }

    public long getSyncTime() {
        return sync_time;
    }

    public void setSyncTime(long syncTime) {
        sync_time = syncTime;
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

    public int getRemindType() {
        return this.remind_type;
    }

    public String getRemindString() {
        return remind_string;
    }

    public void setRemindString(String mRemindString) {
        if (TextUtils.equals(mRemindString, this.remind_string)) {
            LogUtils.v("remind not change");
            return;
        }

        if (TextUtils.isEmpty(mRemindString)) {
            this.remind_string = "";
            this.remind_time = null;
            return;
        }

        this.remind_string = mRemindString;
        parseRemind(mRemindString);
    }

    public String getRemindTime() {
        if (isRemindValid()) {
            return remind_time.timeFormatString();
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
        int comp = this.remind_time.compareTo(node.remind_time);
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
            remind_type = ComDef.REMIND_TYPE_INVALID;
            return;
        }

        try {
            int hh = Integer.parseInt(timeStrings[0]);
            if (hh < 0 || hh > 23) {
                LogUtils.e("Remind:parseRemind: invalid hh=" + hh);
                return;
            }
            remind_time.hour = hh;

            int mm = Integer.parseInt(timeStrings[1]);
            if (mm < 0 || mm > 59) {
                LogUtils.e("Remind:parseRemind: invalid mm=" + mm);
                return;
            }
            remind_time.minute = mm;

            if (timeStrings.length > 2) {
                int ss = Integer.parseInt(timeStrings[2]);
                if (ss >= 0 && ss <= 59) {
                    remind_time.second = ss;
                }
            }
        }
        catch(NumberFormatException ex){
            LogUtils.e("Remind:parseRemind: NumberFormatException.");
            remind_type = ComDef.REMIND_TYPE_INVALID;
            return;
        }

        LogUtils.d("Remind:parseRemind: get remind time at " + remind_time.hour + ":" + remind_time.minute + ":" + remind_time.second);
        remind_type = ComDef.REMIND_TYPE_DAILY_TIME;
    }

    public boolean isRemindValid() {
        return (remind_type != ComDef.REMIND_TYPE_INVALID);
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
        return list;
    }

    public int getChildNumber() {
        if (list == null) {
            return 0;
        }
        else {
            return list.size();
        }
    }

    public Node getChild(int i) {
        if (i >= 0 && i < list.size()) {
            return list.get(i);
        }
        else {
            return null;
        }
    }

    public abstract int getType();

    public void add(Node node) {
        list.add(node);
        node.setParent(this);
    }

    public void remove(int pos) {
        if (pos >= 0 && pos < list.size()) {
            list.remove(pos);
        }
    }

    public Node get(int pos) {
        if (pos >= 0 && pos < list.size()) {
            return list.get(pos);
        }
        else {
            return null;
        }
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
