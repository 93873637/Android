package com.cloudminds.feedback.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.logic.Settings;

/**
 * Created by cloud on 18-4-11.
 */

public class SettingActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private Toolbar mToolbarSet;
    private ImageButton mImgBtnReturn;
    private Switch mOnWifiSwitch,mUserExperienceSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);

        mToolbarSet = findViewById(R.id.toolbar);
        mImgBtnReturn = mToolbarSet.findViewById(R.id.left_img_btn);
        mImgBtnReturn.setBackgroundResource(R.drawable.ic_return);
        mImgBtnReturn.setOnClickListener(this);
        TextView tvTitle = mToolbarSet.findViewById(R.id.tool_bar_title);
        tvTitle.setText(R.string.action_setting);
        mOnWifiSwitch=findViewById(R.id.setting_onwifi_swi);
        mOnWifiSwitch.setOnCheckedChangeListener(this);
        mUserExperienceSwitch = findViewById(R.id.user_switch);
        mUserExperienceSwitch.setOnCheckedChangeListener(this);
        mUserExperienceSwitch.setChecked(Settings.readUserSwitch(this));
        TextView tv = findViewById(R.id.switch_text_user_experience_title);
        tv.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOnWifiSwitch.setChecked(Settings.getFeedbackOnWifi(this));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();

        switch (id) {
            case R.id.setting_onwifi_swi:
                Settings.setFeedbackOnWifi(this,isChecked);
                break;
            case R.id.user_switch:
                Settings.saveUserSwitchOpen(this, isChecked);
                break;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.left_img_btn:
                finish();
                break;
            case R.id.switch_text_user_experience_title:
                startUserFeedbackActivity();
                break;
        }
    }

    private void startUserFeedbackActivity() {
        Intent intent = new Intent(this, UserFeedbackSettingsActivity.class);
        intent.setAction(getIntent().getAction());
        startActivity(intent);
        finish();
    }
}
