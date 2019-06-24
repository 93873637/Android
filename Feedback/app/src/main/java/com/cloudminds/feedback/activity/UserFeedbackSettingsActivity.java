package com.cloudminds.feedback.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.logic.LogConfig;
import com.cloudminds.feedback.logic.Settings;
/**
 * add by jordan.jing
 */
public class UserFeedbackSettingsActivity extends Activity
        implements View.OnClickListener {
    private static final String TAG = "UserFeedbackSettingsActivity";
    private static final String ACTION_SETUP_WIZARD = "com.android.setupwizard.USER_FEEDBACK";
    private Toolbar mToolbarSet;
    private ImageButton mImgBtnReturn;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init(){
        mToolbarSet = findViewById(R.id.toolbar);
//        mImgBtnReturn = mToolbarSet.findViewById(R.id.left_img_btn);
//        mImgBtnReturn.setBackgroundResource(R.drawable.ic_return);
//        mImgBtnReturn.setOnClickListener(this);
        TextView tvTitle = mToolbarSet.findViewById(R.id.tool_bar_title);
        tvTitle.setText(R.string.user_experience_text);

        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_ok).setOnClickListener(this);

    }

    @Override
    protected void onCreate(Bundle savedStates) {
        super.onCreate(savedStates);
        setContentView(R.layout.user_feedback_main);
        String action = getIntent().getAction();
        TextView tv = (TextView) findViewById(R.id.switch_text_user_experience);
        if (tv != null) {
            tv.setOnClickListener(this);
        }
        createUserSwitch();
        init();
    }


    @Override
    public void onClick(View v) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        switch (v.getId()) {
            case R.id.switch_text_user_experience:
                startUserFeedbackDetailActivity();
                break;
            case R.id.left_img_btn:
                startSettingsActivity();
                break;
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_ok:
                Settings.saveUserSwitchOpen(this, true);
                LogConfig.enableDefaultLog();
                startMainActivity();
                break;
            default:
                break;
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(getIntent().getAction());
        startActivity(intent);
        finish();
    }

    private void startUserFeedbackDetailActivity() {
        Intent intent = new Intent(this, UserFeedbackDetailActivity.class);
        intent.setAction(getIntent().getAction());
        startActivity(intent);
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        intent.setAction(getIntent().getAction());
        startActivity(intent);
        finish();
    }

    private void createUserSwitch() {
        Switch sw = (Switch) findViewById(R.id.user_switch);
        sw.setChecked(Settings.readUserSwitch(this));
        sw.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.saveUserSwitchOpen(UserFeedbackSettingsActivity.this, isChecked);
            }
        });
    }

}


