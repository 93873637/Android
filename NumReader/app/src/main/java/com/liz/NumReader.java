package com.liz;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.liz.androidutils.LogUtils;

@SuppressWarnings("WeakerAccess")
public class NumReader {

    public static final String TAG = "NumReader";

    public static final String SP_SETTINGS = "SharedPrefSettings";

    public static final String SP_TIME_SPAN = "SharedPrefCountTimeSpan";
    public static final String SP_READ_SPAN = "SharedPrefCountReadSpan";
    public static final String SP_DIGIT_SPAN = "SharedPrefDigitSpan";
    public static final String SP_PLAY_RATE = "SharedPrefPlaySpan";
    public static final String SP_COUNT_MIN = "SharedPrefCountStart";
    public static final String SP_COUNT_MAX = "SharedPrefCountMax";
    public static final String SP_COUNT_DOWN = "SharedPrefCountDown";
    public static final String SP_ON_COUNT_END = "SharedPrefOnCountEnd";

    public static final int COUNT_ON_END_LOOP = 0;
    public static final int COUNT_ON_END_STOP = 1;

    public static final int DEFAULT_TIME_SPAN = 1;  //default time span(seconds) to count once
    public static final int DEFAULT_READ_SPAN = 5;  //default span to read once
    public static final int DEFAULT_DIGIT_SPAN = 300;  //default time span(seconds) to count once
    public static final float DEFAULT_PLAY_RATE = 1.1f;  //default span to read once
    public static final int DEFAULT_COUNT_MIN = 0;  //default count start number
    public static final int DEFAULT_COUNT_MAX = 100;
    public static final boolean DEFAULT_COUNT_DOWN = false;
    public static final int DEFAULT_ON_COUNT_END = COUNT_ON_END_LOOP;

    public static int mTimeSpan = DEFAULT_TIME_SPAN;  //how many seconds to count once
    public static int mReadSpan = DEFAULT_READ_SPAN;  //how many count to read once
    public static int mDigitSpan = DEFAULT_DIGIT_SPAN;  // span between digit(s) in same number string, unit by ms
    public static float mPlayRate = DEFAULT_PLAY_RATE;  // sound pool play rate, 0.5f - 2.0f, slower - faster
    public static int mCountMin = DEFAULT_COUNT_MIN;
    public static int mCountMax = DEFAULT_COUNT_MAX;
    public static boolean mCountDown = DEFAULT_COUNT_DOWN;
    public static int mOnCountEnd = DEFAULT_ON_COUNT_END;

    //constants
    public static final int TIMER_DELAY = 0;
    public static final int TIMER_EVENT = 1;

    //variables
    private static Timer mCountTimer = null;
    private static int mCountNumber = mCountMin;

    public static final int MSG_NUMBER_UPDATED = 0;

    private static Handler mUIHandler = null;

    public static void setUIHandler(Handler handler) {
        mUIHandler = handler;
    }

    public static void init(Context context) {
        LogUtils.setTag(TAG);
        SharedPreferences spSettings = context.getSharedPreferences(NumReader.SP_SETTINGS, Context.MODE_PRIVATE);
        mTimeSpan = spSettings.getInt(NumReader.SP_TIME_SPAN, NumReader.DEFAULT_TIME_SPAN);
        mReadSpan = spSettings.getInt(NumReader.SP_READ_SPAN, NumReader.DEFAULT_READ_SPAN);
        mDigitSpan = spSettings.getInt(NumReader.SP_DIGIT_SPAN, NumReader.DEFAULT_DIGIT_SPAN);
        mPlayRate = spSettings.getFloat(NumReader.SP_PLAY_RATE, NumReader.DEFAULT_PLAY_RATE);
        mCountMin = spSettings.getInt(NumReader.SP_COUNT_MIN, NumReader.DEFAULT_COUNT_MIN);
        mCountMax = spSettings.getInt(NumReader.SP_COUNT_MAX, NumReader.DEFAULT_COUNT_MAX);
        mCountDown = spSettings.getBoolean(NumReader.SP_COUNT_DOWN, NumReader.DEFAULT_COUNT_DOWN);
        mOnCountEnd = spSettings.getInt(NumReader.SP_ON_COUNT_END, NumReader.DEFAULT_ON_COUNT_END);
        initCountNumber();
        SoundPoolPlayer.init(context);
    }

    public static void initCountNumber() {
        if (mCountDown) {
            mCountNumber = mCountMax;
        }
        else {
            mCountNumber = mCountMin;
        }
    }

    public static void switchPlayPause() {
        if (isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    public static void reset() {
        pause();
        initCountNumber();
    }

    public static void replay() {
        reset();
        start();
    }

    public static void release() {
        pause();
        SoundPoolPlayer.release();
    }

    public static boolean isPlaying() {
        return mCountTimer != null;
    }

    public static void start() {
        if (mCountTimer != null) {
            Log.w(TAG, "timer already start");
        } else {
            mCountTimer = new Timer();
            mCountTimer.schedule(new MyTimerTask(), TIMER_DELAY, mTimeSpan * 1000);
        }
    }

    public static void pause() {
        if (mCountTimer == null) {
            Log.w(TAG, "no timer to pause");
        } else {
            mCountTimer.cancel();
            mCountTimer.purge();
            mCountTimer = null;
        }
    }

    public static String getTimeSpanString() {
        return "" + mTimeSpan;
    }

    public static String getReadSpanString() {
        return "" + mReadSpan;
    }

    public static String getDigitSpanString() {
        return "" + mDigitSpan;
    }

    public static String getCountStartString() {
        return "" + mCountMin;
    }

    public static String getCountMaxString() {
        return "" + mCountMax;
    }

    public static boolean isCountDown() {
        return mCountDown;
    }

    public static boolean isOnEndLoop() {
        return mOnCountEnd == COUNT_ON_END_LOOP;
    }

    public static String getPlayRateString() {
        return "" + mPlayRate;
    }

    public static String getFormatTimeStr() {
        int secondCount = mCountNumber * mTimeSpan;
        int hour = secondCount / (60 * 60);
        int minute = (secondCount - hour * 60) / 60;
        int second = secondCount - hour * 60 * 60 - minute * 60;

        String strHour = "" + hour;
        if (hour < 10) {
            strHour = "0" + hour;
        }
        String strMinute = "" + minute;
        if (minute < 10) {
            strMinute = "0" + minute;
        }
        String strSecond = "" + second;
        if (second < 10) {
            strSecond = "0" + second;
        }

        return strHour + ":" + strMinute + ":" + strSecond;
    }

    public static String getNumberString() {
        return "" + mCountNumber;
    }

    private static class MyTimerTask extends TimerTask {
        public void run() {
            Message message = new Message();
            message.what = TIMER_EVENT;
            handler.sendMessage(message);
        }
    }

    final static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_EVENT:
                    onReadNumber();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static void onReadNumber() {
        LogUtils.td("mCountNumber = " + mCountNumber);
        mUIHandler.sendEmptyMessage(MSG_NUMBER_UPDATED);
        if (mCountNumber % mReadSpan == 0) {
            final int num = mCountNumber;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SoundPoolPlayer.playNumber(num);
                    toNextNumber();
                }
            }).start();
        }
    }

    public static void toNextNumber() {
        if (mCountDown) {
            mCountNumber--;
            if (mCountNumber < mCountMin) {
                if (isOnEndLoop()) {
                    mCountNumber = mCountMax;
                } else {
                    mCountNumber = mCountMin;
                    NumReader.pause();
                }
            }
        }
        else {
            mCountNumber++;
            if (mCountNumber > mCountMax) {
                if (isOnEndLoop()) {
                    mCountNumber = mCountMin;
                } else {
                    mCountNumber = mCountMax;
                    NumReader.pause();
                }
            }
        }
    }
}
