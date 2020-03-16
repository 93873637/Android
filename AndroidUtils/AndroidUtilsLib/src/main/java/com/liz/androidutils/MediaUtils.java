package com.liz.androidutils;

import android.media.MediaPlayer;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * MediaUtils:
 * Created by liz on 2019/1/31.
 */

@SuppressWarnings("unused, WeakerAccess")
public class MediaUtils {

    public static int getWaveDuration() {
        File source = new File("C:\\Users\\5eece771f85d4c0a8ecbf510e078f697.wav");
        Clip clip = AudioSystem.getClip();
        AudioInputStream ais = AudioSystem.getAudioInputStream(source);
        clip.open(ais);
        System.out.println(clip.getMicrosecondLength() / 1000000D + " s");
    }

    public static int getMediaDuration(File file) {
        if (file == null) {
            return -1;
        }
        if (!file.exists()) {
            return -2;
        }
        if (!file.isFile()) {
            return -3;
        }
        return getMediaDuration(file.getAbsolutePath());
    }

    /**
     *
     * @param filePath: media file name with full path
     * @return media duration, unit by milliseconds
     */
    public static int getMediaDuration(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return -1;
        }
        MediaPlayer mediaPlayer = null;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            return mediaPlayer.getDuration();
        } catch (Exception e) {
            System.out.println("ERROR: getMediaDuration exception: " + e.toString());
            return -2;
        } finally {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
    }

    public static String getMediaDurationFormat(File file) {
        return TimeUtils.formatDuration(getMediaDuration(file));
    }

    public static String getMediaDurationFormat(File file, boolean fullFormat) {
        if (fullFormat) {
            return TimeUtils.formatDurationFull(getMediaDuration(file));
        }
        else {
            return TimeUtils.formatDuration(getMediaDuration(file));
        }
    }

    public static String getMediaDurationFormat(String filePath) {
        return TimeUtils.formatDuration(getMediaDuration(filePath));
    }

    public static String getMediaDurationOfUri(String mUri) {
        String duration = null;
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();

        try {
            if (mUri != null) {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("User-Agent",
                        "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1");
                mmr.setDataSource(mUri, headers);
            }

            duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception ex) {
            LogUtils.e("ERROR: MediaUtils.getMediaDuring: ex=" + ex.toString());
        } finally {
            mmr.release();
        }
        return duration;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TEST MAIN

    public static void main(String[] args) {

        System.out.println("\n***Test Begin...");

        System.out.println("***Test Successfully.");
    }

}
