package com.liz;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.liz.androidutils.LogUtils;

public class SwitchButton extends ShapeButton {

    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void onActionDown() {
        if (NumReader.isPlaying()) {
            setBackgroundResource(R.drawable.bg_circle_green_pressed);
        }
        else {
            setBackgroundResource(R.drawable.bg_circle_red_pressed);
        }
    }

    public void onActionUp() {
        NumReader.switchPlayPause();
        setBackgroundResource(NumReader.isPlaying()? R.drawable.bg_circle_green : R.drawable.bg_circle_red);
    }
}
