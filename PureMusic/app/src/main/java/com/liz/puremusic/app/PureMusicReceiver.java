package com.liz.puremusic.app;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.liz.puremusic.logic.DataLogic;
import com.liz.puremusic.utils.LogUtils;

import static android.provider.Settings.System.ALARM_ALERT;

/**
 * PureMusicReceiver:
 * Created by liz on 2019/2/12.
 */

public class PureMusicReceiver extends BroadcastReceiver {

    private static PureMusicReceiver mReceiver;

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

    private static PureMusicReceiver getReceiver() {
        if (mReceiver == null) {
            mReceiver = new PureMusicReceiver();
        }
        return mReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            LogUtils.i("PureMusicReceiver: action null");
            return;
        }

        LogUtils.d("PureMusicReceiver: action=" + action);
        switch (action) {
            case ALARM_ALERT:
                LogUtils.d("PureMusicReceiver: ALARM_ALERT");
                break;
            case Intent.ACTION_SCREEN_OFF:
                LogUtils.d("PureMusicReceiver: Intent.ACTION_SCREEN_OFF");
                break;
            case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                LogUtils.d("PureMusicReceiver: AudioManager.ACTION_AUDIO_BECOMING_NOISY: earphone plug out?");
                DataLogic.pausePlay();
                break;
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                LogUtils.d("PureMusicReceiver: BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED");
                onBluetoothConnectionStateChanged(intent);
                break;
            default:
                LogUtils.d("PureMusicReceiver: unhandled action " + action);
                break;
        }
    }

    private void onBluetoothConnectionStateChanged(Intent intent) {
        int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
        switch (bluetoothState) {
            case BluetoothAdapter.STATE_CONNECTED:
                LogUtils.d("PureMusicReceiver: onBluetoothConnectionStateChanged: STATE_CONNECTED");
                break;
            case BluetoothAdapter.STATE_CONNECTING:
                LogUtils.d("PureMusicReceiver: onBluetoothConnectionStateChanged: STATE_CONNECTING");
                break;
            case BluetoothAdapter.STATE_DISCONNECTED:
                LogUtils.d("PureMusicReceiver: onBluetoothConnectionStateChanged: STATE_DISCONNECTED");
                DataLogic.pausePlay();
                break;
            case BluetoothAdapter.STATE_DISCONNECTING:
                LogUtils.d("PureMusicReceiver: onBluetoothConnectionStateChanged: STATE_DISCONNECTING");
                break;
            default:
                LogUtils.d("PureMusicReceiver: onBluetoothConnectionStateChanged: unhandled state " + bluetoothState);
                break;
        }
    }
}
