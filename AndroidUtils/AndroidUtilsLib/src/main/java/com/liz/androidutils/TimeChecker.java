package com.liz.androidutils;

import android.graphics.Bitmap;

@SuppressWarnings("unused")
public class TimeChecker {

    private String mID;
    private long mBegin;  //begin check time;
    private long mLast;  //last check time

    public TimeChecker(String id){
        mID = id;
        mBegin = System.currentTimeMillis();
        mLast = mBegin;
        checkLog("begin...");
    }

    public void checkPoint(String pointName) {
        long current = System.currentTimeMillis();
        long total = current - mBegin;
        long last = current - mLast;
        checkLog(pointName + ", TIME USED: " + last + "/" + total);
        mLast = current;
    }

    private void checkLog(String msg) {
        LogUtils.d("***TC[" + mID + "]: " + msg);
    }
}
