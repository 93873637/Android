package com.liz.imagetools.app;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.liz.imagetools.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

import static android.provider.Settings.System.ALARM_ALERT;

/**
 * ImageToolsReceiver:
 * Created by liz on 2019/6/28.
 *
 * adb shell am broadcast -n com.liz.imagetools/.app.ImageToolsReceiver -a android.intent.action.NV21toJPG
 */

@SuppressWarnings("unused")
public class ImageToolsReceiver extends BroadcastReceiver {

    ///////////////////////////////////////////////////////////////////////////////////
    //Static Members

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

    public static int addAction(String action, ReceiverListener listener) {
        mActionMaps.put(action, listener);
        LogUtils.d("ImageToolsReceiver: add action " + action + ", size = " + mActionMaps.size());
        return mActionMaps.size();
    }

    private static ImageToolsReceiver mReceiver;
    private static Map<String, ReceiverListener> mActionMaps = new HashMap<>();

    private static ImageToolsReceiver getReceiver() {
        if (mReceiver == null) {
            mReceiver = new ImageToolsReceiver();
        }
        return mReceiver;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    public interface ReceiverListener {
        void onReceiveAction(String action);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            LogUtils.i("ImageToolsReceiver: action null");
            return;
        }

        LogUtils.d("ImageToolsReceiver: receive action = " + action);
        ReceiverListener listener = mActionMaps.get(action);
        if (listener == null) {
            LogUtils.i("ImageToolsReceiver.onReceive: unhandled action: " + intent.getAction());
        }
        else {
            listener.onReceiveAction(action);
        }
    }
}
