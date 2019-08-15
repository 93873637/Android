/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liz.screenhelper.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.screenhelper.R;
import com.liz.screenhelper.app.ThisApp;
import com.liz.screenhelper.logic.ComDef;
import com.liz.screenhelper.logic.DataLogic;
import com.liz.screenhelper.logic.ScreenServer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Provides UI for the screen capture.
 */
@SuppressWarnings({"WeakerAccess, unused"})
public class ScreenCaptureFragment extends Fragment implements View.OnClickListener {

    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";

    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private int mResultCode;
    private Intent mResultData;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private static ImageReader mImageReader = null;

    private int mWindowWidth = 0;
    private int mWindowHeight = 0;
    private int mScreenDensity = 0;

    private TextView mTextServerInfo;
    private Button mBtnSwitchCapture;
    private TextView mTextProgress;
    private ScrollView mScrollProgress;

    private static boolean mCaptureOnce;
    private Timer mUpdateUITimer;

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            //showProgress("onImageAvailable");
            Image img = reader.acquireNextImage();
            DataLogic.enqueueScreenImage(img);
            if (mCaptureOnce) {
                onCaptureOnce(img);
                mCaptureOnce = false;
            }
            img.close();  //NOTE: you must close the image to get next
        }
    };

    private void onCaptureOnce(Image img) {
        String fileTime = TimeUtils.getFileTime();

        //save image to jpg file
        String jpgFileName = genJPGFileName(fileTime);
        int ret = ImageUtils.saveImage2JPGFile(img, jpgFileName);
        if (ret < 0) {
            showProgress("save screen image to " + jpgFileName + " failed with error " + ret);
        } else {
            showProgress("screen image saved to " + jpgFileName);
        }

        //save image data to file
        String dataFileName = genScreenDataFileName(fileTime);
        ret = ImageUtils.saveImageData2File(img, dataFileName);
        if (ret < 0) {
            showProgress("save screen image data to " + dataFileName + " failed with error " + ret);
        } else {
            showProgress("screen image data saved to " + dataFileName);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
        }

        WindowManager windowManager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
        mWindowWidth = windowManager.getDefaultDisplay().getWidth();
        mWindowHeight = windowManager.getDefaultDisplay().getHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenDensity = displayMetrics.densityDpi;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screen_capture, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.switch_capture_onoff).setOnClickListener(this);
        view.findViewById(R.id.capture_screen_once).setOnClickListener(this);
        view.findViewById(R.id.exit_app).setOnClickListener(this);

        mTextServerInfo = view.findViewById(R.id.serverInfo);
        mBtnSwitchCapture = view.findViewById(R.id.switch_capture_onoff);
        view.findViewById(R.id.capture_screen_once).setBackgroundColor(Color.LTGRAY);

        mTextProgress = view.findViewById(R.id.textProgress);
        mTextProgress.setText("");
        mScrollProgress = view.findViewById(R.id.scrollview);
        mScrollProgress.setBackgroundColor(Color.LTGRAY);
        mTextProgress.setMovementMethod(ScrollingMovementMethod.getInstance());

        LogUtils.setLogListener(new LogUtils.LogListener() {
            @Override
            public void onCBLog(String msg, int level) {
                ScreenCaptureFragment.this.showProgress(msg);
            }
        });

        startUITimer();
    }

    private void startUITimer() {
        //detect and update NV21 files of /sdcard/camera
        mUpdateUITimer = new Timer();
        mUpdateUITimer.schedule(new TimerTask() {
            public void run () {
                ScreenCaptureFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void stopUITimer() {
        if (mUpdateUITimer != null) {
            mUpdateUITimer.cancel();
            mUpdateUITimer = null;
        }
    }

    public void updateUI() {
        if (isCaptureOn()) {
            String btnInfo = "Stop Capture(" + DataLogic.getFrameRate() + "/" + DataLogic.getQueueSize() + "/" + DataLogic.getImageSize() + ")";
            mBtnSwitchCapture.setText(btnInfo);
            mBtnSwitchCapture.setBackgroundColor(Color.GREEN);
        }
        else {
            mBtnSwitchCapture.setText("Start Capture");
            mBtnSwitchCapture.setBackgroundColor(Color.LTGRAY);
        }

        mTextServerInfo.setText(ScreenServer.getServerInfo());
        if (ScreenServer.getState().equals(ComDef.SCREEN_SERVER_STATE_LISTENING)) {
            mTextServerInfo.setBackgroundColor(Color.GREEN);
        }
        else if (ScreenServer.getState().equals(ComDef.SCREEN_SERVER_STATE_RUNNING)) {
            mTextServerInfo.setBackgroundColor(Color.LTGRAY);
        }
        else {
            mTextServerInfo.setBackgroundColor(Color.rgb(0xaa, 0xaa, 0xaa));
        }
    }

    private void showProgress(final String msg) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    String logMsg = TimeUtils.getLogTime() + " - " + msg;
                    mTextProgress.append(logMsg + "\n");
                    mScrollProgress.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollProgress.smoothScrollTo(0, mTextProgress.getBottom());
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mMediaProjectionManager = (MediaProjectionManager)activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mMediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                String tip = "ERROR: resultCode = "+resultCode+", NOT OK";
                showProgress(tip);
                Toast.makeText(getActivity(), tip, Toast.LENGTH_SHORT).show();
                return;
            }

            Activity activity = getActivity();
            if (activity == null) {
                showProgress("ERROR: activity is null");
                return;
            }

            showProgress("onActivityResult OK");
            mResultCode = resultCode;
            mResultData = data;
            startScreenCapture();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_capture_onoff:
                mBtnSwitchCapture.setEnabled(false);
                if (isCaptureOn()) {
                    mBtnSwitchCapture.setText("Switch Off...");
                    stopScreenCapture();
                }
                else {
                    mBtnSwitchCapture.setText("Switch On...");
                    startScreenCapture();
                }
                mBtnSwitchCapture.setEnabled(true);
                break;
            case R.id.capture_screen_once:
                captureOnce();
                break;
            case R.id.exit_app:
                stopScreenCapture();
                stopUITimer();
                ThisApp.exitApp();
                break;
        }
    }

    private void startScreenCapture() {
        showProgress("startScreenCapture");

        if (mMediaProjection == null) {
            mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
        }
        else {
            showProgress("ERROR: startScreenCapture: mMediaProjection already exists");
        }

        if (mImageReader == null) {
            //
            //NOTE: only PixelFormat.RGBA_8888(1) is supported for my A2H
            //java.lang.UnsupportedOperationException: The producer output buffer format 0x1 doesn't match the ImageReader's configured buffer format 0x3.
            //
            mImageReader = ImageReader.newInstance(mWindowWidth, mWindowHeight, PixelFormat.RGBA_8888, 2); //ImageFormat.RGB_565
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
        }
        else {
            showProgress("ERROR: startScreenCapture: mImageReader already exists");
        }

        if (mVirtualDisplay == null) {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                    mWindowWidth, mWindowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(),
                    null, null);
        }
        else {
            showProgress("ERROR: startScreenCapture: mVirtualDisplay already exists");
        }
    }

    private void stopScreenCapture() {
        showProgress("stopScreenCapture");

        if (mVirtualDisplay == null) {
            showProgress("ERROR: stopScreenCapture: mVirtualDisplay null");
        }
        else {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }

        if (mImageReader == null) {
            showProgress("ERROR: stopScreenCapture: mImageReader null");
        }
        else {
            mImageReader.setOnImageAvailableListener(null, null);
            mImageReader.close();
            mImageReader = null;
        }

        if (mMediaProjection == null) {
            showProgress("ERROR: stopScreenCapture: mMediaProjection null");
        }
        else {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    public static boolean isCaptureOn() {
        return mImageReader != null;
    }

    public static void captureOnce() {
        LogUtils.d("captureOnce");
        if (isCaptureOn()) {
            mCaptureOnce = true;
        }
        else {
            LogUtils.d("captureOnce: no image reader to capture");
        }
    }

    private static String genDynamicJPGFileName() {
        return ComDef.SCREEN_PICTURE_SAVE_DIR + "ScreenShot_" + TimeUtils.getFileTime() + ".jpg";
    }

    private static String getStaticImageFileName() {
        return "ddz_current.jpg";
    }

    private static String genJPGFileName(String fileTime) {
        return ComDef.SCREEN_PICTURE_SAVE_DIR + "ScreenShot_" + fileTime + ".jpg";
    }

    private static String genScreenDataFileName(String fileTime) {
        return ComDef.SCREEN_PICTURE_SAVE_DIR + "ScreenShot_" + fileTime + ".bin";
    }
}
