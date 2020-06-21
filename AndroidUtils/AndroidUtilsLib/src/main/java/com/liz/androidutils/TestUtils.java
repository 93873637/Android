package com.liz.androidutils;

public class TestUtils {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TEST MAIN

    public static void main(String[] args) {

        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String tag = s[1].getClassName() + " " + s[1].getFileName() + "/" + s[1].getLineNumber();
        System.out.println("\n" + tag + ": Test Start...");

        //todo: write test_item codes here...

        System.out.println("\n" + tag + ": Test Successfully.");
    }
}
