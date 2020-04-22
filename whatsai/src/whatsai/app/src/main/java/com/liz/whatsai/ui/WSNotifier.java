package com.liz.whatsai.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

    private static final String WHATSAI_CHANNEL_ID = "WhatsaiChannelId0";
    private static final String WHATSAI_CHANNEL_NAME = "WhatsaiChannelName";
    public static final String WHATSAI_NOTIFY_KEY = "WhatsaiNotifyKey";
    private static final String ACTION_WHATSAI_NOTIFY = "com.liz.whatsai.notify";

    public static final int NOTIFY_KEY_MAIN_UI = 0;
    public static final int NOTIFY_KEY_APP_STATUS = 1;
    public static final int NOTIFY_KEY_CLOSE_APP = 2;

    private static final int NOTIFY_UPDATE_TIMER_DELAY = 1000;
    private static final int NOTIFY_UPDATE_TIMER_PERIOD = 6000;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static Notification mNotification;
    private static String mChannelID = WHATSAI_CHANNEL_ID;

    /**
     * update channel id each time when create to remove the disgusting alter sound
     * @return new channel id
     */
    private static String genChannelID() {
        return WHATSAI_CHANNEL_ID + System.currentTimeMillis();
    }

    public static void open() {
        LogUtils.trace();
        mChannelID = genChannelID();
        new Timer().schedule(new TimerTask() {
            public void run() {
                updateNotification();
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

    public static void updateNotification() {
        Context context = MyApp.getAppContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            LogUtils.te2("get notification service null");
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(mChannelID, WHATSAI_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            //disable alert sound(NOTE: only take effect with new channel id)
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, mChannelID);
        //设置小图标
        builder.setSmallIcon(WSRecorder.inst().isListening()?R.drawable.micphone_taskbar:R.drawable.icon_bitcomet);
        //设置大图标
        //builder.setLargeIcon(bitmap);
        //设置标题
        //builder.setContentTitle("这是标题");
        //设置通知正文
        //builder.setContentText("这是正文，当前ID是：" + id);
        //设置摘要
        //builder.setSubText("这是摘要");
        //设置是否点击消息后自动clean
        //builder.setAutoCancel(true);
        //显示指定文本
        //builder.setContentInfo("Info");
        //与setContentInfo类似，但如果设置了setContentInfo则无效果
        //用于当显示了多个相同ID的Notification时，显示消息总数
        //builder.setNumber(2);
        //通知在状态栏显示时的文本
        //builder.setTicker("在状态栏上显示的文本");
        //设置优先级
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        //自定义消息时间，以毫秒为单位，当前设置为比系统时间少一小时
        //builder.setWhen(System.currentTimeMillis() - 3600000);
        //设置为一个正在进行的通知，此时用户无法清除通知
        builder.setOngoing(true);
        //设置消息的提醒方式，震动提醒：DEFAULT_VIBRATE     声音提醒：NotificationCompat.DEFAULT_SOUND
        //三色灯提醒NotificationCompat.DEFAULT_LIGHTS     以上三种方式一起：DEFAULT_ALL
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        //设置震动方式，延迟零秒，震动一秒，延迟一秒、震动一秒
        //builder.setVibrate(new long[]{0, 1000, 1000, 1000});
        //set true to alert only once after app start
        builder.setOnlyAlertOnce(true);
        mNotification = builder.build();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        remoteViews.setTextViewText(R.id.tv_whatsai_info, Html.fromHtml(WSRecorder.inst().getProgressInfoForNotify()));

        remoteViews.setOnClickPendingIntent(R.id.main_image, getPendingIntentForActivity(context, NOTIFY_KEY_MAIN_UI));
        remoteViews.setOnClickPendingIntent(R.id.notify_close_app, getPendingIntentForBroadcast(context, NOTIFY_KEY_CLOSE_APP));

        mNotification.bigContentView = remoteViews;
        mNotification.contentView = remoteViews;
        manager.notify(NOTICE_ID_TYPE_0, mNotification);
    }

    private static PendingIntent getPendingIntentForActivity(Context context, int keyId) {
        Intent intent = new Intent(context, WSActivity.class);
        intent.putExtra(WHATSAI_NOTIFY_KEY, keyId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int requestCode = (int) SystemClock.uptimeMillis();
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getPendingIntentForBroadcast(Context context, int keyId) {
        Intent intent = new Intent(ACTION_WHATSAI_NOTIFY);
        intent.setPackage(context.getPackageName());
        intent.putExtra(WHATSAI_NOTIFY_KEY, keyId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int requestCode = (int) SystemClock.uptimeMillis();
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
