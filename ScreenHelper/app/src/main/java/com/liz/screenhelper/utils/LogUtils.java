package com.liz.screenhelper.utils;

import android.util.Log;

@SuppressWarnings("unused")
public class LogUtils {

	private static final String DEF_TAG = "LogUtils";
	private static String mTag = DEF_TAG;

	public static void setTag(String tag) {
		mTag = tag;
	}

	public static void d(String msg) {
		Log.d(mTag, msg);
	}
	public static void e(String msg) {
		Log.e(mTag, msg);
	}
	public static void i(String msg) {
		Log.i(mTag, msg);
	}
	public static void w(String msg) {
		Log.w(mTag, msg);
	}
	public static void v(String msg) {
		Log.v(mTag, msg);
	}

	public static void tipD(String msg) {
		Log.d(mTag, "UITIP: " + msg);
	}
	public static void tipE(String msg) {
		Log.e(mTag, "UITIP:ERROR: " + msg);
	}

	public static void printStack(String msg) {
		new Exception(mTag + ": " + msg).printStackTrace();
	}

	public static void d(Object obj, String msg) {
		Log.d(mTag, obj.getClass().getName() + ":" + msg);
	}
	public static void e(Object obj, String msg) {
		Log.e(mTag, obj.getClass().getName() + ":" + msg);
	}
	public static void i(Object obj, String msg) {
		Log.i(mTag, obj.getClass().getName() + ":" + msg);
	}
	public static void w(Object obj, String msg) {
		Log.w(mTag, obj.getClass().getName() + ":" + msg);
	}
	public static void v(Object obj, String msg) {
		Log.v(mTag, obj.getClass().getName() + ":" + msg);
	}

//	//this can only put codes in place to tacke effect
//	public static void d2(String msg) {
//		String className = Thread.currentThread().getStackTrace()[1].getClassName();
//		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
//		Log.d(mTag, className + "." + methodName + ":" + msg);
//	}
}
