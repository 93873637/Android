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
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liz.screenhelper.R;
import com.liz.screenhelper.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

/**
 * Provides UI for the screen capture.
 */
public class ScreenCaptureFragment extends Fragment implements View.OnClickListener {

    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";

    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private int mResultCode;
    private Intent mResultData;

    private static MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;

    private static int windowWidth = 0;
    private static int windowHeight = 0;
    private static ImageReader mImageReader = null;
    private WindowManager mWindowManager = null;
    private DisplayMetrics metrics = null;
    private static int mScreenDensity = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
        }

        mWindowManager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager.getDefaultDisplay().getHeight();
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565
        metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screen_capture, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.save_capture_image).setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mMediaProjectionManager = (MediaProjectionManager)
                activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startScreenCapture();  //####@:
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_capture_image:
                captureOnce();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(getActivity(), "R.string.user_cancelled", Toast.LENGTH_SHORT).show();
                return;
            }
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            LogUtils.d("Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;
            setUpMediaProjection();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScreenCapture();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tearDownMediaProjection();
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void startScreenCapture() {
        if (mMediaProjection != null) {

            //Capture Screen on Timer
            /*
            new Timer().schedule(new TimerTask() {
                public void run () {
                    ScreenCaptureFragment.this.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            captureOnce();
                        }
                    });
                }
            }, 500, 500);
            //*/

        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection();
        } else {
            LogUtils.i("Requesting confirmation");
            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    public static void captureOnce() {
        LogUtils.d("captureOnce: E...");
        setUpVirtualDisplay2();
        startScreenCapture2();
    }

    private static void setUpVirtualDisplay2() {
        LogUtils.d("setUpVirtualDisplay2: E...");
       mMediaProjection.createVirtualDisplay("screen-mirror",
                windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private static String getStaticImageFileName() {
        return "ddz_current.jpg";
    }

    private static String getDynamicImageFileName() {
        String strDateTime = new SimpleDateFormat("yyyy.MMdd.HHmmss").format(new java.util.Date());
        long currentTimeMillis = System.currentTimeMillis();
        long ms = currentTimeMillis % 1000;
        if (ms < 10)
            strDateTime += ".00" + ms;
        else if (ms < 100)
            strDateTime += ".0" + ms;
        else
            strDateTime += "." + ms;
        return "DDZScreenShot_" + strDateTime + ".jpg";
    }

    private static void startScreenCapture2() {
        LogUtils.d("startScreenCapture2: E...");

        String imagePath = Environment.getExternalStorageDirectory().getPath() + "/Pictures/DDZScreenShots/";
        File filePath = new File(imagePath);
        if (!filePath.exists()) {
            filePath.mkdirs();
            LogUtils.i("create image path: " + filePath);
        }

        String imageFileName = imagePath + getStaticImageFileName();
        LogUtils.d("startScreenCapture2: imageFileName = " + imageFileName);

        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            LogUtils.e("ERROR: acquireLatestImage null");
            return;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        LogUtils.d("startScreenCapture2: image size = " + width + "x" + height);

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();

        if (bitmap == null) {
            LogUtils.e("ERROR: bitmap null");
            return;
        }
        else {
            try {
                File fileImage = new File(imageFileName);
                if (!fileImage.exists()) {
                    fileImage.createNewFile();
                    LogUtils.i("create image file: " + imageFileName);
                }
                FileOutputStream out = new FileOutputStream(fileImage);
                if (out == null) {
                    LogUtils.e("ERROR: get FileOutputStream null");
                    return;
                }
                else {
                    LogUtils.i("bitmap compress...");
                    //###@: bitmap.compress(Bitmap.CompressFormat.  PNG, 100, out);  //PNG: take a long time
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  //using JPEG to fast save
                    LogUtils.i("out flush...");
                    out.flush();
                    LogUtils.i("out close...");
                    out.close();
                    //Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    //Uri contentUri = Uri.fromFile(fileImage);
                    //media.setData(contentUri);
                    //this.sendBroadcast(media);
                    LogUtils.i("screen image saved to " + imageFileName);
                }
            } catch (FileNotFoundException e) {
                LogUtils.e("ERROR: FileNotFoundException e=" + e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                LogUtils.e("ERROR: IOException e=" + e.toString());
                e.printStackTrace();
            } catch (Exception e) {
                LogUtils.e("ERROR: Exception e=" + e.toString());
                e.printStackTrace();
            }
        }

        LogUtils.d("startScreenCapture2: X.");
    }

    private void stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }
}
