package com.liz.multidialer.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TelUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.multidialer.R;
import com.liz.multidialer.app.ThisApp;
import com.liz.multidialer.logic.ComDef;
import com.liz.multidialer.logic.DataLogic;
import com.liz.multidialer.logic.ScreenCapture;

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

    private static final int REQUEST_CODE_FLOATING_BUTTON = 2;

    private EditText mEditDialInterval;
    private TextView mTextTelListInfo;
    private TextView mTextCalledIndex;
    private TextView mTextProgress;
    private ScrollView mScrollProgress;
    private Button mBtnCall;

    private Timer mUITimer;
    private long mEndCallDelay = ComDef.DEFAULT_END_CALL_DELAY;
    TelephonyManager mTelephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogUtils.d("MainActivity: onCreate: MainActivity Object = " + MainActivity.this);
        LogUtils.d("MainActivity: onCreate: MainThreadId = " + android.os.Process.myTid());

        checkPermissions();

        mEditDialInterval = findViewById(R.id.edit_dial_interval);
        String editInterval = "" + ComDef.DEFAULT_END_CALL_DELAY;
        mEditDialInterval.setText(editInterval);

        mTextTelListInfo = findViewById(R.id.text_tel_list_info);
        findViewById(R.id.text_refresh_tel_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataLogic.loadTelList();
                updateUI();
            }
        });

        findViewById(R.id.text_reset_called_index).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataLogic.resetCalledIndex();
            }
        });

        mTextCalledIndex = findViewById(R.id.text_called_index);

        mBtnCall = findViewById(R.id.btn_start_call);
        if (!DataLogic.initCheck()) {
            mBtnCall.setText("初始化失败，请检查号码文件/图片目录后重试");
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
                    onCallButtonClicked();
                }
            });
        }

        findViewById(R.id.btn_exit_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThisApp.exitApp();
            }
        });

        mTextProgress = findViewById(R.id.text_dial_info);
        mTextProgress.setText("");
        mScrollProgress = findViewById(R.id.scrollInfo);
        mTextProgress.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        FloatingButtonService.setFloatingButtonCallback(new FloatingButtonService.FloatingButtonCallback() {
            @Override
            public void onFloatButtonClicked() {
                showProgress("Floating Button Clicked to Stop Call...");
                onStopCall();
            }
        });

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无浮窗权限，请授权", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), REQUEST_CODE_FLOATING_BUTTON);
        }

        FloatingButtonService.start(this);
        startUITimer();

        ScreenCapture.initScreenCapture(MainActivity.this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String daemonAction = bundle.getString("MULTIDIALER_DAEMON_ACTION");
            if (TextUtils.equals(daemonAction, "START")) {
                onStartCall();
            }
        }
    }

    private void onCallButtonClicked() {
        mEditDialInterval.clearFocus();
        if (DataLogic.isCallRunning()) {
            showProgress("Call Button Clicked to Stop Call...");
            onStopCall();
        } else {
            showProgress("Call Button Clicked to Start Call...");
            onStartCall();
        }
    }

    private void onStartCall() {
        mEndCallDelay = Integer.parseInt(mEditDialInterval.getText().toString());
        if (DataLogic.startCall()) {
            loopCallOnNum();
        }
        FloatingButtonService.showFloatingButton(true);
    }

    private void onStopCall() {
        DataLogic.stopCall();
        //stopCaptureTimer();
        FloatingButtonService.showFloatingButton(false);
    }

    private void startUITimer() {
        //detect and update NV21 files of /sdcard/camera
        mUITimer = new Timer();
        mUITimer.schedule(new TimerTask() {
            public void run () {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, ComDef.UI_TIMER_DELAY, ComDef.UI_TIMER_PERIOD);
    }

    private void stopUITimer() {
        if (mUITimer != null) {
            mUITimer.cancel();
            mUITimer = null;
        }
    }

    protected void updateUI() {
        mTextTelListInfo.setText(DataLogic.getTelListInfo());

        String callIndexInfo = "当前已拨打: " + DataLogic.getCurrentCallIndex();
        mTextCalledIndex.setText(callIndexInfo);

        if (DataLogic.isCallRunning()) {
            mBtnCall.setText("停止拨号");
            mBtnCall.setBackgroundColor(Color.RED);

            String progressInfo = "正在拨打 " + (DataLogic.getCurrentCallIndex()+1)
                    + "/" + DataLogic.getTelNumber() + ": "
                    + DataLogic.getCurrentTelNumber() + "\n"
                    + "点击停止";
            FloatingButtonService.updateInfo(progressInfo);
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

    public boolean isTelephonyCalling(){
        return (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) ||
                (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FLOATING_BUTTON) {
                if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "浮窗权限授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "浮窗权限授权成功", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ScreenCapture.onActivityResult(requestCode, resultCode, data);
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

    private void loopCallOnNum() {
        LogUtils.d("loopCallOnNum: DataLogic.getCurrentCallIndex()=" + DataLogic.getCurrentCallIndex());
        //LogUtils.d("loopCallOnNum: DataLogic.getCurrentCallIndex()=" + DataLogic.getCurrentCallIndex() + ", ThreadId=" + android.os.Process.myTid());

        String strTel = DataLogic.getCurrentTelNumber();
        if (!TelUtils.isValidTelNumber(strTel)) {
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Invalid Tel Number = \"" + strTel + "\"");
            callNextNum();
            return;
        }

        //if (mCallState != TelephonyManager.CALL_STATE_IDLE) {
        if (isTelephonyCalling()) {
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Last call not ended, try end it and call again...");
            TelUtils.endCall(MainActivity.this);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    loopCallOnNum();
                }
            }, ComDef.WAIT_CALL_IDLE_DELAY);
            return;
        }

        try {
            String ret = TelUtils.startCall(MainActivity.this, strTel);
            showProgress("#" + (DataLogic.getCurrentCallIndex()+1) + ": Start Call, Tel = " + strTel + ", " + ret);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    ScreenCapture.captureOnce();
                }
            }, getScreenCaptureDelay());

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    String retEndCall = TelUtils.endCall(MainActivity.this);
                    showProgress("End Call: " + retEndCall);
                }
            }, mEndCallDelay);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    callNextNum();
                }
            }, getCallNextDelay());

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "loopCallOnNum Exception: " + e.toString(), Toast.LENGTH_SHORT).show();
            showProgress("loopCallOnNum Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private long getScreenCaptureDelay() {
        long delay = mEndCallDelay - ComDef.CAPTURE_SCREEN_OFFSET;
        if (delay < 0) {
            return 0;
        }
        else {
            return delay;
        }
    }

    private long getCallNextDelay() {
        return mEndCallDelay + ComDef.CALL_NEXT_OFFSET;
    }

    private void callNextNum() {
        if (DataLogic.toNextCall()) {
            loopCallOnNum();
        }
        else {
            showProgress("callNextNum: No next call.");
            FloatingButtonService.showFloatingButton(false);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d("MainActivity: onDestroy");
        FloatingButtonService.stop(this);
        stopUITimer();
    }
}
