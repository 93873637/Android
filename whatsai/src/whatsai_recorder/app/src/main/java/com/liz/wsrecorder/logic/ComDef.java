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

    @SuppressWarnings("deprecation")
    //public static final String WHATSAI_RECORDER_HOME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/0.sd/whatsai.recorder/recorder";
    public static final String WHATSAI_RECORDER_HOME = "/sdcard/0.sd/whatsai.recorder/recorder";



    public static final int INIT_STATUS_LOADING = 0;
    public static final int INIT_STATUS_OK = 1;
    public static final int INIT_STATUS_FAILED = 2;

    public static final String WHATSAI_SHARED_PREFERENCES = APP_NAME + "SharedPreferences";
    public static String KEY_EXIT_STATUS = "ExitStatus";
    public static String KEY_AUDIO_RECORD_FILENAME = "AudioRecordFilename";

    public static int DEFAULT_EXIT_STATUS = 0;
    public static String DEFAULT_AUDIO_RECORD_FILENAME = "";

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
    public static final String WHATSAI_HOME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/0.sd/whatsai.recorder";

    public static final String WHATSAI_DATA_FILE_NAME = "whatsai.recorder.dat";
    public static final String WHATSAI_DATA_FILE = WHATSAI_HOME + "/" + WHATSAI_DATA_FILE_NAME;

    public static final String WHATSAI_DATA_DIR_NAME = "whatsai.recorder.files";
    public static final String WHATSAI_DATA_DIR = WHATSAI_HOME + "/" + WHATSAI_DATA_DIR_NAME;

    public static final String WHATSAI_AUDIO_DIR_NAME = "audio";
    public static final String WHATSAI_AUDIO_DIR = WHATSAI_HOME + "/" + WHATSAI_AUDIO_DIR_NAME;

    public static final String WHATSAI_AUDIO_TEMPLATE_DIR_NAME = "templates";
    public static final String WHATSAI_AUDIO_TEMPLATE_DIR = WHATSAI_AUDIO_DIR + "/" + WHATSAI_AUDIO_TEMPLATE_DIR_NAME;

    /**
     * cache dir for temporary files
     */
    public static final String WHATSAI_CACHE_DIR_NAME = "cache";
    public static final String WHATSAI_CACHE_DIR = WHATSAI_HOME + "/" + WHATSAI_CACHE_DIR_NAME;

    public static final String CLOUD_FILE_NAME_PREFIX = APP_NAME;
    public static final String CLOUD_FILE_NAME_SUFFIX = ".zip";

    public static final long LOCAL_SAVE_DELAY = 10 * 1000L;  //unit by milliseconds
    public static final long LOCAL_SAVE_TIMER = 10 * 1000L;  //unit by milliseconds

    public static final long CLOUD_SAVE_DELAY = 60 * 1000L;  //unit by milliseconds
    public static final long CLOUD_SAVE_TIMER = 24 * 60 * 60 * 1000L;  //unit by millisecond

    // tags name for save
    public static final String TAG_TYPE = "TYPE";
    public static final String TAG_NAME = "NAME";
    public static final String TAG_LIST = "LIST";
    public static final String TAG_SUMMARY = "SUMMARY";
    public static final String TAG_CONTENT = "CONTENT";
    public static final String TAG_PASSWORD = "PASSWORD";

    // WSStorage Definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // WSMail Definitions

    public static final String MAIL_FROM_ADDRESS = "nehzil@sina.com";
    public static final String MAIL_FROM_ACCOUNT = "nehzil@sina.com";
    public static final String MAIL_FROM_PASSWORD = "cfd5f95327cea45a";
    public static final String MAIL_TO_ADDRESS = "93873637@qq.com";
    public static final String MAIL_CC_ADDRESS = "wx.tom.li@qq.com,13910115737@139.com,tom.li@cloudminds.com";
    public static final String MAIL_SMTP_HOST = "smtp.sina.com.cn";

    // WSMail Definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Node Type Definitions

    public static final int NODE_TYPE_UNKNOWN = -1;

    public static final int NODE_TYPE_FILE = 0;
    public static final int NODE_TYPE_DIR = 1;

    public static final int NODE_TYPE_TASK = 2;
    public static final int NODE_TYPE_TEXT = 4;

    public static final int NODE_TYPE_TASKGROUP = 3;

    public static String getNodeTypeStr(int type) {
        switch (type) {
            case ComDef.NODE_TYPE_FILE:
                return "FILE";
            case ComDef.NODE_TYPE_DIR:
                return "DIR";
            case ComDef.NODE_TYPE_TASKGROUP:
                return "TASKGROUP";
            case ComDef.NODE_TYPE_TASK:
                return "TASK";
            case ComDef.NODE_TYPE_TEXT:
                return "TEXT";
            default:
                return "UNKNOWN";
        }
    }

    // Node Type Definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // List Menu Definitions

    private static int WhatsaiListMenuEnumID = 0;
    public enum WhatsaiListMenu {
        OPEN("OPEN"),
        ADD("ADD"),
        MODIFY("MODIFY"),
        DEL("DELETE"),
        PROP("PROPERTIES");

        public int id;
        public String name;
        WhatsaiListMenu(String name) {
            this.name = name;
            this.id = (WhatsaiListMenuEnumID++);
        }
    }

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

    public static final String WHATSAI_ACTION_DAILY_ALARM = "com.liz.reminder.daily";
    public static final String WHATSAI_ACTION_ELAPSED_ALARM = "com.liz.reminder.elapsed";
    public static final int WHATSAI_REQUEST_CODE = 0x101;

    public static final int REMIND_TYPE_INVALID = -1;
    public static final int REMIND_TYPE_DAILY_TIME = 0;

    public static final String ALARM_TAG = "ALARM_TAG";

    public static final int TASK_LIST_ALARM_DELAY = 1000;  //unit by milliseconds
    public static final int TASK_LIST_ALARM_TIMER = 1000;  //unit by milliseconds

    public static final int TIME_BEFORE = -1;
    public static final int TIME_AFTER = 1;
    public static final int TIME_SAME = 0;

    public static final int INVALID_LIST_POS = -1;

    public static final int AUDIO_RECORD_WAVE_SAMPLING_RATE = 1;
}
