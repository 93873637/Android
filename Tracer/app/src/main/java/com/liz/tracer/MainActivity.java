package com.liz.tracer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.liz.androidutils.LogEx;
import com.liz.androidutils.TimeUtils;
import com.liz.androidutils.ui.AppCompatActivityEx;

public class MainActivity extends AppCompatActivityEx {

    private TextView tvTimeCurrent;
    private TextView tvTimeStart;
    private TextView tvTimeElapsed;

    private ImageView ivOrientation;
    private TextView tvStatisInfo;

    private TextView tvCurrentSpeed;
    private TextView tvCurrentSpeedInfo;
    private TextView tvAverageSpeed;
    private TextView tvAverageSpeedInfo;

    private Button btnSwitch;

    private ScrollView scrollInfo;
    private TextView tvLogInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        LogEx.setTag("Tracer");
        LogEx.setLogDir("/sdcard/0.log");
        LogEx.setSaveToFile(true);
        LogEx.setMaxLogFileSize(20 * 1024 * 1024);

        tvTimeCurrent = findViewById(R.id.text_current_time);
        tvTimeStart = findViewById(R.id.text_start_time);
        tvTimeElapsed = findViewById(R.id.text_time_elapsed);
        tvStatisInfo = findViewById(R.id.text_statis_info);
        tvCurrentSpeed = findViewById(R.id.text_current_speed);
        tvCurrentSpeedInfo = findViewById(R.id.text_current_speed_info);
        tvAverageSpeed = findViewById(R.id.text_average_speed);
        tvAverageSpeedInfo = findViewById(R.id.text_average_speed_info);

        ivOrientation = findViewById(R.id.iv_bearing_orientation);
        ivOrientation.setRotation(0);

        btnSwitch = findViewById(R.id.btn_switch_tracing);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationService.inst().switchTracing();
                updateUI();
            }
        });

        findViewById(R.id.btn_reset_running).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationService.inst().onReset();
            }
        });

        findViewById(R.id.btn_exit_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog
                        .Builder(MainActivity.this)
                        .setTitle("Confirm Exit?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int pid = android.os.Process.myPid();
                                LogEx.i("exitApp, pid = " + pid);
                                android.os.Process.killProcess(pid);
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        });

        scrollInfo = findViewById(R.id.scroll_info);
        tvLogInfo = findViewById(R.id.text_log_info);
        tvLogInfo.setMovementMethod(ScrollingMovementMethod.getInstance());

        setUITimer(100, 1000);

        requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new AppCompatActivityEx.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        LogEx.trace();
                        LocationService.inst().init(MainActivity.this, new LocationService.LocationCallback() {
                            @Override
                            public void onLocationUpdate() {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        updateLogInfo();
                                    }
                                });
                            }
                        });
                    }
                    @Override
                    public void onPermissionDenied() {
                        LogEx.trace();
                    }
                }
        );
    }

    private int getSpeedWidth(double ratio) {
        int totalWidth = tvCurrentSpeedInfo.getWidth();
        int speedWidth = (int)(totalWidth * ratio);
        if (speedWidth < comdef.SPEED_WIDTH_BASE) {
            speedWidth = comdef.SPEED_WIDTH_BASE;
        }
        return speedWidth;
    }

    @Override
    protected void updateUI() {
        tvTimeCurrent.setText(TimeUtils.currentTime());
        tvTimeStart.setText(LocationService.inst().getStartTimeText());
        tvTimeElapsed.setText(LocationService.inst().getDurationText());
        tvCurrentSpeedInfo.setText(LocationService.inst().getCurrentSpeedText());
        tvAverageSpeedInfo.setText(LocationService.inst().getAverageSpeedText());

        ivOrientation.setRotation(LocationService.inst().getBearing());
        tvStatisInfo.setText(LocationService.inst().getStatisInfo());

        ///*
        ////////////////////////////////////////////////////////////////////////////////////////
        tvCurrentSpeed.setWidth(1);  // must call this first to make width take effect?
        tvCurrentSpeed.getLayoutParams().width = getSpeedWidth(LocationService.inst().getCurrentSpeedRatio());
        tvAverageSpeed.setWidth(1);
        tvAverageSpeed.getLayoutParams().width = getSpeedWidth(LocationService.inst().getAverageSpeedRatio());
        ////////////////////////////////////////////////////////////////////////////////////////
        //*/

        if (LocationService.inst().isRunning()) {
            btnSwitch.setText("STOP");
            btnSwitch.setBackgroundColor(Color.RED);
        }
        else {
            btnSwitch.setText("START");
            btnSwitch.setBackgroundColor(Color.GREEN);
        }
    }

    private void updateLogInfo() {
        tvLogInfo.append(TimeUtils.getLogTime() + " - " + LocationService.inst().getLastLocationInfo() + "\n");
        scrollInfo.post(new Runnable() {
            @Override
            public void run() {
                scrollInfo.smoothScrollTo(0, tvLogInfo.getBottom());
            }
        });
    }
}
