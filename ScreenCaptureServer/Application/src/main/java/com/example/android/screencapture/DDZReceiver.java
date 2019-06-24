package com.example.android.screencapture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * DDZReceiver:
 * Created by liz on 2019/2/12.
 *
 * adb shell am broadcast -n com.example.android.screencapture/.DDZReceiver -a android.intent.action.DDZScreenShot
 */

public class DDZReceiver extends BroadcastReceiver {

    private static DDZReceiver mReceiver;

    public static void init(Context context) {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        filter.addAction(ALARM_ALERT);
//        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
//        context.registerReceiver(getReceiver(), filter);
    }

    public static void release(Context context) {
        context.unregisterReceiver(getReceiver());
    }

    private static DDZReceiver getReceiver() {
        if (mReceiver == null) {
            mReceiver = new DDZReceiver();
        }
        return mReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            LogUtils.i("DDZReceiver: action null");
            return;
        }

        LogUtils.d("DDZReceiver: action=" + action);
        if (intent.getAction().equals("android.intent.action.DDZScreenShot")) {
            //####@: LogUtils.i("DDZReceiver.onReceive: unhandled action: " + intent.getAction());
            ScreenCaptureFragment.captureOnce();
        } else {
            LogUtils.i("DDZReceiver.onReceive: unhandled action: " + intent.getAction());
        }
    }
}
