package com.liz.androidutils;

import java.text.SimpleDateFormat;

@SuppressWarnings("unused")
public class TimeUtils {

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
        int seconds = (int)timeDiff / 1000;
        int hour = seconds / 3600;
        int minute = (seconds % 3600) / 60;
        int second = seconds % 60;
        String hs = (hour > 10?"":"0") + hour;
        String ms = (minute > 10?"":"0") + minute;
        String ss = (second > 10?"":"0") + second;
        return hs + ":" + ms + ":" + ss;
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
        long start = System.currentTimeMillis();
        for (int i=0; i<20; i++) {
            try {
                Thread.sleep(1234);
            } catch (Exception e) {

            }
            System.out.println(elapsed(start));
        }
//        for (int i=0; i<12; i++) {
//            System.out.println(genTimeID());
//        }
    }
}
