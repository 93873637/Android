package com.liz.whatsai.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.WSListener;
import com.liz.whatsai.logic.WSRecorder;

import java.util.Timer;
import java.util.TimerTask;

public class AudioRecordActivity extends Activity implements View.OnClickListener {

    private WaveSurfaceView mWaveSurfaceView;
    private TextView mTextProgressInfo;
    private TextView mTextAudioFilesInfo;
    private LinearLayout mAudioRecordBar;
    private Button mBtnSwitchListening;
    private AudioListView mAudioListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        LogUtils.trace();

        ((TextView)findViewById(R.id.titlebar_name)).setText("whatsai Audio Recorder");

        mBtnSwitchListening = findViewById(R.id.btn_switch_listening);
        mBtnSwitchListening.setOnClickListener(this);
        mBtnSwitchListening.setText(WSRecorder.inst().isListening()?"STOP":"START");

        findViewById(R.id.btn_switch_listening).setOnClickListener(this);
        findViewById(R.id.btn_audio_listener).setOnClickListener(this);
        findViewById(R.id.btn_audio_template).setOnClickListener(this);
        findViewById(R.id.btn_audio_config).setOnClickListener(this);

        mWaveSurfaceView = findViewById(R.id.wave_surface_view);
        mTextProgressInfo = findViewById(R.id.text_progress_info);
        mAudioRecordBar = findViewById(R.id.ll_audio_record);
        mTextAudioFilesInfo = findViewById(R.id.tv_audio_files_info);

        mWaveSurfaceView.setMaxValue(WSRecorder.inst().getMaxPower());
        mWaveSurfaceView.setWaveItemWidth(1);
        mWaveSurfaceView.setWaveItemSpace(0);

        mAudioListView = findViewById(R.id.lv_audio_files);
        mAudioListView.onCreate(this, ComDef.WHATSAI_AUDIO_DIR);

        loadAudioListInfo();
        startUITimer();
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtils.trace();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.trace();
        WSRecorder.inst().setCallback(mListenerCallback);
    }

    @Override
    public void onPause() {
        LogUtils.trace();
        WSRecorder.inst().setCallback(null);
        super.onPause();
    }

    @Override
    public void onStop() {
        LogUtils.trace();
        WSRecorder.inst().setCallback(null);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LogUtils.trace();
        WSRecorder.inst().setCallback(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        LogUtils.d("AudioTemplateActivity:onBackPressed");
        stopUITimer();
        WSRecorder.inst().setCallback(null);

        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_listening:
                onSwitchListening();
                break;
            case R.id.btn_audio_listener:
                startActivity(new Intent(AudioRecordActivity.this, ListenerActivity.class));
                this.finish();
                break;
            case R.id.btn_audio_template:
                startActivity(new Intent(AudioRecordActivity.this, AudioTemplateActivity.class));
                this.finish();
                break;
            case R.id.btn_audio_config:
                startActivity(new Intent(AudioRecordActivity.this, AudioConfigActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mAudioListView.onContextItemSelected(item)) {
            return true;
        }

        return super.onContextItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI Timer
    private static final long UI_TIMER_DELAY = 0L;
    private static final long UI_TIMER_PERIOD = 2000L;
    private Timer mUITimer;
    private void startUITimer() {
        mUITimer = new Timer();
        mUITimer.schedule(new TimerTask() {
            public void run () {
                AudioRecordActivity.this.runOnUiThread(new Runnable() {
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

    private WSListener.ListenerCallback mListenerCallback = new WSListener.ListenerCallback() {
        @Override
        public void onPowerUpdated() {
            AudioRecordActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    synchronized (WSRecorder.inst().getDataLock()) {
                        mWaveSurfaceView.onUpdateSurfaceData(WSRecorder.inst().getPowerList(), WSRecorder.inst().getMaxPower());
                    }
                }
            });
        }
    };

    private String getProgressInfo() {
        if (WSRecorder.inst().isListening()) {
            return WSRecorder.inst().getProgressInfoSimple();
        }
        else {
            return "";
        }
    }

    private void updateUI() {
        if (WSRecorder.inst().isListening()) {
            mBtnSwitchListening.setText("STOP");
            mAudioRecordBar.setBackgroundColor(Color.GREEN);
        }
        else {
            mBtnSwitchListening.setText("START");
            mAudioRecordBar.setBackgroundColor(Color.RED);
        }
        mTextProgressInfo.setText(Html.fromHtml(this.getProgressInfo()));
        setAudioFilesInfo();
        mAudioListView.updateUI();
    }

    private void loadAudioListInfo() {
        mAudioListView.updateList();
        setAudioFilesInfo();
    }

    private void updateAudioList() {
        loadAudioListInfo();
    }

    private void setAudioFilesInfo() {
        mTextAudioFilesInfo.setText(Html.fromHtml(mAudioListView.getAudioFilesInfo()));
    }

    private void onSwitchListening() {
        WSRecorder.inst().switchListening();
        updateUI();
        updateAudioList();
    }
}
