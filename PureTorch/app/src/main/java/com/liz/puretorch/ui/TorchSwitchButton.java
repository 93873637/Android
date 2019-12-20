package com.liz.puretorch.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.liz.puretorch.utils.LogUtils;

/**
 * TorchSwitchButton:
 * Created by liz on 2018/11/20.
 */

public class TorchSwitchButton extends android.support.v7.widget.AppCompatButton implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    public static final int FLING_TYPE_UNKNOWN = -1;
    public static final int FLING_TYPE_SWITCH = 0;
    public static final int FLING_TYPE_EXIT = 1;

    private static final int FLING_EXIT_MIN_DISTANCE = 1200;
    private static final int FLING_EXIT_MIN_VELOCITY = 15000;
    private static final int FLING_SWITCH_MIN_DISTANCE = 100;
    private static final int FLING_SWITCH_MIN_VELOCITY = 500;

    private int mFlingType = FLING_TYPE_UNKNOWN;

    public TorchSwitchButton(Context context) {
        super(context,null);
    }

    public TorchSwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public int getFlingType() {
        return mFlingType;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event){
//        // Be sure to call the superclass implementation
//        return super.onTouchEvent(event);
//    }

    @Override
    public boolean onDown(MotionEvent event) {
        LogUtils.d("onDown: " + event.toString());
        mFlingType = FLING_TYPE_UNKNOWN;
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
                           float vX, float vY) {
        //LogUtils.d("onFling: e1=" + e1.toString() + ", e2=" + e2.toString());
        //LogUtils.d("onFling: vX=" + vX + ", vY=" + vY);

        double distX = e2.getX() - e1.getX();
        double distY = e2.getY() - e1.getY();
        double dist = Math.sqrt(distX * distX + distY * distY);
        double vel = Math.sqrt(vX * vX + vY * vY);
        //LogUtils.d("onFling: distX=" + distX + ", distY=" + distY);
        LogUtils.d("onFling: dist = " + dist + ", vel = " + vel);

        // exit: fling long and fast
        if (dist > FLING_EXIT_MIN_DISTANCE && vel > FLING_EXIT_MIN_VELOCITY) {
            LogUtils.d("onFling: long and fast, exit...");
            mFlingType = FLING_TYPE_EXIT;
            return false;
        }

        // switch: fling short and fast
        if (dist > FLING_SWITCH_MIN_DISTANCE && vel > FLING_SWITCH_MIN_VELOCITY) {
            LogUtils.d("onFling: short and fast, switch light...");
            mFlingType = FLING_TYPE_SWITCH;
            return false;
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        LogUtils.d("onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        LogUtils.d("onScroll: " + e1.toString()+e2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        LogUtils.d("onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        LogUtils.d("onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        LogUtils.d("onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        LogUtils.d("onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        LogUtils.d("onSingleTapConfirmed: " + event.toString());
        return true;
    }
}
