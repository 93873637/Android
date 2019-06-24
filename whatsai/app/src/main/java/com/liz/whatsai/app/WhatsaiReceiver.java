package com.liz.whatsai.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.liz.whatsai.utils.LogUtils;

/**
 * WhatsaiReceiver
 * Created by liz on 18-3-13.
 * Receive broadcast messages of Feedback app
 *
 * NOTE:
 * for Android O(8.0), we only use explicitly broadcat intent, i.e. specific -n com.liz.whatsai/.app.WhatsaiReceiver
 *
 * ADB Command Examples (NOTE: \" also needed when typing adb command):
 * adb shell am broadcast -n com.liz.whatsai/.app.WhatsaiReceiver -a com.liz.reminder.RING
 *
 */

public class WhatsaiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("WhatsaiReceiver:onReceive: intent=" + intent.toString());
    }
}
