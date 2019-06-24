package com.liz.testcamera.utils;

/**
 * StrUtils:
 * Created by liz on 2019/1/7.
 */
public class StrUtils {
    public static int[] str2IntArr(String str, String delimit) {
        if (str == null || str.isEmpty() || str.trim().equals("")) {
            return null;
        }
        if (delimit == null || delimit.isEmpty() || delimit.trim().equals("")) {
            return null;
        }
        int[] intArr;
        String[] strArr = str.split(delimit);
        intArr = new int[strArr.length];
        for (int i=0; i<strArr.length; i++) {
            intArr[i] = Integer.parseInt(strArr[i]);
        }
        return intArr;
    }
}
