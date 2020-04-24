package com.liz.keytest;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

//启动1x1大小服务窗口进行监听变化，音量按键下按
//  不接受触摸屏事件。
//public static final int FLAG_NOT_TOUCHABLE = 0x00000010;
//
//        当窗口可以获得焦点（没有设置FLAG_NOT_FOCUSALBE选项）时，仍然将窗口范围之外的点设备事件（鼠标、触摸屏）发送给后面的窗口处理。否则它将独占所有的点设备事件，而不管它们是不是发生在窗口范围之内。
//public static final int FLAG_NOT_TOUCH_MODAL = 0x00000020;
//NOTE: 注意不要设置FLAG_NOT_FOCUSABLE
//任何按键均可触发
//缺点:
//锁屏，黑屏后无效
//

public class MonitorView extends View {

    private static WindowManager mWindowManager;
    private static MonitorView mMonitorView;

    public static void showWindow(Context context) {
        if (!Settings.canDrawOverlays(context)) {
            //logd("ERROR: FloatingButtonService: showFloatingWindow: can't draw overlays");
            return;
        }

        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mMonitorView = new MonitorView(context);
        }

//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                100, 100, //Must be at least 1x1
//                //WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//                //Don't know if this is a safe default
//                PixelFormat.TRANSLUCENT);

        //Don't set the preview visibility to GONE or INVISIBLE
        mWindowManager.addView(mMonitorView, genLayoutParams());
    }

    public static void hideWindow() {
        if (mWindowManager != null) {
            mWindowManager.removeView(mMonitorView);
            mMonitorView = null;
        }
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
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL; //###@: | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 1;
        layoutParams.height = 1;
        layoutParams.x = 10;
        layoutParams.y = 10;
        return layoutParams;
    }

    public MonitorView(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("MonitorView", "keyCode " + event.getKeyCode());
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.d("MonitorView", "KEYCODE_VOLUME_DOWN");
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.d("MonitorView", "KEYCODE_VOLUME_UP");
                break;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }
}
