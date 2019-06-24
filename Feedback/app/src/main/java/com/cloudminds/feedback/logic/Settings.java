package com.cloudminds.feedback.logic;

import android.content.Context;
import android.content.SharedPreferences;

import com.cloudminds.feedback.utils.SysUtils;

/**
 * Created by cloud on 18-4-11.
 */

public class Settings {

    private static final String PREFRENCE_SETTING = "com.cloudminds.feedback.sharedpreference";
    private static final String PREFRENCE_SETTING_ONWIFI = "sharedpreference.setting.feedbackonwifi";

    private static final String USER_TYPE_KEY="user.type.key";
    public static final String TEST_USER = "test.user";
    public static final String NORMAL_USER = "normal.user";

    //public static final String KEY_PERSIST_FEEDBACK = "persist.sys.user.experience";

    public static void setUserType(Context context, String type) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFRENCE_SETTING, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(USER_TYPE_KEY,type);
        editor.commit();
    }

    public static String getUserType(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFRENCE_SETTING, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(USER_TYPE_KEY,"");
    }

    public static void setFeedbackOnWifi(Context context, Boolean feedbackonwifi) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFRENCE_SETTING, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(PREFRENCE_SETTING_ONWIFI, feedbackonwifi);
        editor.commit();
    }

    public static boolean getFeedbackOnWifi(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFRENCE_SETTING, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(PREFRENCE_SETTING_ONWIFI,true);
    }

    public static boolean readUserSwitch(Context context) {
        return ComDef.isUserExperienceEnabled(context);
        /*
        boolean r = false;
        int value = SystemProperties.getInt(KEY_PERSIST_FEEDBACK, 0);
        if (value == 1) {
            r = true;
        }
        return r;
        */
    }

    public static void saveUserSwitchOpen(Context context, boolean newValue) {
        //SystemProperties.set(KEY_PERSIST_FEEDBACK, newValue ? "1" : "0");
        SysUtils.setSystemProperty(context, ComDef.PROP_USER_EXPERIENCE, newValue ? "1" : "0");
    }
}
