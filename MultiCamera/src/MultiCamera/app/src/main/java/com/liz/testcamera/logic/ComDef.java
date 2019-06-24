package com.liz.testcamera.logic;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

public class ComDef {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // CONSTANT DEFINITIONS
    public static final String APP_NAME = "TestCamera";

    public static final int CAMERA_ID_BACK_MAIN = 8;//0;
    public static final int CAMERA_ID_BACK_SUB = 2;
    public static final int CAMERA_ID_FRONT_MAIN = 1;
    public static final int CAMERA_ID_FRONT_SUB = 3;
    public static final int CAMERA_ID_DEFAULT = CAMERA_ID_BACK_MAIN;

    public static final int CAMERA_STATUS_IDLE = 0;
    public static final int CAMERA_STATUS_SHOOTING = 1;
    public static final String[] CAMERA_STATUS_NAME = {
            "idle", "shooting"
    };

    public static final int CAMERA_MODE_NORMAL = 0;
    public static final int CAMERA_MODE_NIGHT = 1;

    public static final int PREVIEW_SIZE_WIDTH = 3840;
    public static final int PREVIEW_SIZE_HEIGHT = 2160;
    public static final int PICTURE_SIZE_WIDTH = 3840;
    public static final int PICTURE_SIZE_HEIGHT = 2160;

    public static final int NIGHT_MODE_SHOT_ERROR = 0;  //error on shot logical
    public static final int NIGHT_MODE_SHOT_FINISHED = 1;  //shot finished for this shot
    public static final int NIGHT_MODE_SHOT_CONTINUE = 2;  //continue shooting with current exposure parameter
    public static final int NIGHT_MODE_SHOT_CONTINUE_NEXT_EXPO = 3;  //continue shooting with new exposure parameter
    public static final String[] NIGHT_MODE_ACTION_NAME = {
        "ERROR", "FINISHED", "CONTINUE", "CONTINUE_NEXT_EXPO"
    };

    //NOTE: keep consistant with QCameraNightMode.cpp(hardware/qcom/camera/QCamera2/util/cloudminds/nightmode/), or crash when take picture
    public static final String PROP_NIGHT_MODE_ENABLE = "persist.camera.night.enable";
    public static final String NIGHT_MODE_ENABLE = "1";
    public static final String NIGHT_MODE_DISABLE = "0";
    public static final String DEFAULT_NIGHT_MODE_ENABLE = NIGHT_MODE_DISABLE;

    public static final String PROP_NIGHT_MODE_BURSTNUM = "persist.camera.night.burstnum";
    public static final String DEFAULT_NIGHT_MODE_BURSTNUM = "5";

    public static final String PROP_NIGHT_MODE_EXPTAB = "persist.camera.night.exptab";
    public static final String DEFAULT_NIGHT_MODE_EXPTAB = "200/67,400/100,800/125,1600/167,2200/266,3200/266";
    public static final String DEFAULT_NIGHT_MODE_EXPTAB_LONG = "200/33,800/125,2200/266,2200/500,2200/1024,2200/4096";
    public static final String DEFAULT_SUNNY_MODE_EXPTAB = "100/0.1,100/1.0,100/10,200/5,200/33,200/67,400/10,400/33";

    //adb shell setprop persist.camera.night.exptab 100/10,100/33,100/67,200/33,200/67,400/33
    public static final String DEFAULT_INDOOR_MODE_EXPTAB = "100/10,100/33,100/67,200/33,200/67,400/33";

    public static final String PROP_NIGHT_MODE_DUMP_INPUT = "persist.camera.night.dumpinput";
    public static final String DEFAULT_NIGHT_MODE_DUMP_INPUT = "0";
    public static final String PROP_NIGHT_MODE_DUMP_EXPO = "persist.camera.night.dumpexpo";
    public static final String DEFAULT_NIGHT_MODE_DUMP_EXPO = "0";

    public static final String PROP_NIGHT_MODE_LONG_EXPOSURE = "persist.camera.night.longexposure";
    public static final String DEFAULT_NIGHT_MODE_LONG_EXPOSURE = "0";
    public static final String PROP_NIGHT_MODE_EXPTIME = "persist.camera.night.exptime";
    public static final String DEFAULT_NIGHT_MODE_EXPTIME = "0";

    public static final int MAX_NORMAL_EXPOSURE_TIME = 266;    //unit by ms

    public static final int UI_TIMER_DELAY = 2000;   //unit by ms
    public static final int UI_TIMER_PERIOD = 2000;  //unit by ms

    public static final int TIME_DELAY_FOR_EXPOSURE_CHANGE = 200;  //unit by ms

    // CONSTANT DEFINITIONS
    ///////////////////////////////////////////////////////////////////////////////////////////////
}
