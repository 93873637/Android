package com.liz.whatsai.logic;

import com.liz.androidutils.LogUtils;

/**
 * Task.java
 * Created by liz on 2018/9/17.
 */

public class Task extends WSFile {
    private boolean done;

    public Task() {
        super();
        done = false;
    }

    public Task(String name) {
        super(name);
        done = false;
    }

    public int getTaskNumber() {
        return 1;  //task number only one of itself
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public int getType() {
        return ComDef.NODE_TYPE_TASK;
    }

    @Override
    public void setDone(boolean done) {
        if (this.done == done) {
            LogUtils.d("Task: setDone: not change");
            return;
        }
        setDirty();
    }

    @Override
    public void reverseDone() {
        this.done = !this.done;
        setDirty();
    }
}
