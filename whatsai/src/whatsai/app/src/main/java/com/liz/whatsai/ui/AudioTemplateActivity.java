package com.liz.whatsai.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.WSListener;

import java.util.Timer;
import java.util.TimerTask;

public class AudioTemplateActivity extends Activity implements View.OnClickListener {

    private WaveSurfaceView mWaveSurfaceView;
    private LinearLayout mAudioControlBar;
    private TextView mTextProgressInfo;
    private Button mBtnSwitchListening;
    private WSListener mListener;
    private AudioListView mAudioListView;

    private String mAudioTemplatePath = ComDef.WHATSAI_AUDIO_TEMPLATE_DIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_template);
        LogUtils.d("AudioTemplateActivity:onCreate");

        mListener = new WSListener();
        mListener.setWaveSamplingRate(1);
        mListener.setAudioPath(ComDef.WHATSAI_CACHE_DIR);

        mBtnSwitchListening = findViewById(R.id.btn_switch_listening);
        mBtnSwitchListening.setOnClickListener(this);
        mBtnSwitchListening.setText(mListener.isListening()?"STOP":"START");
        mListener.setCallback(mListenerCallback);

        findViewById(R.id.btn_switch_listening).setOnClickListener(this);
        findViewById(R.id.btn_play_audio).setOnClickListener(this);
        findViewById(R.id.btn_save_audio).setOnClickListener(this);
        findViewById(R.id.btn_audio_listener).setOnClickListener(this);
        findViewById(R.id.btn_audio_recorder).setOnClickListener(this);

        mTextProgressInfo = findViewById(R.id.text_progress_info);
        mWaveSurfaceView = findViewById(R.id.wave_surface_view);
        mAudioControlBar = findViewById(R.id.ll_audio_control_bar);

        mWaveSurfaceView.setMaxValue(mListener.getMaxPower());
        mWaveSurfaceView.setWaveItemWidth(1);
        mWaveSurfaceView.setWaveItemSpace(0);

        mAudioListView = findViewById(R.id.lv_audio_files);
        mAudioListView.onCreate(this, mAudioTemplatePath);

        startUITimer();
    }

    private WSListener.ListenerCallback mListenerCallback = new WSListener.ListenerCallback() {
        @Override
        public void onPowerUpdated() {
            AudioTemplateActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mWaveSurfaceView.onUpdateSurfaceData(mListener.getPowerList(), mListener.getMaxPower());
                }
            });
        }
    };

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
    private static final long UI_TIMER_PERIOD = 1000L;
    private Timer mUITimer;
    private void startUITimer() {
        mUITimer = new Timer();
        mUITimer.schedule(new TimerTask() {
            public void run () {
                AudioTemplateActivity.this.runOnUiThread(new Runnable() {
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
                "<br>" + mWaveSurfaceView.getSurfaceInfo() + " || " + mListener.getAudioConfigInfo();
    }

    private void updateUI() {
        mTextProgressInfo.setText(Html.fromHtml(this.getProgressInfo()));
        if (mListener.isListening()) {
            mBtnSwitchListening.setText("STOP");
            mAudioControlBar.setBackgroundColor(Color.GREEN);
        }
        else {
            mBtnSwitchListening.setText("START");
            mAudioControlBar.setBackgroundColor(Color.RED);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_listening:
                onSwitchListening();
                break;
            case R.id.btn_play_audio:
                onPlayAudio();
                break;
            case R.id.btn_save_audio:
                onSaveAudio();
                break;
            case R.id.btn_audio_listener:
                startActivity(new Intent(AudioTemplateActivity.this, ListenerActivity.class));
                this.finish();
                break;
            case R.id.btn_audio_recorder:
                startActivity(new Intent(AudioTemplateActivity.this, AudioRecordActivity.class));
                this.finish();
                break;
            default:
                break;
        }
    }

    private void onSwitchListening() {
        mListener.switchListening();
        mBtnSwitchListening.setText(mListener.isListening()?"STOP":"START");
        updateUI();
    }

    private void onPlayAudio() {
        if (mListener.isListening()) {
            Toast.makeText(this, "Can't play when listening, stop first", Toast.LENGTH_SHORT).show();
        }
        else {
            mListener.playPCMFile();
        }
    }

    private void onSaveAudio() {
        final EditText et = new EditText(this);
        et.setText(mListener.getNeatFileName());
        new AlertDialog
                .Builder(this)
                .setTitle("Save current voice as template file(*.wav): ")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (TextUtils.isEmpty(et.getText().toString())) {
                            Toast.makeText(AudioTemplateActivity.this, "Please input template name", Toast.LENGTH_LONG).show();
                        }
                        else {
                            String wavFilePath = mAudioTemplatePath + "/" + et.getText().toString() + ".wav";
                            mListener.saveWavFile(wavFilePath, true);
                            mAudioListView.updateList();
                        }
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    @Override
    public void onBackPressed() {
        LogUtils.d("AudioTemplateActivity:onBackPressed");
        stopUITimer();
        super.onBackPressed();
    }
}
