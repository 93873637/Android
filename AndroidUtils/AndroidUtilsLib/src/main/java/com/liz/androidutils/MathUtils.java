package com.liz.androidutils;

public class MathUtils {

    public static double DEGREE_PI = 180.0;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        JLog.d("Test Start...\n");

        AssertUtils.Assert(Math.toRadians(DEGREE_PI) == Math.PI);
        AssertUtils.Assert(Math.toDegrees(Math.PI) == DEGREE_PI);

        JLog.d("Test Successfully.\n");
    }
}
