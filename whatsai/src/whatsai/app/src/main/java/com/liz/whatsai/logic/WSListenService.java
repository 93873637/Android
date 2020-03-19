package com.liz.whatsai.logic;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.liz.androidutils.LogUtils;


public class WSListenService extends Service {

    //////////////////////////////////////////////////////////////////////////////////
    // APIs

    public static void startService(Context context) {
        Intent intent = new Intent(context, WSListenService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void stopService(Context context) {
        context.unbindService(mServiceConnection);
        Intent intent = new Intent(context, WSListenService.class);
        context.stopService(intent);
    }

    // APIs
    //////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Singleton

    public static WSListenService inst() {
        if (mWSListenService == null) {
            mWSListenService = new WSListenService();
        }
        return mWSListenService;
    }

    // the one and only object instance
    private static WSListenService mWSListenService;

    // private constructor for singleton
    private WSListenService() {
        LogUtils.trace();
    }

    // Singleton
    ///////////////////////////////////////////////////////////////////////////////////////////////

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
    public boolean onUnbind(Intent intent) {
        LogUtils.trace();
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.trace();
        return mBinder;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Class WSListenService

    //todo:
}
