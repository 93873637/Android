package com.liz.testcamera.ui;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.liz.testcamera.logic.DataLogic;

/**
 * ResizableSurfaceView:
 * Created by liz on 2018/12/27.
 */

public class ResizableSurfaceView extends SurfaceView {

    private int mWidth = -1;
    private int mHeight = -1;

    public ResizableSurfaceView(Context context) {
        super(context);
    }

    public ResizableSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizableSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void adjustSizeForPreview(int prevWidth, int prevHeight) {
        //get long and short edge of preview size and old view size
        int prevLong = Math.max(prevWidth, prevHeight);
        int prevShort = Math.min(prevWidth, prevHeight);

        int orgWidth = this.getWidth();
        int orgHeight = this.getHeight();
        int orgLong = Math.max(orgWidth, orgHeight);
        int orgShort = Math.min(orgWidth, orgHeight);

        //align long preview with long view, short with short
        //take max scale to ensure all edges included
        double longScale = 1.0 * prevLong / orgLong;
        double shortScale = 1.0 * prevShort / orgShort;
        double scale = Math.max(longScale, shortScale);

        //since phone default orientation is portrait, take long as height, short as width
        int newWidth = (int)(prevShort/scale);
        int newHeight = (int)(prevLong/scale);

        this.resize(newWidth, newHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (-1 == mWidth || -1 == mHeight) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(mWidth, mHeight);
        }
    }

    public void resize(int width, int height) {
        mWidth = width;
        mHeight = height;
        getHolder().setFixedSize(width, height);
        requestLayout();
        invalidate();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                Point point = new Point((int) event.getX(), (int) event.getY());
                DataLogic.focusOnArea(point);
                break;
        }

        performClick();
        return true;
    }
}
