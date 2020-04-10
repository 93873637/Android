package com.liz.wsrecorder.logic;

import android.os.Environment;

import com.liz.androidutils.NumUtils;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

public class ComDef {

    public static final String APP_NAME = "WSRecorder";

    // ##@: set false for release version
    public static boolean DEBUG = true;

    public static final long MAX_AUDIO_FILE_SIZE = 100 * NumUtils.M;
    public static final long MAX_AUDIO_STORAGE_SIZE = 10 * MAX_AUDIO_FILE_SIZE;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // WSStorage Definitions

    //
    // Whatsai Home Directory
    //
    // it should be better to use external sdcard storage, but unfortunately,
    // external storage can't be written by app for security!
    // so we can't set home dir to external storage as:
    // public static final String WHATSAI_HOME = "/storage/0CCD-50F4/0.sd/whatsai.recorder";
    // instead, we use internal sdcard storage:
    //
    @SuppressWarnings("deprecation")
    public static final String WHATSAI_HOME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/0.sd/whatsai";

    public static final String WHATSAI_AUDIO_DIR_NAME = "audio";
    public static final String WHATSAI_AUDIO_DIR = WHATSAI_HOME + "/" + WHATSAI_AUDIO_DIR_NAME;

    public static final String WHATSAI_RECORDER_NAME = "recorder";
    public static final String WHATSAI_RECORDER_HOME = WHATSAI_HOME + "/" + WHATSAI_RECORDER_NAME;
    
    // WSStorage Definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // List Menu Definitions

    private static int AudioListMenuEnumID = 0;
    public enum AudioListMenu {
        PLAY_MORE("PLAY..."),
        RELOAD("RELOAD"),
        DELETE("DELETE"),
        DELETE_ALL("DELETE ALL");
        public int id;
        public String name;
        AudioListMenu(String name) {
            this.name = name;
            this.id = (AudioListMenuEnumID++);
        }
    }

    // List Menu Definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final int AUDIO_RECORD_WAVE_SAMPLING_RATE = 1;
}
