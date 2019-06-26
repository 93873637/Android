package com.liz.imagetools.utils;

import android.util.Log;

@SuppressWarnings("WeakerAccess,unused")
public class LogUtils {

	//todo: change the tag as your requirement
	private static final String TAG = "Feedback";

	public static void d(String msg) {
		Log.d(TAG, msg);
	}
	public static void e(String msg) {
		Log.e(TAG, msg);
	}
	public static void i(String msg) {
		Log.i(TAG, msg);
	}
	public static void w(String msg) {
		Log.w(TAG, msg);
	}
	public static void v(String msg) {
		Log.v(TAG, msg);
	}

	public static void tipD(String msg) {
		Log.d(TAG, "UITIP: " + msg);
	}
	public static void tipE(String msg) {
		Log.e(TAG, "UITIP:ERROR: " + msg);
	}
}
