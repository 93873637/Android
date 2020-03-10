package com.liz.androidutils;

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

	///////////////////////////////////////////////////////////////////////////////////////////////
	//log to file

	private static final int DEFAULT_MAX_LOG_FILE_NUM = 5;
	private static final long DEFAULT_MAX_LOG_FILE_SIZE = 2*1024*1024;  //unit by bytes

	private static boolean mSaveToFile = false;
	private static String mLogDir = "";
	private static String mLogFileName = "";
	private static int mMaxLogFileNum = DEFAULT_MAX_LOG_FILE_NUM;
	private static long mMaxLogFileSize = DEFAULT_MAX_LOG_FILE_SIZE;
	private static ArrayList<String> mLogFileList = new ArrayList<>();

	public static void setSaveToFile(boolean save) {
		mSaveToFile = save;
	}

	public static void setLogDir(String dir) {
		mLogDir = FileUtils.formatDirSeparator(dir);
	}

	public static void setMaxLogFileNum(int maxNum) {
		mMaxLogFileNum = maxNum;
	}

	public static void setMaxLogFileSize(long maxSize) {
		mMaxLogFileSize = maxSize;
	}

	private static String getLogFilePath() {
		return mLogDir + mLogFileName;
	}

	private static void genLogFileName() {
		//SimpleDateFormat format = new SimpleDateFormat("yyMMdd_HHmmss");
		//String strDateTime = format.format(new Date(System.currentTimeMillis()));
		mLogFileName = "log_" + System.currentTimeMillis() + ".txt";
		Log.d(getTag(), "genLogFileName: " + mLogFileName);
	}

	private static void createLogFile() {
		genLogFileName();
		String filePath = getLogFilePath();
		mLogFileList.add(filePath);
		Log.d(getTag(), "createLogFile: " + filePath + ", size = " + mLogFileList.size());
	}

	//remove earliest one when queue full
	private static void removeLogFile() {
		if (mLogFileList.size() >= mMaxLogFileNum) {
			String filePath = mLogFileList.get(0);
			FileUtils.delete(filePath);
			mLogFileList.remove(0);
			Log.d(getTag(), "removeLogFile: " + filePath + ", size = " + mLogFileList.size());
		}
	}

	private static void saveToFile(char type, String tag, String msg) {
		if (TextUtils.isEmpty(mLogDir)) {
			Log.e(getTag(), "ERROR: log file path empty");
			return;
		}

		File filePath = new File(mLogDir);
		if (!filePath.exists()) {
			if (!filePath.mkdirs()) {
				Log.e(getTag(), "ERROR: failed to make log file path " + mLogDir);
				return;
			}
		}

		if (TextUtils.isEmpty(mLogFileName)) {
			createLogFile();
		}

		//get and check log file
		String logFilePath = getLogFilePath();
		if (FileUtils.getFileSize(logFilePath) >= mMaxLogFileSize) {
			removeLogFile();
			createLogFile();
			logFilePath = getLogFilePath();
		}

		//01-04 11:37:58.499 E/AndroidRuntime(23013): FATAL EXCEPTION: Thread-12
		String logInfo = TimeUtils.getLogTime() + " " + type + "/" + tag + ": " + msg + "\n";

		FileOutputStream fos;
		BufferedWriter bw = null;
		try {
			fos = new FileOutputStream(logFilePath, true);
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
			} catch (IOException e) {
				Log.e(getTag(), "ERROR: saveToFile: close exception " + e.toString());
				e.printStackTrace();
			}
		}
	}

	//log to file
	///////////////////////////////////////////////////////////////////////////////////////////////
}
