package com.liz.whatsai.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageButton;
import android.widget.TextView;

import com.liz.whatsai.R;
import com.liz.whatsai.logic.WSRecorder;

public class AudioConfigActivity extends Activity {

    private ImageButton mBtnSwitchRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_config);

        ((TextView)findViewById(R.id.titlebar_name)).setText("Audio Recorder Config");
        ((TextView)findViewById(R.id.text_audio_config_info)).setText(Html.fromHtml(WSRecorder.inst().getAudioConfigInfoFull()));
    }

    @Override
    public void onBackPressed() {
        //###@:
        super.onBackPressed();
    }
}
