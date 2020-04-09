package com.liz.whatsai.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.Html;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.app.MyApp;
import com.liz.whatsai.logic.WSRecorder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * PlayNotifier:
 * Created by liz on 2019/2/3.
 */

public class WSNotifier {

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int NOTICE_ID_TYPE_0 = R.string.app_name;

    private static final String WHATSAI_CHANNEL_ID = "WhatsaiChannelId";
    private static final String WHATSAI_CHANNEL_NAME = "WhatsaiChannelName";
    public static final String WHATSAI_NOTIFY_KEY = "WhatsaiNotifyKey";
    private static final String ACTION_WHATSAI_NOTIFY = "com.liz.whatsai.notify";

    public static final int NOTIFY_KEY_APP_STATUS = 1;
    public static final int NOTIFY_KEY_CLOSE_APP = 2;

    private static final int NOTIFY_UPDATE_TIMER_DELAY = 500;
    private static final int NOTIFY_UPDATE_TIMER_PERIOD = 2000;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static Notification mNotification;

    public static void open() {
        LogUtils.trace();
        final Context context = MyApp.getAppContext();
        createNotification(context);
        new Timer().schedule(new TimerTask() {
            public void run() {
                updateNotifyView(context);
            }
        }, NOTIFY_UPDATE_TIMER_DELAY, NOTIFY_UPDATE_TIMER_PERIOD);
    }

    public static void close() {
        LogUtils.trace();
        final Context context = MyApp.getAppContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            LogUtils.te2("get notification service null");
        } else {
            manager.cancel(NOTICE_ID_TYPE_0);
        }
    }

    public static Notification getNotification() {
        return mNotification;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static void createNotification(final Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            LogUtils.te2("get notification service null");
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(WHATSAI_CHANNEL_ID, WHATSAI_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WHATSAI_CHANNEL_ID);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setSmallIcon(R.drawable.icon_bitcomet);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        mNotification = builder.build();
        mNotification.bigContentView = remoteViews;
        mNotification.contentView = remoteViews;
        manager.notify(NOTICE_ID_TYPE_0, mNotification);
    }

    private static void updateNotifyView(Context context) {
        //LogUtils.trace();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            LogUtils.te2("get notification service null");
        } else {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
            remoteViews.setTextViewText(R.id.tv_whatsai_info, Html.fromHtml(WSRecorder.inst().getProgressInfoForNotify()));
            mNotification.bigContentView = remoteViews;
            mNotification.contentView = remoteViews;
            manager.notify(NOTICE_ID_TYPE_0, mNotification);
        }
    }

    private static PendingIntent getPendingIntentForBroadcast(Context context, int keyId) {
        Intent intent = new Intent(ACTION_WHATSAI_NOTIFY);
        intent.setPackage(context.getPackageName());
        intent.putExtra(WHATSAI_NOTIFY_KEY, keyId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int requestCode = (int) SystemClock.uptimeMillis();
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getPendingIntentForActivity(Context context, int keyId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(WHATSAI_NOTIFY_KEY, keyId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int requestCode = (int) SystemClock.uptimeMillis();
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
