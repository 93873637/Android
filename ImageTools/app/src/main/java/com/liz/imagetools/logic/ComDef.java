package com.liz.imagetools.logic;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

public class ComDef {

    /**
     *  CONSTANT DEFINITIONS
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static final String APP_NAME = "ImageTools";


    public final static String MANUAL_STRING = ""
            + "1. adb shell cp /data/misc/camera/*.nv21 " + ComDef.NV21_FILES_PATH + "\n"
            + "     for Android P: \n"
            + "     adb shell cp /data/vendor/camera/*.nv21 " + ComDef.NV21_FILES_PATH + "\n"
            + "2. click button \"NV21->JPG\"\n"
            + "3. adb pull " + ComDef.JPG_FILES_PATH + " ./\n"
            + "4. NOTE: widht/height got from properties, if not, please set: \n"
            + "     adb shell setprop " + ComDef.PROP_IMAGE_WIDTH + " 4096\n"
            + "     adb shell setprop " + ComDef.PROP_IMAGE_HEIGHT + " 3040"
            ;

    public static final String NV21_FILES_PATH = "/sdcard/camera/nv21/";
    public static final String JPG_FILES_PATH = "/sdcard/camera/jpg/";
    public static final String NV21_FILE_EXTNAME = ".nv21";

    public static final String ACTION_IMAGE_TOOLS_NV21_TO_JPG = "android.intent.action.NV21toJPG";
    public static final String IMAGE_TOOLS_MSG = "image.tools.msg";

    public static final String PROP_IMAGE_WIDTH = "persist.image.tool.width";
    public static final String PROP_IMAGE_HEIGHT = "persist.image.tool.height";

    public static final int JPG_IMAGE_QUALITY = 100;  //bad to good: 1-100
}
