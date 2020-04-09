package com.liz.whatsai.logic;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.app.MyApp;
import com.liz.whatsai.ui.WSNotifier;


public class WSListenService extends Service {

    public static final String LISTEN_SERVICE_ACTION = "LISTEN_SERVICE_ACTION";
    public static final String START_LISTENING = "START_LISTENING";
    public static final String STOP_LISTENING = "STOP_LISTENING";

    //////////////////////////////////////////////////////////////////////////////////
    // APIs

    public static void start() {
        LogUtils.trace();
        Context context = MyApp.getAppContext();
        Intent intent = new Intent(context, WSListenService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void stop() {
        LogUtils.trace();
        Context context = MyApp.getAppContext();
        context.unbindService(mServiceConnection);
        context.stopService(new Intent(context, WSListenService.class));
    }

    public static void startListening() {
        LogUtils.trace();
        Context context = MyApp.getAppContext();
        Intent intent = new Intent(context, WSListenService.class);
        intent.putExtra(LISTEN_SERVICE_ACTION, START_LISTENING);
        context.startService(intent);
    }

    public static void stopListening() {
        LogUtils.trace();
        Context context = MyApp.getAppContext();
        Intent intent = new Intent(context, WSListenService.class);
        intent.putExtra(LISTEN_SERVICE_ACTION, STOP_LISTENING);
        context.startService(intent);
    }

    public static boolean isListening() {
        return WSRecorder.inst().isListening();
    }

    // APIs
    //////////////////////////////////////////////////////////////////////////////////

    private ServiceBinder mBinder = new ServiceBinder();
    private class ServiceBinder extends Binder {
        private WSListenService getService() {
            return WSListenService.this;
        }
    }

    private static WSListenService mService = null;
    private static ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.trace();
            mService = ((ServiceBinder) service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.trace();
        }
    };

    //////////////////////////////////////////////////////////////////////////////////
    // The Service Class

    @Override
    public void onCreate() {
        LogUtils.trace();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.trace(this.toString());

        if (WSNotifier.getNotification() != null) {
            WSListenService.this.startForeground(WSNotifier.NOTICE_ID_TYPE_0, WSNotifier.getNotification());
        } else {
            LogUtils.tw2("no notification to startForeground");
        }

        if (intent == null) {
            LogUtils.td("onStartCommand: intent null");
        }
        else {
            String action = intent.getStringExtra(LISTEN_SERVICE_ACTION);
            LogUtils.td("onStartCommand: action = " + action);
            if (TextUtils.equals(action, START_LISTENING)) {
                WSRecorder.inst().startListening();
            } else if (TextUtils.equals(action, STOP_LISTENING)) {
                WSRecorder.inst().stopListening();
            } else {
                LogUtils.td("unhandled action " + action);
            }
        }

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

    @Override
    public void onDestroy() {
        LogUtils.trace();
        super.onDestroy();
    }
}
