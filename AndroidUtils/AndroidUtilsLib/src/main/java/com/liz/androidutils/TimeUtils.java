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
     * @return String of file name format as 191103.173655.626
     */
    public static String genTimeID() {
        return "" + System.currentTimeMillis();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        for (int i=0; i<12; i++) {
            System.out.println(genTimeID());
        }
    }
}
