package com.liz.puremusic.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.liz.androidutils.LogUtils;
import com.liz.puremusic.logic.DataLogic;

/**
 * MediaButtonReceiver:
 * Created by liz on 2018/12/16.
 */

public class MediaButtonReceiver extends BroadcastReceiver {
    private static String TAG = "MediaButtonReceiver";

    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        LogUtils.d("MediaButtonReceiver.BroadcastReceiver: action=" + action + ", event=" + event);
        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            int keycode = event.getKeyCode();
            int eventAction = event.getAction();
            LogUtils.d("MediaButtonReceiver.BroadcastReceiver: ACTION_MEDIA_BUTTON, keycode=" + keycode + ", eventAction=" + eventAction);

            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    LogUtils.d("MediaButtonReceiver.BroadcastReceiver: ACTION_MEDIA_BUTTON.KEYCODE_MEDIA_PLAY");
                    if (eventAction == KeyEvent.ACTION_UP) {
                        DataLogic.startPlay();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    LogUtils.d("MediaButtonReceiver.BroadcastReceiver: ACTION_MEDIA_BUTTON.KEYCODE_MEDIA_PAUSE");
                    if (eventAction == KeyEvent.ACTION_UP) {
                        DataLogic.pausePlay();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    LogUtils.d("MediaButtonReceiver.BroadcastReceiver: ACTION_MEDIA_BUTTON.KEYCODE_MEDIA_NEXT");
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    LogUtils.d("MediaButtonReceiver.BroadcastReceiver: ACTION_MEDIA_BUTTON.KEYCODE_MEDIA_PREVIOUS");
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    LogUtils.d("MediaButtonReceiver.BroadcastReceiver: ACTION_MEDIA_BUTTON.KEYCODE_HEADSETHOOK");
                    //中间按钮,暂停or播放
                    //可以通过发送一个新的广播通知正在播放的视频页面,暂停或者播放视频
                    break;
                default:
                    LogUtils.d("MediaButtonReceiver.BroadcastReceiver: ACTION_MEDIA_BUTTON: unhandled keycode=" + keycode);
                    break;
            }
        }
    }
}
