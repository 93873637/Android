package com.liz.multidialer.logic;

import android.os.Environment;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused, WeakerAccess")
public class ComDef {

    /**
     *  CONSTANT DEFINITIONS
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static final String APP_NAME = "MultiDialer";

    public static final boolean DEBUG = false;

    public static final long DEFAULT_END_CALL_DELAY = 3000L;  //挂断电话延迟时间(ms)
    public static final long CAPTURE_SCREEN_OFFSET = 500L;  //截屏延迟时间(ms) = 挂断电话延迟时间 - OFFSET
    public static final long CALL_NEXT_OFFSET = 1000L;  //拨打下一个号码延迟时间(ms) = 挂断电话延迟时间 + OFFSET

    public static final long LISTEN_CALL_STATE_TIME = 100L;  //unit by ms
    public static final long WAIT_CALL_IDLE_DELAY = 500L;  //unit by ms

    public static final long UI_TIMER_DELAY = 500L;  //unit by ms
    public static final long UI_TIMER_PERIOD = 1000L;  //unit by ms

    public static final long HEARTBEAT_TIMER_DELAY = 60*1000L;  //unit by ms
    public static final long HEARTBEAT_TIMER_MIN = 5*1000L;  //unit by ms

    public static final long DAEMON_TASK_TIMER_DELAY = 60*1000L;  //unit by ms
    public static final long DAEMON_TASK_TIMER_PERIOD = 10*60*1000L;  //unit by ms

    public static final String DIALER_DIR = Environment.getExternalStorageDirectory().getPath() + "/multidialer";
    public static final String DIALER_NUM_DIR = DIALER_DIR + "/num";
    public static final String DIALER_PIC_DIR = DIALER_DIR + "/pic";
    public static final String DIALER_LOG_DIR = DIALER_DIR + "/log";

    public static final String MULTIDIALER_SHARED_PREFERENCES = "MultiDialerSharedPreferences";
    public static final String KEY_DEVICE_ID = "DeviceId";
    public static final String KEY_SERVER_ADDRESS = "ServerAddress";
    public static final String KEY_SERVER_PORT = "ServerPort";
    public static final String KEY_USER_NAME = "UserName";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_NETWORK_TYPE = "NetworkType";
    public static final String KEY_SERVER_HOME = "ServerHome";
    public static final String KEY_JPEG_QUALITY = "JpegQuality";
    public static final String KEY_TEL_LIST_FILE = "TelListFile";
    public static final String KEY_CURRENT_CALLED_INDEX = "CurrentCalledIndex";
    public static final String KEY_PICTURE_PATH = "PicturePath";
    public static final String KEY_HEARTBEAT_TIMER = "HeartbeatTimer";

    public static final String DEFAULT_DEVICE_ID = "";
    public static final String DEFAULT_SERVER_ADDRESS = "";
    public static final int DEFAULT_SERVER_PORT = 22;
    public static final String DEFAULT_USER_NAME = "";
    public static final String DEFAULT_PASSWORD = "";
    public static final String DEFAULT_NETWORK_TYPE = "sftp";
    public static final String DEFAULT_SERVER_HOME = "/home/shandong1";
    public static final int DEFAULT_JPEG_QUALITY = 30;  //1~100%
    public static final String DEFAULT_TEL_LIST_FILE = "";
    public static final int DEFAULT_CURRENT_CALLED_INDEX = 0;
    public static final String DEFAULT_PICTURE_PATH = "";
    public static final long DEFAULT_HEARTBEAT_TIMER_PERIOD = 5*60*1000L;  //unit by ms

    public static final String UPLOAD_FILE_PATH = "upload_file_path";

    // Dir settings of remote sftp server
    public static final String SFTP_PUB_SPACE = "PUB_SPACE";
    public static final String SFTP_NUM_DATA = "NUM_DATA";
    public static final String SFTP_PIC_DATA = "PIC_DATA";
    public static final String SFTP_WAIT_DATA = "WAIT_DATA";
    public static final String SFTP_RUN_DATA = "RUN_DATA";
    public static final String SFTP_END_DATA = "END_DATA";

    public static final String SFTP_DEV_RES = "DEV_RES";

    public static final String SFTP_PATH_PUB_SPACE = DEFAULT_SERVER_HOME + "/" + SFTP_PUB_SPACE;

    public static final String SFTP_PATH_NUM_DATA = SFTP_PATH_PUB_SPACE + "/" + SFTP_NUM_DATA;
    public static final String SFTP_PATH_PIC_DATA = SFTP_PATH_PUB_SPACE + "/" + SFTP_PIC_DATA;

    public static final String SFTP_PATH_NUM_WAIT_DATA = SFTP_PATH_NUM_DATA + "/" + SFTP_WAIT_DATA;
    public static final String SFTP_PATH_NUM_RUN_DATA = SFTP_PATH_NUM_DATA + "/" + SFTP_RUN_DATA;
    public static final String SFTP_PATH_NUM_END_DATA = SFTP_PATH_NUM_DATA + "/" + SFTP_END_DATA;
    public static final String SFTP_PATH_HEARTBEAT = SFTP_PATH_NUM_DATA + "/" + SFTP_DEV_RES;

    public static final String SFTP_PATH_PIC_WAIT_DATA = SFTP_PATH_PIC_DATA + "/" + SFTP_WAIT_DATA;
}
