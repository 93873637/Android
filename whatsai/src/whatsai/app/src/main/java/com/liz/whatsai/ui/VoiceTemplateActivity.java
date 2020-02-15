package com.liz.whatsai.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.app.AudioListAdapter;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.WhatsaiAudio;
import com.liz.whatsai.logic.WhatsaiListener;

import java.util.Timer;
import java.util.TimerTask;

public class VoiceTemplateActivity extends Activity implements View.OnClickListener {

    WaveSurfaceView mWaveSurfaceView;
    TextView mTextProgressInfo;
    Button mBtnSwitchListening;
    WhatsaiListener mListener;
    AudioListAdapter mAudioListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_template);
        LogUtils.d("VoiceTemplateActivity:onCreate");

        mListener = new WhatsaiListener();
        mListener.setWaveSamplingRate(32);

        mBtnSwitchListening = findViewById(R.id.btn_switch_listening);
        mBtnSwitchListening.setOnClickListener(this);
        mBtnSwitchListening.setText(mListener.isListening()?"STOP":"START");
        mListener.setCallback(mListenerCallback);

        findViewById(R.id.btn_switch_listening).setOnClickListener(this);
        findViewById(R.id.btn_play_audio).setOnClickListener(this);
        findViewById(R.id.btn_save_audio).setOnClickListener(this);

        mTextProgressInfo = findViewById(R.id.text_progress_info);
        mWaveSurfaceView = findViewById(R.id.wave_surface_view);

        mWaveSurfaceView.setMaxValue(mListener.getMaxPower());
        mWaveSurfaceView.setWaveItemWidth(1);
        mWaveSurfaceView.setWaveItemSpace(0);

        startUITimer();

        mAudioListAdapter = new AudioListAdapter(ComDef.WHATSAI_AUDIO_TEMPLATE_DIR);

        ListView listView = findViewById(R.id.lv_audio_files);
        listView.addFooterView(new ViewStub(this));
        listView.setAdapter(mAudioListAdapter);
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                for (ComDef.AudioListMenu c : ComDef.AudioListMenu.values()) {
                    menu.add(0, c.id, 0, c.name);
                }
            }
        });
    }

    private WhatsaiListener.ListenerCallback mListenerCallback = new WhatsaiListener.ListenerCallback() {
        @Override
        public void onPowerUpdated() {
            VoiceTemplateActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mWaveSurfaceView.onUpdateSurfaceData(mListener.getPowerList(), mListener.getMaxPower());
                }
            });
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int itemId = item.getItemId();
        if (itemId == ComDef.AudioListMenu.PLAY.id) {
            mListener.playAudio(mAudioListAdapter.getAudioFilePath((int)info.id));
            return true;
        }
        else if (itemId == ComDef.AudioListMenu.STOP.id) {
            WhatsaiAudio.stopPlay((int)info.id);
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI Timer
    private static final long UI_TIMER_DELAY = 0L;
    private static final long UI_TIMER_PERIOD = 1000L;
    private Timer mUITimer;
    private void startUITimer() {
        mUITimer = new Timer();
        mUITimer.schedule(new TimerTask() {
            public void run () {
                VoiceTemplateActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, UI_TIMER_DELAY, UI_TIMER_PERIOD);
    }
    private void stopUITimer() {
        if (mUITimer != null) {
            mUITimer.cancel();
            mUITimer = null;
        }
    }
    // UI Timer
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String getProgressInfo() {
        return mListener.getProgressInfo() +
                "<br>" + mWaveSurfaceView.getSurfaceInfo() + " || " + mListener.getAudioConfigInfo();
    }

    private void updateUI() {
        mTextProgressInfo.setText(Html.fromHtml(this.getProgressInfo()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_listening:
                onSwitchListening();
                break;
            case R.id.btn_play_audio:
                onPlayAudio();
                break;
            case R.id.btn_save_audio:
                onSaveAudio();
                break;
            default:
                break;
        }
    }

    private void onSwitchListening() {
        mListener.switchListening();
        mBtnSwitchListening.setText(mListener.isListening()?"STOP":"START");
    }

    private void onPlayAudio() {
        if (mListener.isListening()) {
            Toast.makeText(this, "Can't play when listening, stop first", Toast.LENGTH_SHORT).show();
        }
        else {
            mListener.playAudio();
        }
    }

    private void onSaveAudio() {
        final EditText et = new EditText(this);
        et.setText(mListener.getPCMFileName());
        new AlertDialog
                .Builder(this)
                .setTitle("Save Voice Template File As: ")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (TextUtils.isEmpty(et.getText().toString())) {
                            Toast.makeText(VoiceTemplateActivity.this, "Please input template name", Toast.LENGTH_LONG).show();
                        }
                        else {
                            String srcFilePath = mListener.getPCMFileAbsolute();
                            String tarPilePath = ComDef.WHATSAI_AUDIO_TEMPLATE_DIR + "/" + et.getText().toString();
                            FileUtils.mv(srcFilePath, tarPilePath);
                            mAudioListAdapter.onUpdateList();
                        }
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    @Override
    public void onBackPressed() {
        LogUtils.d("VoiceTemplateActivity:onBackPressed");
        stopUITimer();
        super.onBackPressed();
    }
}
