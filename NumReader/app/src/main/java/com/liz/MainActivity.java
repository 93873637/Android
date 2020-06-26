package com.liz;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.liz.androidutils.LogUtils;

public class MainActivity extends Activity {

    private Button mBtnSwitch;
    private Button mBtnReplay;
    private Button mBtnReset;
    private TextView mTextTimeCount = null;
//    private View mLayoutSettings = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnSwitch = findViewById(R.id.btn_switch);
        mBtnSwitch.setOnTouchListener(mOnSwitchTouchListener);

        mBtnReplay = findViewById(R.id.btn_replay);
        mBtnReplay.setOnTouchListener(mOnReplayTouchListener);

        mBtnReset = findViewById(R.id.btn_reset);
        mBtnReset.setOnTouchListener(mOnResetTouchListener);

        mTextTimeCount = findViewById(R.id.textTimeCount);

        NumReader.init(this);
        NumReader.setUIHandler(mUIHandler);
    }

    private void setBtnSwitchColor(boolean actionDown) {
        int resIdPlay = R.drawable.bg_circle_red;
        int resIdStop = R.drawable.bg_circle_green;
        if (actionDown) {
            resIdPlay = R.drawable.bg_circle_red_pressed;
            resIdStop = R.drawable.bg_circle_green_pressed;
        }
        mBtnSwitch.setBackgroundResource(NumReader.isPlaying() ?  resIdPlay: resIdStop);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private View.OnTouchListener mOnSwitchTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN://收缩到0.8(正常值是1)，速度500
                    //textView.animate().scaleX(0.8f).scaleY(0.8f).setDuration(500).start();
                    LogUtils.td("ACTION_DOWN");
                    setBtnSwitchColor(true);
                    break;
                case MotionEvent.ACTION_UP:
                    //textView.animate().scaleX(1).scaleY(1).setDuration(500).start();
                    LogUtils.td("ACTION_UP");
                    NumReader.switchPlayPause();
                    setBtnSwitchColor(false);
                    break;
            }
            return mBtnSwitch.performClick();
        }
    };

    private View.OnTouchListener mOnReplayTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //textView.animate().scaleX(0.8f).scaleY(0.8f).setDuration(500).start();
                    LogUtils.td("ACTION_DOWN");
                    mBtnReplay.setBackgroundResource(R.drawable.bg_circle_yellow_pressed);
                    break;
                case MotionEvent.ACTION_UP:
                    //textView.animate().scaleX(1).scaleY(1).setDuration(500).start();
                    LogUtils.td("ACTION_UP");
                    mBtnReplay.setBackgroundResource(R.drawable.bg_circle_yellow);
                    NumReader.replay();
                    updateUI();
                    break;
            }
            return mBtnReplay.performClick();
        }
    };

    private View.OnTouchListener mOnResetTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //textView.animate().scaleX(0.8f).scaleY(0.8f).setDuration(500).start();
                    LogUtils.td("ACTION_DOWN");
                    mBtnReset.setBackgroundResource(R.drawable.bg_circle_blue_pressed);
                    break;
                case MotionEvent.ACTION_UP:
                    //textView.animate().scaleX(1).scaleY(1).setDuration(500).start();
                    LogUtils.td("ACTION_UP");
                    mBtnReset.setBackgroundResource(R.drawable.bg_circle_blue);
                    NumReader.reset();
                    updateUI();
                    break;
            }
            return mBtnReset.performClick();
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    //To in case:
    //This Handler class should be static or leaks might occur

    /**
     * Instances of static inner classes do not hold an implicit reference to
     * their outer class.
     */
    private static class UIHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        UIHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            activity.handleMessage(msg);
        }
    }

    private final UIHandler mUIHandler = new UIHandler(this);
    ///////////////////////////////////////////////////////////////////////////

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case NumReader.MSG_NUMBER_UPDATED:
                updateUI();
                break;
            default:
                break;
        }
    }

    public void updateUI() {
        mTextTimeCount.setText(NumReader.getFormatTimeStr());
        mBtnSwitch.setText(NumReader.getNumberString());
        setBtnSwitchColor(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            onMenuSettings();
            return true;
        }
        if (id == R.id.action_test) {
            onMenuTest();
            return true;
        } else if (id == R.id.action_exit) {
            onMenuExit();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuSettings() {
        new SettingsDlg(this, new SettingsDlg.SettingsDlgCallback() {
            @Override
            public void onSettingsUpdated() {
                MainActivity.this.updateUI();
            }
        }).show(this);
    }

    private void onMenuTest() {
        LayoutInflater inflater = getLayoutInflater();
        View layoutTest = inflater.inflate(R.layout.layout_test,
                (ViewGroup) findViewById(R.id.settings_dialog));
        final EditText editNum = layoutTest.findViewById(R.id.edit_test_num);
        layoutTest.findViewById(R.id.btn_play_num).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundPoolPlayer.playNumberString(editNum.getText().toString());
            }
        });
        new AlertDialog.Builder(this)
                .setTitle("Test")
                .setView(layoutTest)
                .setPositiveButton("OK", null)
                .show();
    }

    private void onMenuExit() {
        NumReader.release();
        this.finish();
        System.exit(0);
    }
}
