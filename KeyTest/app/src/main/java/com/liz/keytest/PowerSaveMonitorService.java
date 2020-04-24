package com.liz.keytest;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import com.liz.androidutils.LogUtils;

public class PowerSaveMonitorService extends AccessibilityService {

    private static final String TAG = PowerSaveMonitorService.class.getSimpleName();

    private static PowerSaveMonitorService mPowerSaveMonitorService = null;

    public static void start(Context context) {
        if (mPowerSaveMonitorService == null) {
            Intent intent = new Intent(context, PowerSaveMonitorService.class);
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    private static Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                default:
                    LogUtils.e("ERROR: FloatingButtonService: Unknown message code: " + msg.what);
                    break;
            }
            return false;
        }
    });

    //初始化Messenger，用于消息发送和接收
    private static Messenger sMessenger= new Messenger(mHandler);

    private static ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sMessenger = null;
        }
    };

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.i(TAG, "onKeyEvent");
        int key = event.getKeyCode();
        switch(key){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.i(TAG, "KEYCODE_VOLUME_DOWN");
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.i(TAG, "KEYCODE_VOLUME_UP");
                break;
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String pkgName = event.getPackageName().toString();
        String className = event.getClassName().toString();
        int eventType = event.getEventType();

        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                break;
        }
    }
}
