package com.liz.whatsai.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.WSListenService;
import com.liz.whatsai.logic.WSListener;
import com.liz.whatsai.logic.WSRecorder;

import java.util.Timer;
import java.util.TimerTask;

public class AudioRecordActivity extends Activity implements View.OnClickListener {

    private static final double RECORDER_WAVE_ITEM_WIDTH = 1;
    private static final double RECORDER_WAVE_ITEM_SPACE = 0;
    private final static int THUMBNAIL_MIN_WIDTH = 20;

    private WaveSurfaceViewEx mWaveSurfaceViewEx;
    private WaveSurfaceViewEx mWaveSurfaceThumbnial;
    private Button mBtnCanvasThumbnail;
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

        mBtnSwitchListening = findViewById(R.id.btn_switch_listening);
        mBtnSwitchListening.setOnClickListener(this);
        mBtnSwitchListening.setText(WSRecorder.inst().isListening()?"STOP":"START");

        findViewById(R.id.btn_switch_listening).setOnClickListener(this);
        findViewById(R.id.btn_audio_listener).setOnClickListener(this);
        findViewById(R.id.btn_audio_template).setOnClickListener(this);
        findViewById(R.id.btn_audio_config).setOnClickListener(this);
        findViewById(R.id.text_reload_file_list).setOnClickListener(this);

        mTextProgressInfo = findViewById(R.id.text_progress_info);
        mAudioRecordBar = findViewById(R.id.ll_audio_record);
        mTextAudioFilesInfo = findViewById(R.id.tv_audio_files_info);

        mWaveSurfaceViewEx = findViewById(R.id.wave_surface_view);
        mWaveSurfaceViewEx.setMaxWaveValue(WSRecorder.inst().getMaxPower());
        mWaveSurfaceViewEx.setWaveSamplingRate(ComDef.AUDIO_RECORD_WAVE_SAMPLING_RATE);
        mWaveSurfaceViewEx.setWaveItemWidth(RECORDER_WAVE_ITEM_WIDTH);
        mWaveSurfaceViewEx.setWaveItemSpace(RECORDER_WAVE_ITEM_SPACE);

        mWaveSurfaceThumbnial = findViewById(R.id.wave_surface_thumbnail);
        mWaveSurfaceThumbnial.setBackground(0xff, 0x60, 0x60, 0x60);
        mWaveSurfaceThumbnial.setMaxWaveValue(WSRecorder.inst().getMaxPower());
        mWaveSurfaceThumbnial.setWaveSamplingRate(ComDef.AUDIO_RECORD_WAVE_SAMPLING_RATE * 1024);
        mWaveSurfaceThumbnial.setWaveItemWidth(RECORDER_WAVE_ITEM_WIDTH);
        mWaveSurfaceThumbnial.setWaveItemSpace(RECORDER_WAVE_ITEM_SPACE);
        mWaveSurfaceThumbnial.setMaxListSize(65536);
        mWaveSurfaceThumbnial.setFullMode(true);
        mWaveSurfaceThumbnial.setDrawGrid(false);

        mAudioListView = findViewById(R.id.lv_audio_files);
        mAudioListView.onCreate(this, ComDef.WHATSAI_AUDIO_DIR);

        initThumbnail();

        loadAudioListInfo();
        startUITimer();
    }

    private void initThumbnail() {
        mWaveSurfaceThumbnial = findViewById(R.id.wave_surface_thumbnail);
        mBtnCanvasThumbnail = findViewById(R.id.btn_canvas_thumbnail);
    }

    private static void setViewWidth(View v, int width) {
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.width = width;
        v.setLayoutParams(layoutParams);
    }

    private void updateThumbnail(int waveLen, int canvasLen) {
        LogUtils.td("waveLen = " + waveLen + ", canvasLen = " + canvasLen);
        if (waveLen > canvasLen) {
            int canvasWidth = mWaveSurfaceThumbnial.getWidth() * canvasLen / waveLen;
            if (canvasWidth < THUMBNAIL_MIN_WIDTH) canvasWidth = THUMBNAIL_MIN_WIDTH;
            LogUtils.td("canvasWidth = " + canvasWidth);
            setViewWidth(mBtnCanvasThumbnail, canvasWidth);
        }
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
            case R.id.text_reload_file_list:
                Toast.makeText(AudioRecordActivity.this, "Reloading...", Toast.LENGTH_SHORT).show();
                mAudioListView.updateList();
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
                        mWaveSurfaceViewEx.updateSurface(WSRecorder.inst().getPowerList(), WSRecorder.inst().getMaxPower());
                    }
                }
            });
        }

        @Override
        public void onReadAudioData(final int size, final byte[] data) {
            AudioRecordActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    synchronized (WSRecorder.inst().getDataLock()) {
                        mWaveSurfaceViewEx.addAudioData(data, size);
                        mWaveSurfaceViewEx.redrawSurface();
                        mWaveSurfaceThumbnial.addAudioData(data, size);
                        //mWaveSurfaceThumbnial.redrawSurface();  //not update thumbnail surface on time
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
            mWaveSurfaceThumbnial.redrawSurface();
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
        if (WSRecorder.inst().isListening()) {
            mWaveSurfaceViewEx.clearCanvas();
            mWaveSurfaceThumbnial.clearCanvas();
        }
        WSListenService.switchOnOff();
        updateUI();
        updateAudioList();
    }
}
