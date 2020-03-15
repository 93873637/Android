package com.liz.whatsai.logic;

import android.content.Context;
import android.content.SharedPreferences;


@SuppressWarnings({"unused"})
public class Settings {

    private static SharedPreferences mSharedPreferences = null;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //Interface Functions

    public static void init(Context context) {
        mSharedPreferences = context.getSharedPreferences(ComDef.WHATSAI_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static int readExitStatus() { return readItemInt(ComDef.KEY_EXIT_STATUS, ComDef.DEFAULT_EXIT_STATUS); }
    public static void saveExitStatus(int value) { saveItemInt(ComDef.KEY_EXIT_STATUS, value); }

    public static String readAudioRecordFileName() { return readItem(ComDef.KEY_AUDIO_RECORD_FILENAME, ComDef.DEFAULT_AUDIO_RECORD_FILENAME); }
    public static void saveAudioRecordFileName(String value) { saveItem(ComDef.KEY_AUDIO_RECORD_FILENAME, value); }

//    public static long readHeartbeatTimer() { return readItemLong(ComDef.KEY_HEARTBEAT_TIMER, ComDef.DEFAULT_HEARTBEAT_TIMER_PERIOD); }
//    public static void saveHeartbeatTimer(long value) { saveItemLong(ComDef.KEY_HEARTBEAT_TIMER, value); }

    //Interface Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static int readItemInt(String key, int defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    private static void saveItemInt(String key, int value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private static long readItemLong(String key, long defaultValue) {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    private static void saveItemLong(String key, long value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    private static String readItem(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    private static void saveItem(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
