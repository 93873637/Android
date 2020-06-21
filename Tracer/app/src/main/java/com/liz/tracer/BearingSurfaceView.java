package com.liz.tracer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.liz.androidutils.AudioUtils;
import com.liz.androidutils.LogUtils;

public class BearingSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public BearingSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtils.trace();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtils.trace();
        initSurfaceView();
    }

    protected void initSurfaceView() {
        LogUtils.trace();
        Canvas canvas = this.getHolder().lockCanvas(
                new Rect(0, 0, this.getWidth(), this.getHeight()));
        if (canvas == null) {
            LogUtils.e("ERROR: initSurfaceView: canvas null");
        }
        else {
            //drawCanvas(canvas, mDataList);
            this.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

}
