package com.liz;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.liz.androidutils.LogUtils;

public class ShapeButton extends Button {

    public ShapeButton(Context context) {
        this(context, null);
    }

    public ShapeButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        //textView.animate().scaleX(0.8f).scaleY(0.8f).setDuration(500).start();
                        LogUtils.td("ACTION_DOWN");
                        onActionDown();
                        break;
                    case MotionEvent.ACTION_UP:
                        //textView.animate().scaleX(1).scaleY(1).setDuration(500).start();
                        LogUtils.td("ACTION_UP");
                        onActionUp();
                        break;
                }
                return performClick();
            }
        });
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void onActionDown() {
        //todo: override
    }

    public void onActionUp() {
        //todo: override
    }
}
