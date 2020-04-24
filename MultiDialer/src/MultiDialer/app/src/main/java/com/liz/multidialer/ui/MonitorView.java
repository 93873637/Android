package com.liz.multidialer.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.liz.androidutils.LogUtils;

public class MonitorView extends View {

    private static WindowManager mWindowManager;
    private static MonitorView mMonitorView;

    public static void showWindow(Context context) {
        LogUtils.trace();
        if (mWindowManager == null) {
            mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            mMonitorView = new MonitorView(context);
        }

//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                1, 1, //Must be at least 1x1
//                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                //Don't know if this is a safe default
//                PixelFormat.TRANSLUCENT);

        //Don't set the preview visibility to GONE or INVISIBLE
        mWindowManager.addView(mMonitorView, genLayoutParams());
        mMonitorView.setBackgroundColor(0x00ff00);
        mMonitorView.setVisibility(View.VISIBLE);
    }

    private static WindowManager.LayoutParams genLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 1000;
        layoutParams.height = 1000;
        layoutParams.x = 0;
        layoutParams.y = 0;
        return layoutParams;
    }

    public static void hideWindow() {
        LogUtils.trace();
        if(null != mWindowManager) {
            mWindowManager.removeView(mMonitorView);
        }
    }

    public MonitorView(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogUtils.td("keyCode " + event.getKeyCode());
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.d("###@:", "KEYCODE_VOLUME_DOWN");
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                LogUtils.td("KEYCODE_VOLUME_UP");
                break;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }
}

