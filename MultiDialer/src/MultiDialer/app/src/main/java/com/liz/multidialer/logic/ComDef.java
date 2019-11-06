package com.liz.multidialer.logic;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class ComDef {

    /**
     *  CONSTANT DEFINITIONS
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static final String APP_NAME = "MultiDialer";

    public static final String TEL_LIST_FILE_NAME = "/sdcard/tellist.txt";

    public static final int CALL_INTERVAL = 5;  //延迟n秒后自动挂断电话
    public static final long END_CALL_DELAY = CALL_INTERVAL*1000L;

    public static final long LISTEN_CALL_STATE_TIME = 500L;
    public static final long WAIT_CALL_IDLE_TIME = 500L;

    public static final String DIALER_DIR = "/sdcard/multidialer";

    public static final int JPEG_QUALITY = 90;  //1~100

    public static final String KEY_CURRENT_CALLED_INDEX = "CurrentCalledIndex";
}
