package com.liz.multicamera.logic;

import android.text.TextUtils;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.SysUtils;
import com.liz.multicamera.app.ThisApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.liz.multicamera.logic.ComDef.NIGHT_MODE_DISABLE;
import static com.liz.multicamera.logic.ComDef.NIGHT_MODE_ENABLE;

/**
 * NightMode:
 * Created by liz on 2018/12/29.
 */

public class NightMode {
    private static ArrayList<ExpoParam> mExpoParams = new ArrayList<>();
    private static int mPictureNum = 0;     //picture number for once shot, a static value read from property
    private static int mPictureIndex = 0;   //picture index during night mode shooting
    private static int mExposureIndex = 0;  //exposure parameter index during night mode shooting
    private static int mFrameIndex = 0;     //frame index of current exposure parameter during night mode shooting

    private static int mFrameNum = Integer.parseInt(ComDef.DEFAULT_NIGHT_MODE_BURSTNUM);
    private static String mExpTab = ComDef.DEFAULT_NIGHT_MODE_EXPTAB;

    public static void init() {
        LogUtils.d("NightMode.loadExposureTable: init");
        loadExposureParams();
        resetShootingParams();
    }

    public static void onStartShooting() {
        resetShootingParams();

        //set night mode properties for low layer running
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_ENABLE, NIGHT_MODE_ENABLE);
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_BURSTNUM, "" + mFrameNum);
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_EXPTAB, mExpTab);
    }

    public static void onStopShooting() {
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_ENABLE, NIGHT_MODE_DISABLE);
        closeLongExposureMode();
    }

    private static void loadTestParams() {
        mExpoParams.clear();

        mFrameNum = Integer.parseInt(
                SysUtils.getSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_BURSTNUM, ComDef.DEFAULT_NIGHT_MODE_BURSTNUM));
        LogUtils.d("NightMode.loadTestParams: mFrameNum=" + mFrameNum);

        //NOTE: the combo exposure number is DEFAULT_ISO_ARRAY.len * DEFAULT_EXP_ARRAY.len
        final int[] ISO_ARRAY = {100, 200, 400, 800, 1200, 1600, 2200, 3000, 4000, 5000, 7000, 10000, 15000, 20000};
        final int[] EXP_ARRAY = {10, 20, 33, 67, 125, 167, 200, 266};
        //private static final int[] DEFAULT_ISO_ARRAY = {200, 400, 800, 1600, 2200, 3600};
        //private static final int[] DEFAULT_EXP_ARRAY = {33, 67, 128, 266};
        //private static final int[] DEFAULT_ISO_ARRAY = {1600, 2200};
        //private static final int[] DEFAULT_EXP_ARRAY = {33, 67, 128};

        for (int iso : ISO_ARRAY) {
            for (int exp : EXP_ARRAY) {
                ExpoParam expoParam = new ExpoParam(iso, exp, mFrameNum);
                mExpoParams.add(expoParam);
                mPictureNum += expoParam.frame_num;
            }
        }

        Collections.sort(mExpoParams, new Comparator< ExpoParam >() {
             @Override
             public int compare(ExpoParam lhs, ExpoParam rhs) {
                 return (lhs.expVal() > rhs.expVal()) ? 1 : -1;
             }
         });

        LogUtils.d("NightMode.loadExposureTable: params num = " + mExpoParams.size() + ", picture total = " + mPictureNum);
    }

    public static void loadExposureParams() {
        mFrameNum = Integer.parseInt(
                SysUtils.getSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_BURSTNUM, ComDef.DEFAULT_NIGHT_MODE_BURSTNUM));
        LogUtils.d("NightMode.loadExposureParams: mFrameNum=" + mFrameNum);

        mExpTab = SysUtils.getSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_EXPTAB, ComDef.DEFAULT_NIGHT_MODE_EXPTAB);
        LogUtils.d("NightMode.loadExposureParams: mExpTab=" + mExpTab);
        loadExposureTable(mExpTab);
    }

    public static int getPictureIndex() {
        return mPictureIndex;
    }

    private static void loadExposureTable(String expStr) {
        LogUtils.d("NightMode.loadExposureTable: expStr=" + expStr);
        mExpoParams.clear();

        if (TextUtils.isEmpty(expStr)) {
            LogUtils.d("NightMode.loadExposureTable: invalid expStr=" + expStr);
            return;
        }

        String[] expList = expStr.split(",");
        mPictureNum = 0;
        for (String expItem:expList) {
            String[] exp = expItem.split("/");
            if (exp == null || exp.length != 2) {
                LogUtils.e("NightMode.loadExposureTable: invalid expStr=" + expItem);
            }
            else {
                ExpoParam expoParam = new ExpoParam(Integer.parseInt(exp[0]), Float.parseFloat(exp[1]), mFrameNum);
                mExpoParams.add(expoParam);
                mPictureNum += expoParam.frame_num;
            }
        }
        LogUtils.d("NightMode.loadExposureTable: params num = " + mExpoParams.size() + ", picture total = " + mPictureNum);
    }

    private static void resetShootingParams() {
        mPictureIndex = 0;
        mExposureIndex = 0;
        mFrameIndex = 0;
    }

    public static int getCurrentISO() {
        return mExpoParams.get(mExposureIndex).iso_value;
    }

    public static float getCurrentExposureTime() {
        return mExpoParams.get(mExposureIndex).exposure_time;
    }

    public static void openLongExposureMode(float expTime) {
        LogUtils.d("NightMode.openLongExposureMode: expTime=" + expTime);
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_LONG_EXPOSURE, "1");
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_EXPTIME, "" + (int)expTime);
    }

    public static void closeLongExposureMode() {
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_LONG_EXPOSURE, ComDef.DEFAULT_NIGHT_MODE_LONG_EXPOSURE);
        SysUtils.setSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_EXPTIME, ComDef.DEFAULT_NIGHT_MODE_EXPTIME);
    }

    public static int getNextAction() {
        LogUtils.d("NightMode.getNextAction: mPictureIndex=" + mPictureIndex + ", mExposureIndex=" + mExposureIndex + ", mFrameIndex=" + mFrameIndex);

        //check in case exception
        if (mPictureIndex >= mPictureNum) {
            LogUtils.e("ERROR: mPictureIndex(" + mPictureIndex + ") exceed mPictureNum(" + mPictureNum + ")");
            return ComDef.NIGHT_MODE_SHOT_ERROR;
        }
        if (mExposureIndex >= mExpoParams.size()) {
            LogUtils.e("ERROR: mExposureIndex(" + mExposureIndex + ") exceed mExpoParams.size(" + mExpoParams.size() + ")");
            return ComDef.NIGHT_MODE_SHOT_ERROR;
        }
        if (mFrameIndex >= mExpoParams.get(mExposureIndex).frame_num) {
            LogUtils.e("ERROR: mFrameIndex(" + mFrameIndex + ") exceed mFrameNum(" + mExpoParams.get(mExposureIndex).frame_num + ")");
            return ComDef.NIGHT_MODE_SHOT_ERROR;
        }

        mPictureIndex++;
        mFrameIndex++;
        if (mFrameIndex == mExpoParams.get(mExposureIndex).frame_num) {
            mExposureIndex ++;
        }

        if (mPictureIndex == mPictureNum) {
            return ComDef.NIGHT_MODE_SHOT_FINISHED;
        }
        else {
            if (mFrameIndex < mExpoParams.get(mExposureIndex).frame_num){
                return ComDef.NIGHT_MODE_SHOT_CONTINUE;
            }
            else if (mFrameIndex == mExpoParams.get(mExposureIndex).frame_num) {
                mFrameIndex = 0;
                return ComDef.NIGHT_MODE_SHOT_CONTINUE_NEXT_EXPO;
            }
            else {  //mFrameIndex > frame_num
                LogUtils.e("ERROR: mFrameIndex(" + mFrameIndex + ") exceed mFrameNum(" + mExpoParams.get(mExposureIndex).frame_num + ")");
                return ComDef.NIGHT_MODE_SHOT_ERROR;
            }
        }
    }

    public static int getPictureTotal() {
        return mPictureNum;
    }

    public static boolean isNightModeEnable() {
        return TextUtils.equals(
                SysUtils.getSystemProperty(ThisApp.getAppContext(), ComDef.PROP_NIGHT_MODE_ENABLE, ComDef.DEFAULT_NIGHT_MODE_ENABLE),
                NIGHT_MODE_ENABLE);
    }

    public static String getProgressInfo(boolean shooting) {
        LogUtils.d("NightMode.getProgressInfo: mPictureIndex=" + mPictureIndex + ", mExposureIndex=" + mExposureIndex + ", mFrameIndex=" + mFrameIndex + ", shooting=" + shooting);
        StringBuilder sb = new StringBuilder();

        sb.append(shooting? mPictureIndex + 1: mPictureIndex);
        sb.append("/");
        sb.append(mPictureNum);
        sb.append("  ");

        sb.append(shooting?mExposureIndex + 1:mExposureIndex);
        sb.append("/");
        sb.append(mExpoParams.size());
        sb.append("  ");

        if (mExpoParams.size() > 0) {
            int index = mExposureIndex;
            if (index >= mExpoParams.size()) {
                index = mExpoParams.size() - 1;
            }
            ExpoParam curExpo = mExpoParams.get(index);

            sb.append(shooting? mFrameIndex + 1: mFrameIndex);
            sb.append("/");
            sb.append(curExpo.frame_num);
            sb.append("  ");

            sb.append(curExpo.iso_value);
            sb.append("/");
            sb.append(curExpo.exposure_time);
        }
        else {
            sb.append(mFrameIndex);
            sb.append("/");
            sb.append(0);  //unknown frame num
            sb.append("  ");

            sb.append(0);  //unknown iso value
            sb.append("/");
            sb.append(0);  //unknown exposure time
        }

        return  sb.toString();
    }
}
