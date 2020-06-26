package com.liz.tracer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.liz.androidutils.LogEx;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.TimeUtils;
import com.liz.androidutils.ui.AppCompatActivityEx;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivityEx {

    private static final int UI_TIMER_DELAY = 200;
    private static final int UI_TIMER_PERIOD = 1000;

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

        setUITimer(UI_TIMER_DELAY, UI_TIMER_PERIOD);

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

        mCurrentBearing = LocationService.inst().getBearing();
        ivOrientation.setRotation(mCurrentBearing);

        //##@:
        //mCurrentBearing = LocationService.inst().getBearing();
        //ivOrientation.setRotation(mCurrentBearing);
        //setBearingAnimation(170);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Bearing Animation

    private static final int BEARING_MIN = 0;
    private static final int BEARING_MAX = 360;
    private static final int BEARING_ROTATION_MAX = BEARING_MAX / 2;
    private static final int BEARING_ANIMATION_TIMER_DELAY = 0;    // unit by milli-seconds
    private static final int BEARING_ANIMATION_TIMER_PERIOD = 10;  // unit by milli-seconds
    private static final int BEARING_ANIMATION_NUM = 40;
    private static final float BEARING_INC_MIN = 0.5f;

    private float mCurrentBearing = 0;
    private Timer mBearingTimer = null;

    private void stopBearingTimer() {
        if (mBearingTimer != null) {
            mBearingTimer.cancel();
            mBearingTimer = null;
        }
    }

    /**
     * setBearingAnimation: moving to target bearing with animation effect
     * NOTE: all bearing should be 0~359
     * @param targetBearing: number of 0~359, clockwise
     */
    private void setBearingAnimation(final float targetBearing) {
        LogUtils.td("current=" + mCurrentBearing + ", target=" + targetBearing);
        // calc bearing diff, negative means anti-clockwise
        float bearingDiff = targetBearing - mCurrentBearing;
        if (Math.abs(bearingDiff) < BEARING_INC_MIN) {
            LogUtils.td("diff too small, no animation, set directly");
            ivOrientation.setRotation(targetBearing);
            return;
        }

        if (bearingDiff > BEARING_ROTATION_MAX) {
            // anti-clockwise rotation
            bearingDiff = bearingDiff - BEARING_MAX;
        }
        if (bearingDiff < 0) {
            if (bearingDiff <= -BEARING_ROTATION_MAX) {
                bearingDiff += BEARING_MAX;
            }
        }

        float bearingInc = 1.0f * bearingDiff / BEARING_ANIMATION_NUM;
        if (bearingInc > 0 && bearingInc < BEARING_INC_MIN) {
            bearingInc = BEARING_INC_MIN;
        }
        if (bearingInc < 0 && bearingInc > -BEARING_INC_MIN) {
            bearingInc = -BEARING_INC_MIN;
        }
        final float timerInc = bearingInc;
        LogUtils.td("diff=" + bearingDiff + ", inc=" + bearingInc + ", timerInc=" + timerInc);

        stopBearingTimer();

        mBearingTimer = new Timer();
        mBearingTimer.schedule(new TimerTask() {
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mCurrentBearing += timerInc;
                        if (mCurrentBearing < BEARING_MIN) {
                            mCurrentBearing += BEARING_MAX;
                        }
                        if (mCurrentBearing >= BEARING_MAX) {
                            mCurrentBearing -= BEARING_MAX;
                        }
                        float diff = mCurrentBearing - targetBearing;
                        LogUtils.td("target=" + targetBearing + ", current=" + mCurrentBearing + ", inc=" + timerInc + ", diff=" + diff);
                        if (Math.abs(diff) < Math.abs(timerInc)) {
                            stopBearingTimer();
                            ivOrientation.setRotation(targetBearing);
                            LogUtils.td("rotation in place!");
                        }
                        else {
                            ivOrientation.setRotation(mCurrentBearing);
                        }
                    }
                });
            }
        }, BEARING_ANIMATION_TIMER_DELAY, BEARING_ANIMATION_TIMER_PERIOD);
    }

    // Bearing Animation
    //////////////////////////////////////////////////////////////////////////////////////////////

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
        tvStatisInfo.setText(Html.fromHtml(LocationService.inst().getStatisInfo()));

        if (LocationService.inst().isRunning() && LocationService.inst().hasSpeed()){
            ivOrientation.setBackgroundResource(R.drawable.orientation);
            if (comdef.BEARING_ANIMATION) {
                setBearingAnimation(LocationService.inst().getBearing());
            } else {
                ivOrientation.setRotation(LocationService.inst().getBearing());
            }
        }
        else {
            ivOrientation.setRotation(0);
            ivOrientation.setBackgroundResource(R.drawable.compass);
        }

        // set speed bar showing
        tvCurrentSpeed.setWidth(1);  // must call this first to make width take effect?
        tvCurrentSpeed.getLayoutParams().width = getSpeedWidth(LocationService.inst().getCurrentSpeedRatio());
        tvAverageSpeed.setWidth(1);
        tvAverageSpeed.getLayoutParams().width = getSpeedWidth(LocationService.inst().getAverageSpeedRatio());

        if (LocationService.inst().isRunning()) {
            btnSwitch.setText("STOP");
            btnSwitch.setBackgroundColor(Color.RED);
            btnSwitch.setTextColor(Color.rgb(0x00, 0xff, 0xff));
        }
        else {
            btnSwitch.setText("START");
            btnSwitch.setBackgroundColor(Color.GREEN);
            btnSwitch.setTextColor(Color.rgb(0xff, 0x00, 0xff));
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
