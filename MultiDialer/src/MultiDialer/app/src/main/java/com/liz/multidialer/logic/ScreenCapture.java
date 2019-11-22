package com.liz.multidialer.logic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ScreenCapture {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //Interfaces

    public  static final int REQUEST_CODE_MEDIA_PROJECTION = 100;

    public static void initScreenCapture(Activity activity) {
        getInstance()._initScreenCapture(activity);
    }

    public static void startScreenCapture() {
        getInstance()._startScreenCapture();
    }

    public static void captureOnce() {
        getInstance()._captureOnce();
    }

    public static void stopScreenCapture() {
        getInstance()._stopScreenCapture();
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        getInstance()._onActivityResult(requestCode, resultCode, data);
    }

    //Interfaces
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //Singleton Decl.

    private static ScreenCapture instance_ = null;

    private static ScreenCapture getInstance() {
        if (instance_ == null) {
            instance_ = new ScreenCapture();
        }
        return instance_;
    }

    //Singleton Decl.
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private ImageReader mImageReader = null;
    private int mWindowWidth = 0;
    private int mWindowHeight = 0;
    private int mScreenDensity = 0;

    private int mResultCode;
    private Intent mResultData;

//    private ImageReader.OnImageAvailableListener mOnImageAvailableListener
//            = new ImageReader.OnImageAvailableListener() {
//        @Override
//        public void onImageAvailable(ImageReader reader) {
//            //showProgress("onImageAvailable");
//            Image img = reader.acquireNextImage();
//            DataLogic.saveCaptureImage(img);
//            img.close();  //NOTE: you must close the image to get next
//        }
//    };

    private void _initScreenCapture(Activity activity) {
        LogUtils.d("_initScreenCapture");
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            Toast.makeText(activity, "ERROR: get WINDOW_SERVICE failed", Toast.LENGTH_LONG).show();
            activity.finish();
        } else {
            Display display = windowManager.getDefaultDisplay();
            if (display == null) {
                Toast.makeText(activity, "ERROR: getDefaultDisplay failed", Toast.LENGTH_LONG).show();
                activity.finish();
            } else {
                mWindowWidth = display.getWidth();
                mWindowHeight = display.getHeight();
                DisplayMetrics displayMetrics = new DisplayMetrics();
                display.getMetrics(displayMetrics);
                mScreenDensity = displayMetrics.densityDpi;
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                mScreenDensity = metrics.densityDpi;
                mMediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                if (mMediaProjectionManager == null) {
                    Toast.makeText(activity, "ERROR: get MEDIA_PROJECTION_SERVICE failed", Toast.LENGTH_LONG).show();
                    activity.finish();
                } else {
                    activity.startActivityForResult(
                            mMediaProjectionManager.createScreenCaptureIntent(),
                            REQUEST_CODE_MEDIA_PROJECTION);
                }
            }
        }
    }

    private void _onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ScreenCapture.REQUEST_CODE_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                LogUtils.d("ERROR: REQUEST_CODE_MEDIA_PROJECTION: onActivityResult failed");
            } else {
                LogUtils.d("REQUEST_CODE_MEDIA_PROJECTION: onActivityResult OK");
                mResultCode = resultCode;
                mResultData = data;
                _startScreenCapture();
            }
        }
    }

    private void _startScreenCapture() {
        LogUtils.d("_startScreenCapture");

        if (mMediaProjection == null) {
            mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
        }
        else {
            LogUtils.d("ERROR: startScreenCapture: mMediaProjection already exists");
        }

        if (mImageReader == null) {
            //
            //NOTE: only PixelFormat.RGBA_8888(1) is supported for my A2H
            //java.lang.UnsupportedOperationException: The producer output buffer format 0x1 doesn't match the ImageReader's configured buffer format 0x3.
            //
            mImageReader = ImageReader.newInstance(mWindowWidth, mWindowHeight, PixelFormat.RGBA_8888, 2); //ImageFormat.RGB_565

            //too many images? so no listener
            //mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
            mImageReader.setOnImageAvailableListener(null, null);
        }
        else {
            LogUtils.d("ERROR: startScreenCapture: mImageReader already exists");
        }

        if (mVirtualDisplay == null) {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                    mWindowWidth, mWindowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(),
                    null, null);
        }
        else {
            LogUtils.d("ERROR: startScreenCapture: mVirtualDisplay already exists");
        }
    }

    public void _captureOnce() {
        LogUtils.d("captureOnce: E...");
        if (mImageReader == null) {
            LogUtils.e("captureOnce: no image reader to capture");
            return;
        }

        while(true) {
            Image img = mImageReader.acquireLatestImage();
            if (img == null) {
                LogUtils.e("captureOnce: acquireLatestImage return null");
                continue;
            }
            DataLogic.saveCaptureImage(img);
            img.close();
            break;
        }
    }

    private void _stopScreenCapture() {
        LogUtils.d("stopScreenCapture");

        if (mVirtualDisplay == null) {
            LogUtils.d("ERROR: stopScreenCapture: mVirtualDisplay null");
        }
        else {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }

        if (mImageReader == null) {
            LogUtils.d("ERROR: stopScreenCapture: mImageReader null");
        }
        else {
            mImageReader.setOnImageAvailableListener(null, null);
            mImageReader.close();
            mImageReader = null;
        }

        if (mMediaProjection == null) {
            LogUtils.d("ERROR: stopScreenCapture: mMediaProjection null");
        }
        else {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

}
