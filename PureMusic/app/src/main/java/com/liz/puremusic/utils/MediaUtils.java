package com.liz.puremusic.utils;

import android.media.MediaPlayer;

import com.liz.androidutils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * MediaUtils:
 * Created by liz on 2019/1/31.
 */

public class MediaUtils {
    private static final String DATE_FORMAT_PATTERN = "mm:ss";

    public static String getMediaDuration(String filePath) {
        if (filePath == null) {
            return "";
        }

        int duration = 0;
        MediaPlayer mediaPlayer = null;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
        } catch (Exception e) {
            LogUtils.e("ERROR: getMediaDuration exception: " + e.toString());
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        return duration>0?formatDuration(duration):"";
    }

    public static String getMediaDurationOfUri(String mUri) {
        String duration = null;
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();

        try {
            if (mUri != null) {
                HashMap<String, String> headers = null;
                if (headers == null) {
                    headers = new HashMap<String, String>();
                    headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1");
                }
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

    public static String formatDuration(int duration) {
        return new SimpleDateFormat(DATE_FORMAT_PATTERN).format(duration);
    }
}
