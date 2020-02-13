package com.liz.whatsai.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.WhatsaiListener;

import java.util.Timer;
import java.util.TimerTask;

public class ListenerActivity extends Activity implements View.OnClickListener {

    Button mBtnSwitchListening;
    TextView mTextAudioConfig;
    TextView mTextProgressInfo;
    WaveSurfaceView mWaveSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listener);
        LogUtils.d("ListenerActivity:onCreate");

        mBtnSwitchListening = findViewById(R.id.btn_switch_listening);
        mBtnSwitchListening.setOnClickListener(this);
        mBtnSwitchListening.setText(WhatsaiListener.isListening()?"STOP":"START");

        findViewById(R.id.btn_play_audio).setOnClickListener(this);
        findViewById(R.id.btn_audio_config).setOnClickListener(this);

        mTextAudioConfig = findViewById(R.id.text_audio_config);
        mTextProgressInfo = findViewById(R.id.text_progress_info);
        mWaveSurfaceView = findViewById(R.id.wave_surface_view);
        mWaveSurfaceView.setMaxValue(WhatsaiListener.getMaxPower());

        startUITimer();
        WhatsaiListener.setCallback(mListenerCallback);
    }

    private WhatsaiListener.ListenerCallback mListenerCallback = new WhatsaiListener.ListenerCallback() {
        @Override
        public void onPowerUpdated() {
            ListenerActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mWaveSurfaceView.onUpdateSurfaceData(WhatsaiListener.getPowerList(), WhatsaiListener.getMaxPower());
                }
            });
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI Timer
    private static final long UI_TIMER_DELAY = 0L;
    private static final long UI_TIMER_PERIOD = 1000L;
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
