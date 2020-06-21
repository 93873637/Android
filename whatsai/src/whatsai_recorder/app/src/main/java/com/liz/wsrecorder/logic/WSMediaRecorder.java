package com.liz.wsrecorder.logic;

import android.media.MediaRecorder;

import java.io.IOException;

public class WSMediaRecorder {
    private boolean isRecord = false;

    private MediaRecorder mMediaRecorder;

    private WSMediaRecorder() {
    }

    private static WSMediaRecorder mInstance;

    public synchronized static WSMediaRecorder getInstance() {
        if (mInstance == null)
            mInstance = new WSMediaRecorder();
        return mInstance;
    }

    public int start() {
        if (mMediaRecorder == null) {
            createMediaRecord();
        }
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            // 让录制状态为true
            isRecord = true;
            return 0;
        } catch (IOException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public void stop() {
        if (mMediaRecorder != null) {
            System.out.println("stopRecord");
            isRecord = false;
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void createMediaRecord() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile("/sdcard/test.aac");
    }
}
