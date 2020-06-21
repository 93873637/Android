package com.liz.androidutils;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("unused")
public class TimeUtils {

    public static String currentTime() {
        return currentTime(false);
    }

    public static String currentTime(boolean withMS) {
        return formatTime(System.currentTimeMillis(), withMS);
    }

    public static String formatTime(long timeMillis) {
        return formatTime(timeMillis, false);
    }

    public static String formatTime(long timeMillis, boolean withMS) {
        String strTime = new SimpleDateFormat("HH:mm:ss").format(new Date(timeMillis));
        if (withMS) {
            long ms = timeMillis % 1000;
            if (ms < 10)
                strTime += ".00" + ms;
            else if (ms < 100)
                strTime += ".0" + ms;
            else
                strTime += "." + ms;
        }
        return strTime;
    }

    /**
     * @return String of log time format as 11-03 17:36:55.626
     */
    public static String getLogTime() {
        String strLogTime = new SimpleDateFormat("MM-dd HH:mm:ss").format(new java.util.Date());
        long currentTimeMillis = System.currentTimeMillis();
        long ms = currentTimeMillis % 1000;
        if (ms < 10)
            strLogTime += ".00" + ms;
        else if (ms < 100)
            strLogTime += ".0" + ms;
        else
            strLogTime += "." + ms;
        return strLogTime;
    }

    /**
     * @return String of file name format as 191103.173655.626
     */
    public static String getFileTime() {
        String strFileTime = new SimpleDateFormat("yyMMdd.HHmmss").format(new java.util.Date());
        long currentTimeMillis = System.currentTimeMillis();
        long ms = currentTimeMillis % 1000;
        if (ms < 10)
            strFileTime += ".00" + ms;
        else if (ms < 100)
            strFileTime += ".0" + ms;
        else
            strFileTime += "." + ms;
        return strFileTime;
    }

    /**
     * @param withMS: append ms if true
     * @return String of file name format as 191103.173655.626
     */
    public static String getFileTime(boolean withMS) {
        String strFileTime = new SimpleDateFormat("yyMMdd.HHmmss").format(new java.util.Date());
        if (withMS) {
            long currentTimeMillis = System.currentTimeMillis();
            long ms = currentTimeMillis % 1000;
            if (ms < 10)
                strFileTime += ".00" + ms;
            else if (ms < 100)
                strFileTime += ".0" + ms;
            else
                strFileTime += "." + ms;
        }
        return strFileTime;
    }

    /**
     * @return String of time format as 19:11:03
     */
    public static String elapsed(long startTime) {
        long timeDiff = System.currentTimeMillis() - startTime;
        int seconds = (int) timeDiff / 1000;
        int hour = seconds / 3600;
        int minute = (seconds % 3600) / 60;
        int second = seconds % 60;
        String hs = (hour >= 10 ? "" : "0") + hour;
        String ms = (minute >= 10 ? "" : "0") + minute;
        String ss = (second >= 10 ? "" : "0") + second;
        return hs + ":" + ms + ":" + ss;
    }

    /**
     * formatDuration: return formatted string by given milliseconds
     *
     * @param duration: time duration, unit by millisecond
     * @return formatted time string:
     * 189:23:56 -- 189 hours 23 minutes 56 seconds
     * 16:23:56 -- 16 hours 23 minutes 56 seconds
     * 1:23:56 -- 1 hour 23 minutes 56 seconds
     * 23:56 -- 23 minutes 56 seconds
     * 9.678 -- 9.678 seconds
     * 678 -- 678 milliseconds
     * So there are total four formats, separated by HOUR, 10s, 1s
     */
    public static String formatDuration(int duration) {

        final int SECOND = 1000;  // unit by ms
        final int MINUTE = SECOND * 60;
        final int HOUR = MINUTE * 60;
        final int TEN_SECONDS = SECOND * 10;

        int hh = duration / HOUR;
        int mm = duration % HOUR / MINUTE;
        int ss = duration % MINUTE / SECOND;
        int ms = duration % SECOND;

        String hhs = "" + hh;
        String mms = NumUtils.zeroPadding(mm, 2);
        String sss = NumUtils.zeroPadding(ss, 2);
        String mss = NumUtils.zeroPadding(ms, 3);

        if (duration >= HOUR) {
            return hhs + ":" + mms + ":" + sss;
        } else if (duration >= TEN_SECONDS) {
            return mms + ":" + sss;
        } else if (duration >= SECOND) {
            return ss + "." + mss;
        } else if (duration > 0) {
            return "" + ms;
        } else {
            return "0";
        }
    }

    /**
     * formatDuration: return formatted string (hh:MM:ss.mmm) by given milliseconds
     *
     * @param duration: time duration, unit by millisecond
     * @return time string format as hh:MM:ss.mmm
     */
    public static String formatDurationFull(int duration) {

        final int SECOND = 1000;  // unit by ms
        final int MINUTE = SECOND * 60;
        final int HOUR = MINUTE * 60;

        int hh = duration / HOUR;
        int mm = duration % HOUR / MINUTE;
        int ss = duration % MINUTE / SECOND;
        int ms = duration % SECOND;

        String hhs = NumUtils.zeroPadding(hh, 2);
        String mms = NumUtils.zeroPadding(mm, 2);
        String sss = NumUtils.zeroPadding(ss, 2);
        String mss = NumUtils.zeroPadding(ms, 3);

        return hhs + ":" + mms + ":" + sss + "." + mss;
    }

    public static String formatDuration(long duration) {
        return formatDuration(duration, false);
    }

    public static String formatDuration(long duration, boolean withMS) {

        final long SECOND = 1000;  // unit by ms
        final long MINUTE = SECOND * 60;
        final long HOUR = MINUTE * 60;

        long hh = duration / HOUR;
        long mm = duration % HOUR / MINUTE;
        long ss = duration % MINUTE / SECOND;

        String hhs = NumUtils.zeroPadding(hh, 2);
        String mms = NumUtils.zeroPadding(mm, 2);
        String sss = NumUtils.zeroPadding(ss, 2);

        String timeString = hhs + ":" + mms + ":" + sss;

        if (withMS) {
            long ms = duration % SECOND;
            String mss = NumUtils.zeroPadding(ms, 3);
            timeString += "." + mss;
        }

        return timeString;
    }

    /**
     * @return String of file name format as 191103.173655.626
     */
    public static String genTimeID() {
        return "" + System.currentTimeMillis();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {

        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String tag = s[1].getClassName() + " " + s[1].getFileName() + "/" + s[1].getLineNumber();
        System.out.println("\n" + tag + ": Test Start...");

        AssertUtils.Assert(formatDuration(-1).equals("0"));
        AssertUtils.Assert(formatDuration(0).equals("0"));
        AssertUtils.Assert(formatDuration(1).equals("1"));
        AssertUtils.Assert(formatDuration(9).equals("9"));
        AssertUtils.Assert(formatDuration(10).equals("10"));
        AssertUtils.Assert(formatDuration(100).equals("100"));
        AssertUtils.Assert(formatDuration(999).equals("999"));
        AssertUtils.Assert(formatDuration(1000).equals("1.000"));
        AssertUtils.Assert(formatDuration(1001).equals("1.001"));
        AssertUtils.Assert(formatDuration(1010).equals("1.010"));
        AssertUtils.Assert(formatDuration(1100).equals("1.100"));
        AssertUtils.Assert(formatDuration(1056).equals("1.056"));
        AssertUtils.Assert(formatDuration(1234).equals("1.234"));
        AssertUtils.Assert(formatDuration(9000).equals("9.000"));
        AssertUtils.Assert(formatDuration(10 * 1000).equals("00:10"));
        AssertUtils.Assert(formatDuration(59 * 1000).equals("00:59"));
        AssertUtils.Assert(formatDuration(60 * 1000).equals("01:00"));
        AssertUtils.Assert(formatDuration((59 * 60 + 59) * 1000).equals("59:59"));
        AssertUtils.Assert(formatDuration(3600 * 1000).equals("1:00:00"));
        AssertUtils.Assert(formatDuration((23 * 3600 + 59 * 60 + 59) * 1000).equals("23:59:59"));
        AssertUtils.Assert(formatDuration((123 * 3600 + 59 * 60 + 59) * 1000).equals("123:59:59"));

//        long start = System.currentTimeMillis();
//        for (int i=0; i<20; i++) {
//            try {
//                Thread.sleep(1234);
//            } catch (Exception e) {
//
//            }
//            System.out.println(elapsed(start));
//        }
////        for (int i=0; i<12; i++) {
////            System.out.println(genTimeID());
////        }
        System.out.println("\n" + tag + ": Test Successfully.");
    }
}
