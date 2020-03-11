package com.liz.whatsai.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.WhatsaiAudio;
import com.liz.whatsai.logic.WhatsaiListener;

import java.util.Timer;
import java.util.TimerTask;

public class AudioRecordActivity extends Activity implements View.OnClickListener {

    public static final int RECORD_WAVE_SAMPLING_RATE = 1;

    private WaveSurfaceView mWaveSurfaceView;
    private TextView mTextProgressInfo;
    private TextView mTextAudioFilesInfo;
    private LinearLayout mAudioRecordBar;
    private Button mBtnSwitchListening;
    private WhatsaiListener mListener;
    private AudioListView mAudioListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);
        LogUtils.d("AudioTemplateActivity:onCreate");

        mListener = new WhatsaiListener();
        mListener.setWaveSamplingRate(RECORD_WAVE_SAMPLING_RATE);
        mListener.setAutoSave(true);

        mBtnSwitchListening = findViewById(R.id.btn_switch_listening);
        mBtnSwitchListening.setOnClickListener(this);
        mBtnSwitchListening.setText(mListener.isListening()?"STOP":"START");
        mListener.setCallback(mListenerCallback);

        findViewById(R.id.btn_switch_listening).setOnClickListener(this);
        findViewById(R.id.btn_audio_listener).setOnClickListener(this);
        findViewById(R.id.btn_audio_template).setOnClickListener(this);

        mWaveSurfaceView = findViewById(R.id.wave_surface_view);
        mTextProgressInfo = findViewById(R.id.text_progress_info);
        mAudioRecordBar = findViewById(R.id.ll_audio_record);
        mTextAudioFilesInfo = findViewById(R.id.tv_audio_files_info);

        mWaveSurfaceView.setMaxValue(mListener.getMaxPower());
        mWaveSurfaceView.setWaveItemWidth(1);
        mWaveSurfaceView.setWaveItemSpace(0);

        mAudioListView = findViewById(R.id.lv_audio_files);
        mAudioListView.onCreate(this, ComDef.WHATSAI_AUDIO_DIR);

        startUITimer();
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
            default:
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //###@:todo: improve it by save audio file as wave file and play it
//        if (mAudioListView.onContextItemSelected(item)) {
//            return true;
//        }
//
//        return super.onContextItemSelected(item);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int itemId = item.getItemId();
        if (itemId == ComDef.AudioListMenu.PLAY.id) {
            String filePath = mAudioListView.getAudioFilePath((int)info.id);
            mListener.playAudio(filePath);
            //WhatsaiAudio.startPlay(filePath);
            return true;
        }
        else if (itemId == ComDef.AudioListMenu.STOP.id) {
            WhatsaiAudio.stopPlay();
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
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

    private WhatsaiListener.ListenerCallback mListenerCallback = new WhatsaiListener.ListenerCallback() {
        @Override
        public void onPowerUpdated() {
            AudioRecordActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    synchronized (mListener.mDataLock) {
                        mWaveSurfaceView.onUpdateSurfaceData(mListener.getPowerList(), mListener.getMaxPower());
                    }
                }
            });
        }
    };

    private String getProgressInfo() {
        if (mListener.isListening()) {
            return mListener.getProgressInfoSimple();
        }
        else {
            return "";
        }
    }

    private void updateUI() {
        if (mListener.isListening()) {
            mBtnSwitchListening.setText("STOP");
            mAudioRecordBar.setBackgroundColor(Color.GREEN);
        }
        else {
            mBtnSwitchListening.setText("START");
            mAudioRecordBar.setBackgroundColor(Color.RED);
        }
        mTextProgressInfo.setText(Html.fromHtml(this.getProgressInfo()));
        updateAudioList();
    }

    private void updateAudioList() {
        mAudioListView.updateList();
        setAudioFilesInfo();
    }

    private void setAudioFilesInfo() {
        mTextAudioFilesInfo.setText(Html.fromHtml(mAudioListView.getAudioFilesInfo()));
    }

    private void onSwitchListening() {
        mListener.switchListening();
        updateUI();
    }

    @Override
    public void onBackPressed() {
        LogUtils.d("AudioTemplateActivity:onBackPressed");
        stopUITimer();
        super.onBackPressed();
    }
}
