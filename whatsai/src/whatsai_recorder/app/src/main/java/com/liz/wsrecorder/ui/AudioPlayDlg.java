package com.liz.wsrecorder.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.wsrecorder.R;
import com.liz.wsrecorder.logic.ComDef;
import com.liz.wsrecorder.logic.WSPlayer;
import com.liz.wsrecorder.logic.WSRecorder;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * AudioPlayDlg.java
 * Dialog for Add Task and Task Group
 * Created by admin on 2018/9/28.
 */

@SuppressWarnings("WeakerAccess")
class AudioPlayDlg extends Dialog {

    private WaveSurfaceViewEx mWaveSurfaceView;
    private WaveSurfaceViewEx mWaveSurfaceThumbnail;

    //progress bar
    private TextView mTvPlayProgressInfo;
    private SeekBar mSbPlayProgress;

    private Button mBtnPlayOrPause;

    private File mAudioFile;
    private WSPlayer mPlayer = new WSPlayer();

    private boolean mAutoStart = false;

    public static void openPlayDlg(Activity activity, File audioFile) {
        openPlayDlg(activity, audioFile, false);
    }

    public static void openPlayDlg(Activity activity, File audioFile, boolean autoStart) {
        new AudioPlayDlg(activity).openDlg(audioFile, autoStart);
    }

    private AudioPlayDlg(Activity activity) {
        super(activity);
        LogUtils.trace();
        this.setOwnerActivity(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.trace();
        View layout = View.inflate(getContext(), R.layout.audio_play_dlg, null);
        this.setContentView(layout);
    }

    private void openDlg(File audioFile, boolean autoStart) {
        LogUtils.trace();
        this.show();  // this will call onCreate
        mAudioFile = audioFile;
        Window dlgWindow = this.getWindow();
        if (dlgWindow == null) {
            Toast.makeText(this.getContext(), "ERROR: open play dlg null", Toast.LENGTH_LONG).show();
            LogUtils.te2("dlgWindow null");
            this.dismiss();
            return;
        }

        if (!mPlayer.load(mAudioFile.getAbsolutePath())) {
            Toast.makeText(this.getContext(), "ERROR: load " + mAudioFile + " failed", Toast.LENGTH_LONG).show();
            LogUtils.te2("load " + mAudioFile + " failed");
            this.dismiss();
            return;
        }

        mPlayer.setOnCompletionListener(mOnCompletionListener);
        mPlayer.setOnErrorListener(mOnErrorListener);

        WindowManager.LayoutParams layoutParams = dlgWindow.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dlgWindow.setAttributes(layoutParams);

        TextView tvAudioInfo = findViewById(R.id.text_audio_info);
        tvAudioInfo.setText(getAudioTitle());

        mWaveSurfaceView = findViewById(R.id.wave_surface_view);
        mWaveSurfaceView.setBackground(0xff, 0x60, 0x60, 0x60);
        mWaveSurfaceView.setMaxWaveValue(WSRecorder.inst().getMaxPower());
        mWaveSurfaceView.setWaveSamplingRate(ComDef.AUDIO_RECORD_WAVE_SAMPLING_RATE);

        mWaveSurfaceThumbnail = findViewById(R.id.wave_surface_thumbnail);
        mWaveSurfaceThumbnail.setBackground(0xff, 0xb0, 0xb0, 0xb0);
        mWaveSurfaceThumbnail.setWaveItemColor(Color.rgb(0x00, 0x00, 0xff));
        mWaveSurfaceThumbnail.setCanvasMiddleGridColor(Color.rgb(0x00, 0x00, 0x00));
        mWaveSurfaceThumbnail.setMaxWaveValue(WSRecorder.inst().getMaxPower());
        mWaveSurfaceThumbnail.setDrawGrid(false);
        mWaveSurfaceThumbnail.setFullMode(true);
        mWaveSurfaceThumbnail.setWaveFile(audioFile);

        mTvPlayProgressInfo = findViewById(R.id.tv_play_progress_info);

        mSbPlayProgress = findViewById(R.id.sb_play_progress);
        mSbPlayProgress.setMax(mPlayer.getDuration());
        mSbPlayProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                LogUtils.trace();
                stopUITimer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                int max = seekBar.getMax();
                LogUtils.td("onStopTrackingTouch: progress = " + progress + "/" + max);
                mPlayer.seekTo(progress);
                startUITimer();
            }
        });

        mBtnPlayOrPause = findViewById(R.id.btn_play_or_pause);
        findViewById(R.id.btn_play_or_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlayOrPause();
            }
        });
        findViewById(R.id.btn_stop_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStopPlay();
            }
        });
        findViewById(R.id.btn_close_dlg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCloseDlg();
            }
        });

        mTvPlayProgressInfo.setText(mPlayer.getProgressInfo());

        this.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                LogUtils.trace();
                stopUITimer();
                mPlayer.stop();
                mPlayer.release();
            }
        });

        if (autoStart) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Activity activity = AudioPlayDlg.this.getOwnerActivity();
                    LogUtils.td("owner activity = " + activity);
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onPlay();
                            }
                        });
                    }
                }
            }, 1000L);
        }
    }

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            LogUtils.trace();
            onStopPlay();
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogUtils.td("onError: what=" + what + ", extra=" + extra);
            //###@:
            return true;  // return true in case calling onCompletion
        }
    };

    private String getAudioTitle() {
        return mAudioFile.getName() + " / " + FileUtils.getFileSizeFormat(mAudioFile);
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
                Activity activity = AudioPlayDlg.this.getOwnerActivity();
                LogUtils.td("owner activity = " + activity);
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AudioPlayDlg.this.updateUI();
                        }
                    });
                }
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
        mTvPlayProgressInfo.setText(mPlayer.getProgressInfo());
        mSbPlayProgress.setProgress(mPlayer.getCurrentPosition());
        mBtnPlayOrPause.setText(mPlayer.isPlaying()?"PAUSE":"PLAY");
    }

    private void onPlayOrPause() {
        if (mPlayer.isPlaying()) {
            onPlay();
        }
        else {
            onPause();
        }
    }

    private void onPlay() {
        mPlayer.start();
        startUITimer();
        updateUI();
    }

    private void onPause() {
        mPlayer.pause();
        stopUITimer();
        updateUI();
    }

    private void onStopPlay() {
        mPlayer.stop();
        stopUITimer();
        mPlayer.load(mAudioFile.getAbsolutePath());
        updateUI();
    }

    private void onCloseDlg() {
        LogUtils.trace();
        this.dismiss();
    }
}
