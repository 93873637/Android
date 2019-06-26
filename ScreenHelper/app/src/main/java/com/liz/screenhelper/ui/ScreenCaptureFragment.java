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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liz.screenhelper.R;
import com.liz.screenhelper.app.ThisApp;
import com.liz.screenhelper.logic.ComDef;
import com.liz.screenhelper.logic.ScreenServer;
import com.liz.screenhelper.utils.ImageUtils;
import com.liz.screenhelper.utils.LogUtils;
import com.liz.screenhelper.utils.TimeUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Provides UI for the screen capture.
 */
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
            if (mCaptureOnce) {
                String fileName = getDynamicImageFileName();
                int ret = ImageUtils.saveImage(img, fileName);
                if (ret < 0) {
                    showProgress("save screen image to " + fileName + " failed with error " + ret);
                }
                else {
                    showProgress("screen image saved to " + fileName);
                }
                mCaptureOnce = false;
            }
            img.close();
        }
    };

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
        view.findViewById(R.id.start_capture_image).setOnClickListener(this);
        view.findViewById(R.id.stop_capture_image).setOnClickListener(this);
        view.findViewById(R.id.capture_screen_once).setOnClickListener(this);
        view.findViewById(R.id.exit_app).setOnClickListener(this);
        mTextServerInfo = view.findViewById(R.id.serverInfo);

        mTextProgress = view.findViewById(R.id.textProgress);
        mTextProgress.setText("");
        mScrollProgress = view.findViewById(R.id.scrollview);
        mTextProgress.setMovementMethod(ScrollingMovementMethod.getInstance());

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
        mTextServerInfo.setText(ScreenServer.getServerInfo());
        if (ScreenServer.getState().equals(ComDef.SCREEN_SERVER_STATE_RUNNING)) {
            mTextServerInfo.setBackgroundColor(Color.GREEN);
        }
        else {
            mTextServerInfo.setBackgroundColor(Color.LTGRAY);
        }
    }

    private void showProgress(final String msg) {
        LogUtils.i(msg);
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
            case R.id.start_capture_image:
                startScreenCapture();
                break;
            case R.id.stop_capture_image:
                stopScreenCapture();
                break;
            case R.id.capture_screen_once:
                captureOnce();
                break;
            case R.id.exit_app:
                stopUITimer();
                stopScreenCapture();
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
            mImageReader = ImageReader.newInstance(mWindowWidth, mWindowHeight, 0x1, 2); //ImageFormat.RGB_565
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

    public static void captureOnce() {
        LogUtils.d("captureOnce");
        if (mImageReader == null) {
            LogUtils.i("captureOnce: no image reader to capture");
        }
        else {
            mCaptureOnce = true;
        }
    }

    private static String getDynamicImageFileName() {
        return ComDef.SCREEN_PICTURE_SAVE_DIR + "ScreenShot_" + TimeUtils.getFileTime() + ".jpg";
    }

    private static String getStaticImageFileName() {
        return "ddz_current.jpg";
    }

//    private static void startScreenCapture2() {
//        showProgress("startScreenCapture2: E...");
//
//        String imagePath = Environment.getExternalStorageDirectory().getPath() + "/Pictures/DDZScreenShots/";
//        File filePath = new File(imagePath);
//        if (!filePath.exists()) {
//            filePath.mkdirs();
//            LogUtils.i("create image path: " + filePath);
//        }
//
//        String imageFileName = imagePath + getStaticImageFileName();
//        showProgress("startScreenCapture2: imageFileName = " + imageFileName);
//
//        Image image = mImageReader.acquireLatestImage();
//        if (image == null) {
//            showProgress("ERROR: acquireLatestImage null");
//            return;
//        }
//
//        int width = image.getWidth();
//        int height = image.getHeight();
//        showProgress("startScreenCapture2: image size = " + width + "x" + height);
//
//        final Image.Plane[] planes = image.getPlanes();
//        final ByteBuffer buffer = planes[0].getBuffer();
//        int pixelStride = planes[0].getPixelStride();
//        int rowStride = planes[0].getRowStride();
//        int rowPadding = rowStride - pixelStride * width;
//        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
//        bitmap.copyPixelsFromBuffer(buffer);
//        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
//        image.close();
//
//        if (bitmap == null) {
//            showProgress("ERROR: bitmap null");
//            return;
//        }
//        else {
//            try {
//                File fileImage = new File(imageFileName);
//                if (!fileImage.exists()) {
//                    fileImage.createNewFile();
//                    LogUtils.i("create image file: " + imageFileName);
//                }
//                FileOutputStream out = new FileOutputStream(fileImage);
//                if (out == null) {
//                    showProgress("ERROR: get FileOutputStream null");
//                    return;
//                }
//                else {
//                    LogUtils.i("bitmap compress...");
//                    //###@: bitmap.compress(Bitmap.CompressFormat.  PNG, 100, out);  //PNG: take a long time
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  //using JPEG to fast save
//                    LogUtils.i("out flush...");
//                    out.flush();
//                    LogUtils.i("out close...");
//                    out.close();
//                    //Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                    //Uri contentUri = Uri.fromFile(fileImage);
//                    //media.setData(contentUri);
//                    //this.sendBroadcast(media);
//                    LogUtils.i("screen image saved to " + imageFileName);
//                }
//            } catch (FileNotFoundException e) {
//                showProgress("ERROR: FileNotFoundException e=" + e.toString());
//                e.printStackTrace();
//            } catch (IOException e) {
//                showProgress("ERROR: IOException e=" + e.toString());
//                e.printStackTrace();
//            } catch (Exception e) {
//                showProgress("ERROR: Exception e=" + e.toString());
//                e.printStackTrace();
//            }
//        }
//
//        showProgress("startScreenCapture2: X.");
//    }
}
