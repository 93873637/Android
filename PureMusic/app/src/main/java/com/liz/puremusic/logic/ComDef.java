package com.liz.puremusic.logic;

import android.os.Environment;

import java.io.File;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

public class ComDef {

    /**
     *  CONSTANT DEFINITIONS
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static final String APP_NAME = "PureMusic";
    public static final String PURE_MUSIC_DEFAULT_PATH = Environment.getExternalStorageDirectory().getPath() + "/0.sd/Music";
    public static final String DEFAULT_MUSIC_FILE = PURE_MUSIC_DEFAULT_PATH + "/music.mp3";

    public static final int PLAYUI_REFRESH_TIMER = 1000;  //unit by milli-seconds
    public static final int PLAYLIST_TIMER_DELAY = 1000;   //unit by ms
    public static final int PLAYLIST_TIMER_PERIOD = 1000;  //unit by ms

    public static final String DATE_FORMAT_PATTERN = "mm:ss";
    public static final String INVALID_DURATION_FORMAT = "FF:FF";
    public static final String START_POSITION_FORMAT = "00:00";
    public static final int INVALID_DURATION = 0xFFFF;
    public static final int PLAY_START_POSITION = 0;

    public static final int PLAY_MODE_LIST = 0;   //play list one by one, stop at end
    public static final int PLAY_MODE_LIST_LOOP = 1;   //play list one by one, loop at end
    public static final int PLAY_MODE_RANDOM = 2;   //play a random one of list each time
    public static final int PLAY_MODE_SINGLE = 3;   //only play current music and only once
    public static final int PLAY_MODE_SINGLE_LOOP = 4;   //only play current music and loop
    public static final int PLAY_MODE_DEFAULT = PLAY_MODE_LIST_LOOP;
    public static final String PLAY_MODE_NAME[] = {
           "顺序播放", "循环播放", "随机播放", "单曲循环", "单曲播放"
    };

    public static final int PLAY_STATUS_NO_SERVICE = 0;
    public static final int PLAY_STATUS_IDLE = 1;
    public static final int PLAY_STATUS_INITIALIZED = 2;
    public static final int PLAY_STATUS_PREPARED = 3;
    public static final int PLAY_STATUS_STARTED = 4;  //playing
    public static final int PLAY_STATUS_PAUSED = 5;
    public static final int PLAY_STATUS_STOPPED = 6;
    public static final int PLAY_STATUS_COMPLETION = 7;  //play completion,waiting for next action
    public static final String PLAY_STATUS[] = {
            "NO_SERVICE", "IDLE", "INITIALIZED", "PREPARED", "PLAYING", "PAUSED", "STOPPED", "COMPLETION"
    };

    public static final String PLAY_LIST_TITLE = "PlayList";

    public static final File ROOT_PATH = Environment.getExternalStorageDirectory();
    public static final String PARENT_DIR = "..";

    public static final String TEXT_PLAY_LIST_NULL = "LIST NULL";
    public static final String TEXT_PLAY_LIST_EMPTY = "LIST EMPTY";
    public static final String TEXT_INVALID_POS = "INVALID POS";
}
