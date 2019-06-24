package com.cloudminds.feedback.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.logic.LogConfig;


public class LogConfigActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static final int RESULT_CODE_LOG_BUTTON_CLICK = 2;

    private Switch mAppLog;
    private Switch mModemLog;
    private Switch mNetLog;
    private Switch mKernelLog;
    private Switch mGpsLog;
    private Switch mWlanLog;
    private Switch mSysprofLog;
    private Switch mChargeLog;
    private Switch mSensorLog;

    private Button mBtnOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_config_activity);

        Toolbar toolbarSet = findViewById(R.id.toolbar);

        ImageButton imgBtnReturn = toolbarSet.findViewById(R.id.left_img_btn);
        imgBtnReturn.setBackgroundResource(R.drawable.ic_return);
        imgBtnReturn.setOnClickListener(this);

        TextView tvTitle = toolbarSet.findViewById(R.id.tool_bar_title);
        tvTitle.setText(R.string.action_logconfig);

        mAppLog = findViewById(R.id.app_log_swi);
        mAppLog.setOnCheckedChangeListener(this);

        mModemLog = findViewById(R.id.modem_log_swi);
        mModemLog.setOnCheckedChangeListener(this);

        mNetLog = findViewById(R.id.net_log_swi);
        mNetLog.setOnCheckedChangeListener(this);

        mKernelLog = findViewById(R.id.kernel_log_swi);
        mKernelLog.setOnCheckedChangeListener(this);

        mGpsLog = findViewById(R.id.gps_log_swi);
        mGpsLog.setOnCheckedChangeListener(this);

        mWlanLog = findViewById(R.id.wlan_log_swi);
        mWlanLog.setOnCheckedChangeListener(this);

        mSysprofLog = findViewById(R.id.sysprof_log_swi);
        mSysprofLog.setOnCheckedChangeListener(this);

        mChargeLog = findViewById(R.id.charge_log_swi);
        mChargeLog.setOnCheckedChangeListener(this);

        mSensorLog = findViewById(R.id.sensor_log_swi);
        mSensorLog.setOnCheckedChangeListener(this);

        mBtnOK = findViewById(R.id.btn_ok);
        mBtnOK.setOnClickListener(this);

        initConfig();
        initLogButton();
    }

    public void initConfig() {
        mAppLog.setChecked(LogConfig.app_flag);
        mModemLog.setChecked(LogConfig.modem_flag);
        mNetLog.setChecked(LogConfig.net_flag);
        mKernelLog.setChecked(LogConfig.kernel_flag);
        mGpsLog.setChecked(LogConfig.gps_flag);
        mWlanLog.setChecked(LogConfig.wlan_flag);
        mSysprofLog.setChecked(LogConfig.sysprof_flag);
        mChargeLog.setChecked(LogConfig.charge_flag);
        mSensorLog.setChecked(LogConfig.sensor_flag);
    }

    @Override
    public void onCheckedChanged(CompoundButton btn, boolean checked) {
        switch (btn.getId()) {
            case R.id.app_log_swi:
                //force set app flag to true
                LogConfig.app_flag = true;
                mAppLog.setChecked(true);
                break;
            case R.id.modem_log_swi:
                LogConfig.modem_flag = checked;
                break;
            case R.id.net_log_swi:
                LogConfig.net_flag = checked;
                break;
            case R.id.kernel_log_swi:
                //force set kernel flag to true
                LogConfig.kernel_flag = true;
                mKernelLog.setChecked(true);
                break;
            case R.id.gps_log_swi:
                LogConfig.gps_flag = checked;
                break;
            case R.id.wlan_log_swi:
                LogConfig.wlan_flag = checked;
                break;
            case R.id.sysprof_log_swi:
                LogConfig.sysprof_flag = checked;
                break;
            case R.id.charge_log_swi:
                LogConfig.charge_flag = checked;
                break;
            case R.id.sensor_log_swi:
                LogConfig.sensor_flag = checked;
                break;
        }
    }

    public void initLogButton() {
        if (LogConfig.isLogdRunning()) {
            mBtnOK.setBackgroundColor(Color.GREEN);
            mBtnOK.setText(R.string.log_running);
        }
        else {
            mBtnOK.setBackgroundColor(Color.RED);
            mBtnOK.setText(R.string.log_stopped);
        }
    }

    public void onClickLogButton() {
        //switch log state on/off
        if (LogConfig.isLogdRunning()) {
            LogConfig.stopLogd();
            //Toast.makeText(this, R.string.log_stopped_tip, Toast.LENGTH_LONG).show();
            mBtnOK.setBackgroundColor(Color.RED);
        }
        else {
            //log stopped, run it
            LogConfig.startLogd();
            //Toast.makeText(this, R.string.log_running_tip, Toast.LENGTH_LONG).show();
            mBtnOK.setBackgroundColor(Color.GREEN);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                onClickLogButton();
                setResult(RESULT_CODE_LOG_BUTTON_CLICK, new Intent());
                finish();
                break;
            case R.id.left_img_btn:
                finish();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //let config take effect when activity exit
        LogConfig.updateConfig();
    }
}
