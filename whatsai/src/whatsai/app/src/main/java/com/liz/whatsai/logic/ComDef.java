package com.liz.whatsai.logic;

import android.os.Environment;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("WeakerAccess")
public class ComDef {

    public static final String APP_NAME = "whatsai";

    // ##@: set false for release version
    public static boolean DEBUG = true;

    public static final int INIT_STATUS_LOADING = 0;
    public static final int INIT_STATUS_OK = 1;
    public static final int INIT_STATUS_FAILED = 2;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // WhatsaiStorage Definitions

    //
    // Whatsai Home Directory
    //
    // it should be better to use external sdcard storage, but
    // unfortunately, external storage can't be written by app for security!
    // so we can't set home dir to external storage like following:
    //
    // public static final String WHATSAI_HOME = "/storage/0CCD-50F4/0.sd/whatsai";
    //
    // instead, we use internal sdcard storage:
    //
    public static final String WHATSAI_HOME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/0.sd/whatsai";

    public static final String WHATSAI_DATA_FILE_NAME = "whatsai.dat";
    public static final String WHATSAI_DATA_FILE = WHATSAI_HOME + "/" + WHATSAI_DATA_FILE_NAME;

    public static final String WHATSAI_DATA_DIR_NAME = "whatsai.files";
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

    // WhatsaiStorage Definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // WhatsaiMail Definitions

    public static final String MAIL_FROM_ADDRESS = "nehzil@sina.com";
    public static final String MAIL_FROM_ACCOUNT = "nehzil@sina.com";
    public static final String MAIL_FROM_PASSWORD = "cfd5f95327cea45a";
    public static final String MAIL_TO_ADDRESS = "93873637@qq.com";
    public static final String MAIL_CC_ADDRESS = "wx.tom.li@qq.com,13910115737@139.com,tom.li@cloudminds.com";
    public static final String MAIL_SMTP_HOST = "smtp.sina.com.cn";

    // WhatsaiMail Definitions
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
        PLAY("PLAY"),
        STOP("STOP"),
        DEL("DELETE");

        public int id;
        public String name;
        AudioListMenu(String name) {
            this.name = name;
            this.id = (AudioListMenuEnumID++);
        }
    }

//    public static enum LIST_MENU {
//        LIST_MENU_ID_OPEN,
//        LIST_MENU_ID_ADD,
//
//    }
//    public static final int LIST_MENU_ID_OPEN = 0;
//    public static final int LIST_MENU_ID_ADD = 1;
//    public static final int LIST_MENU_ID_MODIFY = 2;
//    public static final int LIST_MENU_ID_DELETE = 3;
//    public static final int LIST_MENU_ID_PROPERTIES = 4;
//
//    public static final String LIST_MENU_NAME_ADD = "ADD";
//    public static final String LIST_MENU_NAME_MODIFY = "MODIFY";
//    public static final String LIST_MENU_NAME_DELETE = "DELETE";
//    public static final String LIST_MENU_NAME_PROPERTIES = "PROPERTIES";

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
}
