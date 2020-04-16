package com.liz.androidutils;

/**
 * An android based log extension class
 * No any dependencies, you can copy and use directly on android app.
 * Author: liz
 */

@SuppressWarnings("unused, WeakerAccess")
public class JLog {

	public static void p(String msg) { System.out.print(msg); }
	public static void pl(String msg) { System.out.println(msg); }

	public static void v(String msg) { System.out.println("VERBOSE: " + msg); }
	public static void d(String msg) { System.out.println("DEBUG: " + msg); }
	public static void i(String msg) { System.out.println("INFO: " + msg); }
	public static void w(String msg) { System.out.println("WARNING: " + msg); }
	public static void e(String msg) { System.out.println("ERROR: " + msg); }

	public static void tv(String msg) { System.out.println("VERBOSE: " + t() + " - " + msg); }
	public static void td(String msg) { System.out.println("DEBUG: " + t() + " - " + msg); }
	public static void ti(String msg) { System.out.println("INFO: " + t() + " - " + msg); }
	public static void tw(String msg) { System.out.println("WARNING: " + t() + " - " + msg); }
	public static void te(String msg) { System.out.println("ERROR: " + t() + " - " + msg); }

	public static void trace() {
		StringBuilder sb = new StringBuilder("TRACE");
		sb.append(" ");
		sb.append(Thread.currentThread().getId());
		sb.append("/");
		sb.append(Thread.currentThread().getName());
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		final int INDEX = 2;  //skip elements to real trace point
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
		System.out.println(sb.toString());
	}

	public static void trace(String msg) {
		StringBuilder sb = new StringBuilder("TRACE");
		sb.append(" ");
		sb.append(Thread.currentThread().getId());
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		final int INDEX = 2;  //skip elements to real trace point
		if (s.length > INDEX) {
			sb.append(" ");
			sb.append(s[INDEX].getFileName());
			sb.append("/");
			sb.append(s[INDEX].getLineNumber());
			sb.append(" ");
			sb.append(s[INDEX].getMethodName());
		}
		sb.append(": ");
		sb.append(msg);
		System.out.println(sb.toString());
	}

	private static String t() {
		StringBuilder sb = new StringBuilder();
		sb.append(Thread.currentThread().getId());
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		final int INDEX = 3;  //skip elements to real trace point
		if (s.length > INDEX) {
			sb.append("/");
			sb.append(s[INDEX].getFileName());
			sb.append("/");
			sb.append(s[INDEX].getLineNumber());
			sb.append("/");
			sb.append(s[INDEX].getMethodName());
		}
		return sb.toString();
	}

	public static void newLine() {
		System.out.println("\n");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// TEST MAIN

	public static void main(String[] args) {
		System.out.println("\n");
		JLog.i("***Test Begin...");

		JLog.v("this is test message");
		JLog.d("this is test message");
		JLog.i("this is test message");
		JLog.w("this is test message");
		JLog.e("this is test message");

		JLog.trace();
		JLog.trace("this is test message");

		JLog.tv("this is test message");
		JLog.td("this is test message");
		JLog.ti("this is test message");
		JLog.tw("this is test message");
		JLog.te("this is test message");

		JLog.i("***Test Successfully.");
		System.out.println("\n");
	}
}
