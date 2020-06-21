package com.liz.androidutils;

public class AssertUtils {

    public static final String CLASS_PATHS = "D:\\WorkSpace\\Android\\AndroidUtils\\AndroidUtilsLib\\build\\intermediates\\javac\\debug\\compileDebugJavaWithJavac\\classes";

    public static void Assert(boolean expression) {
        if (!expression) {
            createAssertException();
        }
    }

    public static void assertEquals(String expression, String resultExpected) {
        String retStr = JavaUtils.strEval(expression, CLASS_PATHS);
        System.out.print(expression + " = \"" + retStr + "\"");
        if (retStr == null || !retStr.equals(resultExpected)) {
            System.out.println(", EXPECT \"" + resultExpected + "\" --FAILED");
            createAssertException();
        }
        System.out.println(" --OK");
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

    public static String TEST_RETURN_EXPECTED = "this is a test return";
    public static String TEST_RETURN_UNEXPECTED = "this is NOT a test return";

    public static void main(String[] args) {
        //AssertUtils.Assert(true);
        //AssertUtils.Assert(false);
        AssertUtils.assertEquals("com.liz.androidutils.AssertUtils.returnStringTestFunc()", TEST_RETURN_EXPECTED);
        AssertUtils.assertEquals("com.liz.androidutils.AssertUtils.returnStringTestFunc()", TEST_RETURN_UNEXPECTED);
    }

    public static String returnStringTestFunc() {
        return TEST_RETURN_EXPECTED;
    }
}
