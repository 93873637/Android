package com.liz.multidialerdaemon.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.multidialerdaemon.logic.ComDef;
import com.liz.multidialerdaemon.logic.DataLogic;

public class DaemonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("Receiver Broadcast of Intent: " + intent.getAction());
        if (intent.getAction() == ComDef.LIFE_BROADCAST_MSG) {
            DataLogic.refreshLife();
        }
    }

}
