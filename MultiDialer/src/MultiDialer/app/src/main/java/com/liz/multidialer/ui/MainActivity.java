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
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TelUtils;
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
    private static final int REQUEST_CODE_DEVICE_CONFIG = 3;

    private TextView mTextDeviceId;
    private TextView mTextTelListFile;
    private TextView mTextTelListNum;
    private TextView mTextCalledNum;
    private EditText mEditDialInterval;

    private Button mBtnCall;

    private Timer mUITimer;
    TelephonyManager mTelephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogUtils.d("MainActivity: onCreate: MainActivity Object = " + MainActivity.this);
        LogUtils.d("MainActivity: onCreate: MainThreadId = " + android.os.Process.myTid());

        checkPermissions();

        initView();
        initFloatingWindow();
        startUITimer();

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        ScreenCapture.initScreenCapture(MainActivity.this);
    }

    private void initView() {
        mTextDeviceId = findViewById(R.id.device_id);
        findViewById(R.id.text_config_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, DeviceConfigActivity.class),
                        REQUEST_CODE_DEVICE_CONFIG);
            }
        });

        mTextTelListFile = findViewById(R.id.tel_list_file);
        mTextTelListNum = findViewById(R.id.tel_list_num);

        mTextCalledNum = findViewById(R.id.called_num);
        findViewById(R.id.text_reset_called_index).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataLogic.resetCalledIndex();
            }
        });

        mEditDialInterval = findViewById(R.id.edit_dial_interval);
        String editInterval = "" + ComDef.DEFAULT_END_CALL_DELAY;
        mEditDialInterval.setText(editInterval);

        mBtnCall = findViewById(R.id.btn_start_call);
        mBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallButtonClicked();
            }
        });

        findViewById(R.id.btn_exit_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThisApp.exitApp();
            }
        });
    }

    private void initFloatingWindow() {
        FloatingButtonService.setFloatingButtonCallback(new FloatingButtonService.FloatingButtonCallback() {
            @Override
            public void onFloatButtonClicked() {
                showProgress("Floating Button Clicked to Stop Call...");
                onStopCall();
            }
        });

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无浮窗权限，请授权", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())),
                    REQUEST_CODE_FLOATING_BUTTON);
        }
        else {
            FloatingButtonService.start(this);
        }
    }

//
//    private void initScreenCapture() {
//        ScreenCapture.initScreenCapture(MainActivity.this);
//
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            String daemonAction = bundle.getString("MULTIDIALER_DAEMON_ACTION");
//            if (TextUtils.equals(daemonAction, "START")) {
//                onStartCall();
//            }
//        }
//    }

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
        DataLogic.setEndCallDelay(Integer.parseInt(mEditDialInterval.getText().toString()));
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
        //###@: set/update device id

        String calledNumInfo = "" + DataLogic.getCalledNum();
        mTextCalledNum.setText(Html.fromHtml(calledNumInfo));

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
                mBtnCall.setText("拨号结束, 点击重置重新拨号");
                mBtnCall.setBackgroundColor(Color.BLUE);
                mBtnCall.setEnabled(false);
            }
            else {
                mBtnCall.setText("开始拨号");
                mBtnCall.setBackgroundColor(Color.GREEN);
                mBtnCall.setEnabled(true);
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

        if (requestCode == REQUEST_CODE_DEVICE_CONFIG) {
            updateUI();
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
            }, DataLogic.getEndCallDelay());

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
        long delay = DataLogic.getEndCallDelay() - ComDef.CAPTURE_SCREEN_OFFSET;
        if (delay < 0) {
            return 0;
        }
        else {
            return delay;
        }
    }

    private long getCallNextDelay() {
        return DataLogic.getEndCallDelay() + ComDef.CALL_NEXT_OFFSET;
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
        //###@: todo: write to log file
//        runOnUiThread(new Runnable() {
//            public void run() {
//                String logMsg = TimeUtils.getLogTime() + " - " + msg;
//                mTextProgress.append(logMsg + "\n");
//                mScrollProgress.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mScrollProgress.smoothScrollTo(0, mTextProgress.getBottom());
//                    }
//                });
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d("MainActivity: onDestroy");
        FloatingButtonService.stop(this);
        stopUITimer();
    }
}
