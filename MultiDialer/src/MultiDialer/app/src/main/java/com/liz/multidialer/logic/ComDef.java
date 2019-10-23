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
    public static final String APP_NAME = "ScreenHelper";


    public static final String TEL_LIST_FILE_NAME = "/sdcard/tellist.txt";

    public static final long CALL_INTERVAL = 5L;  //延迟n秒后自动挂断电话
    public static final long END_CALL_DELAY = CALL_INTERVAL*1000L;

    public static final String DIALER_DIR = "/sdcard/multidialer";

    public static final int JPEG_QUALITY = 90;  //1~100
}
