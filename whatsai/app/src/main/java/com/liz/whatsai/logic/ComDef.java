package com.liz.whatsai.logic;

import android.os.Environment;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("WeakerAccess")
public class ComDef {

    public static final String APP_NAME = "whatsai";

    private static final String WHATSAI_DATA_FILE_NAME = "whatsai.xml";
    //public static final String WHATSAI_DATA_PATH = "/storage/0CCD-50F4/0.sd/whatsai";  //can't write for android app!
    public static final String WHATSAI_DATA_PATH = "/sdcard/0.sd/whatsai";
    public static final String WHATSAI_DATA_FILE = WHATSAI_DATA_PATH  + "/" + WHATSAI_DATA_FILE_NAME;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // WhatsaiStorage Definitions

    public static final String TAG_SYNC_TIME = "sync_time";

    public static final String XML_TAG_DIR = "dir";
    public static final String XML_TAG_FILE = "file";

    public static final String XML_ATTR_NAME = "name";

    public static final String XML_ATTR_TYPE = "type";
    public static final String XML_ATTR_DONE = "done";

    public static final String XML_ATTR_DETAIL = "detail";
    public static final String XML_ATTR_REMIND = "remind";

    public static final String XML_BOOL_TRUE = "true";

    public static final String MAIL_FROM_ADDRESS = "nehzil@sina.com";
    public static final String MAIL_FROM_ACCOUNT = "nehzil@sina.com";
    public static final String MAIL_FROM_PASSWORD = "cfd5f95327cea45a";
    public static final String MAIL_TO_ADDRESS = "93873637@qq.com";
    public static final String MAIL_SMTP_HOST = "smtp.sina.com.cn";
    public static final String MAIL_ATTACH_FILE_NAME = "whatsai.zip";
    public static final String MAIL_ATTACH_FILE_PATH = WHATSAI_DATA_PATH  + "/" + MAIL_ATTACH_FILE_NAME;

    public static final long CLOUD_SAVE_PERIOD = 24 * 3600 * 1000;  //one day, unit by millisecond

    // WhatsaiStorage Definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final int NODE_TYPE_UNKNOWN = -1;
    public static final int NODE_TYPE_FILE = 0;
    public static final int NODE_TYPE_DIR = 1;

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

    public static final int REMIND_TYPE_INVALID = -1;
    public static final int REMIND_TYPE_DAILY_TIME = 0;

    public static final String ALARM_TAG = "ALARM_TAG";

    public static final int TASK_LIST_ALARM_DELAY = 1000;  //unit by milliseconds
    public static final int TASK_LIST_ALARM_TIMER = 1000;  //unit by milliseconds

    public static final int TASK_LIST_SAVING_DELAY = 3*1000;  //unit by milliseconds
    public static final int TASK_LIST_SAVING_TIMER = 6*1000;  //unit by milliseconds

    public static final int TIME_BEFORE = -1;
    public static final int TIME_AFTER = 1;
    public static final int TIME_SAME = 0;
}
