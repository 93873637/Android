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
//			if (mSaveToFile) {
//				saveToFile('V', mTag, msg);
//			}
		}
	}

	public static void d(String msg) {
		if (mLevel <= LOG_LEVEL_D) {
			Log.d(mTag, msg);
			if (mLogListener != null) {
				mLogListener.onCBLog(msg, LOG_LEVEL_D);
			}
//			if (mSaveToFile) {
//				saveToFile('D', mTag, msg);
//			}
		}
	}

	public static void i(String msg) {
		if (mLevel <= LOG_LEVEL_I) {
			Log.i(mTag, msg);
			if (mLogListener != null) {
				mLogListener.onCBLog(msg, LOG_LEVEL_I);
			}
//			if (mSaveToFile) {
//				saveToFile('I', mTag, msg);
//			}
		}
	}

	public static void w(String msg) {
		if (mLevel <= LOG_LEVEL_W) {
			Log.w(mTag, msg);
			if (mLogListener != null) {
				mLogListener.onCBLog(msg, LOG_LEVEL_W);
			}
//			if (mSaveToFile) {
//				saveToFile('W', mTag, msg);
//			}
		}
	}

	public static void e(String msg) {
		//since e is top level, not need to judge, direct show
		Log.e(mTag, msg);
		if (mLogListener != null) {
			mLogListener.onCBLog(msg, LOG_LEVEL_E);
		}
//		if (mSaveToFile) {
//			saveToFile('E', mTag, msg);
//		}
	}

	public static void v(Object obj, String msg) { Log.v(mTag, obj.getClass().getName() + ":" + msg); }
	public static void d(Object obj, String msg) { Log.d(mTag, obj.getClass().getName() + ":" + msg); 	}
	public static void i(Object obj, String msg) { Log.i(mTag, obj.getClass().getName() + ":" + msg); 	}
	public static void w(Object obj, String msg) { Log.w(mTag, obj.getClass().getName() + ":" + msg);	}
	public static void e(Object obj, String msg) { Log.e(mTag, obj.getClass().getName() + ":" + msg);	}

	// this can only put codes in place to take effect
	public static void d2(String msg) {
		String className = Thread.currentThread().getStackTrace()[1].getClassName();
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		d(className + "." + methodName + ":" + msg);
	}

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

	private static String t() {
		StringBuilder sb = new StringBuilder();
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

	public static void te(String msg) {
		e(t() + msg);
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

//	///////////////////////////////////////////////////////////////////////////////////////////////
//	//log to file
//
//	private static final int DEFAULT_MAX_LOG_FILE_NUM = 5;
//	private static final long DEFAULT_MAX_LOG_FILE_SIZE = 2*1024*1024;  //unit by bytes
//
//	private static boolean mSaveToFile = false;
//	private static String mLogDir = "";
//	private static String mLogFileName = "";
//	private static int mMaxLogFileNum = DEFAULT_MAX_LOG_FILE_NUM;
//	private static long mMaxLogFileSize = DEFAULT_MAX_LOG_FILE_SIZE;
//	private static ArrayList<String> mLogFileList = new ArrayList<>();
//
//	public static void setSaveToFile(boolean save) {
//		mSaveToFile = save;
//	}
//
//	public static void setLogDir(String dir) {
//		mLogDir = FileUtils.formatDirSeparator(dir);
//	}
//
//	public static void setMaxLogFileNum(int maxNum) {
//		mMaxLogFileNum = maxNum;
//	}
//
//	public static void setMaxLogFileSize(long maxSize) {
//		mMaxLogFileSize = maxSize;
//	}
//
//	private static String getLogFilePath() {
//		return mLogDir + mLogFileName;
//	}
//
//	private static void genLogFileName() {
//		//SimpleDateFormat format = new SimpleDateFormat("yyMMdd_HHmmss");
//		//String strDateTime = format.format(new Date(System.currentTimeMillis()));
//		mLogFileName = "log_" + System.currentTimeMillis() + ".txt";
//		Log.d(mTag, "genLogFileName: " + mLogFileName);
//	}
//
//	private static void createLogFile() {
//		genLogFileName();
//		String filePath = getLogFilePath();
//		mLogFileList.add(filePath);
//		Log.d(mTag, "createLogFile: " + filePath + ", size = " + mLogFileList.size());
//	}
//
//	//remove earliest one when queue full
//	private static void removeLogFile() {
//		if (mLogFileList.size() >= mMaxLogFileNum) {
//			String filePath = mLogFileList.get(0);
//			FileUtils.delete(filePath);
//			mLogFileList.remove(0);
//			Log.d(mTag, "removeLogFile: " + filePath + ", size = " + mLogFileList.size());
//		}
//	}
//
//	private static void saveToFile(char type, String tag, String msg) {
//		if (TextUtils.isEmpty(mLogDir)) {
//			Log.e(mTag, "ERROR: log file path empty");
//			return;
//		}
//
//		File filePath = new File(mLogDir);
//		if (!filePath.exists()) {
//			if (!filePath.mkdirs()) {
//				Log.e(mTag, "ERROR: failed to make log file path " + mLogDir);
//				return;
//			}
//		}
//
//		if (TextUtils.isEmpty(mLogFileName)) {
//			createLogFile();
//		}
//
//		//get and check log file
//		String logFilePath = getLogFilePath();
//		if (FileUtils.getSingleFileSize(logFilePath) >= mMaxLogFileSize) {
//			removeLogFile();
//			createLogFile();
//			logFilePath = getLogFilePath();
//		}
//
//		//01-04 11:37:58.499 E/AndroidRuntime(23013): FATAL EXCEPTION: Thread-12
//		String logInfo = TimeUtils.getLogTime() + " " + type + "/" + tag + ": " + msg + "\n";
//
//		FileOutputStream fos;
//		BufferedWriter bw = null;
//		try {
//			fos = new FileOutputStream(logFilePath, true);
//			bw = new BufferedWriter(new OutputStreamWriter(fos));
//			bw.write(logInfo);
//		} catch (Exception e) {
//			Log.e(mTag, "ERROR: saveToFile: write exception " + e.toString());
//			e.printStackTrace();
//		} finally {
//			try {
//				if (bw != null) {
//					bw.close();
//				}
//			} catch (IOException e) {
//				Log.e(mTag, "ERROR: saveToFile: close exception " + e.toString());
//				e.printStackTrace();
//			}
//		}
//	}
//
//	//log to file
//	///////////////////////////////////////////////////////////////////////////////////////////////
}
