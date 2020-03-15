package com.liz.androidutils;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Settings {
//
//    private static SharedPreferences mSharedPreferences;
//
//
////    ///////////////////////////////////////////////////////////////////////////////////////////////
////    //Interface Functions
////
////    public static String readDeviceId() { return readItem(ComDef.KEY_DEVICE_ID, ComDef.DEFAULT_DEVICE_ID); }
////    public static void saveDeviceId(String value) { saveItem(ComDef.KEY_DEVICE_ID, value); }
////
////    public static int readServerPort() { return readItemInt(ComDef.KEY_SERVER_PORT, ComDef.DEFAULT_SERVER_PORT); }
////    public static void saveServerPort(int value) { saveItemInt(ComDef.KEY_SERVER_PORT, value); }
////
////    public static long readHeartbeatTimer() { return readItemLong(ComDef.KEY_HEARTBEAT_TIMER, ComDef.DEFAULT_HEARTBEAT_TIMER_PERIOD); }
////    public static void saveHeartbeatTimer(long value) { saveItemLong(ComDef.KEY_HEARTBEAT_TIMER, value); }
////
////    //Interface Functions
////    ///////////////////////////////////////////////////////////////////////////////////////////////
//
//    private static int readItemInt(String key, int defaultValue) {
//        SharedPreferences sharedPreferences = ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
//        return sharedPreferences.getInt(key, defaultValue);
//    }
//
//    private static void saveItemInt(String key, int value) {
//        SharedPreferences sharedPreferences= ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(key, value);
//        editor.apply();
//    }
//
//    private static long readItemLong(String key, long defaultValue) {
//        SharedPreferences sharedPreferences = ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
//        return sharedPreferences.getLong(key, defaultValue);
//    }
//
//    private static void saveItemLong(String key, long value) {
//        SharedPreferences sharedPreferences= ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putLong(key, value);
//        editor.apply();
//    }
//
//    private static String readItem(String key, String defaultValue) {
//        SharedPreferences sharedPreferences = ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
//        return sharedPreferences.getString(key, defaultValue);
//    }
//
//    private static void saveItem(String key, String value) {
//        SharedPreferences sharedPreferences= ThisApp.getAppContext().getSharedPreferences(ComDef.MULTIDIALER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(key, value);
//        editor.apply();
//    }
}
