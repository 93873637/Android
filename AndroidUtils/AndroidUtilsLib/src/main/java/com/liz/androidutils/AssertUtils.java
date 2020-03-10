package com.liz.androidutils;

public class AssertUtils {

    public static void Assert(boolean expression) {
        if (!expression) {
            createAssertException();
        }
    }

    private static void createAssertException() {
        createNullException();
    }

    private static void createNullException() {
        String a = null;
        if (a.equals("foo")) {
            a.isEmpty();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        AssertUtils.Assert(true);
        AssertUtils.Assert(false);
    }
}
