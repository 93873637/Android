package com.liz.androidutils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


/**
 * LogEx: Extend class from LogUtils, Add features:
 * * Save Log to File
 * Dependencies: LogUtils, FileUtils, TimeUtils
 * Author: liz
 */

@SuppressWarnings("unused")
public class LogEx extends LogUtils {

	public static void v(String msg) {
		LogUtils.v(msg);
		if (getLevel() == LOG_LEVEL_V) {
			if (mSaveToFile) {
				saveToFile('V', getTag(), msg);
			}
		}
	}

	public static void d(String msg) {
		LogUtils.d(msg);
		if (getLevel() <= LOG_LEVEL_D) {
			if (mSaveToFile) {
				saveToFile('D', getTag(), msg);
			}
		}
	}

	public static void i(String msg) {
		LogUtils.i(msg);
		if (getLevel() <= LOG_LEVEL_I) {
			if (mSaveToFile) {
				saveToFile('I', getTag(), msg);
			}
		}
	}

	public static void w(String msg) {
		LogUtils.w(msg);
		if (getLevel() <= LOG_LEVEL_W) {
			if (mSaveToFile) {
				saveToFile('W', getTag(), msg);
			}
		}
	}

	public static void e(String msg) {
		LogUtils.e(msg);

		//since e is top level, not need to judge, direct show
		if (mSaveToFile) {
			saveToFile('E', getTag(), msg);
		}
	}

	public static void setSaveToFile(boolean save) {
		mSaveToFile = save;
	}

	public static void setLogDir(String dir) {
		if (TextUtils.isEmpty(dir)) {
			mLogDir = DEFAULT_LOG_DIR;
		}
		else {
			mLogDir = FileUtils.touchSeparator(dir);
		}
	}

	public static void setLogFilePrefix(String prefix) {
		if (TextUtils.isEmpty(prefix)) {
			mLogFilePrefix = DEFAULT_LOG_FILE_PREFIX;
		}
		else {
			mLogFilePrefix = prefix;
		}
	}

	public static void setMaxLogFileNum(int maxNum) {
		mMaxLogFileNum = maxNum;
	}

	public static void setMaxLogFileSize(long maxSize) {
		mMaxLogFileSize = maxSize;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// log to file

	private static final int DEFAULT_MAX_LOG_FILE_NUM = 5;
	private static final long DEFAULT_MAX_LOG_FILE_SIZE = 2*1024*1024;  // unit by bytes
	@SuppressLint("SdCardPath")
	private static final String DEFAULT_LOG_DIR = "/sdcard/log/";  // using current working dir as default
	private static final String DEFAULT_LOG_FILE_PREFIX = "log";

	private static String mLogDir = DEFAULT_LOG_DIR;
	private static String mLogFilePrefix = DEFAULT_LOG_FILE_PREFIX;
	private static String mLogFileName = "";
	private static boolean mSaveToFile = false;
	private static int mMaxLogFileNum = DEFAULT_MAX_LOG_FILE_NUM;
	private static long mMaxLogFileSize = DEFAULT_MAX_LOG_FILE_SIZE;
	private static ArrayList<String> mLogFileList = new ArrayList<>();

	private static String getLogFileAbsolute() {
		return mLogDir + mLogFileName;
	}

	private static void genLogFileName() {
		mLogFileName = mLogFilePrefix + "_" + TimeUtils.getFileTime(false) + ".txt";
		Log.d(getTag(), "genLogFileName \"" + mLogFileName + "\"");
	}

	private static boolean createLogFile() {
		genLogFileName();
		String filePath = getLogFileAbsolute();
		mLogFileList.add(filePath);
		Log.d(getTag(), "createLogFile: " + filePath + ", list size = " + mLogFileList.size());
		return true;
	}

	//remove oldest log file when file list queue full
	private static void checkLogFileList() {
		if (mLogFileList.size() >= mMaxLogFileNum) {
			String filePath = mLogFileList.get(0);
			FileUtils.delete(filePath);
			mLogFileList.remove(0);
			Log.d(getTag(), "removeLogFile: " + filePath + ", size = " + mLogFileList.size());
		}
	}

	private static long getCurrentLogFileSize() {
		return FileUtils.getFileSize(getLogFileAbsolute());
	}

	private static boolean checkLogFile() {
		// need ensure log dir exists if dir not empty
		if (!TextUtils.isEmpty(mLogDir)) {
			File dir = new File(mLogDir);
			if (!dir.exists()) {
				Log.d(getTag(), "log dir \"" + dir + "\" not exist, create...");
				if (!dir.mkdirs()) {
					Log.e(getTag(), "ERROR: create log dir \"" + dir + "\" failed.");
					return false;
				}
			}
		}

		// we need create log file at the beginning
		if (TextUtils.isEmpty(mLogFileName)) {
			return createLogFile();
		}

		// check if current log file up to max file size
		if (getCurrentLogFileSize() >= mMaxLogFileSize) {
			checkLogFileList();
			return createLogFile();
		}

		return true;
	}

	private static void saveToFile(char type, String tag, String msg) {
		if (!checkLogFile()) {
			Log.e(getTag(), "ERROR: saveToFile: check log file failed.");
			return;
		}

		//
		// log format as:
		// 01-04 11:37:58.499 E/AndroidRuntime(23013): FATAL EXCEPTION: Thread-12
		//
		String logInfo = TimeUtils.getLogTime() + " " + type + "/" + tag + ": " + msg + "\n";

		FileOutputStream fos = null;
		BufferedWriter bw = null;
		try {
			fos = new FileOutputStream(getLogFileAbsolute(), true);
			bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.write(logInfo);
		} catch (Exception e) {
			Log.e(getTag(), "ERROR: saveToFile: write exception " + e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				Log.e(getTag(), "ERROR: saveToFile: close exception " + e.toString());
				e.printStackTrace();
			}
		}
	}

	//log to file
	///////////////////////////////////////////////////////////////////////////////////////////////
}
