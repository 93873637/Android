package com.liz.puretorch.utils;

/**
 * NumOp:
 * Created by liz on 2019/1/6.
 */

public class NumOp {

    public static String int2hex(int n) {
        StringBuilder sb = new StringBuilder();
        char[] b = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        do {
            sb = sb.append(b[n % 16]);
            n = n / 16;
        } while(n != 0);
        return sb.reverse().toString();
    }
}
