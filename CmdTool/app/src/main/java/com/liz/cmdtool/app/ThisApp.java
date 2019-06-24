package com.liz.cmdtool.app;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.liz.cmdtool.utils.LogUtils;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * ThisApp.java
 * Created by liz on 18-1-8.
 */

@SuppressWarnings("unused")
public class ThisApp extends Application {
    private static ThisApp mAppInst;

    public static String mAppVersion = "";
    public static String mAppVersionShow = "";  //text show in log

    private static ArrayList<String> mCmdList;
    private static int mCmdPos = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d("ThisApp.onCreate");
        mAppInst = this;
        mCmdList = new ArrayList<>();

        //init cmd list
        addToCmdList("getprop persist.camera.night.enable");
        addToCmdList("setprop persist.camera.night.enable 1");
        addToCmdList("setprop persist.camera.night.enable 0");
        addToCmdList("getprop persist.camera.night.burstnum");
        addToCmdList("setprop persist.camera.night.burstnum 5");
        addToCmdList("getprop persist.camera.night.explist");

        addToCmdList("getenforce");
        addToCmdList("getprop sys.usb.config");

//        addToCmdList("getprop persist.camera.ExposureFusion.enable");
//        addToCmdList("setprop persist.camera.ExposureFusion.enable 1");
//        addToCmdList("setprop persist.camera.ExposureFusion.enable 0");
//
//        addToCmdList("getprop persist.camera.ExposureFusion.dump");
//        addToCmdList("getprop persist.camera.ExposureFusion.dump.jpg");
//        addToCmdList("getprop persist.camera.ExposureFusion.align");

        addToCmdList("getprop persist.camera.archdr.dump");
        addToCmdList("setprop persist.camera.archdr.dump 0");
        addToCmdList("setprop persist.camera.archdr.dump 1");

        addToCmdList("ls -l /sdcard");
        addToCmdList("ls -l /sdcard/DCIM/camera");
        addToCmdList("ls -l /sdcard/DCIM/camera | wc -l");
        addToCmdList("ls -l /data/misc/camera");
    }

    public static Context getAppContext() {
        return mAppInst;
    }

    public static String getCmd(int index) {
        if (index < 0 || index >= mCmdList.size()) {
            return "";
        }
        else {
            return mCmdList.get(index);
        }
    }

    public static void addToCmdList(String cmdStr) {
        if (!TextUtils.isEmpty(cmdStr)) {
            mCmdList.add(cmdStr);
            mCmdPos = mCmdList.size();  //move to last cmd
            LogUtils.d("addToCmdList: cmsStr=" + cmdStr + ", size=" + mCmdPos + "/" + mCmdList.size());
        }
    }

    public static String getPrevCmd() {
        String cmdStr = "";
        int listSize = mCmdList.size();
        LogUtils.d("getPrevCmd: size=" + listSize + ", pos=" + mCmdPos);
        if (mCmdList.size() > 0 && mCmdPos >= 0) {
            mCmdPos --;
            if (mCmdPos >= 0) {
                cmdStr = mCmdList.get(mCmdPos);
            }
        }
        return cmdStr;
    }

    public static String getNextCmd() {
        String cmdStr = "";
        int listSize = mCmdList.size();
        LogUtils.d("getNextCmd: size=" + listSize + ", pos=" + mCmdPos);
        if (listSize > 0 && mCmdPos < listSize) {
            mCmdPos ++;
            if (mCmdPos < listSize) {
                cmdStr = mCmdList.get(mCmdPos);
            }
        }
        return cmdStr;
    }

    public static boolean isSpecialCmd(String cmdStr) {
        return (cmdStr != null) && (
                "help".equalsIgnoreCase(cmdStr)
                        || "list".equalsIgnoreCase(cmdStr)
                        || "history".equalsIgnoreCase(cmdStr)
                        || "h".equalsIgnoreCase(cmdStr)
                        || "l".equalsIgnoreCase(cmdStr)
                        || "?".equalsIgnoreCase(cmdStr)
        );
    }

    public static boolean isNumeric(String str){
        return Pattern.compile("[0-9]*").matcher(str).matches();
    }

    public static String getCmdInfo() {
        StringBuilder info = new StringBuilder("");
        for (int i=0; i<mCmdList.size(); i++) {
            String cmdStr = (i+1) + "  " + mCmdList.get(i) + "\n";
            info.append(cmdStr);
        }
        return info.toString();
    }
}
