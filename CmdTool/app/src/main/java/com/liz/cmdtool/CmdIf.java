package com.liz.cmdtool;

/**
 * CmdIf.java
 * Created by yang on 16-9-9.
 */
public class CmdIf {
    static {
        System.loadLibrary("CmdIf");
    }

    public static native String runCmd(String str);
}
