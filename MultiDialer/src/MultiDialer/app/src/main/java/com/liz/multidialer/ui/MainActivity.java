package com.liz.multidialer.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
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
import com.liz.androidutils.TimeUtils;
import com.liz.multidialer.R;
import com.liz.multidialer.app.ThisApp;
import com.liz.multidialer.logic.ComDef;
import com.liz.multidialer.logic.DataLogic;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Permissions Required
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE
    };

    private static final int REQUEST_CODE_MEDIA_PROJECTION = 1;
    private static final int REQUEST_CODE_FLOATING_BUTTON = 2;

    private EditText mEditDialInterval;
    private TextView mTextTelListInfo;
    private TextView mTextCalledIndex;
    private TextView mTextProgress;
    private ScrollView mScrollProgress;
    private Button mBtnCall;

    private Timer mUpdateUITimer;

    private int mCallState = TelephonyManager.CALL_STATE_IDLE;
    private int mCallInterval = ComDef.CALL_INTERVAL;

    private int mResultCode;
    private Intent mResultData;

    TelephonyManager mTelephonyManager;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private ImageReader mImageReader = null;

    private int mWindowWidth = 0;
    private int mWindowHeight = 0;
    private int mScreenDensity = 0;

    private Messenger sMessenger;
    private boolean isConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogUtils.d("onCreate: MainThreadId=" + android.os.Process.myTid());

        checkPermissions();

        mEditDialInterval = findViewById(R.id.edit_dial_interval);
        String editInterval = "" + ComDef.CALL_INTERVAL;
        mEditDialInterval.setText(editInterval);

        mTextTelListInfo = findViewById(R.id.text_tel_list_info);

        findViewById(R.id.text_reset_called_index).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataLogic.resetCalledIndex();
            }
        });

        mTextCalledIndex = findViewById(R.id.text_called_index);

        mBtnCall = findViewById(R.id.btn_start_call);
        if (!DataLogic.initCheck()) {
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
        else {
            mBtnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditDialInterval.clearFocus();
                    if (!DataLogic.isCallRunning()) {
                        mCallInterval = Integer.parseInt(mEditDialInterval.getText().toString());
                        if (DataLogic.startCall()){
                            loopCallOnNum();
                        }
                    }
                    else {
                        DataLogic.stopCall();
                    }
                }
            });
        }

        mTextProgress = findViewById(R.id.text_dial_info);
        mTextProgress.setText("");
        mScrollProgress = findViewById(R.id.scrollInfo);
        mTextProgress.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        initScreenCapture(MainActivity.this);
        loopListenOnPhoneState();

        FloatingButtonService.setFloatingButtonCallback(new FloatingButtonService.FloatingButtonCallback() {
            @Override
            public void onFloatButtonClicked() {
                DataLogic.stopCall();
            }
        });

        if (Settings.canDrawOverlays(this)) {
            FloatingButtonService.start(this, conn);
        }
        else {
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), REQUEST_CODE_FLOATING_BUTTON);
        }

        startUITimer();
    }

    private void startUITimer() {
        //detect and update NV21 files of /sdcard/camera
        mUpdateUITimer = new Timer();
        mUpdateUITimer.schedule(new TimerTask() {
            public void run () {
                MainActivity.this.runOnUiThread(new Runnable() {
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

    protected void updateUI() {
        mTextTelListInfo.setText(DataLogic.getTelListInfo());

        String callIndexInfo = "当前已拨打: " + DataLogic.getCurrentCallIndex();
        mTextCalledIndex.setText(callIndexInfo);

        if (DataLogic.isCallRunning()) {
            mBtnCall.setText("停止拨号");
            mBtnCall.setBackgroundColor(Color.RED);


            String progressInfo = "正在拨打 " + (DataLogic.getCurrentCallIndex()+1) + "/" + DataLogic.getTelNumber() + ": " + DataLogic.getCurrentTelNumber();
            Toast.makeText(MainActivity.this, progressInfo, Toast.LENGTH_LONG).show();

            // 初始化发送给Service的消息，并将cMessenger传递给Service
            Message msg = Message.obtain();
            msg.what = 0;
            Bundle b = new Bundle();
            b.putString("progressInfo", progressInfo);
            msg.setData(b);
            try {
                sMessenger.send(msg);

            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            if (DataLogic.isCallFinished()) {
                mBtnCall.setText("拨号结束, 点击退出");
                mBtnCall.setBackgroundColor(Color.BLUE);
                mBtnCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ThisApp.exitApp();
                    }
                });
            }
            else {
                mBtnCall.setText("开始拨号");
                mBtnCall.setBackgroundColor(Color.GREEN);
            }
        }
    }

    private void loopListenOnPhoneState() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mTelephonyManager.listen(new PhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE);
                loopListenOnPhoneState();
            }
        }, ComDef.LISTEN_CALL_STATE_TIME);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_MEDIA_PROJECTION) {
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
        else if (requestCode == REQUEST_CODE_FLOATING_BUTTON) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                FloatingButtonService.start(this, conn);
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //定义Connection，作为启动Services的参数
    private ServiceConnection conn=new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sMessenger = new Messenger(service);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sMessenger=null;
        }
    };

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

    private void loopCallOnNum() {
        LogUtils.d("loopCallOnNum: DataLogic.getCurrentCallIndex()=" + DataLogic.getCurrentCallIndex());
        //LogUtils.d("loopCallOnNum: DataLogic.getCurrentCallIndex()=" + DataLogic.getCurrentCallIndex() + ", ThreadId=" + android.os.Process.myTid());

        String strTel = DataLogic.getCurrentTelNumber();
        if (!TelUtils.isValidTelNumber(strTel)) {
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Invalid Tel Number = \"" + strTel + "\"");
            callNextNum();
            return;
        }

        if (mCallState != TelephonyManager.CALL_STATE_IDLE) {
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Last call not ended, try end and call again...");
            TelUtils.endCall(MainActivity.this);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    loopCallOnNum();
                }
            }, ComDef.WAIT_CALL_IDLE_TIME);
            return;
        }

        try {
            String ret = TelUtils.startCall(MainActivity.this, strTel);
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Start Call, Tel = " + strTel + ", " + ret);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mTelephonyManager.listen(new PhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE);

                    //before end call, we take the screen picture
                    captureOnce();

                    //end current call
                    String retEndCall = TelUtils.endCall(MainActivity.this);
                    showProgress("End Call: " + retEndCall);

                    callNextNum();
                }
            }, mCallInterval*1000L);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "loopCallOnNum Exception: " + e.toString(), Toast.LENGTH_SHORT).show();
            showProgress("loopCallOnNum Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private void callNextNum() {
        if (DataLogic.toNextCall()) {
            loopCallOnNum();
        }
        else {
            LogUtils.d("callNextNum: can't go to next call.");
        }
    }

    private void showProgress(final String msg) {
        LogUtils.d("showProgress: " + msg);
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

    public class PhoneCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //LogUtils.d("onCallStateChanged: ThreadId = " + android.os.Process.myTid());
            mCallState = state;
            //LogUtils.d("onCallStateChanged: mCallState = " + mCallState);
//            switch (state) {
//                case TelephonyManager.CALL_STATE_IDLE:
//                    LogUtils.d("onCallStateChanged: CALL_STATE_IDLE");
//                    break;
//                case TelephonyManager.CALL_STATE_OFFHOOK:  //接听 or 正在拨号
//                    LogUtils.d("onCallStateChanged: CALL_STATE_OFFHOOK");
//                    break;
//                case TelephonyManager.CALL_STATE_RINGING:
//                    LogUtils.d("onCallStateChanged: CALL_STATE_RINGING");
//                    break;
//            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    private void initScreenCapture(Activity activity) {
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
                    startActivityForResult(
                            mMediaProjectionManager.createScreenCaptureIntent(),
                            REQUEST_CODE_MEDIA_PROJECTION);
                }
            }
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

    public void captureOnce() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUITimer();
    }
}
