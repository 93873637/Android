package com.liz.whatsai.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.liz.whatsai.R;
import com.liz.whatsai.logic.DataLogic;

import java.util.Timer;
import java.util.TimerTask;

/**
 * PlayNotifier:
 * Created by liz on 2019/2/3.
 */

public class WSNotifier {
    private static final String WHATSAI_CHANNEL_ID = "WhatsaiChannelId";
    private static final String WHATSAI_CHANNEL_NAME = "WhatsaiChannelName";
    public static final String WHATSAI_NOTIFY_KEY = "WhatsaiNotifyKey";
    private static final String ACTION_WHATSAI_NOTIFY = "com.liz.whatsai.notify";
    private static final int NOTICE_ID_TYPE_0 = R.string.app_name;

    public static final int NOTIFY_KEY_APP_STATUS = 1;
    public static final int NOTIFY_KEY_CLOSE_APP = 2;

    private static final int NOTIFY_UPDATE_TIMER_DELAY = 200;
    private static final int NOTIFY_UPDATE_TIMER_PERIOD = 1000;

    private static Timer mNotifyUpdateTimer;

    public static void onCreate(final Context context) {
        mNotifyUpdateTimer = new Timer();
        mNotifyUpdateTimer.schedule(new TimerTask() {
            public void run() {
                addNotification(context);
            }
        }, NOTIFY_UPDATE_TIMER_DELAY, NOTIFY_UPDATE_TIMER_PERIOD);
    }

    public static void onDestory(final Context context) {
        mNotifyUpdateTimer.cancel();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTICE_ID_TYPE_0);
    }

    private static void addNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(WHATSAI_CHANNEL_ID, WHATSAI_CHANNEL_NAME, importance);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WHATSAI_CHANNEL_ID);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        remoteViews.setTextViewText(R.id.PlayInfo, DataLogic.getCurrentMusicName());
        remoteViews.setInt(R.id.BtnPlayOrPause, "setBackgroundResource", DataLogic.isPlaying()?R.drawable.pause:R.drawable.play);
        remoteViews.setInt(R.id.BtnPlayMode, "setBackgroundResource", MainActivity.getPlayModeResId());

        remoteViews.setOnClickPendingIntent(R.id.BtnPlayOrPause, getPendingIntentForBroadcast(context, NOTIFY_KEY_PLAY_OR_PAUSE));
        remoteViews.setOnClickPendingIntent(R.id.notify_play_prev, getPendingIntentForBroadcast(context, NOTIFY_KEY_PLAY_PREV));
        remoteViews.setOnClickPendingIntent(R.id.notify_play_next, getPendingIntentForBroadcast(context, NOTIFY_KEY_PLAY_NEXT));
        remoteViews.setOnClickPendingIntent(R.id.BtnStop, getPendingIntentForBroadcast(context, NOTIFY_KEY_PLAY_STOP));
        remoteViews.setOnClickPendingIntent(R.id.BtnPlayMode, getPendingIntentForBroadcast(context, NOTIFY_KEY_PLAY_MODE));
        remoteViews.setOnClickPendingIntent(R.id.notify_play_list, getPendingIntentForActivity(context, NOTIFY_KEY_PLAY_LIST));
        remoteViews.setOnClickPendingIntent(R.id.notify_close_app, getPendingIntentForBroadcast(context, NOTIFY_KEY_CLOSE_APP));

        builder.setSmallIcon(R.drawable.abc_ic_menu_share_mtrl_alpha);
        Notification notification = builder.build();

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
            notification.bigContentView = remoteViews;
        }

        notification.contentView = remoteViews;
        manager.notify(NOTICE_ID_TYPE_0, notification);
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
            Intent intent = new Intent(context, PlayListActivity.class);
            intent.putExtra(WHATSAI_NOTIFY_KEY, keyId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            int requestCode = (int) SystemClock.uptimeMillis();
            return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
