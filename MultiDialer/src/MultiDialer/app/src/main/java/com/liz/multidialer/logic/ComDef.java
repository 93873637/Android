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

    //public static final String TEL_LIST_FILE_PATH = "/sdcard/tellist.txt";
    public static final String TEL_LIST_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/tellist.txt";

    public static final long DEFAULT_END_CALL_DELAY = 3000L;  //挂断电话延迟时间(ms)
    public static final long CAPTURE_SCREEN_OFFSET = 500L;  //截屏延迟时间(ms) = 挂断电话延迟时间 - OFFSET
    public static final long CALL_NEXT_OFFSET = 1000L;  //拨打下一个号码延迟时间(ms) = 挂断电话延迟时间 + OFFSET

    public static final long LISTEN_CALL_STATE_TIME = 100L;  //unit by ms
    public static final long WAIT_CALL_IDLE_DELAY = 500L;  //unit by ms

    public static final long UI_TIMER_DELAY = 500L;  //unit by ms
    public static final long UI_TIMER_PERIOD = 1000L;  //unit by ms

    public static final long CAPTURE_TIMER_DELAY = 1000L;  //unit by ms
    public static final long CAPTURE_TIMER_PERIOD = 1000L;  //unit by ms

    //public static final String DIALER_DIR = "/sdcard/multidialer";
    public static final String DIALER_DIR = Environment.getExternalStorageDirectory().getPath() + "/multidialer";

    //该程序运行一次最多能拨打的号码数量
    public static final int MAX_CALL_NUM = Integer.MAX_VALUE;

    public static final int JPEG_QUALITY = 90;  //1~100

    public static final String MULTIDIALER_SHARED_PREFERENCES = "MultiDialerSharedPreferences";
    public static final String KEY_DEVICE_ID = "DeviceId";
    public static final String KEY_SERVER_ADDRESS = "ServerAddress";
    public static final String KEY_USER_NAME = "UserName";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_NETWORK_TYPE = "NetworkType";
    public static final String KEY_TEL_LIST_FILE = "TelListFile";
    public static final String KEY_CURRENT_CALLED_INDEX = "CurrentCalledIndex";

    public static final String DEFAULT_DEVICE_ID = "";
    public static final String DEFAULT_SERVER_ADDRESS = "";
    public static final String DEFAULT_USER_NAME = "";
    public static final String DEFAULT_PASSWORD = "";
    public static final String DEFAULT_NETWORK_TYPE = "sftp";
    public static final String DEFAULT_TEL_LIST_FILE = "";
    public static final int DEFAULT_CURRENT_CALLED_INDEX = 0;
}
