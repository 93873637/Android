package com.liz.multidialerdaemon.logic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.liz.androidutils.LogUtils;
import com.liz.multidialerdaemon.app.DaemonReceiver;
import com.liz.multidialerdaemon.app.ThisApp;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.liz.multidialerdaemon.logic.ComDef.DAEMON_TIMER_DELAY;
import static com.liz.multidialerdaemon.logic.ComDef.DAEMON_TIMER_PERIOD;

/**
 * DataLogic:
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class DataLogic {

    private static Timer mDaemonTimer;
    public static int mLifeCount = ComDef.MIN_LIFE_COUNT;

    public static void init() {
        startReceiver();
        startDaemonTimer();
    }

    private static void startReceiver() {
        IntentFilter intentFilterMultiDialer = new IntentFilter();
        intentFilterMultiDialer.addAction(ComDef.LIFE_BROADCAST_MSG);
        DaemonReceiver myReceiver = new DaemonReceiver();
        ThisApp.getAppContext().registerReceiver(myReceiver, intentFilterMultiDialer);
    }

    public static void refreshLife() {
        mLifeCount = ComDef.MAX_LIFE_COUNT;
    }

    private static void onDaemonCheck() {
        LogUtils.d("onDaemonCheck: mLifeCount = " + mLifeCount);
        mLifeCount --;
        if (mLifeCount <= ComDef.MIN_LIFE_COUNT) {
            mLifeCount = ComDef.MIN_LIFE_COUNT;
            startMultiDialerApp(ThisApp.getAppContext());
        }
    }

    public static void startMultiDialerApp(Context context) {
        LogUtils.d("startMultiDialerApp");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        ComponentName componentName = new ComponentName("com.liz.multidialer", "com.liz.multidialer.ui.MainActivity");
        intent.setComponent(componentName);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("MULTIDIALER_DAEMON_ACTION", "START");
        context.startActivity(intent);
    }

    private static void startDaemonTimer() {
        //detect and update NV21 files of /sdcard/camera
        mDaemonTimer = new Timer();
        mDaemonTimer.schedule(new TimerTask() {
            public void run() {
                onDaemonCheck();
            }
        }, DAEMON_TIMER_DELAY, DAEMON_TIMER_PERIOD);
    }

    private static void stopDaemonTimer() {
        if (mDaemonTimer != null) {
            mDaemonTimer.cancel();
            mDaemonTimer = null;
        }
    }
}
