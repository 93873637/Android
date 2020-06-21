package com.liz;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;

public class MainActivity extends Activity {

    private Button mBtnSwitch;
    private Button mBtnReplay;
    private Button mBtnReset;
    //private SwitchButton mBtnSwitch;
    private TextView mTextTimeCount = null;
    private View mLayoutSettings = null;

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

//		findViewById(R.id.ib_replay).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				NumReader.replay();
//				updateUI();
//			}
//		});
//
//		findViewById(R.id.btn_reset).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				NumReader.reset();
//				updateUI();
//			}
//		});
//
//        final EditText editNum = findViewById(R.id.edit_test_num);
//        findViewById(R.id.btn_play_num).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SoundPoolPlayer.playNumberString(editNum.getText().toString());
//            }
//        });

        NumReader.init(this);
        NumReader.setUIHandler(mUIHandler);
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
                    if (NumReader.isPlaying()) {
                        mBtnSwitch.setBackgroundResource(R.drawable.bg_circle_green_pressed);
                    } else {
                        mBtnSwitch.setBackgroundResource(R.drawable.bg_circle_red_pressed);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //textView.animate().scaleX(1).scaleY(1).setDuration(500).start();
                    LogUtils.td("ACTION_UP");
                    NumReader.switchPlayPause();
                    mBtnSwitch.setBackgroundResource(NumReader.isPlaying() ? R.drawable.bg_circle_green : R.drawable.bg_circle_red);
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
        mBtnSwitch.setBackgroundResource(NumReader.isPlaying() ? R.drawable.bg_circle_green : R.drawable.bg_circle_red);
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
        prepareSettings();
        new AlertDialog.Builder(this)
                .setTitle("Settings")
                .setView(mLayoutSettings)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onSettingsOK();
                            }
                        }).show();
    }

    private void onMenuTest() {
        LayoutInflater inflater = getLayoutInflater();
        View layoutTest = inflater.inflate(R.layout.layout_test,
                (ViewGroup) findViewById(R.id.dialog));
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

    public void prepareSettings() {
        LayoutInflater inflater = getLayoutInflater();
        mLayoutSettings = inflater.inflate(R.layout.layout_settings,
                (ViewGroup) findViewById(R.id.dialog));

        EditText etCountTimeSpan = mLayoutSettings.findViewById(R.id.etCountTimeSpan);
        EditText etCountReadSpan = mLayoutSettings.findViewById(R.id.etCountReadSpan);
        EditText editDigitSpan = mLayoutSettings.findViewById(R.id.edit_digits_span);
        EditText editPlayRate = mLayoutSettings.findViewById(R.id.edit_play_rate);
        EditText editCountStart = mLayoutSettings.findViewById(R.id.edit_count_start);
        EditText editCountMax = mLayoutSettings.findViewById(R.id.edit_count_max);
        RadioButton rbOnMaxLoop = mLayoutSettings.findViewById(R.id.rb_on_max_loop);
        RadioButton rbOnMaxStop = mLayoutSettings.findViewById(R.id.rb_on_max_stop);

        etCountTimeSpan.setText(NumReader.getTimeSpanString());
        etCountReadSpan.setText(NumReader.getReadSpanString());
        editDigitSpan.setText(NumReader.getDigitSpanString());
        editPlayRate.setText(NumReader.getPlayRateString());
        editCountStart.setText(NumReader.getCountStartString());
        editCountMax.setText(NumReader.getCountMaxString());
        if (NumReader.isOnMaxLoop()) {
            rbOnMaxLoop.setChecked(true);
        }
        else {
            rbOnMaxStop.setChecked(true);
        }
    }

    public void onSettingsOK() {
        EditText etCountTimeSpan = mLayoutSettings.findViewById(R.id.etCountTimeSpan);
        EditText etCountReadSpan = mLayoutSettings.findViewById(R.id.etCountReadSpan);
        EditText editDigitSpan = mLayoutSettings.findViewById(R.id.edit_digits_span);
        EditText editPlayRate = mLayoutSettings.findViewById(R.id.edit_play_rate);
        EditText editCountStart = mLayoutSettings.findViewById(R.id.edit_count_start);
        EditText editCountMax = mLayoutSettings.findViewById(R.id.edit_count_max);
        RadioButton rbOnMaxLoop = mLayoutSettings.findViewById(R.id.rb_on_max_loop);

        //set origin value
        int newTimeSpan;
        int newReadSpan;
        int newDigitSpan;
        float newPlayRate;
        int newCountStart;
        int newCountMax;
        int newCountOnMax;

        try {
            newTimeSpan = Integer.parseInt(etCountTimeSpan.getText().toString());
            newReadSpan = Integer.parseInt(etCountReadSpan.getText().toString());
            newDigitSpan = Integer.parseInt(editDigitSpan.getText().toString());
            newPlayRate = Float.parseFloat(editPlayRate.getText().toString());
            newCountStart = Integer.parseInt(editCountStart.getText().toString());
            newCountMax = Integer.parseInt(editCountMax.getText().toString());
            newCountOnMax = rbOnMaxLoop.isChecked()?NumReader.COUNT_ON_MAX_LOOP:NumReader.COUNT_ON_MAX_STOP;
        } catch (NumberFormatException ex) {
            Toast.makeText(MainActivity.this, "NumberFormatException: " + ex.toString(), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean countStartChanged = (newCountStart != NumReader.mCountStart);

        if (newTimeSpan != NumReader.mTimeSpan
                || newReadSpan != NumReader.mReadSpan
                || newDigitSpan != NumReader.mDigitSpan
                || newPlayRate != NumReader.mPlayRate
                || newCountStart != NumReader.mCountStart
                || newCountMax != NumReader.mCountMax
                || newCountOnMax != NumReader.mCountOnMax
        ) {

            //setting change, save new settings
            SharedPreferences.Editor editor = this.getSharedPreferences(NumReader.SP_SETTINGS, Context.MODE_PRIVATE).edit();
            editor.clear();
            editor.putInt(NumReader.SP_TIME_SPAN, newTimeSpan);
            editor.putInt(NumReader.SP_READ_SPAN, newReadSpan);
            editor.putInt(NumReader.SP_DIGIT_SPAN, newDigitSpan);
            editor.putFloat(NumReader.SP_PLAY_RATE, newPlayRate);
            editor.putInt(NumReader.SP_COUNT_START, newCountStart);
            editor.putInt(NumReader.SP_COUNT_MAX, newCountMax);
            editor.putInt(NumReader.SP_COUNT_ON_MAX, newCountOnMax);
            editor.apply();

            //update value and pause current reading
            NumReader.mTimeSpan = newTimeSpan;
            NumReader.mReadSpan = newReadSpan;
            NumReader.mDigitSpan = newDigitSpan;
            NumReader.mPlayRate = newPlayRate;
            NumReader.mCountStart = newCountStart;
            NumReader.mCountMax = newCountMax;
            NumReader.mCountOnMax = newCountOnMax;
        }

        if (countStartChanged) {
            updateUI();
        }
    }
}
