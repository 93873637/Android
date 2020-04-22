package com.liz.keytest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class WhiteService extends Service {

    static public boolean isPowerKeyPressed = false;

    private final static String TAG = WhiteService.class.getSimpleName();
    private final static int FOREGROUND_ID = 1000;
    private Notification mNotification;

    /**
     * 参考:
     * 1 https://blog.csdn.net/q445697127/article/details/8432513
     * 2 https://blog.csdn.net/lfdfhl/article/details/9903693
     */
    private HomeKeyObserver mHomeKeyObserver;
    private PowerKeyObserver mPowerKeyObserver;
    private VolumeKeyObserver mVolumeKeyObserver;

    private void init() {
        mHomeKeyObserver = new HomeKeyObserver(this);
        mHomeKeyObserver.setHomeKeyListener(new HomeKeyObserver.OnHomeKeyListener() {
            @Override
            public void onHomeKeyPressed() {
                Log.i(TAG,"----> 按下Home键");
                System.out.println("----> 按下Home键");
            }

            @Override
            public void onHomeKeyLongPressed() {
                Log.i(TAG,"----> 长按Home键");
                System.out.println("----> 长按Home键");
            }
        });
        mHomeKeyObserver.startListen();

        //////////////////////////////////////////
        mPowerKeyObserver = new PowerKeyObserver(this);
        mPowerKeyObserver.setPowerKeyListener(new PowerKeyObserver.OnPowerKeyListener() {
            @Override
            public void onPowerKeyPressed() {
                Log.i(TAG,"----> 按下电源键");
                System.out.println("----> 按下电源键");
                isPowerKeyPressed = true;
            }
        });
        mPowerKeyObserver.startListen();

        //////////////////////////////////////////
        mVolumeKeyObserver = new VolumeKeyObserver(this);
        mVolumeKeyObserver.setVolumeKeyListener(new VolumeKeyObserver.OnVolumeKeyListener() {
            @Override
            public void onVolumeKeyPressed() {
                Log.i(TAG,"----> 按下电源键");
                System.out.println("----> 按下电源键");
            }
        });
        mVolumeKeyObserver.startListen();
    }

    public WhiteService(){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //return null;
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "WhiteService->onCreate");
        super.onCreate();
        init();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "WhiteService->onDestroy");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        //if(null !=mNotification)
        {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        }

        mHomeKeyObserver.stopListen();
        mPowerKeyObserver.stopListen();
        mVolumeKeyObserver.stopListen();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(null != mNotification){
            Log.i(TAG, "WhiteService->onStartCommand->Notification exists");
            return super.onStartCommand(intent, flags, startId);
        }

        Log.i(TAG, "WhiteService->onStartCommand->Create Notification");
        //NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Foreground");
        builder.setContentText("Text shown on notification bar");
        builder.setContentInfo("Content Info");
        builder.setWhen(System.currentTimeMillis());

        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        mNotification = builder.build();
        startForeground(FOREGROUND_ID, mNotification);
        return super.onStartCommand(intent, flags, startId);
    }
}
