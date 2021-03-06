package com.serenegiant.usbcameratest7.utils;

import android.util.Log;

@SuppressWarnings("unused, WeakerAccess")
public class LogUtils {

	public static final int LOG_LEVEL_V = 0;
	public static final int LOG_LEVEL_D = 1;
	public static final int LOG_LEVEL_I = 2;
	public static final int LOG_LEVEL_W = 3;
	public static final int LOG_LEVEL_E = 4;
	public static final int LOG_LEVEL_MIN = LOG_LEVEL_V;
	public static final int LOG_LEVEL_MAX = LOG_LEVEL_E;
	public static final int LOG_LEVEL_DEF = LOG_LEVEL_D;

	private static final String DEF_TAG = "LogUtils";
	private static String mTag = DEF_TAG;
	private static int mLevel = LOG_LEVEL_DEF;

	public static void setTag(String tag) {
		mTag = tag;
	}

	public static void setLevel(int level) {
		if (level < LOG_LEVEL_MIN || level > LOG_LEVEL_MAX) {
			e("ERROR: Can't set log level with invalid value " + level + ", should be [" + LOG_LEVEL_MIN + ", " + LOG_LEVEL_MAX + "]");
		}
		else {
			mLevel = level;
		}
	}

	public static int getLevel() {
		return mLevel;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Callback Log By Listener

	//####@:
	//use this callback to show log on ui?
//
//	public void testUsingLogCallback(final Activity activity) {
//		LogUtils.setLogListener(new LogUtils.LogListener() {
//			public void onCBLog(String msg, int level) {
//				activity.runOnUiThread(new Runnable() {
//					public void run() {
//						String logMsg = TimeUtils.getLogTime() + " - " + msg;
//						mProgressBuffer.append(logMsg);
//						String progressInfo = mProgressBuffer.getBuffer() + "\n";
//						mTextProgressInfo.setText(progressInfo);
//					}
//				});
//			}
//		});
//	}

	public interface LogListener {
		void onCBLog(String msg, int level);
	}

	private static LogListener mLogListener;

	public static void setLogListener(LogListener listener) {
		mLogListener = listener;
	}

	// Callback Log By Listener
	////////////////////////////////////////////////////////////////////////////////////////////////

	public static void v(String msg) {
		if (mLevel == LOG_LEVEL_V) {
			Log.v(mTag, msg);
			if (mLogListener != null) {
				mLogListener.onCBLog(msg, LOG_LEVEL_V);
			}
		}
	}

	public static void d(String msg) {
		if (mLevel <= LOG_LEVEL_D) {
			Log.d(mTag, msg);
			if (mLogListener != null) {
				mLogListener.onCBLog(msg, LOG_LEVEL_D);
			}
		}
	}

	public static void i(String msg) {
		if (mLevel <= LOG_LEVEL_I) {
			Log.i(mTag, msg);
			if (mLogListener != null) {
				mLogListener.onCBLog(msg, LOG_LEVEL_I);
			}
		}
	}

	public static void w(String msg) {
		if (mLevel <= LOG_LEVEL_W) {
			Log.w(mTag, msg);
			if (mLogListener != null) {
				mLogListener.onCBLog(msg, LOG_LEVEL_W);
			}
		}
	}

	public static void e(String msg) {
		//since e is top level, not need to judge, direct show
		Log.e(mTag, msg);
		if (mLogListener != null) {
			mLogListener.onCBLog(msg, LOG_LEVEL_E);
		}
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

	public static void v(Object obj, String msg) { Log.v(mTag, obj.getClass().getName() + ":" + msg); }
	public static void d(Object obj, String msg) { Log.d(mTag, obj.getClass().getName() + ":" + msg); 	}
	public static void i(Object obj, String msg) { Log.i(mTag, obj.getClass().getName() + ":" + msg); 	}
	public static void w(Object obj, String msg) { Log.w(mTag, obj.getClass().getName() + ":" + msg);	}
	public static void e(Object obj, String msg) { Log.e(mTag, obj.getClass().getName() + ":" + msg);	}

	//this can only put codes in place to take effect
	public static void d2(String msg) {
		String className = Thread.currentThread().getStackTrace()[1].getClassName();
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		Log.d(mTag, className + "." + methodName + ":" + msg);
	}

	public static void printBytes(byte[] bytes) {
		//instead by foreach
		//for (int i=0; i<bytes.length; i++) {
		for (byte b : bytes) {
			System.out.print(b + " ");
		}
	}
}
