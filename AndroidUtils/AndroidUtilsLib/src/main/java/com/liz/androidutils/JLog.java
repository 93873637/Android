package com.liz.androidutils;

/**
 * An android based log extension class
 * No any dependencies, you can copy and use directly on android app.
 * Author: liz
 */

@SuppressWarnings("unused, WeakerAccess")
public class JLog {

	public static void print(String msg) { System.out.print(msg); }
	public static void printLine(String msg) { System.out.println(msg); }
	public static void newLine() {
		System.out.println("\n");
	}

	public static void v(String msg) { System.out.println("[V] " + msg); }
	public static void d(String msg) { System.out.println("[D] " + msg); }
	public static void i(String msg) { System.out.println("[I] " + msg); }
	public static void w(String msg) { System.out.println("[W] " + msg); }
	public static void e(String msg) { System.out.println("[E] " + msg); }

	public static void tv(String msg) { System.out.println("[V] " + _t() + " - " + msg); }
	public static void td(String msg) { System.out.println("[D] " + _t() + " - " + msg); }
	public static void ti(String msg) { System.out.println("[I] " + _t() + " - " + msg); }
	public static void tw(String msg) { System.out.println("[W] " + _t() + " - " + msg); }
	public static void te(String msg) { System.out.println("[E] " + _t() + " - " + msg); }

	public static void trace() { System.out.println("[T] " + _t()); }
	public static void trace(String msg) { System.out.println("[T] " + _t() + " - " + msg); }

	/**
	 * @return trace string embedded in v,d,i,w,e
	 */
	private static String _t() {
		StringBuilder sb = new StringBuilder();
		sb.append(Thread.currentThread().getId());
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		final int INDEX = 3;  //skip elements to real trace point
		if (s.length > INDEX) {
			sb.append(" ");
			sb.append(s[INDEX].getFileName());
			sb.append(":");
			sb.append(s[INDEX].getLineNumber());
			sb.append(":");
			sb.append(s[INDEX].getMethodName());
		}
		return sb.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// TEST MAIN

	public static void main(String[] args) {
		JLog.print("\n");
		JLog.printLine("***Test Begin...");

		JLog.newLine();
		JLog.v("this is test_item message");
		JLog.d("this is test_item message");
		JLog.i("this is test_item message");
		JLog.w("this is test_item message");
		JLog.e("this is test_item message");

		JLog.newLine();
		JLog.trace();
		JLog.trace("this is test_item message");

		JLog.newLine();
		JLog.tv("this is test_item message");
		JLog.td("this is test_item message");
		JLog.ti("this is test_item message");
		JLog.tw("this is test_item message");
		JLog.te("this is test_item message");

		JLog.newLine();
		JLog.printLine("***Test Successfully.");
	}
}
