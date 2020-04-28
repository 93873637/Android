package com.liz;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@SuppressWarnings("WeakerAccess")
public class NumReader {

    public static final String TAG = "NumReader";

    public static final String SP_SETTINGS = "SharedPrefSettings";

    public static final String SP_TIME_SPAN = "SharedPrefCountTimeSpan";
    public static final String SP_READ_SPAN = "SharedPrefCountReadSpan";
    public static final String SP_DIGIT_SPAN = "SharedPrefDigitSpan";
    public static final String SP_PLAY_RATE = "SharedPrefPlaySpan";
    public static final String SP_COUNT_START = "SharedPrefCountStart";

    public static final int DEFAULT_TIME_SPAN = 1;  //default time span(seconds) to count once
    public static final int DEFAULT_READ_SPAN = 5;  //default span to read once
    public static final int DEFAULT_DIGIT_SPAN = 300;  //default time span(seconds) to count once
    public static final float DEFAULT_PLAY_RATE = 1.1f;  //default span to read once
    public static final int DEFAULT_COUNT_START = 0;  //default count start number

    public static int mTimeSpan = DEFAULT_TIME_SPAN;  //how many seconds to count once
    public static int mReadSpan = DEFAULT_READ_SPAN;  //how many count to read once
    public static int mDigitSpan = DEFAULT_DIGIT_SPAN;  // span between digit(s) in same number string, unit by ms
    public static float mPlayRate = DEFAULT_PLAY_RATE;  // sound pool play rate, 0.5f - 2.0f, slower - faster
    public static int mCountStart = DEFAULT_COUNT_START;

    //constants
    public static final int TIMER_DELAY = 0;
    public static final int TIMER_EVENT = 1;

    //variables
    private static Timer mCountTimer = null;
    private static int mCountNumber = mCountStart;

    public static final int MSG_NUMBER_UPDATED = 0;

    private static Handler mUIHandler = null;

    public static void setUIHandler(Handler handler) {
        mUIHandler = handler;
    }

    public static void init(Context context) {
        SharedPreferences spSettings = context.getSharedPreferences(NumReader.SP_SETTINGS, Context.MODE_PRIVATE);
        mTimeSpan = spSettings.getInt(NumReader.SP_TIME_SPAN, NumReader.DEFAULT_TIME_SPAN);
        mReadSpan = spSettings.getInt(NumReader.SP_READ_SPAN, NumReader.DEFAULT_READ_SPAN);
        mDigitSpan = spSettings.getInt(NumReader.SP_DIGIT_SPAN, NumReader.DEFAULT_DIGIT_SPAN);
        mPlayRate = spSettings.getFloat(NumReader.SP_PLAY_RATE, NumReader.DEFAULT_PLAY_RATE);
        mCountStart = spSettings.getInt(NumReader.SP_COUNT_START, NumReader.DEFAULT_COUNT_START);
        mCountNumber = mCountStart;
        SoundPoolPlayer.init(context);
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
        mCountNumber = mCountStart;
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
        return "" + mCountStart;
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

    public static int getNumber() {
        return mCountNumber;
    }

    private static class MyTimerTask extends TimerTask {
        public void run() {
            Message message = new Message();
            message.what = TIMER_EVENT;
            handler.sendMessage(message);
        }
    }

    ;

    final static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_EVENT:
                    onTimerEvent();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public static void onTimerEvent() {
        mCountNumber++;
        mUIHandler.sendEmptyMessage(MSG_NUMBER_UPDATED);

        if (mCountNumber % mReadSpan == 0) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SoundPoolPlayer.playNumber(mCountNumber);
                }
            }).start();
        }
    }
}
