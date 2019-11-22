package com.liz.multidialer.logic;

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

    public static final String TEL_LIST_FILE_NAME = "/sdcard/tellist.txt";

    public static final long DEFAULT_END_CALL_DELAY = 3500L;  //挂断电话延迟时间(ms)
    public static final long CAPTURE_SCREEN_OFFSET = 500L;  //截屏延迟时间(ms) = 挂断电话延迟时间 - OFFSET
    public static final long CALL_NEXT_OFFSET = 1000L;  //拨打下一个号码延迟时间(ms) = 挂断电话延迟时间 + OFFSET

    public static final long LISTEN_CALL_STATE_TIME = 100L;  //unit by ms
    public static final long WAIT_CALL_IDLE_DELAY = 500L;  //unit by ms

    public static final long UI_TIMER_DELAY = 500L;  //unit by ms
    public static final long UI_TIMER_PERIOD = 1000L;  //unit by ms

    public static final long CAPTURE_TIMER_DELAY = 1000L;  //unit by ms
    public static final long CAPTURE_TIMER_PERIOD = 1000L;  //unit by ms

    public static final String DIALER_DIR = "/sdcard/multidialer";

    public static final int JPEG_QUALITY = 90;  //1~100

    public static final String KEY_CURRENT_CALLED_INDEX = "CurrentCalledIndex";

    public static final int INVALID_CALL_INDEX = -1;
}
