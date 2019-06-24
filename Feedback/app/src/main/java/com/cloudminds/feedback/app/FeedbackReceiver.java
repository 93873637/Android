package com.cloudminds.feedback.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.cloudminds.feedback.activity.UploadActivity;
import com.cloudminds.feedback.logic.ComDef;
import com.cloudminds.feedback.logic.Settings;
import com.cloudminds.feedback.utils.ComUtils;
import com.cloudminds.feedback.utils.LogUtils;

/**
 * FeebadbackReceiver
 * Created by liz on 18-3-13.
 *
 * Receive broadcast messages of Feedback app
 * NOTE:
 * 1. for Android O(8.0), we only use explicitly broadcat intent, i.e. specific -n com.cloudminds.feedback/.app.FeedbackReceiver
 * 2. the Caller must ensure register/unregister by same context.
 *
 * ADB Command Examples (NOTE: \" also needed when typing adb command):
 * SSR_RESET:
 * adb shell am broadcast -n com.cloudminds.feedback/.app.FeedbackReceiver -a android.intent.action.FEEDBACK --es \"msg_type\" \"SSR_RESET\" --es "msg_cont" \"adsp reset\" --es \"file_path\" \"/data/ramdump/\"
 *
 * APP_ERRORS:
 * adb shell am broadcast -n com.cloudminds.feedback/.app.FeedbackReceiver -a android.intent.action.FEEDBACK --es \"msg_type\" \"APP_ERRORS\" --es "msg_cont" \"com.apple.store Exception\" --es \"file_path\" \"/data/anr/\" --es \"file_name\" \"anr_2018-04-11-17-09-59-899\"
 *
 * Call Method in C-Layer:
 * const char *feedback_broadcast =
 *     "/system/bin/am broadcast -n com.cloudminds.feedback/.app.FeedbackReceiver -a android.intent.action.FEEDBACK --es \"msg_type\" \"SSR_RESET\" --es \"msg_cont\" \"adspreset\" --es \"file_path\" \"/data/ramdump/\"";
 * system(feedback_broadcast);
 *
 * For program of vendor, using /system/bin/cmd activity in case cmd not found error:
 * const char *feedback_broadcast =
 *     "/system/bin/cmd activity broadcast -n com.cloudminds.feedback/.app.FeedbackReceiver -a android.intent.action.FEEDBACK --es \"msg_type\" \"SSR_RESET\" --es \"msg_cont\" \"adsp reset\" --es \"file_path\" \"/data/ramdump/\"";
 *
 */

public class FeedbackReceiver extends BroadcastReceiver {

    public static FeedbackReceiver mReceiver = null;

    public static void register(Context context) {
        LogUtils.d("FeedbackReceiver.register: mReceiver=" + mReceiver);
        if (mReceiver == null) {
            LogUtils.d("FeedbackReceiver.register: create new receiver");
            mReceiver = new FeedbackReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ComDef.FEEDBACK_RECEIVER_ACTION);
        context.registerReceiver(mReceiver, filter);
    }

    public static void unregister(Context context) {
        LogUtils.d("FeedbackReceiver.unregister: mReceiver=" + mReceiver);
        if (mReceiver == null) {
            LogUtils.e("ERROR: FeedbackReceiver.unregister: no receiver");
        } else {
            context.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("FeedbackReceiver.onReceive: intent=" + intent.toString());
        //Toast.makeText(context, "Feedback Broadcast: " + intent.toString(), Toast.LENGTH_LONG).show();

        if (intent.getAction().equals(ComDef.FEEDBACK_RECEIVER_ACTION)) {
            ComUtils.setUploadUrl(Settings.getUserType(context));
            String msgType = intent.getStringExtra(ComDef.FEEDBACK_MSG_TYPE);
            if (msgType == null) {
                LogUtils.e("FeedbackReceiver.onReceive: no message type");
            } else if (msgType.equals(ComDef.FEEDBACK_TYPE_OFFLINE_LOG)) {
                //offline log call feedback by startActivity directly, not go this way
            } else if (msgType.equals(ComDef.FEEDBACK_TYPE_SSR_RESET)) {
                onReceiveSSRRest(context, intent);
            } else if (msgType.equals(ComDef.FEEDBACK_TYPE_SYS_RESET)) {
                onReceiveSysReset(context, intent);
            } else if (msgType.equals(ComDef.FEEDBACK_TYPE_APP_ERRORS)) {
                onReceiveAppErrors(context, intent);
            } else if (msgType.equals(ComDef.FEEDBACK_TYPE_NATIVE_CRASH)) {
                onReceiveNativeCrash(context, intent);
            } else {
                LogUtils.e("FeedbackReceiver.onReceive: unsupported message type: " + msgType);
            }
        } else {
            LogUtils.i("FeedbackReceiver.onReceive: unhandled action: " + intent.getAction());
        }
    }

    public void onReceiveSSRRest(Context context, Intent intent) {
        if (!ComDef.isUserExperienceEnabled(context)) {
            LogUtils.i("FeedbackReceiver.onReceiveSSRRest: no report without user experience");
            return;
        }

        String msgCont = intent.getStringExtra(ComDef.FEEDBACK_MSG_CONT);
        String filePath = intent.getStringExtra(ComDef.FEEDBACK_FILE_PATH);
        String errorType = "subsystem_reset";
        String packageName = "android";
        LogUtils.d("FeedbackReceiver.onReceiveSSRRest: msgCont=" + msgCont + ", filePath=" + filePath);

        if (TextUtils.isEmpty(msgCont)) {
            LogUtils.e("FeedbackReceiver.onReceiveSSRRest: invalid message without content");
        } else {
            Intent activityIntent = new Intent(context, UploadActivity.class);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_TYPE, ComDef.FEEDBACK_TYPE_SSR_RESET);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_CONT, msgCont);
            activityIntent.putExtra(ComDef.FEEDBACK_FILE_PATH, filePath);
            activityIntent.putExtra(ComDef.FEEDBACK_PACKAGE_NAME, packageName);
            activityIntent.putExtra(ComDef.FEEDBACK_ERROR_TYPE, errorType);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }

    private void onReceiveSysReset(Context context, Intent intent) {
        if (!ComDef.isUserExperienceEnabled(context)) {
            LogUtils.i("FeedbackReceiver.onReceiveSysReset: no report without user experience");
            return;
        }

        String msgCont = intent.getStringExtra(ComDef.FEEDBACK_MSG_CONT);
        String filePath = intent.getStringExtra(ComDef.FEEDBACK_FILE_PATH);
        String errorType = "kernel_panic";
        String packageName = "kernel";
        LogUtils.d("FeedbackReceiver.onReceiveSysReset: msgCont=" + msgCont + ", filePath=" + filePath);

        if (TextUtils.isEmpty(msgCont)) {
            LogUtils.e("FeedbackReceiver.onReceiveSysReset: invalid message without content");
        } else {
            Intent activityIntent = new Intent(context, UploadActivity.class);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_TYPE, ComDef.FEEDBACK_TYPE_SYS_RESET);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_CONT, msgCont);
            activityIntent.putExtra(ComDef.FEEDBACK_FILE_PATH, filePath);
            activityIntent.putExtra(ComDef.FEEDBACK_PACKAGE_NAME, packageName);
            activityIntent.putExtra(ComDef.FEEDBACK_ERROR_TYPE, errorType);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }

    public void onReceiveAppErrors(Context context, Intent intent) {
        if (!ComDef.isUserExperienceEnabled(context)) {
            LogUtils.i("FeedbackReceiver.onReceiveAppErrors: no report without user experience");
            return;
        }

        String msgCont = intent.getStringExtra(ComDef.FEEDBACK_MSG_CONT);
        String filePath = intent.getStringExtra(ComDef.FEEDBACK_FILE_PATH);
        String fileName = intent.getStringExtra(ComDef.FEEDBACK_FILE_NAME);
        String errorType = intent.getStringExtra(ComDef.FEEDBACK_ERROR_TYPE);
        String packageName = intent.getStringExtra(ComDef.FEEDBACK_PACKAGE_NAME);
        LogUtils.d("FeedbackReceiver.onReceiveAppErrors: msgCont=" + msgCont + ", filePath=" + filePath + ", fileName=" + fileName);
        LogUtils.d("FeedbackReceiver.onReceiveAppErrors: errorType=" + errorType + ", packageName=" + packageName);

        //Excluding feedback self exceptions in case dead loop
        if (!TextUtils.isEmpty(packageName) && packageName.equals(ComDef.PACKAGE_NAME)) {
            LogUtils.i("FeedbackReceiver.onReceiveAppErrors: excluding feedback self exceptions");
            return;
        }

        if (TextUtils.isEmpty(msgCont)) {
            LogUtils.e("FeedbackReceiver.onReceiveAppErrors: invalid message without content");
        } else {
            ///*
            Intent activityIntent = new Intent(context, UploadActivity.class);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_TYPE, ComDef.FEEDBACK_TYPE_APP_ERRORS);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_CONT, msgCont);
            activityIntent.putExtra(ComDef.FEEDBACK_FILE_PATH, filePath);
            activityIntent.putExtra(ComDef.FEEDBACK_FILE_NAME, fileName);
            activityIntent.putExtra(ComDef.FEEDBACK_PACKAGE_NAME, packageName);
            activityIntent.putExtra(ComDef.FEEDBACK_ERROR_TYPE, errorType);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
            //*/

            /*
            //pull out main ui for feedback
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_TYPE, ComDef.FEEDBACK_TYPE_APP_ERRORS);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_CONT, msgCont);
            activityIntent.putExtra(ComDef.FEEDBACK_FILE_PATH, filePath);
            activityIntent.putExtra(ComDef.FEEDBACK_FILE_NAME, fileName);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
            //*/
        }
    }

    private void onReceiveNativeCrash(Context context, Intent intent) {
        if (!ComDef.isUserExperienceEnabled(context)) {
            LogUtils.i("FeedbackReceiver.onReceiveNativeCrash: no report without user experience");
            return;
        }

        String msgCont = intent.getStringExtra(ComDef.FEEDBACK_MSG_CONT);
        String filePath = intent.getStringExtra(ComDef.FEEDBACK_FILE_PATH);
        String errorType = "native_crash";
        String packageName = "android";
        LogUtils.d("FeedbackReceiver.onReceiveNativeCrash: msgCont=" + msgCont + ", filePath=" + filePath);

        if (TextUtils.isEmpty(msgCont)) {
            LogUtils.e("FeedbackReceiver.onReceiveNativeCrash: invalid message without content");
        } else {
            Intent activityIntent = new Intent(context, UploadActivity.class);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_TYPE, ComDef.FEEDBACK_TYPE_NATIVE_CRASH);
            activityIntent.putExtra(ComDef.FEEDBACK_MSG_CONT, msgCont);
            activityIntent.putExtra(ComDef.FEEDBACK_FILE_PATH, filePath);
            activityIntent.putExtra(ComDef.FEEDBACK_PACKAGE_NAME, packageName);
            activityIntent.putExtra(ComDef.FEEDBACK_ERROR_TYPE, errorType);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }
}
