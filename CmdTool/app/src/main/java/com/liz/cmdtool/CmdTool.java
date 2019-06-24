package com.liz.cmdtool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * CmdTool.java
 * Created by admin on 2018/10/3.
 * <p>
 * for example:
 * getprop ro.build.fingerprint
 */

class CmdTool {

    /*
    static String runCmd(String cmdStr) {
        LogUtils.d("CmdTool:runCmd: cmdStr=\"" + cmdStr + "\"");
        return CmdIf.runCmd(cmdStr);
    }
    //*/

    //*
    static String runCmd(String cmdStr) {
        BufferedReader reader;
        String content;
        try {
            Process process = Runtime.getRuntime().exec(cmdStr);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            int read;
            char[] buffer = new char[16384];
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            content = output.toString();
        } catch (Exception e) {
            content = e.toString();
        }
        return content;
    }
    //*/

    /*
    static String runCmd(String cmd) {
        StringBuilder result = new StringBuilder();
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            Process p = Runtime.getRuntime().runCmd("sh");
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line;
            while ((line = dis.readLine()) != null) {
                result.append(line + "\n");
            }
            p.waitFor();
        } catch (Exception e) {
            result.append(e.toString());
            LogUtils.e("runCmd cmd exception: " + e.toString());
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    result.append(e.toString());
                    LogUtils.e("close dos exception: " + e.toString());
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    result.append(e.toString());
                    LogUtils.e("close dis exception: " + e.toString());
                }
            }
        }
        return result.toString();
    }
    //*/
}
