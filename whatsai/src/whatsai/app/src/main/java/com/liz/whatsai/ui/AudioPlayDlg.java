package com.liz.whatsai.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.WSRecorder;

import java.io.File;

/**
 * AudioPlayDlg.java
 * Dialog for Add Task and Task Group
 * Created by admin on 2018/9/28.
 */

@SuppressWarnings("WeakerAccess")
class AudioPlayDlg extends Dialog {

    private WaveSurfaceViewEx mWaveSurfaceView;
    private WaveSurfaceViewEx mWaveSurfaceThumbnail;
    private File mAudioFile;

    public static void onPlayAudio(Context context, File audioFile) {
        new AudioPlayDlg(context).openDlg(audioFile);
    }

    private AudioPlayDlg(Context context) {
        super(context);
        LogUtils.trace();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.trace();
        View layout = View.inflate(getContext(), R.layout.audio_play_dlg, null);
        this.setContentView(layout);
    }

    private void openDlg(File audioFile) {
        LogUtils.trace();
        this.show();  // this will call onCreate
        mAudioFile = audioFile;
        Window dlgWindow = this.getWindow();
        if (dlgWindow == null) {
            LogUtils.te2("dlgWindow null");
            return;
        }

        WindowManager.LayoutParams layoutParams = dlgWindow.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dlgWindow.setAttributes(layoutParams);

        TextView tvAudioInfo = findViewById(R.id.text_audio_info);
        tvAudioInfo.setText(audioFile.getName());

        mWaveSurfaceView = findViewById(R.id.wave_surface_view);
        mWaveSurfaceView.setBackground(0xff, 0x60, 0x60, 0x60);
        mWaveSurfaceView.setMaxWaveValue(WSRecorder.inst().getMaxPower());
        mWaveSurfaceView.setWaveSamplingRate(ComDef.AUDIO_RECORD_WAVE_SAMPLING_RATE);

        mWaveSurfaceThumbnail = findViewById(R.id.wave_surface_thumbnail);
        mWaveSurfaceThumbnail.setBackground(0xff, 0xb0, 0xb0, 0xb0);
        mWaveSurfaceThumbnail.setMaxWaveValue(WSRecorder.inst().getMaxPower());
        mWaveSurfaceThumbnail.setDrawGrid(false);
        mWaveSurfaceThumbnail.setFullMode(true);
        mWaveSurfaceThumbnail.setWaveFile(audioFile);
    }
}
