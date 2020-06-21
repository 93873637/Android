package com.liz.androidutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JavaUtils {

    public static void execMethod(String pkgName, String clsName, String methodName, Object[] params) {
//        try {
//        Class<?> cls = Class.forName("com.liz.androidutils.NumUtils");
//        Method method = cls.getMethod("formatShow", Integer.TYPE);
//        AssertUtils.Assert(((String)(method.invoke(cls, 123))).equals("1234"));
//    } catch (Exception ex) {
//        JLog.te(ex.toString());
//        // LogUtils.e("SysUtils:getSystemProperty:Exception: " + ex.toString());
//    }
    }


    public static void eval(String javaStr) throws Exception {
        StringBuffer str = new StringBuffer();
        str.append("public class Eval {").append("public static void main(String[] args) {").append(javaStr).append("}}");
        OutputStream out = new FileOutputStream("Eval.java");
        out.write(str.toString().getBytes("gbk"));
        out.close();

        Process javacProcess = Runtime.getRuntime().exec("javac Eval.java");
        InputStream compileError = javacProcess.getErrorStream();
        System.err.println(read(compileError));
        compileError.close();

        Process javaProcess = Runtime.getRuntime().exec("java Eval");
        InputStream err = javaProcess.getErrorStream();
        System.err.println(read(err));
        err.close();

        InputStream success = javaProcess.getInputStream();
        System.out.println(read(success));
        success.close();

        new File("Eval.java").delete();
        new File("Eval.class").delete();
    }

    /**
     * Runtime.getRuntime().exec("javac -classpath D:\\WorkSpace\\Android\\AndroidUtils\\AndroidUtilsLib\\build\\intermediates\\javac\\debug\\compileDebugJavaWithJavac\\classes Eval.java");
     * Runtime.getRuntime().exec("java -classpath .\\;D:\\WorkSpace\\Android\\AndroidUtils\\AndroidUtilsLib\\build\\intermediates\\javac\\debug\\compileDebugJavaWithJavac\\classes Eval");
     *
     * @param imports:    note: end with ';'
     * @param javaStr:
     * @param classPaths:
     */
    public static void eval(String imports, String javaStr, String classPaths) {
        try {
            StringBuffer javaFileStr = new StringBuffer();
            javaFileStr.append(imports);
            javaFileStr.append("public class Eval {").append("public static void main(String[] args) {").append(javaStr).append("}}");
            OutputStream out = new FileOutputStream("Eval.java");
            out.write(javaFileStr.toString().getBytes("gbk"));
            out.close();

            StringBuffer javacCmdStr = new StringBuffer();
            javacCmdStr.append("javac -classpath ");
            javacCmdStr.append(classPaths);
            javacCmdStr.append(" Eval.java");
            Process javacProcess = Runtime.getRuntime().exec(javacCmdStr.toString());
            InputStream compileError = javacProcess.getErrorStream();
            System.err.println(read(compileError));
            compileError.close();

            StringBuffer execCmdStr = new StringBuffer();
            execCmdStr.append("java -classpath .\\;");
            execCmdStr.append(classPaths);
            execCmdStr.append(" Eval");
            Process javaProcess = Runtime.getRuntime().exec(execCmdStr.toString());
            InputStream err = javaProcess.getErrorStream();
            System.err.println(read(err));
            err.close();

            InputStream success = javaProcess.getInputStream();
            System.out.println(read(success));
            success.close();

            new File("Eval.java").delete();
            new File("Eval.class").delete();
        } catch (Exception ex) {
            JLog.te("eval failed, ex = " + ex.toString());
        }
    }

    /**
     * @param javaStr:    note: add full path for class you using
     * @param classPaths:
     */
    public static void eval(String javaStr, String classPaths) {
        try {
            StringBuffer javaFileStr = new StringBuffer();
            javaFileStr.append("public class Eval {").append("public static void main(String[] args) {").append(javaStr).append("}}");
            OutputStream out = new FileOutputStream("Eval.java");
            out.write(javaFileStr.toString().getBytes("gbk"));
            out.close();

            StringBuffer javacCmdStr = new StringBuffer();
            javacCmdStr.append("javac -classpath ");
            javacCmdStr.append(classPaths);
            javacCmdStr.append(" Eval.java");
            Process javacProcess = Runtime.getRuntime().exec(javacCmdStr.toString());
            InputStream compileError = javacProcess.getErrorStream();
            System.err.println("compileError = \"" + read(compileError) + "\"");
            compileError.close();

            StringBuffer execCmdStr = new StringBuffer();
            execCmdStr.append("java -classpath .\\;");
            execCmdStr.append(classPaths);
            execCmdStr.append(" Eval");
            Process javaProcess = Runtime.getRuntime().exec(execCmdStr.toString());
            InputStream execErr = javaProcess.getErrorStream();
            System.err.println("execErr = \"" + read(execErr) + "\"");
            execErr.close();

            InputStream success = javaProcess.getInputStream();
            System.out.println(read(success));
            success.close();

            new File("Eval.java").delete();
            new File("Eval.class").delete();
        } catch (Exception ex) {
            JLog.te("eval failed, ex = " + ex.toString());
        }
    }

    public static String strEval(String expressStr, String classPaths) {
        try {
            StringBuffer javaFileStr = new StringBuffer();
            javaFileStr.append("public class Eval {")
                    .append("public static void main(String[] args) {")
                    .append("System.out.print(" + expressStr + ");")
                    .append("}}");
            OutputStream out = new FileOutputStream("Eval.java");
            out.write(javaFileStr.toString().getBytes("gbk"));
            out.close();

            StringBuffer javacCmdStr = new StringBuffer();
            javacCmdStr.append("javac -classpath ");
            javacCmdStr.append(classPaths);
            javacCmdStr.append(" Eval.java");
            Process javacProcess = Runtime.getRuntime().exec(javacCmdStr.toString());
            InputStream compileError = javacProcess.getErrorStream();
            String compileErrorStr = read(compileError);
            compileError.close();
            if (!compileErrorStr.isEmpty()) {
                JLog.te("eval failed, compileErrorStr = " + compileErrorStr);
                return null;
            }

            StringBuffer execCmdStr = new StringBuffer();
            execCmdStr.append("java -classpath .\\;");
            execCmdStr.append(classPaths);
            execCmdStr.append(" Eval");
            Process javaProcess = Runtime.getRuntime().exec(execCmdStr.toString());
            InputStream execErr = javaProcess.getErrorStream();
            String execErrStr = read(execErr);
            execErr.close();
            if (!execErrStr.isEmpty()) {
                JLog.te("eval failed, execErrStr = " + execErrStr);
                return null;
            }

            InputStream success = javaProcess.getInputStream();
            String retStr = read(success);
            success.close();
            return retStr;
        } catch (Exception ex) {
            JLog.te("eval failed, ex = " + ex.toString());
            return null;
        } finally {
            new File("Eval.java").delete();
            new File("Eval.class").delete();
        }
    }

    private static String read(InputStream in) throws IOException {
        byte[] b = new byte[1024];
        int len = -1;
        StringBuffer str = new StringBuffer();
        while ((len = in.read(b)) != -1) {
            str.append(new String(b, 0, len, "gbk"));
        }
        return str.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // test main func

    public static void main(String[] args) throws Exception {
//        eval("int a = 1; int b = 2; System.out.println(a+b);");
//        eval("import com.liz.androidutils.NumUtils;",
//                "System.out.println(NumUtils.formatShow(12345));",
//                "D:\\WorkSpace\\Android\\AndroidUtils\\AndroidUtilsLib\\build\\intermediates\\javac\\debug\\compileDebugJavaWithJavac\\classes");
//        eval("System.out.println(com.liz.androidutils.NumUtils.formatShow(12345));",
//                "D:\\WorkSpace\\Android\\AndroidUtils\\AndroidUtilsLib\\build\\intermediates\\javac\\debug\\compileDebugJavaWithJavac\\classes");
        System.out.println(
                strEval("com.liz.androidutils.NumUtils.formatShow(12345)",
                "D:\\WorkSpace\\Android\\AndroidUtils\\AndroidUtilsLib\\build\\intermediates\\javac\\debug\\compileDebugJavaWithJavac\\classes")
        );
    }
}
