package com.liz.screenhelper.logic;

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
    public static final String APP_NAME = "ScreenHelper";

    public static final String SCREEN_HELPER_ACTION_CAPTURE_ONCE = "android.intent.action.ScreenHelperSaveOnce";

    public static final String SCREEN_PICTURE_SAVE_DIR = Environment.getExternalStorageDirectory().getPath() + "/Pictures/ScreenShots/";

    public static final int DEFAULT_SCREEN_SERVER_PORT = 8888;
    public static final int DEFAULT_SCREEN_CLIENT_PORT = 8086;

    public static final String SCREEN_SERVER_STATE_STOPPED = "STOPPED";
    public static final String SCREEN_SERVER_STATE_RUNNING = "RUNNING";
    public static final String SCREEN_SERVER_STATE_LISTENING = "LISTENING";
    public static final int SCREEN_SERVER_LOOP_INTERVAL = 2000;  // unit by ms
    public static final int MAX_SCREEN_IMAGE_QUEUE_SIZE = 5;

    public static final String SCREEN_IMAGE_HEADER_FLAG = "########";
    public static final byte[] SCREEN_IMAGE_HEADER_FLAG_BYTES = SCREEN_IMAGE_HEADER_FLAG.getBytes();
    public static final int SCREEN_IMAGE_HEADER_FLAG_LEN = SCREEN_IMAGE_HEADER_FLAG_BYTES.length;
    public static final int SCREEN_DATAGRAM_PACKET_SIZE = 60000;

    public static final int JPEG_QUALITY = 80;  //1~100
}
