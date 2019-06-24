package com.cloudminds.feedback.logic;

import android.content.Context;
import android.os.Environment;

import com.cloudminds.feedback.utils.LogUtils;
import com.cloudminds.feedback.utils.SysUtils;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

public class ComDef {

    /**
     *  CONSTANT DEFINITIONS
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static final String APPLICATION_ID = "com.cloudminds.feedback";
    public static final String PACKAGE_NAME = "com.cloudminds.feedback";

    /**
     *  User Experience Definitions
     *  NOTE:
     *  The values and default value must keep consistent with System Settings module.
     */
    public static final String PROP_USER_EXPERIENCE = "persist.sys.user.experience";
    public static final String PROP_USER_EXPERIENCE_ON = "1";
    public static final String PROP_USER_EXPERIENCE_OFF = "0";
    public static final String PROP_USER_EXPERIENCE_DEFAULT = PROP_USER_EXPERIENCE_OFF;

    //check current version is user/userdebug
    public static final String PROP_BUILD_TYPE = "ro.build.type";
    public static final String PROP_BUILD_TYPE_USER = "user";
    public static final String PROP_BUILD_TYPE_USERDEBUG = "userdebug";
    public static final String PROP_BUILD_TYPE_DEFAULT = PROP_BUILD_TYPE_USERDEBUG;

    public static final String PROBLEM_TYPE= "problem";
    public static final String SUGGESTION_TYPE = "suggestion";
    public static final String QUESTION_TYPE = "consultation";

    /**
     * [persist.sys.cmlogd]: [0]
     * [persist.sys.cmlogd.list]: [1:logcat;7:qxdm;1:tcpdump;1:kmsg;1:wlan;1:charge;1:sysprof;1:sensor]
     * [persist.sys.cmlogd.path]: [/sdcard/logs/offlineLogs/Log_20180410_173138]
     * [persist.sys.cmlogd.save]: [1]
     * [persist.sys.cmlogd.state]: [x]
     * [persist.sys.cmlogd.save]: [ 0-remove log of prop path, 1-copy anr/tombstone to /sdcard/log , 2-do nothingx]
     */
    public static final String PROP_SYS_LOG_STATE = "persist.sys.cmlogd";
    public static final String PROP_SYS_LOG_PATH = "persist.sys.cmlogd.path";
    public static final String PROP_SYS_LOG_CONFIG_LIST = "persist.sys.cmlogd.list";
    public static final String PROP_SYS_LOG_CONFIG_LIST_DEFAULT = "1:logcat;1:kmsg;1:charge";
    public static final String PROP_SYS_LOG_SAVE = "persist.sys.cmlogd.save";

    public static final  int CMLOGD_RESTART_DELAY = 2000;  //unit by milliseconds

    //broadcast receiver action
    public static final String FEEDBACK_RECEIVER_ACTION = "android.intent.action.FEEDBACK";

    public static final String FEEDBACK_TYPE_DIRECT_RUN = "DIRECT_RUN";
    public static final String FEEDBACK_TYPE_OFFLINE_LOG = "OFFLINE_LOG";
    public static final String FEEDBACK_TYPE_SSR_RESET = "SSR_RESET";
    public static final String FEEDBACK_TYPE_SYS_RESET = "SYS_RESET";
    public static final String FEEDBACK_TYPE_APP_ERRORS = "APP_ERRORS";
    public static final String FEEDBACK_TYPE_NATIVE_CRASH = "NATIVE_CRASH";

    //call interface parameters
    public static final String FEEDBACK_MSG_TYPE = "msg_type";
    public static final String FEEDBACK_MSG_CONT = "msg_cont";
    public static final String FEEDBACK_FILE_PATH = "file_path";
    public static final String FEEDBACK_FILE_NAME = "file_name";
    public static final String FILE_ABSOLUTE_PATH = "file_absolute_path";
    public static final String FEEDBACK_ERROR_TYPE = "error_type";
    public static final String FEEDBACK_PACKAGE_NAME = "package_name";

    //NOTE: log zip path on sdcard, using getExternalStorageDirectory get sdcard dir
    private static final String OFFLINE_LOG_ZIP_PATH = "logs/offlineLogsZip/";

    /* *
     * server configurations
     */
    //amazon web api
    public static String WEB_SERVER_BASE_URL = "http://34.219.46.125:9600/";
    public static String WEB_SERVER_UPLOAD_PATH = "logFileMeta/api/upload";
    public static String WEB_SERVER_POST_URL = WEB_SERVER_BASE_URL + WEB_SERVER_UPLOAD_PATH;

    public static final String FORMAL_WEB_SERVER_BASE_URL = "http://34.219.46.125:9600/";
    public static final String TEST_WEB_SERVER_BASE_URL = "http://111.13.138.141:9600/";

    //amazon server for ftp
    public static String FTP_SERVER_URL = "34.219.46.125";

    public static final String FORMAL_FTP_SERVER_URL = "34.219.46.125";
    public static final String TEST_FTP_SERVER_URL = "111.13.138.141";
    public static final int FTP_SERVER_PORT = 801;

    public static final String FTP_USERNAME = "feedback_user";
    public static final String FTP_PASSWORD = "GODfeedback1@";

    /*
    //ailing.zhang pc
    //##@:test web api(ailing.zhang pc)
    public static final String WEB_SERVER_BASE_URL = "http://10.11.32.40:9600/";
    public static final String WEB_SERVER_POST_URL = WEB_SERVER_BASE_URL + "logFileMeta/api/upload";

    //test ftp server(ailing.zhang pc)
    public static final String FTP_SERVER_URL = "10.11.32.40";
    public static final int FTP_SERVER_PORT = 21;
    public static final String FTP_USERNAME = "logs";
    public static final String FTP_PASSWORD = "123";
    //*/
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static String getOfflineLogZipPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/" + OFFLINE_LOG_ZIP_PATH;
    }

    public static boolean isUserExperienceEnabled(Context context) {
        String prop = SysUtils.getSystemProperty(context, PROP_USER_EXPERIENCE, PROP_USER_EXPERIENCE_DEFAULT);
        if (prop == null) {
            LogUtils.w("WARNING: user experience disabled by null prop");
            return false;
        }
        else {
            return prop.equals(PROP_USER_EXPERIENCE_ON);
        }
    }
}
