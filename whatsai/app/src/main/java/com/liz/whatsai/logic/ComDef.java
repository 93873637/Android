package com.liz.whatsai.logic;

import java.util.ArrayList;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("WeakerAccess")
public class ComDef {

    public static final String APP_NAME = "whatsai";

    //private static final String WHATSAI_DATA_FILE_NAME = "whatsai.xml";
    private static final String WHATSAI_DATA_FILE_NAME = "whatsai.dat";
    //public static final String WHATSAI_DATA_PATH = "/storage/0CCD-50F4/0.sd/whatsai";  //for security, external storage can't write for android app!
    public static final String WHATSAI_DATA_PATH = "/sdcard/0.sd/whatsai";
    public static final String WHATSAI_DATA_FILE = WHATSAI_DATA_PATH  + "/" + WHATSAI_DATA_FILE_NAME;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // WhatsaiStorage Definitions

    public static final String TAG_TYPE = "TYPE";
    public static final String TAG_NAME = "NAME";
    public static final String TAG_LIST = "LIST";
    public static final String TAG_SUMMARY = "SUMMARY";
    public static final String TAG_CONTENT = "CONTENT";
    public static final String TAG_SYNC_TIME = "SYNC_TIME";

    public static final String XML_TAG_DIR = "dir";
    public static final String XML_TAG_FILE = "file";
    public static final String XML_ATTR_DONE = "DONE";
    public static final String XML_ATTR_REMIND = "remind";
    public static final String XML_BOOL_TRUE = "true";

    public static final int WHATSAI_SAVING_DELAY = 3 * 1000;  //unit by milliseconds
    public static final int WHATSAI_SAVING_TIMER = 10 * 1000;  //unit by milliseconds
    public static final long CLOUD_SAVE_PERIOD = 24 * 3600 * 1000;  //one day, unit by millisecond

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
    public static final String MAIL_ATTACH_FILE_NAME = "whatsai.zip";
    public static final String MAIL_ATTACH_FILE_PATH = WHATSAI_DATA_PATH  + "/" + MAIL_ATTACH_FILE_NAME;

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

    // Node Type Definitions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // List Menu Definitions

    private static int ListMenuEnumID = 0;

    public enum ListMenu {
        OPEN("OPEN"),
        ADD("ADD"),
        MODIFY("MODIFY"),
        DEL("DELETE"),
        PROP("PROPERTIES");

        public int id;
        public String name;
        ListMenu(String name) {
            this.name = name;
            this.id = (ListMenuEnumID++);
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
}
