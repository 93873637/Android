package com.liz.multidialer.logic;

import android.content.Context;
import android.content.SharedPreferences;

import com.liz.multidialer.app.ThisApp;


@SuppressWarnings({"unused", "WeakerAccess"})
public class Settings {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //Interface Functions

    public static String readDeviceId() { return readItem(ComDef.KEY_DEVICE_ID, ComDef.DEFAULT_DEVICE_ID); }
    public static void saveDeviceId(String value) { saveItem(ComDef.KEY_DEVICE_ID, value); }

    public static String readServerAddress() { return readItem(ComDef.KEY_SERVER_ADDRESS, ComDef.DEFAULT_SERVER_ADDRESS); }
    public static void saveServerAddress(String value) { saveItem(ComDef.KEY_SERVER_ADDRESS, value); }

    public static int readServerPort() { return readItemInt(ComDef.KEY_SERVER_PORT, ComDef.DEFAULT_SERVER_PORT); }
    public static void saveServerPort(int value) { saveItemInt(ComDef.KEY_SERVER_PORT, value); }

    public static String readUserName() { return readItem(ComDef.KEY_USER_NAME, ComDef.DEFAULT_USER_NAME); }
    public static void saveUserName(String value) { saveItem(ComDef.KEY_USER_NAME, value); }

    public static String readPassword() { return readItem(ComDef.KEY_PASSWORD, ComDef.DEFAULT_PASSWORD); }
    public static void savePassword(String value) { saveItem(ComDef.KEY_PASSWORD, value); }

    public static String readNetworkType() { return readItem(ComDef.KEY_NETWORK_TYPE, ComDef.DEFAULT_NETWORK_TYPE); }
    public static void saveNetworkType(String value) { saveItem(ComDef.KEY_NETWORK_TYPE, value); }

    public static String readServerHome() { return readItem(ComDef.KEY_SERVER_HOME, ComDef.DEFAULT_SERVER_HOME); }
    public static void saveServerHome(String value) { saveItem(ComDef.KEY_SERVER_HOME, value); }

    public static int readJpegQuality() { return readItemInt(ComDef.KEY_JPEG_QUALITY, ComDef.DEFAULT_JPEG_QUALITY); }
    public static void saveJpegQuality(int value) { saveItemInt(ComDef.KEY_JPEG_QUALITY, value); }

    public static String readFileListFile() { return readItem(ComDef.KEY_TEL_LIST_FILE, ComDef.DEFAULT_TEL_LIST_FILE); }
    public static void saveFileListFile(String value) { saveItem(ComDef.KEY_TEL_LIST_FILE, value); }

    public static int readCurrentCallIndex() { return readItemInt(ComDef.KEY_CURRENT_CALLED_INDEX, ComDef.DEFAULT_CURRENT_CALLED_INDEX); }
    public static void saveCurrentCallIndex(int currentCallIndex) { saveItemInt(ComDef.KEY_CURRENT_CALLED_INDEX, currentCallIndex); }

    public static String readPicturePath() { return readItem(ComDef.KEY_PICTURE_PATH, ComDef.DEFAULT_PICTURE_PATH); }
    public static void savePicturePath(String value) { saveItem(ComDef.KEY_PICTURE_PATH, value); }

    //Interface Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static int readItemInt(String key, int defaultValue) {
        SharedPreferences sharedPreferences = ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }

    private static void saveItemInt(String key, int value) {
        SharedPreferences sharedPreferences= ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private static String readItem(String key, String defaultValue) {
        SharedPreferences sharedPreferences = ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    private static void saveItem(String key, String value) {
        SharedPreferences sharedPreferences= ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
