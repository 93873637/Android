package com.liz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

@SuppressWarnings("WeakerAccess")
public class SettingsDlg {

    private Context mContext;
    private View mLayoutSettings = null;
    private SettingsDlgCallback mCallback;

    private EditText mEditCountTimeSpan;
    private EditText mEditCountReadSpan;
    private EditText mEditDigitSpan;
    private EditText mEditPlayRate;
    private EditText mEditCountMin;
    private EditText mEditCountMax;
    private RadioButton mRbOnMaxLoop;
    private CheckBox mCbCountDown;

    public SettingsDlg(Context context, SettingsDlgCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    public interface SettingsDlgCallback {
        void onSettingsUpdated();
    }

    public void show(Activity activity) {
        prepareSettings(activity);
        new AlertDialog.Builder(mContext)
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


    private void prepareSettings(Activity activity) {
        LayoutInflater inflater = activity.getLayoutInflater();
        mLayoutSettings = inflater.inflate(R.layout.layout_settings,
                (ViewGroup) activity.findViewById(R.id.settings_dialog));

        mEditCountTimeSpan = mLayoutSettings.findViewById(R.id.etCountTimeSpan);
        mEditCountReadSpan = mLayoutSettings.findViewById(R.id.etCountReadSpan);
        mEditDigitSpan = mLayoutSettings.findViewById(R.id.edit_digits_span);
        mEditPlayRate = mLayoutSettings.findViewById(R.id.edit_play_rate);
        mEditCountMin = mLayoutSettings.findViewById(R.id.edit_count_min);
        mEditCountMax = mLayoutSettings.findViewById(R.id.edit_count_max);
        mCbCountDown = mLayoutSettings.findViewById(R.id.cb_count_down);
        mRbOnMaxLoop = mLayoutSettings.findViewById(R.id.rb_on_max_loop);
        RadioButton rbOnMaxStop = mLayoutSettings.findViewById(R.id.rb_on_max_stop);

        mEditCountTimeSpan.setText(NumReader.getTimeSpanString());
        mEditCountReadSpan.setText(NumReader.getReadSpanString());
        mEditDigitSpan.setText(NumReader.getDigitSpanString());
        mEditPlayRate.setText(NumReader.getPlayRateString());
        mEditCountMin.setText(NumReader.getCountStartString());
        mEditCountMax.setText(NumReader.getCountMaxString());
        mCbCountDown.setChecked(NumReader.isCountDown());
        if (NumReader.isOnEndLoop()) {
            mRbOnMaxLoop.setChecked(true);
        }
        else {
            rbOnMaxStop.setChecked(true);
        }
    }

    private void onSettingsOK() {
        //set origin value
        int newTimeSpan;
        int newReadSpan;
        int newDigitSpan;
        float newPlayRate;
        int newCountMin;
        int newCountMax;
        boolean newCountDown;
        int newOnCountEnd;

        try {
            newTimeSpan = Integer.parseInt(mEditCountTimeSpan.getText().toString());
            newReadSpan = Integer.parseInt(mEditCountReadSpan.getText().toString());
            newDigitSpan = Integer.parseInt(mEditDigitSpan.getText().toString());
            newPlayRate = Float.parseFloat(mEditPlayRate.getText().toString());
            newCountMin = Integer.parseInt(mEditCountMin.getText().toString());
            newCountMax = Integer.parseInt(mEditCountMax.getText().toString());
            newCountDown = mCbCountDown.isChecked();
            newOnCountEnd = mRbOnMaxLoop.isChecked()?NumReader.COUNT_ON_END_LOOP :NumReader.COUNT_ON_END_STOP;
        } catch (NumberFormatException ex) {
            Toast.makeText(mContext, "NumberFormatException: " + ex.toString(), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean countStartChanged = (newCountMin != NumReader.mCountMin);

        if (newTimeSpan != NumReader.mTimeSpan
                || newReadSpan != NumReader.mReadSpan
                || newDigitSpan != NumReader.mDigitSpan
                || newPlayRate != NumReader.mPlayRate
                || newCountMin != NumReader.mCountMin
                || newCountMax != NumReader.mCountMax
                || newCountDown != NumReader.mCountDown
                || newOnCountEnd != NumReader.mOnCountEnd
        ) {
            //setting change, save new settings
            SharedPreferences.Editor editor = mContext.getSharedPreferences(NumReader.SP_SETTINGS, Context.MODE_PRIVATE).edit();
            editor.clear();
            editor.putInt(NumReader.SP_TIME_SPAN, newTimeSpan);
            editor.putInt(NumReader.SP_READ_SPAN, newReadSpan);
            editor.putInt(NumReader.SP_DIGIT_SPAN, newDigitSpan);
            editor.putFloat(NumReader.SP_PLAY_RATE, newPlayRate);
            editor.putInt(NumReader.SP_COUNT_MIN, newCountMin);
            editor.putInt(NumReader.SP_COUNT_MAX, newCountMax);
            editor.putBoolean(NumReader.SP_COUNT_DOWN, newCountDown);
            editor.putInt(NumReader.SP_ON_COUNT_END, newOnCountEnd);
            editor.apply();

            //update value and pause current reading
            NumReader.mTimeSpan = newTimeSpan;
            NumReader.mReadSpan = newReadSpan;
            NumReader.mDigitSpan = newDigitSpan;
            NumReader.mPlayRate = newPlayRate;
            NumReader.mCountMin = newCountMin;
            NumReader.mCountMax = newCountMax;
            NumReader.mCountDown = newCountDown;
            NumReader.mOnCountEnd = newOnCountEnd;
        }

        if (countStartChanged && mCallback != null) {
            mCallback.onSettingsUpdated();
        }
    }
}
