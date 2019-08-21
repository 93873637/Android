package com.liz.whatsai.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.utils.LogUtils;
import com.liz.whatsai.utils.SysUtils;

public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        String alarmTime = "UNKNOWN TIME";
        String alarmInfo = "UNKNOWN ALARM";

        String tag = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            tag = bundle.getString(ComDef.ALARM_TAG);
        }
        if (TextUtils.isEmpty(tag)) {
            LogUtils.e("empty tag");
        }
        else {
            Node node = DataLogic.getNodeByAlarmTime(tag);
            if (node == null) {
                alarmInfo = "UNKNOWN NODE";
            }
            else {
                alarmTime = node.getRemindString();
                alarmInfo =  node.getName();
            }
        }

        TextView tvTime = findViewById(R.id.alarm_time);
        tvTime.setText(alarmTime);

        TextView tv = findViewById(R.id.alarm_tag);
        tv.setText(alarmInfo);
        //tv.setOnClickListener(this);

        findViewById(R.id.alarm_layout).setOnClickListener(this);

        //Button btn = findViewById(R.id.btnOK);
        //btn.setOnClickListener(this);
        SysUtils.playRingTone(this);
    }

    @Override
    public void onClick(View v) {
        stopAlarm();
    }

    @Override
    public void onBackPressed() {
        stopAlarm();
    }

    public void stopAlarm() {
        SysUtils.stopRingTone();
        this.finish();
    }
}
