package com.liz.multidialer.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.liz.androidutils.LogUtils;

/**
 * Created by dongzhong on 2018/5/30.
 */

public class FloatingButtonService extends Service {

    private static final int LAYOUT_PARAM_WIDTH = 500;
    private static final int LAYOUT_PARAM_HEIGHT = 120;
    private static final int LAYOUT_PARAM_X = 0;
    private static final int LAYOUT_PARAM_Y = 200;

    private static final int BACKGROUND_COLOR_NORMAL = Color.RED;
    private static final int BACKGROUND_COLOR_DOWN = Color.GRAY;

    private static boolean isStarted = false;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Interface Functions

    public static void start(Context context, ServiceConnection conn) {
        if (!FloatingButtonService.isStarted) {
            //context.startService(new Intent(context, FloatingButtonService.class));
            Intent intent = new Intent(context, FloatingButtonService.class);
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    public static void stop(Context context) {
        if (FloatingButtonService.isStarted) {
            context.stopService(new Intent(context, FloatingButtonService.class));
        }
    }

    // Interface Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Callback

    public interface FloatingButtonCallback {
        void onFloatButtonClicked();
    }

    private static FloatingButtonCallback mFloatingButtonCallback;

    public static void setFloatingButtonCallback(FloatingButtonCallback callback) {
        mFloatingButtonCallback = callback;
    }

    // Callback
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private FloatingButton mFloatButton;

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtils.d("FloatingButtonService: onCreate");
        isStarted = true;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayoutParams = genLayoutParams();

        showFloatingWindow();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return sMessenger.getBinder();
    }

    // 定义Handler，重载Handler的消息处理方法
    private Handler mHandler = new  Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Bundle b = msg.getData();
                    String progressInfo = b.getString("progressInfo");
                    LogUtils.d("#################@: get what0: info=" + progressInfo);
                    mFloatButton.setText(progressInfo);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //初始化Messenger，用于消息发送和接收
    private Messenger sMessenger= new Messenger(mHandler);

    private WindowManager.LayoutParams genLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = LAYOUT_PARAM_WIDTH;
        layoutParams.height = LAYOUT_PARAM_HEIGHT;
        layoutParams.x = LAYOUT_PARAM_X;
        layoutParams.y = LAYOUT_PARAM_Y;
        return layoutParams;
    }

//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d("FloatingButtonService: onStartCommand");
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showFloatingWindow() {
        if (!Settings.canDrawOverlays(this)) {
            LogUtils.e("ERROR: FloatingButtonService: showFloatingWindow: can't draw overlays");
            return;
        }

        if (mFloatButton == null) {
            mFloatButton = new FloatingButton(getApplicationContext());
        }

        mWindowManager.addView(mFloatButton, mLayoutParams);
    }

    private class FloatingButton extends AppCompatButton implements View.OnClickListener, View.OnTouchListener {

        private int x;
        private int y;

        //sum of all moved distances to judge if we move button or click button
        private static final int MIN_MOVED_DISTANCE = 10;
        private int moved;

        public FloatingButton(Context context) {
            super(context);
            LogUtils.d("FloatingButton: FloatingButton");
            setText("点击这里停止拨号");
            setTextColor(Color.BLUE);
            setTextSize(16);
            setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            setBackgroundColor(BACKGROUND_COLOR_NORMAL);
            setGravity(Gravity.CENTER);
            setOnClickListener(this);
            setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            LogUtils.d("FloatingButton: onClick");
            if (mFloatingButtonCallback != null) {
                mFloatingButtonCallback.onFloatButtonClicked();
            }
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //LogUtils.d("FloatingButton: ACTION_DOWN: (x, y)=" + event.getRawX() + ", " + event.getRawY());
                    view.setBackgroundColor(BACKGROUND_COLOR_DOWN);
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    moved = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //LogUtils.d("FloatingButton: ACTION_MOVE");
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    moved += Math.abs(movedX) + Math.abs(movedY);
                    x = nowX;
                    y = nowY;
                    mLayoutParams.x +=  movedX;
                    mLayoutParams.y +=  movedY;
                    mWindowManager.updateViewLayout(view, mLayoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    //LogUtils.d("FloatingButton: ACTION_UP: (x, y)=" + event.getRawX() + ", " + event.getRawY() + ", moved=" + moved);
                    view.setBackgroundColor(BACKGROUND_COLOR_NORMAL);
                    if (moved < MIN_MOVED_DISTANCE) {
                        //no enough moved distance, take as click
                        view.performClick();
                    }
                    return true;  //return true to consume the click event by default
                default:
                    break;
            }

            return false;
        }
    }  // FloatingButton
}
