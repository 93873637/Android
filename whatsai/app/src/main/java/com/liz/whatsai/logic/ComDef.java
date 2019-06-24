package com.liz.whatsai.logic;

import android.os.Environment;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

public class ComDef {

    static final String APP_NAME = "whatsai";

    private static final String WHATSAI_DATA_FILE_NAME = "whatsai.xml";
    static final String WHATSAI_DATA_PATH = Environment.getExternalStorageDirectory().getPath() + "/whatsai";
    static final String WHATSAI_DATA_FILE = WHATSAI_DATA_PATH  + "/" + WHATSAI_DATA_FILE_NAME;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Storage Definitions

    static final String XML_TAG_DIR = "dir";
    static final String XML_TAG_FILE = "file";

    static final String XML_ATTR_NAME = "name";

    static final String XML_ATTR_TYPE = "type";
    static final String XML_ATTR_DONE = "done";

    static final String XML_ATTR_DETAIL = "detail";
    static final String XML_ATTR_REMIND = "remind";

    static final String XML_BOOL_TRUE = "true";

    // Storage Definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    static final int NODE_TYPE_UNKNOWN = -1;
    static final int NODE_TYPE_FILE = 0;
    static final int NODE_TYPE_DIR = 1;

    public static final int LIST_MENU_ID_ADD = 0;
    public static final int LIST_MENU_ID_UPDATE = 1;
    public static final int LIST_MENU_ID_DEL = 2;
    public static final int LIST_MENU_ID_INFO = 3;

    public static final String LIST_MENU_NAME_ADD = "添加";
    public static final String LIST_MENU_NAME_UPDATE = "修改";
    public static final String LIST_MENU_NAME_DEL = "删除";
    public static final String LIST_MENU_NAME_INFO = "详情";

    public static final String WHATSAI_ACTION_DAILY_ALARM = "com.liz.reminder.daily";
    public static final String WHATSAI_ACTION_ELAPSED_ALARM = "com.liz.reminder.elapsed";
    public static final int WHATSAI_REQUEST_CODE = 0x101;

    static final int REMIND_TYPE_INVALID = -1;
    static final int REMIND_TYPE_DAILY_TIME = 0;

    public static final String ALARM_TAG = "ALARM_TAG";

    static final int TASK_LIST_ALARM_DELAY = 1000;  //unit by milliseconds
    static final int TASK_LIST_ALARM_TIMER = 1000;  //unit by milliseconds

    static final int TASK_LIST_SAVING_DELAY = 3*1000;  //unit by milliseconds
    static final int TASK_LIST_SAVING_TIMER = 6*1000;  //unit by milliseconds

    static final int TIME_BEFORE = -1;
    static final int TIME_AFTER = 1;
    static final int TIME_SAME = 0;
}
