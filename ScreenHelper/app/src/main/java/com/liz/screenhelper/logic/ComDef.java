package com.liz.screenhelper.logic;

import android.os.Environment;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

public class ComDef {

    /**
     *  CONSTANT DEFINITIONS
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static final String APP_NAME = "ScreenHelper";

    public static final String SCREEN_HELPER_ACTION_CAPTURE_ONCE = "android.intent.action.ScreenHelperSaveOnce";

    public static final String SCREEN_PICTURE_SAVE_DIR = Environment.getExternalStorageDirectory().getPath() + "/Pictures/ScreenShots/";

    public static final int DEFAULT_SCREEN_SERVER_PORT = 8888;

    public static final String SCREEN_SERVER_STATE_STOPPED = "STOPPED";
    public static final String SCREEN_SERVER_STATE_RUNNING = "RUNNING";

    public static final int MAX_SCREEN_BUFFER_QUEUE_SIZE = 5;
}
