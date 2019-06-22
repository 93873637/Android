package com.liz.screenhelper.utils;

import java.text.SimpleDateFormat;

public class TimeUtils {

    //11-03 17:36:55.626
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
}
