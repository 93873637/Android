package com.liz.whatsai.ui;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Html;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.WhatsaiListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ListenerActivity extends Activity implements View.OnClickListener {

    private final int POWER_MAX = 256;
    private final int POWER_ITEM_WIDTH = 1;  // unit by pixel
    private final int POWER_ITEM_SPACE = 0;  // unit by pixel
    private final int POWER_UNIT_WIDTH = POWER_ITEM_WIDTH + POWER_ITEM_SPACE;
    private final int POWER_ITEM_COLOR = Color.rgb(79, 208, 89);
    private final int CANVAS_BG_A = 255;
    private final int CANVAS_BG_R = 43;
    private final int CANVAS_BG_G = 43;
    private final int CANVAS_BG_B = 43;
    private final int CANVAS_GRID_COLOR = Color.rgb(212, 212, 212);
    private final int[] CANVAS_GRID_Y = {64, 128, 192};

    Button mBtnSwitchListening;
    TextView mTextAudioConfig;
    TextView mTextProgressInfo;
    SurfaceView mWaveSurfaceView;

    Paint mPowerPaint;
    Paint mGridPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listener);
        LogUtils.d("ListenerActivity:onCreate");

        findViewById(R.id.titlebar_menu).setOnClickListener(this);
        findViewById(R.id.titlebar_close).setOnClickListener(this);

        mBtnSwitchListening = findViewById(R.id.btn_switch_listening);
        mBtnSwitchListening.setOnClickListener(this);
        mBtnSwitchListening.setText(WhatsaiListener.isListening()?"STOP":"START");

        findViewById(R.id.btn_play_audio).setOnClickListener(this);
        findViewById(R.id.btn_audio_config).setOnClickListener(this);

        mTextAudioConfig = findViewById(R.id.text_audio_config);
        mTextProgressInfo = findViewById(R.id.text_progress_info);
        mWaveSurfaceView = findViewById(R.id.wave_surface_view);

        startUITimer();
        WhatsaiListener.setCallback(mListenerCallback);

        initCanvas();
    }

    private void initCanvas() {
        mPowerPaint = new Paint();
        mPowerPaint.setColor(POWER_ITEM_COLOR);
        mGridPaint = new Paint();
        mGridPaint.setColor(CANVAS_GRID_COLOR);
    }

    private WhatsaiListener.ListenerCallback mListenerCallback = new WhatsaiListener.ListenerCallback() {
        @Override
        public void onPowerUpdated() {
            ListenerActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    updateWaveSurface();
                }
            });
        }
    };

    private void updateWaveSurface() {
        Canvas canvas = mWaveSurfaceView.getHolder().lockCanvas(
                new Rect(0, 0, mWaveSurfaceView.getWidth(), mWaveSurfaceView.getHeight()));
        if (canvas == null) {
            LogUtils.e("ERROR: updateWaveSurface: canvas null");
            return;
        }

        int canvasWidth = mWaveSurfaceView.getWidth();
        int canvasHeight = mWaveSurfaceView.getHeight();

        // draw background
        canvas.drawARGB(CANVAS_BG_A, CANVAS_BG_R, CANVAS_BG_G, CANVAS_BG_B);

        // draw grid
        {
            int h;
            for (int y : CANVAS_GRID_Y) {
                h = canvasHeight * y / POWER_MAX;
                canvas.drawLine(0, h, canvasWidth, h, mGridPaint);
            }
        }

        int listSize = WhatsaiListener.getPowerListSize();
        if (listSize <= 0) {
            LogUtils.i("No power list");
        }
        else {
            int lastPower = WhatsaiListener.getLastPower();
            LogUtils.d("updateWaveSurface: listSize = " + listSize + ", lastPower = " + lastPower);
            List<Integer> powerList = WhatsaiListener.getPowerList();

            int maxVisibleSize = canvasWidth / POWER_UNIT_WIDTH;
            int startIndex = 0;
            int showSize = listSize;
            if (listSize > maxVisibleSize) {
                //canvas not enough to show all powers, only show last items
                startIndex = listSize - maxVisibleSize;
                showSize = maxVisibleSize;
            }
            LogUtils.d("updateWaveSurface: startIndex = " + startIndex + ", showSize = " + showSize);

            int powerHeight;
            if (POWER_ITEM_WIDTH == 1) {
                // just draw a line for one power value
                int posX, posY;
                for (int i = 0; i < showSize; i++) {
                    powerHeight = canvasHeight * powerList.get(i+startIndex) / POWER_MAX;
                    posX = i * POWER_UNIT_WIDTH;
                    posY = canvasHeight - powerHeight;
                    canvas.drawLine(posX, posY, posX, canvasHeight, mPowerPaint);
                }
            }
            else {
                //draw a rect for one power value
                //left, top, right, bottom of rect
                int l, t, r, b;
                for (int i = 0; i < showSize; i++) {
                    powerHeight = canvasHeight * powerList.get(i+startIndex) / POWER_MAX;
                    l = i * POWER_UNIT_WIDTH;
                    r = l + POWER_ITEM_WIDTH;
                    t = canvasHeight - powerHeight;
                    b = canvasHeight;
                    canvas.drawRect(new Rect(l, t, r, b), mPowerPaint);
                }
            }
        }

        mWaveSurfaceView.getHolder().unlockCanvasAndPost(canvas);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI Timer
    private static final long UI_TIMER_DELAY = 1000L;
    private static final long UI_TIMER_PERIOD = 2000L;
    private Timer mUITimer;
    private void startUITimer() {
        mUITimer = new Timer();
        mUITimer.schedule(new TimerTask() {
            public void run () {
                ListenerActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, UI_TIMER_DELAY, UI_TIMER_PERIOD);
    }
    private void stopUITimer() {
        if (mUITimer != null) {
            mUITimer.cancel();
            mUITimer = null;
        }
    }
    // UI Timer
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void updateUI() {
        mTextAudioConfig.setText(Html.fromHtml(WhatsaiListener.getConfigInfo()));
        mTextProgressInfo.setText(Html.fromHtml(WhatsaiListener.getProgressInfo()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_audio_config:
                onAudioConfig();
                break;
            case R.id.btn_switch_listening:
                onSwitchListening();
                break;
            case R.id.btn_play_audio:
                onPlayAudio();
                break;
            case R.id.titlebar_menu:
            case R.id.titlebar_close:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void onAudioConfig() {
        //###@:
    }

    private void onSwitchListening() {
        WhatsaiListener.switchListening();
        mBtnSwitchListening.setText(WhatsaiListener.isListening()?"STOP":"START");
    }

    private void onPlayAudio() {
        if (WhatsaiListener.isListening()) {
            Toast.makeText(this, "Can't play when listening, stop first", Toast.LENGTH_SHORT).show();
        }
        else {
            WhatsaiListener.playAudio();
        }
    }

    @Override
    public void onBackPressed() {
        LogUtils.d("ListenerActivity:onBackPressed");
        stopUITimer();
        super.onBackPressed();
    }
}
