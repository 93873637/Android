package com.liz.wsrecorder.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

import com.liz.androidutils.LogUtils;


public class WaveSurfaceViewEx extends WaveSurfaceView implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private GestureDetectorCompat mDetector;

    public WaveSurfaceViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtils.trace();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        LogUtils.trace();
    }

    @Override
    protected void initSurfaceView() {
        super.initSurfaceView();
        LogUtils.trace();

        this.setOnTouchListener(mOnTouchListener);

        // Instantiate the gesture detector with the application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetectorCompat(this.getContext(), this);
        // Set the gesture detector as the double tap listener.
        mDetector.setOnDoubleTapListener(this);
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            LogUtils.td(event.toString());
            mDetector.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
            }
            return true;
        }
    };

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
        LogUtils.td(event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
        LogUtils.td("e1=" + e1.getAction() + ", e2=" + e2.getAction() + ", vX=" + vX + ", vY=" + vY);

        // no need move left/right when full show
        if (!canFullShow()) {
            int direction = (vX > 0) ? 1 : -1;
            int incrementX = (int) (Math.abs(vX - 200));
            updateOffset(direction * incrementX);
            drawSurface();
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        LogUtils.td(event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        LogUtils.td(e1.toString() + e2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        LogUtils.td(event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        LogUtils.td(event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        LogUtils.td(event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        LogUtils.td(event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        LogUtils.td(event.toString());
        return true;
    }
}
