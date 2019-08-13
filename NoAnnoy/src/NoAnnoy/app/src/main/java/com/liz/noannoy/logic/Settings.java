package com.liz.noannoy.logic;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by liz on 18-4-11.
 */

@SuppressWarnings("unused, WeakerAccess")
public class Settings {

    private static final String PREFERENCE_SETTINGS = "com.liz.noannoy.sharedpreference";

    private static final String KEY_LOCAL_MDN ="user.local.mdn";
    private static final String KEY_NODE_URL ="serer.node.url";

    public static String getLocalMdn(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_LOCAL_MDN,"");
    }

    public static void setLocalMdn(Context context, String localMdn) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_LOCAL_MDN, localMdn);
        editor.apply();
    }

    public static void setNodeUrl(Context context, String nodeUrl) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_NODE_URL, nodeUrl);
        editor.apply();
    }

    public static String getNodeUrl(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_NODE_URL,"");
    }
}
