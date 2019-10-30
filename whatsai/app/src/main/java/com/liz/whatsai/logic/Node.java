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
    private String summary;
    private String content;

    //if set, need password to open
    private String password;

    private String remind_string;
    private int remind_type;
    private RemindTime remind_time;

    private String attach_file;

    private Node parent;
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
        summary = "";
        remind_string = "";
        remind_type = ComDef.REMIND_TYPE_INVALID;
        remind_time = new RemindTime();
        list = new ArrayList<>();
        sync_time = 0;
    }

    protected void setDirty() {
        DataLogic.setDirty();
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
        if (TextUtils.equals(name, this.name)) {
            LogUtils.d("Node: setName: not change");
            return;
        }
        if (TextUtils.isEmpty(name)) {
            this.name = "";
        } else {
            this.name = name;
        }
        setDirty();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        if (TextUtils.equals(summary, this.summary)) {
            LogUtils.d("Node: setSummary: not change");
            return;
        }
        if (TextUtils.isEmpty(summary)) {
            this.summary = "";
        } else {
            this.summary = summary;
        }
        setDirty();
    }

    public boolean hasSummary() {
        return !TextUtils.isEmpty(summary);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (TextUtils.equals(content, this.content)) {
            LogUtils.d("Node: setContent: not change");
            return;
        }
        if (TextUtils.isEmpty(content)) {
            this.content = "";
        } else {
            this.content = content;
        }
        setDirty();
    }

    public boolean hasContent() {
        return !TextUtils.isEmpty(content);
    }

    public void setParent(Node node) {
        if (this.parent == node) {
            LogUtils.d("Node: setParent: not change");
            return;
        }
        this.parent = node;
        setDirty();
    }

    public Node getParent() {
        return parent;
    }

    public long getSyncTime() {
        return sync_time;
    }

    public void setSyncTime(long sync_time) {
        if (this.sync_time == sync_time) {
            LogUtils.d("Node: setSyncTime: not change");
            return;
        }
        this.sync_time = sync_time;
        setDirty();
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        if (TextUtils.equals(this.password, password)) {
            LogUtils.d("Node: setPassword: not change");
            return;
        }
        this.password = password;
        setDirty();
    }

    public boolean hasPassword() {
        return !TextUtils.isEmpty(password);
    }

    public boolean samePassword(String password) {
        if (TextUtils.isEmpty(this.password)) {
            return TextUtils.isEmpty(password);
        }
        else {
            return TextUtils.equals(this.password, password);
        }
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

    public void setRemindString(String remind_string) {
        if (TextUtils.equals(remind_string, this.remind_string)) {
            LogUtils.v("remind not change");
            return;
        }

        if (TextUtils.isEmpty(remind_string)) {
            this.remind_string = "";
            this.remind_time = null;
        }
        else {
            this.remind_string = remind_string;
            parseRemind(remind_string);
        }

        setDirty();
    }

    public String getRemindTime() {
        if (isRemindValid()) {
            return remind_time.timeFormatString();
        } else {
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
        } catch (NumberFormatException ex) {
            LogUtils.e("Remind:parseRemind: NumberFormatException.");
            remind_type = ComDef.REMIND_TYPE_INVALID;
            return;
        }

        LogUtils.d("Remind:parseRemind: get remind time at " + remind_time.hour + ":" + remind_time.minute + ":" + remind_time.second);
        remind_type = ComDef.REMIND_TYPE_DAILY_TIME;
    }

    public String getAttachFile() {
        return attach_file;
    }

    public void setAttachFile(String attachFile) {
        attach_file = attachFile;
    }

    public boolean isRemindValid() {
        return (remind_type != ComDef.REMIND_TYPE_INVALID);
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isTask() {
        return this.getType() == ComDef.NODE_TYPE_TASK;
    }

    public boolean isDir() {
        return (this.getType() & 0x1) == ComDef.NODE_TYPE_DIR;
    }

    public static Node createNode(int type) {
        switch (type) {
            case ComDef.NODE_TYPE_TASK:
                return new Task();
            case ComDef.NODE_TYPE_TEXT:
                return new WhatsaiText();
            case ComDef.NODE_TYPE_DIR:
                return new WhatsaiDir();
            case ComDef.NODE_TYPE_TASKGROUP:
                return new TaskGroup();
            default:
                return new WhatsaiFile();
        }
    }

    public List<Node> getList() {
        return list;
    }

    public int getChildNumber() {
        if (list == null) {
            return 0;
        } else {
            return list.size();
        }
    }

    public Node getChild(int i) {
        if (i >= 0 && i < list.size()) {
            return list.get(i);
        } else {
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
        } else {
            return null;
        }
    }

    public String getPath() {
        Node node = this;
        StringBuilder path = new StringBuilder(node.getName());
        while (node.parent != null) {
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
