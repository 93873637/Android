package com.liz.androidutils;

import android.util.Log;

import androidx.annotation.NonNull;


/**
 * An android based log extension class
 * No any dependencies, you can copy and use directly on android app.
 * Author: liz
 */

@SuppressWarnings("unused, WeakerAccess")
public class LogUtils {

    // log level, ref to Log.*
    public static final int LOG_LEVEL_V = 2;
	public static final int LOG_LEVEL_D = 3;
	public static final int LOG_LEVEL_I = 4;
	public static final int LOG_LEVEL_W = 5;
	public static final int LOG_LEVEL_E = 6;
	public static final int LOG_LEVEL_MIN = LOG_LEVEL_V;
	public static final int LOG_LEVEL_MAX = LOG_LEVEL_E;
	public static final int LOG_LEVEL_DEF = LOG_LEVEL_D;

	private static final String DEF_TAG = "LogUtils";
	private static String mTag = DEF_TAG;
	private static int mLevel = LOG_LEVEL_DEF;

    public static void setTag(String tag) {
        mTag = tag;
    }

    public static String getTag() {
        return mTag;
    }

	public static void setLevel(int level) {
		if (level < LOG_LEVEL_MIN || level > LOG_LEVEL_MAX) {
			e("ERROR: Can'trace set log level with invalid value " + level + ", should be [" + LOG_LEVEL_MIN + ", " + LOG_LEVEL_MAX + "]");
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

	public static void w2(String msg) {
		w("WARNING: " + msg);
	}

	public static void e(String msg) {
		//since e is top level, not need to judge, direct show
		Log.e(mTag, msg);
		if (mLogListener != null) {
			mLogListener.onCBLog(msg, LOG_LEVEL_E);
		}
	}

	public static void e2(String msg) {
		e("ERROR: " + msg);
	}

	public static void v(Object obj, String msg) { Log.v(mTag, obj.getClass().getName() + ":" + msg); }
	public static void d(Object obj, String msg) { Log.d(mTag, obj.getClass().getName() + ":" + msg); 	}
	public static void i(Object obj, String msg) { Log.i(mTag, obj.getClass().getName() + ":" + msg); 	}
	public static void w(Object obj, String msg) { Log.w(mTag, obj.getClass().getName() + ":" + msg);	}
	public static void e(Object obj, String msg) { Log.e(mTag, obj.getClass().getName() + ":" + msg);	}

	public static void trace() {
		StringBuilder sb = new StringBuilder("TRACE");
		sb.append(" ");
		sb.append(android.os.Process.myPid());
		sb.append("/");
		sb.append(Thread.currentThread().getId());
		sb.append(" ");
		sb.append(Thread.currentThread().getName());
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		final int INDEX = 3;  //skip elements to real trace point
		if (s.length > INDEX) {
			sb.append(" ");
			sb.append(s[INDEX].getFileName());
			sb.append("/");
			sb.append(s[INDEX].getLineNumber());
			sb.append(" ");
			sb.append(s[INDEX].getClassName());
			sb.append(".");
			sb.append(s[INDEX].getMethodName());
		}
		d(sb.toString());
	}

	public static void trace(String msg) {
		StringBuilder sb = new StringBuilder("TRACE");
		sb.append(" ");
		sb.append(android.os.Process.myPid());
		sb.append("/");
		sb.append(Thread.currentThread().getId());
		sb.append(" ");
		sb.append(Thread.currentThread().getName());
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		final int INDEX = 3;  //skip elements to real trace point
		if (s.length > INDEX) {
			sb.append(" ");
			sb.append(s[INDEX].getFileName());
			sb.append("/");
			sb.append(s[INDEX].getLineNumber());
			sb.append(" ");
			sb.append(s[INDEX].getClassName());
			sb.append(".");
			sb.append(s[INDEX].getMethodName());
		}
		sb.append( " - ");
		sb.append(msg);
		d(sb.toString());
	}

	private static String t() {
		StringBuilder sb = new StringBuilder();
		sb.append(Thread.currentThread().getId());
		sb.append(" ");
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		final int INDEX = 4;  //skip elements to real trace point
		if (s.length > INDEX) {
			sb.append(s[INDEX].getFileName());
			sb.append("/");
			sb.append(s[INDEX].getLineNumber());
			sb.append(" ");
		}
		return sb.toString();
	}

	public static void tv(String msg) {
		v(t() + msg);
	}

	public static void td(String msg) {
		d(t() + msg);
	}

	public static void ti(String msg) {
		i(t() + msg);
	}

	public static void tw(String msg) {
		w(t() + msg);
	}

	public static void tw2(String msg) {
		w2(t() + msg);
	}

	public static void te(String msg) {
		e(t() + msg);
	}

	public static void te2(String msg) {
		e2(t() + msg);
	}

	public static void tipD(String msg) {
		d("UITIP: " + msg);
	}
	public static void tipE(String msg) {
		e("UITIP:ERROR: " + msg);
	}
	public static void printStack(String msg) {
		new Exception(mTag + ": " + msg).printStackTrace();
	}
	public static void thread_d(String msg) {
		d(msg + ": thread id " + Thread.currentThread().getId());
	}

	public static void printBytes(@NonNull  byte[] bytes) {
		//instead by foreach
		//for (int i=0; i<bytes.length; i++) {
		for (byte b : bytes) {
			System.out.print(b + " ");
		}
	}
}
