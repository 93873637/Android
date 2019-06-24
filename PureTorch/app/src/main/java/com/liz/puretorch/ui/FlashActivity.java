package com.liz.puretorch.ui;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.liz.puretorch.app.FlashCloseReceiver;
import com.liz.puretorch.R;

@TargetApi(Build.VERSION_CODES.M)
public class FlashActivity extends AppCompatActivity {

    public static final String CLOSE_FLASH_ACTION = "android.intent.action.close_flash";
    private static final int NOTIFICATIONID = 0;
    private Button btFlash;
    private boolean mIsFlashOn = false;
    private CameraManager cameraManager = null;
    private String[] mCameraIds;
    private Notification mFlashOnNotification = null;
    private NotificationManager notificationManager = null;
    private boolean isFlashAvailbale = true;
    private FrameLayout mContentPanel = null;


    //闪光灯状态变化的回调
    private CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeUnavailable(String cameraId) {
            super.onTorchModeUnavailable(cameraId);
            //onTorchModeUnavailable 当前闪光灯不可用，如果当前闪光处于打开状态，则关闭它，并且对应的标志位
            if (cameraId.equals(mCameraIds[0]) && mIsFlashOn) {
               // reverseFlashState();
            }
            isFlashAvailbale = false;
            System.out.println("cameraId = " + cameraId + " onTorchModeUnavailable");
        }

        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            //onTorchModeChanged 闪光灯状态变化回调 enabled=false 闪光灯关闭
            //enabled=true 闪光灯已经开启
            //通过这个回调设置标志位,如果当前闪光灯开着但是收到了闪光灯已经被关闭的回调，则改变对应的状态
            isFlashAvailbale = true;
            System.out.println("cameraid = " + cameraId + " enabled = " + enabled + " misFlashOn = " + mIsFlashOn);
            if (cameraId.equals(mCameraIds[0]) && enabled == false && mIsFlashOn) {
               // reverseFlashState();
            }
            System.out.println("cameraId = " + cameraId + " onTorchModeChanged enabled = " + enabled);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_button);

        //闪光灯开关按钮
        btFlash = (Button) findViewById(R.id.bt_flash);
        //整个布局
        mContentPanel = (FrameLayout) findViewById(R.id.flash_content);

        btFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reverseFlashState();
            }
        });
        //根据当前闪光灯装填设置一下UI界面的颜色
        changeFlashUi(mIsFlashOn);
    }

    //flash状态翻转
    private void reverseFlashState() {
        //如果当前Flash 处于unavailable状态，说明当前闪光灯被占用，无法使用
        if (!isFlashAvailbale) {
            //显示当前闪光灯被占用的提示
            showFlashBusy();
            return;
        }
        changeFlashState(mIsFlashOn);//开->关  关->开
        mIsFlashOn = !mIsFlashOn;//标志位装换
        changeFlashUi(mIsFlashOn);//界面UI切换，这里主要就是为了突出闪光灯开关的状态不同
        applyNotification(mIsFlashOn);//闪光灯开启的提示显示和消除
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void applyNotification(boolean isFlashOn) {
        if (!isFlashOn) {
            dismissNotification();
            return;
        }
        if (mFlashOnNotification != null && notificationManager != null) {
            notificationManager.notify(NOTIFICATIONID, mFlashOnNotification);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        cameraManager.registerTorchCallback(torchCallback, null);//注册回调
        getCameraList();//获取当前手机的摄像头个数
        generateNotify();//生成需要显示的提示，方便后面显示
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.unregisterTorchCallback(torchCallback);//ondestory的时候解除回调
    }

    @TargetApi(Build.VERSION_CODES.M) //只有M版本的手机可以使用这个方法
    private void changeFlashState(boolean isFlashOn) {
        if (cameraManager != null && mCameraIds != null) {
            try {
                cameraManager.setTorchMode(mCameraIds[0], !isFlashOn);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)  //只有L版本的收集可以使用这个方法
    private void getCameraList() {
        if (cameraManager != null) {
            try {
                mCameraIds = cameraManager.getCameraIdList();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    //按钮的背景图切换
    private void changeFlashUi(boolean isFlashOn) {
//        if (isFlashOn) {
//            btFlash.setBackgroundResource(R.drawable.flash_open);
//        } else {
//            btFlash.setBackgroundResource(R.drawable.flash_close);
//        }
    }

    //生成notification的大图标
    private Bitmap createNotificationLargeIcon(Context c) {
        Resources res = c.getResources();
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        Bitmap result = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_round), width, height, false);
        return result;
    }

    //生成notification
    private void generateNotify() {
        if (mFlashOnNotification != null) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        //builder.setLargeIcon(createNotificationLargeIcon(this))
        builder.setContentTitle("手电筒已开启")
                .setContentText("点击可关闭手电筒")
                .setSmallIcon(R.mipmap.ic_launcher_round, 3)
                .setContentIntent(createCloseFlashPendingIntent());
        mFlashOnNotification = builder.build();
    }

    //消除notification
    private void dismissNotification() {
        if (notificationManager != null && mFlashOnNotification != null) {
            notificationManager.cancel(NOTIFICATIONID);
        }
    }

    //创建点击notification对应的PendingIntent
    private PendingIntent createCloseFlashPendingIntent() {
        Intent intent = new Intent();
        intent.setClass(this, FlashCloseReceiver.class);
        intent.setAction(CLOSE_FLASH_ACTION);

        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    //显示一个手电筒忙碌的提示
    private void showFlashBusy() {
        View toastContent = getLayoutInflater().inflate(R.layout.busy_toast, null, false);
        Toast toast = new Toast(this);
        toast.setView(toastContent);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}
