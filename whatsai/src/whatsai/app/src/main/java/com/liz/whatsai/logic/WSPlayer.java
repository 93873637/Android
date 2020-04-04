package com.liz.whatsai.logic;

import android.media.MediaPlayer;

import com.liz.androidutils.LogUtils;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class WSPlayer {

    private MediaPlayer mMediaPlayer = null;

    public void startPlay(String filePath) {
        startPlay(filePath, null);
    }

    public void startPlay(String filePath, MediaPlayer.OnCompletionListener listener) {
        LogUtils.td("filePath = " + filePath);
        if (mMediaPlayer != null) {
            LogUtils.td("play already started, stop first");
            stopPlay();
        }
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            LogUtils.e("play ex = " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopPlay() {
        if (mMediaPlayer == null) {
            LogUtils.td("play already stopped");
        }
        else {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public int getCurrentPlayPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getCurrentPlayDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }
}
