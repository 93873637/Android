package com.liz.whatsai.ui;

import android.app.Activity;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.DataLogic;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.logic.WhatsaiAudio;

import java.text.SimpleDateFormat;

public class AudioActivity extends Activity implements View.OnClickListener {

    private ImageButton mBtnSwitchAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        findViewById(R.id.titlebar_menu).setOnClickListener(this);
        findViewById(R.id.titlebar_close).setOnClickListener(this);

        mBtnSwitchAudio = findViewById(R.id.btn_switch_audio);
        mBtnSwitchAudio.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.titlebar_menu:
            case R.id.titlebar_close:
                onBackPressed();
                break;
            case R.id.btn_switch_audio:
                onSwitchAudio();
                break;
            default:
                break;
        }
    }

    protected void onSwitchAudio() {
        WhatsaiAudio.switchAudio();
        mBtnSwitchAudio.setBackgroundResource(WhatsaiAudio.isOpen() ? R.drawable.bg_circle_green : R.drawable.bg_circle_red);
    }
}
