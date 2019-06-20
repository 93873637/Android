package com.liz.screenhelper.app;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.liz.screenhelper.logic.ComDef;
import com.liz.screenhelper.ui.ScreenCaptureFragment;
import com.liz.screenhelper.utils.LogUtils;

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
    }
}
