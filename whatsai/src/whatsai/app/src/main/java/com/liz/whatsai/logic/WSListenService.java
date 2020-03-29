package com.liz.whatsai.logic;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.app.MyApp;
import com.liz.whatsai.ui.WSNotifier;


public class WSListenService extends Service {

    // the one and only object instance
    private static WSListenService mWSListenService = new WSListenService();

    //////////////////////////////////////////////////////////////////////////////////
    // APIs

    public static void start() {
        Context context = MyApp.getAppContext();
        context.startService(new Intent(context, WSListenService.class));
    }

    public static void stop() {
        Context context = MyApp.getAppContext();
        context.stopService(new Intent(context, WSListenService.class));
    }

    public static void switchOnOff() {
        mWSListenService._switchOnOff();
    }

    // APIs
    //////////////////////////////////////////////////////////////////////////////////

    private ServiceBinder mBinder = new ServiceBinder();
    private class ServiceBinder extends Binder {
        private WSListenService getService() {
            return WSListenService.this;
        }
    }

    private static ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.trace();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.trace();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.trace();
        if (WSNotifier.getNotification() != null) {
            WSListenService.this.startForeground(WSNotifier.NOTICE_ID_TYPE_0, WSNotifier.getNotification());
        }
        else {
            LogUtils.tw2("no notification to startForeground service");
        }
        WSRecorder.inst().startListening();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.trace();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.trace();
        return super.onUnbind(intent);
    }

    private  void _switchOnOff() {
        WSRecorder.inst().switchListening();
    }
}
