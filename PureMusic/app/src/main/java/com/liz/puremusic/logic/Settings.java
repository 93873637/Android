package com.liz.puremusic.logic;

import android.content.Context;
import android.content.SharedPreferences;

import com.liz.puremusic.app.MyApp;


@SuppressWarnings({"unused"})
public class Settings {

    private static final String MY_SHARED_PREFERENCES = ComDef.APP_NAME + "SharedPreferences";

    private static final String KEY_MUSIC_HOME = "MusicHome";
    private static final String DEFAULT_MUSIC_HOME = ComDef.MUSIC_DEFAULT_HOME;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // INTERFACES

    public static String readMusicHome() { return readItem(KEY_MUSIC_HOME, DEFAULT_MUSIC_HOME); }
    public static void saveMusicHome(String value) { saveItem(KEY_MUSIC_HOME, value); }

//    public static int readExitStatus() { return readItemInt(ComDef.KEY_EXIT_STATUS, ComDef.DEFAULT_EXIT_STATUS); }
//    public static void saveExitStatus(int value) { saveItemInt(ComDef.KEY_EXIT_STATUS, value); }

//    public static long readHeartbeatTimer() { return readItemLong(ComDef.KEY_HEARTBEAT_TIMER, ComDef.DEFAULT_HEARTBEAT_TIMER_PERIOD); }
//    public static void saveHeartbeatTimer(long value) { saveItemLong(ComDef.KEY_HEARTBEAT_TIMER, value); }

    // INTERFACES
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static SharedPreferences mSharedPreferences;

    static {
        mSharedPreferences = MyApp.getAppContext().getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

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
