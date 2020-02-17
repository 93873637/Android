package com.liz.whatsai.logic;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import com.liz.androidutils.LogUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;

@SuppressWarnings("WeakerAccess")
public class WhatsaiAudio {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Interfaces

    public static void init() {
        LogUtils.d("WhatsaiAudio:init");
        mWhatsaiAudio = new WhatsaiAudio();
    }

    public static void switchRecord() {
        if (isRecording()) {
            mWhatsaiAudio.stopRecord();
        } else {
            mWhatsaiAudio.startRecord();
        }
    }

    public static boolean isRecording() {
        return mWhatsaiAudio.mMediaRecorder != null;
    }

    public interface WhatsaiAudioCallback {
        void onAudioRecordStopped();
    }

    public static void setAudioCallback(WhatsaiAudioCallback cb) {
        mWhatsaiAudio.mAudioCallback = cb;
    }

    public static int getPlayItemPos() {
        return mPlayItemPos;
    }

    public static void startPlay(String filePath) {
        mWhatsaiAudio._startPlay(filePath);
    }

    public static void stopPlay() {
        mWhatsaiAudio._stopPlay();
    }

    // Interfaces
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static WhatsaiAudio mWhatsaiAudio;
    private MediaRecorder mMediaRecorder = null;
    private MediaPlayer mMediaPlayer = null;
    private WhatsaiAudioCallback mAudioCallback = null;
    private static int mPlayItemPos = ComDef.INVALID_LIST_POS;

    //Singleton Constructor
    private WhatsaiAudio() {
        LogUtils.d("WhatsaiAudio:WhatsaiAudio");
    }

    /**
     * 19.1103.173655
     * @return String
     */
    private String getRecordFilePath() {
        String strFileTime = new SimpleDateFormat("yy.MMdd.HHmmss").format(new java.util.Date());
        String audioFileName = strFileTime + ".m4a";
        return ComDef.WHATSAI_AUDIO_DIR + "/" + audioFileName;
    }

    private void startRecord() {
        if (mMediaRecorder != null) {
            LogUtils.d("WhatsaiAudio: startRecord: already start");
            return;
        }
        mMediaRecorder = new MediaRecorder();
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setOutputFile(getRecordFilePath());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            LogUtils.e("startRecord exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        if (mMediaRecorder == null) {
            LogUtils.d("WhatsaiAudio: stopRecord: already stopped");
            return;
        }
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        } catch (RuntimeException e) {
            LogUtils.e("WhatsaiAudio: stop record exception: " + e.toString());
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (mAudioCallback != null) {
            mAudioCallback.onAudioRecordStopped();
        }
    }

    public void _startPlay(String filePath) {
        LogUtils.d("WhatsaiAudio: _startPlay: filePath = " + filePath);
        if (mMediaPlayer != null) {
            LogUtils.d("WhatsaiAudio: _startPlay: already started, stop first");
            _stopPlay();
        }
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            LogUtils.e("_startPlay exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void _stopPlay() {
        if (mMediaPlayer == null) {
            LogUtils.d("WhatsaiAudio: _stopPlay: already stopped");
        }
        else {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
