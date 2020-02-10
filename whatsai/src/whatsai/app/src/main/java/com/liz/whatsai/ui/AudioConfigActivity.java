package com.liz.whatsai.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;

import com.liz.whatsai.R;

public class AudioConfigActivity extends Activity {

    private ImageButton mBtnSwitchRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_config);
    }

    @Override
    public void onBackPressed() {
        //###@:
        super.onBackPressed();
    }
}
