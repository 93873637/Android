package com.liz.wsrecorder.logic;

import android.media.MediaPlayer;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TimeUtils;

@SuppressWarnings("WeakerAccess")
public class WSPlayer extends MediaPlayer {

    public static void play(final String filePath, OnCompletionListener completionListener) {
        WSPlayer player = new WSPlayer();
        player.setOnCompletionListener(completionListener);
        player.load(filePath);
        player.start();
    }

    public WSPlayer() {
        this.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                LogUtils.trace();
            }
        });
    }

    public boolean load(String filePath) {
        LogUtils.td("load filePath = " + filePath);
        try {
            this.seekTo(0);
            this.reset();
            this.setDataSource(filePath);
            this.prepare();
            return true;
        } catch (Exception e) {
            LogUtils.te2("load " + filePath + " failed, ex = " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getCurrentPlayPositionFormat() {
        return TimeUtils.formatDurationFull(this.getCurrentPosition());
    }

    public String getMediaDurationFormat() {
        return TimeUtils.formatDurationFull(this.getDuration());
    }

    /**
     * @return percent of play progress
     */
    public String getProgressInfo() {
        return getCurrentPlayPositionFormat() + " / " + getMediaDurationFormat();
    }
}
