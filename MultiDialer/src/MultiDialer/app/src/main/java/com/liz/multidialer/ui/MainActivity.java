package com.liz.multidialer.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.NetUtils;
import com.liz.androidutils.StringBufferQueue;
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
    private static final int REQUEST_CODE_DEVICE_CONFIG = 3;

    private TextView mTextDeviceId;
    private TextView mTextTelListFile;
    private TextView mTextTelListNum;
    private TextView mTextCalledNum;
    private EditText mEditDialInterval;

    private Button mBtnCall;

    private ScrollView mScrollProgressInfo;
    private TextView mTextStaticInfo;
    private TextView mTextProgressInfo;
    private StringBufferQueue mProgressBuffer;

    private Timer mUITimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogUtils.d("MainActivity: onCreate: MainActivity Object = " + MainActivity.this);
        LogUtils.d("MainActivity: onCreate: MainThreadId = " + android.os.Process.myTid());
        ((ThisApp)getApplication()).addActivity(this);

        checkPermissions();

        initView();
        initFloatingWindow();
        startUITimer();

        ScreenCapture.initScreenCapture(MainActivity.this);
    }

    private void initView() {
        mTextDeviceId = findViewById(R.id.device_id);
        mTextDeviceId.setText(DataLogic.getDeviceId());
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
        findViewById(R.id.text_clear_numfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataLogic.clearTelListFile();
            }
        });

        findViewById(R.id.text_config_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, DeviceConfigActivity.class),
                        REQUEST_CODE_DEVICE_CONFIG);
            }
        });

        mEditDialInterval = findViewById(R.id.edit_dial_interval);
        String editInterval = "" + ComDef.DEFAULT_END_CALL_DELAY;
        mEditDialInterval.setText(editInterval);

        mBtnCall = findViewById(R.id.btn_call);
        mBtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallButtonClicked();
            }
        });

        if (ComDef.DEBUG) {
            mScrollProgressInfo = findViewById(R.id.scroll_progress_info);
            mTextStaticInfo = findViewById(R.id.text_static_info);
            mTextProgressInfo = findViewById(R.id.text_progress_info);
            mProgressBuffer = new StringBufferQueue(32);
            DataLogic.setProgressCallback(new DataLogic.ShowProgressCallback() {
                @Override
                public void onShowProgress(String msg) {
                    MainActivity.this.showProgressInfo(msg);
                }
            });
        }

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
                showProgressInfo("Floating Button Clicked to Stop Call...");
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

    private void onCallButtonClicked() {
        if (!checkStart()) {
            return;
        }

        mEditDialInterval.clearFocus();
        DataLogic.setEndCallDelay(Integer.parseInt(mEditDialInterval.getText().toString()));

        if (DataLogic.isWorking()) {
            showProgressInfo("Call Button Clicked to Stop Call...");
            onStopCall();
        } else {
            showProgressInfo("Call Button Clicked to Start Call...");
            onStartCall();
        }
    }

    private boolean checkStart() {
        if (TextUtils.isEmpty(DataLogic.getDeviceId())) {
            Toast.makeText(this, "无设备编号, 请设置", Toast.LENGTH_LONG).show();
            return false;
        }
        if (TextUtils.isEmpty(DataLogic.getServerAddress())) {
            Toast.makeText(this, "无服务器地址, 请设置", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void onStartCall() {
        DataLogic.startWorking(MainActivity.this);
        FloatingButtonService.showFloatingButton(true, DataLogic.getFloatingButtonText());
    }

    private void onStopCall() {
        DataLogic.stopWorking();
        FloatingButtonService.showFloatingButton(false, DataLogic.getFloatingButtonText());
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
        MainActivity.this.setTitle(ComDef.APP_NAME + " - " + NetUtils.getLocalIpAddress(this));

        mTextDeviceId.setText(DataLogic.getDeviceId());

        mTextTelListFile.setText(DataLogic.getTelListFileInfo());
        mTextTelListNum.setText(DataLogic.getTelListNumInfo());
        mTextCalledNum.setText(DataLogic.getCalledNumInfo());

        if (DataLogic.isWorking()) {
            mBtnCall.setText("停止拨号");
            mBtnCall.setBackgroundColor(Color.RED);
        } else {
            mBtnCall.setText("开始拨号");
            mBtnCall.setBackgroundColor(Color.GREEN);
        }

        FloatingButtonService.updateButtonInfo(
                DataLogic.getFloatingButtonText(),
                DataLogic.getFloatingButtonColor()
        );

        // show and log status on debug version
        if (ComDef.DEBUG) {
            mTextStaticInfo.setText(DataLogic.getClientStatus());
            LogUtils.d(DataLogic.getClientStatus());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.d("MainActivity: onActivityResult: requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (requestCode == REQUEST_CODE_FLOATING_BUTTON) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "浮窗权限授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "浮窗权限授权成功", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (requestCode == REQUEST_CODE_DEVICE_CONFIG) {
            if (resultCode == DeviceConfigActivity.RESULT_CODE_UPDATE) {
                updateUI();
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

    private void showProgressInfo(final String msg) {
        LogUtils.i(msg);
        if (ComDef.DEBUG) {
            runOnUiThread(new Runnable() {
                public void run() {
                    String logMsg = TimeUtils.getLogTime() + " - " + msg;
                    mProgressBuffer.append(logMsg);
                    String progressInfo = mProgressBuffer.getBuffer() + "\n";
                    mTextProgressInfo.setText(progressInfo);
                    mScrollProgressInfo.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollProgressInfo.smoothScrollTo(0, mTextProgressInfo.getBottom());
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d("MainActivity: onDestroy");
        FloatingButtonService.stop(this);
        stopUITimer();
    }
}
