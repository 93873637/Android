package com.liz.puremusic.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.liz.puremusic.logic.DataLogic;
import com.liz.puremusic.ui.PlayNotifier;
import com.liz.puremusic.utils.LogUtils;

/**
 * NotificationBroadcastReceiver:
 * Created by liz on 2019/2/4.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("NotificationBroadcastReceiver.onReceive");
        //collapseStatusBar(context);　　//clear status bar
        int notifyId = intent.getIntExtra(PlayNotifier.PURE_MUSIC_NOTIFY_KEY, -1);
        if (notifyId == -1){
            LogUtils.e("ERROR: NotificationBroadcastReceiver.onReceive: unknown notify key");
        }
        else {
            switch (notifyId) {
                case PlayNotifier.NOTIFY_KEY_PLAY_OR_PAUSE:
                    DataLogic.switchPlayOrPause();
                    break;
                case PlayNotifier.NOTIFY_KEY_PLAY_PREV:
                    DataLogic.goPrev();
                    break;
                case PlayNotifier.NOTIFY_KEY_PLAY_NEXT:
                    DataLogic.goNext();
                    break;
                case PlayNotifier.NOTIFY_KEY_PLAY_STOP:
                    DataLogic.onStopPlay();
                    break;
                case PlayNotifier.NOTIFY_KEY_PLAY_MODE:
                    DataLogic.switchPlayMode();
                    break;
                default:
                    LogUtils.w("WARNING: NotificationBroadcastReceiver.onReceive: unsupported notify key " + notifyId);
                    break;
            }
        }
    }
}
