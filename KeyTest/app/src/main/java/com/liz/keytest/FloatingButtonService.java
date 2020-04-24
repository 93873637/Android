package com.liz.keytest;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.liz.androidutils.LogUtils;

/**
 * //启动1x1大小服务窗口进行监听变化，音量按键下按
 * //  不接受触摸屏事件。
 * //public static final int FLAG_NOT_TOUCHABLE = 0x00000010;
 * //
 * //        当窗口可以获得焦点（没有设置FLAG_NOT_FOCUSALBE选项）时，仍然将窗口范围之外的点设备事件（鼠标、触摸屏）发送给后面的窗口处理。否则它将独占所有的点设备事件，而不管它们是不是发生在窗口范围之内。
 * //public static final int FLAG_NOT_TOUCH_MODAL = 0x00000020;
 * //NOTE: 注意不要设置FLAG_NOT_FOCUSABLE
 * //任何按键均可触发
 * //缺点:
 * //锁屏，黑屏后无效
 * //
 */

@SuppressWarnings("unused")
public class FloatingButtonService extends Service {

    private static final int LAYOUT_PARAM_WIDTH = 800;
    private static final int LAYOUT_PARAM_HEIGHT = 160;
    private static final int LAYOUT_PARAM_X = 0;
    private static final int LAYOUT_PARAM_Y = 1230;

    private static final int MSG_CODE_SHOW_FLOATING_BUTTON = 0;
    private static final int MSG_CODE_HIDE_FLOATING_BUTTON = 1;
    private static final int MSG_CODE_BUTTON_INFO = 2;

    private static final String MSG_KEY_BUTTON_TEXT = "FB_TEXT";
    private static final String MSG_KEY_BUTTON_COLOR = "FB_COLOR";

    private static final int BACKGROUND_COLOR_NORMAL = Color.RED;
    private static final int BACKGROUND_COLOR_DOWN = Color.GRAY;

    private static FloatingButtonService mFloatingButtonService = null;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Interface Functions

    public static void start(Context context) {
        if (mFloatingButtonService == null) {
            //context.startService(new Intent(context, FloatingButtonService.class));
            Intent intent = new Intent(context, FloatingButtonService.class);
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    public static void stop(Context context) {
        if (mFloatingButtonService != null) {
            context.stopService(new Intent(context, FloatingButtonService.class));
        }
    }

    public static void updateButtonInfo(String text, int color) {
        if (mFloatingButtonService == null) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = MSG_CODE_BUTTON_INFO;
        Bundle b = new Bundle();
        b.putString(MSG_KEY_BUTTON_TEXT, text);
        b.putInt(MSG_KEY_BUTTON_COLOR, color);
        msg.setData(b);
        try {
            sMessenger.send(msg);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void showFloatingButton(boolean show, String text) {
        if (mFloatingButtonService == null) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = show ? MSG_CODE_SHOW_FLOATING_BUTTON : MSG_CODE_HIDE_FLOATING_BUTTON;
        Bundle b = new Bundle();
        b.putString(MSG_KEY_BUTTON_TEXT, text);
        msg.setData(b);
        try {
            sMessenger.send(msg);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    private static FloatingButton mFloatButton;

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtils.d("FloatingButtonService: onCreate");
        mFloatingButtonService = this;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayoutParams = genLayoutParams();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return sMessenger.getBinder();
    }

    private static Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_CODE_SHOW_FLOATING_BUTTON:
                    if (mFloatingButtonService != null) {
                        Bundle b = msg.getData();
                        String textInfo = b.getString(MSG_KEY_BUTTON_TEXT);
                        mFloatingButtonService.showFloatingWindow(textInfo);
                    }
                    break;
                case MSG_CODE_HIDE_FLOATING_BUTTON:
                    if (mFloatingButtonService != null) {
                        mFloatingButtonService.hideFloatingWindow();
                    }
                    break;
                case MSG_CODE_BUTTON_INFO:
                    if (mFloatButton != null) {
                        Bundle b = msg.getData();
                        String progressInfo = b.getString(MSG_KEY_BUTTON_TEXT);
                        LogUtils.d("FloatingButtonService: get button text: " + progressInfo);
                        if (progressInfo != null) {
                            mFloatButton.setText(progressInfo);
                        }
                        int color = b.getInt(MSG_KEY_BUTTON_COLOR);
                        LogUtils.d("FloatingButtonService: get button color: " + color);
                        mFloatButton.setBackgroundColor(color);
                    }
                    break;
                default:
                    LogUtils.e("ERROR: FloatingButtonService: Unknown message code: " + msg.what);
                    break;
            }
            return false;
        }
    });

    //初始化Messenger，用于消息发送和接收
    private static Messenger sMessenger= new Messenger(mHandler);

    private static ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sMessenger = null;
        }
    };

    private WindowManager.LayoutParams genLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL; //####@:| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 1;//###@: LAYOUT_PARAM_WIDTH;
        layoutParams.height = 1; //###@@: LAYOUT_PARAM_HEIGHT;
        layoutParams.x = LAYOUT_PARAM_X;
        layoutParams.y = LAYOUT_PARAM_Y;
        return layoutParams;
    }

//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        LogUtils.d("FloatingButtonService: onStartCommand");
//        return super.onStartCommand(intent, flags, startId);
//    }

    private void showFloatingWindow(String textInfo) {
        if (!Settings.canDrawOverlays(this)) {
            LogUtils.e("ERROR: FloatingButtonService: showFloatingWindow: can't draw overlays");
            return;
        }

        //####@:
        MonitorView.showWindow(getApplicationContext());

        if (mFloatButton == null) {
            mFloatButton = new FloatingButton(getApplicationContext());
            mWindowManager.addView(mFloatButton, mLayoutParams);
        }

        //mWindowManager.addView(mFloatButton, mLayoutParams);
        mFloatButton.setVisibility(View.VISIBLE);
        mFloatButton.setText(textInfo);
    }

    private void hideFloatingWindow() {
        if (mFloatButton != null) {
            mFloatButton.setVisibility(View.INVISIBLE);
            //mWindowManager.removeView(mFloatButton);
        }
    }

    private class FloatingButton extends AppCompatButton implements View.OnClickListener, View.OnTouchListener {

        private final String DEFAULT_BUTTON_TEXT = "FLOATING BUTTON";
        private final String DEFAULT_BUTTON_CLICKED = DEFAULT_BUTTON_TEXT + ", CLICKED...";

        private int x;
        private int y;

        //sum of all moved distances to judge if we move button or click button
        private static final int MIN_MOVED_DISTANCE = 10;
        private int moved;

        public FloatingButton(Context context) {
            super(context);
            LogUtils.d("FloatingButton: FloatingButton");
            setText(DEFAULT_BUTTON_TEXT);
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
            setText(DEFAULT_BUTTON_CLICKED);
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

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            LogUtils.td("keyCode " + event.getKeyCode());
//            switch (event.getKeyCode()) {
//                case KeyEvent.KEYCODE_BACK:
//                case KeyEvent.KEYCODE_MENU:
//                case KeyEvent.KEYCODE_VOLUME_DOWN:
//                    Log.d("###@:", "KEYCODE_VOLUME_DOWN");
//                    break;
//                case KeyEvent.KEYCODE_VOLUME_UP:
//                    Log.d("###@:", "KEYCODE_VOLUME_UP");
//                    break;
//                default:
//                    break;
//            }
            return super.dispatchKeyEvent(event);
        }

    }  // class FloatingButton
}
