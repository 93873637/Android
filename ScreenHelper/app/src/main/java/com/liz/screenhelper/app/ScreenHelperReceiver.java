package com.liz.screenhelper.app;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.liz.androidutils.LogUtils;
import com.liz.screenhelper.logic.ComDef;
import com.liz.screenhelper.ui.ScreenCaptureFragment;

import static android.provider.Settings.System.ALARM_ALERT;

/**
 * ScreenHelperReceiver:
 * Created by liz on 2019/2/12.
 *
 * adb shell am broadcast -n com.liz.screenhelper/.ScreenHelperReceiver -a android.intent.action.DDZScreenShot
 */

public class ScreenHelperReceiver extends BroadcastReceiver {

    private static ScreenHelperReceiver mReceiver;

    private static ScreenHelperReceiver getReceiver() {
        if (mReceiver == null) {
            mReceiver = new ScreenHelperReceiver();
        }
        return mReceiver;
    }

    public static void init(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(ALARM_ALERT);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        context.registerReceiver(getReceiver(), filter);
    }

    public static void release(Context context) {
        context.unregisterReceiver(getReceiver());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            LogUtils.i("ScreenHelperReceiver: action null");
            return;
        }

        LogUtils.d("ScreenHelperReceiver: receive action = " + action);
        if (intent.getAction().equals(ComDef.SCREEN_HELPER_ACTION_CAPTURE_ONCE)) {
            ScreenCaptureFragment.captureOnce();
        } else {
            LogUtils.i("ScreenHelperReceiver.onReceive: unhandled action: " + intent.getAction());
        }
//
//        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {//这个监听wifi的打开与关闭，与wifi的连接无关
//            Log.v("my2", "收到WIFI_STATE_CHANGED_ACTION");
//            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1111);
//            switch (wifiState) {
//                case WifiManager.WIFI_STATE_DISABLED:
//                    Log.v("my2", "收到" + "WIFI_STATE_DISABLED");
//                    ScreenServer.sst.interrupt();
//                    break;
//                case WifiManager.WIFI_STATE_DISABLING:
//                    Log.v("my2", "收到" + "WIFI_STATE_DISABLING");
//                    break;
//                case WifiManager.WIFI_STATE_ENABLED:
//                    Log.v("my2", "收到" + "WIFI_STATE_ENABLED");
//                    break;
//                case WifiManager.WIFI_STATE_ENABLING:
//                    Log.v("my2", "收到" + "WIFI_STATE_ENABLING");
//                    break;
//                case WifiManager.WIFI_STATE_UNKNOWN:
//                    Log.v("my2", "WIFI_STATE_UNKNOWN");
//
//            }
//        }
    }
}
