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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TelUtils;
import com.liz.androidutils.FileUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.multidialer.app.ThisApp;
import com.liz.multidialer.logic.ComDef;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText mEditDialInterval;
    private TextView mTextProgress;
    private ScrollView mScrollProgress;
    private TextView mTextTelNumber;
    private Button mBtnCall;

    private int mTelIndex = 0;
    private ArrayList<String> mTelList;
    private String mPictureDir = ComDef.DIALER_DIR;

    // Permissions Required
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
    };

    private boolean mCallRunning = false;
    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private int mResultCode;
    private Intent mResultData;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private ImageReader mImageReader = null;

    private int mWindowWidth = 0;
    private int mWindowHeight = 0;
    private int mScreenDensity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        mEditDialInterval = findViewById(R.id.edit_dial_interval);
        mEditDialInterval.setText("" + ComDef.END_CALL_DELAY);

        mTextTelNumber = findViewById(R.id.text_tel_list_info);
        mTelList = FileUtils.readTxtFileLines(ComDef.TEL_LIST_FILE_NAME);

        String telListInfo;
        boolean bExit = false;
        if (mTelList == null) {
            telListInfo = "ERROR: 没有号码列表文件: " + ComDef.TEL_LIST_FILE_NAME;
            bExit = true;
        }
        else if (mTelList.size() == 0) {
            telListInfo = "ERROR: 号码列表为空";
            bExit = true;
        }
        else {
            telListInfo = "电话号码数量(" + ComDef.TEL_LIST_FILE_NAME + "): " + mTelList.size();
        }
        mTextTelNumber.setText(telListInfo);

        mBtnCall = findViewById(R.id.btn_start_call);
        if (bExit) {
            mBtnCall.setText("请先退出，准备好号码文件后重试");
            mBtnCall.setBackgroundColor(Color.RED);
            mBtnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ThisApp.exitApp();
                }
            });
            return;
        }

        mBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCallRunning) {
                    startCallOnList();
                    mBtnCall.setText("停止拨号");
                    mBtnCall.setBackgroundColor(Color.RED);
                }
                else {
                    mBtnCall.setText("开始拨号");
                    mBtnCall.setBackgroundColor(Color.GREEN);
                }
                mCallRunning = !mCallRunning;
            }
        });

        mTextProgress = findViewById(R.id.text_dial_info);
        mTextProgress.setText("");
        mScrollProgress = findViewById(R.id.scrollInfo);
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

        LogUtils.d("MainThreadId=" + android.os.Process.myTid());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                String tip = "ERROR: onActivityResult failed, resultCode = " + resultCode;
                showProgress(tip);
                Toast.makeText(MainActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
            else {
                showProgress("onActivityResult OK");
                mResultCode = resultCode;
                mResultData = data;
                startScreenCapture();
            }
            return;
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
            showProgress("ERROR: startCallOnList: list null");
            return;
        }

        if (!genPictureDir()) {
            showProgress("ERROR: startCallOnList: create picture dir failed.");
            return;
        }

        int dialInterval = Integer.parseInt(mEditDialInterval.getText().toString());
        showProgress("***startCallOnList: number=" + mTelList.size() + ", interval=" + dialInterval + "...");

        mTelIndex = 0;
        startCallOnNum(dialInterval);
    }

    private String getCurrentTelNumber() {
        return mTelList.get(mTelIndex);
    }

    private void startCallOnNum(final int dialInterval) {
        LogUtils.d("startCallOnNum: ThreadId=" + android.os.Process.myTid());
        String strTel = getCurrentTelNumber();
        if (!TelUtils.isValidTelNumber(strTel)) {
            showProgress("#" + (mTelIndex+1) + ": Invalid Tel Number = \"" + strTel + "\"");
            startNextCall(dialInterval);
            return;
        }
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

                    startNextCall(dialInterval);
                }
            }, dialInterval);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "OnClick Exception: " + e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            showProgress("startCallOnNum Exception: " + e.toString());
        }
    }

    private void startNextCall(int dialInterval) {
        if (mCallRunning) {
            mTelIndex++;
            if (mTelIndex < mTelList.size()) {
                startCallOnNum(dialInterval);
            }
            else {
                mBtnCall.setText("拨号结束, 点击退出");
                mBtnCall.setBackgroundColor(Color.BLUE);
                mBtnCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ThisApp.exitApp();
                    }
                });
                mCallRunning = false;
                showProgress("***startNextCall: Calls Finished.");
            }
        }
        else {
            showProgress("***startNextCall: Calls Canceled.");
        }
    }

    private void showProgress(final String msg) {
        runOnUiThread(new Runnable() {
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
            //####@: mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
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

    public void captureOnce() {
        LogUtils.d("captureOnce: E...");
        if (mImageReader == null) {
            LogUtils.e("captureOnce: no image reader to capture");
            return;
        }

        while(true) {
            Image img = mImageReader.acquireLatestImage();
            if (img == null) {
                continue;
            }
            saveCaptureImage(img);
            img.close();
            break;
        }
    }

    private boolean genPictureDir() {
        String strTimeDir = new SimpleDateFormat("yyMMdd.HHmmss").format(new java.util.Date());
        mPictureDir = ComDef.DIALER_DIR + "/" + strTimeDir;
        if (!FileUtils.touchDir(mPictureDir)) {
            showProgress("ERROR: create picture dir " + mPictureDir + " failed.");
            return false;
        }
        return true;
    }

    private void saveCaptureImage(Image img) {
        LogUtils.d("saveCaptureImage: E...");
        String jpgFileName = mPictureDir + "/" + getCurrentTelNumber() + ".jpg";
        int ret = ImageUtils.saveImage2JPGFile(img, jpgFileName);
        if (ret < 0) {
            showProgress("save screen image to " + jpgFileName + " failed with error " + ret);
        } else {
            showProgress("screen image saved to " + jpgFileName);
        }
    }
}
