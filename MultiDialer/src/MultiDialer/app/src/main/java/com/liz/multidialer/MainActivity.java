package com.liz.multidialer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TelUtils;
import com.liz.androidutils.FileUtils;
import com.liz.androidutils.TimeUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TEL_LIST_FILE_NAME = "/sdcard/tellist.txt";

    private TextView mTextProgress;
    private ScrollView mScrollProgress;
    private TextView mTextTelNumber;

    private int mTelIndex = 0;
    private ArrayList<String> mTelList;

    // Permissions Required
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
    };

    private static boolean mCaptureOnce;

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

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            //showProgress("onImageAvailable");
            Image img = reader.acquireNextImage();
            if (mCaptureOnce) {
                onCaptureOnce(img);
                mCaptureOnce = false;
            }
            img.close();  //NOTE: you must close the image to get next
        }
    };

    private void onCaptureOnce(Image img) {
        //save image to jpg file
        String jpgFileName = "/sdcard/multidialer/" + getCurrentTelNumber() + ".jpg";;
        int ret = ImageUtils.saveImage2JPGFile(img, jpgFileName);
        if (ret < 0) {
            showProgress("save screen image to " + jpgFileName + " failed with error " + ret);
        } else {
            showProgress("screen image saved to " + jpgFileName);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        mTelList = FileUtils.readTxtFileLines(TEL_LIST_FILE_NAME);

        mTextTelNumber = findViewById(R.id.text_tel_list_info);
        mTextTelNumber.setText("电话号码数量(" + TEL_LIST_FILE_NAME + "): " + mTelList.size());

        mTextProgress = findViewById(R.id.text_dial_info);
        mTextProgress.setText("");
        mScrollProgress = findViewById(R.id.scrollInfo);
        mScrollProgress.setBackgroundColor(Color.LTGRAY);
        mTextProgress.setMovementMethod(ScrollingMovementMethod.getInstance());

        WindowManager windowManager = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
        mWindowWidth = windowManager.getDefaultDisplay().getWidth();
        mWindowHeight = windowManager.getDefaultDisplay().getHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenDensity = displayMetrics.densityDpi;

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mMediaProjectionManager = (MediaProjectionManager)this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mMediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);

        //##@: test
//        for (int i=0; i<100; i++) {
//            showProgress("#" + i + ": Start Call: 12345678901, OKssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
//        }

        findViewById(R.id.btn_start_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCallOnList();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                String tip = "ERROR: resultCode = "+resultCode+", NOT OK";
                showProgress(tip);
                Toast.makeText(MainActivity.this, tip, Toast.LENGTH_SHORT).show();
                return;
            }

            Activity activity = MainActivity.this;
            if (activity == null) {
                showProgress("ERROR: activity is null");
                return;
            }

            showProgress("onActivityResult OK");
            mResultCode = resultCode;
            mResultData = data;
            startScreenCapture();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void checkPermissions() {
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    permissions,
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCallOnList() {
        if (mTelList == null) {
            showProgress("startCallOnList: list null");
            return;
        }
        mTelIndex = 0;
        startCallOnNum();
    }

    private String getCurrentTelNumber() {
        return mTelList.get(mTelIndex);
    }

    private void startCallOnNum() {
        String strTel = getCurrentTelNumber();
        try {
            String ret = TelUtils.startCall(MainActivity.this, strTel);
            showProgress("#" + (mTelIndex+1) + ": Start Call, Tel = " + strTel + ", " + ret);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //before end call, we should take screen picture
                    captureOnce();

                    //end current call
                    String retEndCall = TelUtils.endCall(MainActivity.this);
                    showProgress("End Call: " + retEndCall);

                    //start next call
                    mTelIndex ++;
                    if (mTelIndex < mTelList.size()) {
                        startCallOnNum(mTelIndex);
                    }
                    else {

                    }
                }
            }, 2 * 1000);  // 延迟n秒后自动挂断电话
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "OnClick Exception: " + e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            showProgress("startCallOnNum Exception: " + e.toString());
        }
    }

    private void showProgress(final String msg) {
        Activity activity = MainActivity.this;
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
}
