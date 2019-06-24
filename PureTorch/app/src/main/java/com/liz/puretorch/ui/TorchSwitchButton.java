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

    private static final int FLING_MIN_DISTANCE = 1500;
    private static final int FLING_MIN_VELOCITY = 15000;

    public boolean fastFling = false;

    public TorchSwitchButton(Context context) {
        super(context,null);
    }

    public TorchSwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

//
//    @Override
//    public boolean onTouchEvent(MotionEvent event){
//        // Be sure to call the superclass implementation
//        return super.onTouchEvent(event);
//    }

    @Override
    public boolean onDown(MotionEvent event) {
        LogUtils.d("onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
                           float vX, float vY) {
        LogUtils.d("onFling: e1=" + e1.toString());
        LogUtils.d("onFling: e2=" + e2.toString());
        LogUtils.d("onFling: vX=" + vX + ", vY=" + vY);

        float distX = e2.getX() - e1.getX();
        float distY = e2.getY() - e1.getY();
        LogUtils.d("onFling: distX=" + distX + ", distY=" + distY);

        if ((distX * distX + distY * distY) > FLING_MIN_DISTANCE * FLING_MIN_DISTANCE &&
                (vX * vX + vY * vY) > FLING_MIN_VELOCITY * FLING_MIN_VELOCITY) {
            LogUtils.d("#####################@: onFling: get fast moving gesture, exit...");
            fastFling = true;
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
