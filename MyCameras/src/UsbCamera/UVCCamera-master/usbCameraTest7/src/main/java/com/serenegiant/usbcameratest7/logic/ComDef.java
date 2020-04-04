package com.serenegiant.usbcameratest7.logic;

import com.serenegiant.usb.UVCCamera;

/**
 * Common Definitions
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("WeakerAccess")
public class ComDef {
    public static final String APP_NAME = "USB MultiCam Test";

    // ##@: set false for release version
    public static boolean DEBUG = true;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //###@: Control Params

    public static boolean OPEN_CAMERA_PREVIEW = false;
    public static int CAMERA_FRAME_FORMAT = UVCCamera.FRAME_FORMAT_MJPEG;
    public static int CAMERA_PREVIEW_WIDTH = 1280;
    public static int CAMERA_PREVIEW_HEIGHT = 960;

    // Control Params
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
