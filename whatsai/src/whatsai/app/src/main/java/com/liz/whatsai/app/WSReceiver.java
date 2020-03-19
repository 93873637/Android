package com.liz.whatsai.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.liz.androidutils.LogUtils;

/**
 * WSReceiver
 * Created by liz on 18-3-13.
 * Receive broadcast messages of Feedback app
 *
 * NOTE:
 * for Android O(8.0), we only use explicitly broadcat intent, i.e. specific -n com.liz.whatsai/.app.WSReceiver
 *
 * ADB Command Examples (NOTE: \" also needed when typing adb command):
 * adb shell am broadcast -n com.liz.whatsai/.app.WSReceiver -a com.liz.reminder.RING
 *
 */

public class WSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("WSReceiver:onReceive: intent=" + intent.toString());
    }
}
