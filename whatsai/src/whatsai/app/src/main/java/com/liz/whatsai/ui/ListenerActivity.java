package com.liz.whatsai.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.WSListener;
import com.liz.whatsai.logic.WSRecorder;

import java.util.Timer;
import java.util.TimerTask;

public class ListenerActivity extends Activity implements View.OnClickListener {

    Button mBtnSwitchListening;
    TextView mTextSpeech;
    TextView mTextProgressInfo;
    WaveSurfaceView mWaveSurfaceView;
    WSListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listener);
        LogUtils.d("ListenerActivity:onCreate");

        mListener = new WSListener();
        mListener.setCallback(mListenerCallback);
        mListener.setVoiceRecognition(true);

        mBtnSwitchListening = findViewById(R.id.btn_switch_listening);
        mBtnSwitchListening.setOnClickListener(this);
        mBtnSwitchListening.setText(mListener.isListening()?"STOP":"START");

        findViewById(R.id.btn_play_or_pause).setOnClickListener(this);
        findViewById(R.id.btn_audio_config).setOnClickListener(this);
        findViewById(R.id.btn_audio_template).setOnClickListener(this);
        findViewById(R.id.btn_audio_recorder).setOnClickListener(this);

        mTextSpeech = findViewById(R.id.text_speech);
        mTextProgressInfo = findViewById(R.id.text_progress_info);
        mWaveSurfaceView = findViewById(R.id.wave_surface_view);
        mWaveSurfaceView.setMaxWaveValue(mListener.getMaxPower());

        startUITimer();
    }

    private WSListener.ListenerCallback mListenerCallback = new WSListener.ListenerCallback() {

        @Override
        public void onListenStarted(){
        }

        @Override
        public void onListenStopped(boolean save) {
        }

        @Override
        public void onReadAudioData(final int size, final byte[] data) {
            ListenerActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    synchronized (WSRecorder.inst().getDataLock()) {
                        mWaveSurfaceView.addAudioData(data, size);
                    }
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

    private String getProgressInfo() {
        return mListener.getProgressInfo() +
                "<br>" +  mListener.getAudioConfigInfo() + " || " + mWaveSurfaceView.getSurfaceInfo();
    }

    private void updateUI() {
        mTextSpeech.setText(Html.fromHtml(mListener.getSpeechText()));
        mTextProgressInfo.setText(Html.fromHtml(this.getProgressInfo()));
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
            case R.id.btn_play_or_pause:
                onPlayAudio();
                break;
            case R.id.btn_audio_template:
                startActivity(new Intent(ListenerActivity.this, AudioTemplateActivity.class));
                this.finish();
                break;
            case R.id.btn_audio_recorder:
                startActivity(new Intent(ListenerActivity.this, AudioRecordActivity.class));
                this.finish();
                break;
            default:
                break;
        }
    }

    private void onAudioConfig() {
        //###@:
    }

    private void onSwitchListening() {
        mListener.switchListening();
        mBtnSwitchListening.setText(mListener.isListening()?"STOP":"START");
    }

    private void onPlayAudio() {
        if (mListener.isListening()) {
            Toast.makeText(this, "Can't play when listening, stop first", Toast.LENGTH_SHORT).show();
        }
        else {
            mListener.playPCMFile();
        }
    }

    @Override
    public void onBackPressed() {
        LogUtils.d("ListenerActivity:onBackPressed");
        stopUITimer();
        super.onBackPressed();
    }
}
