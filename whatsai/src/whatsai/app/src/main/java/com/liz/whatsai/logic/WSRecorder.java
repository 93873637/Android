package com.liz.whatsai.logic;

import com.liz.androidutils.LogUtils;

public class WSRecorder extends WSListener {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Singleton

    public static WSRecorder inst() {
        if (mWSRecorder == null) {
            mWSRecorder = new WSRecorder();
        }
        return mWSRecorder;
    }

    // the one and only object instance
    private static WSRecorder mWSRecorder;

    // set constructor private for singleton
    private WSRecorder() {
        LogUtils.trace();
        setAutoSave(true);
    }

    // Singleton
    ///////////////////////////////////////////////////////////////////////////////////////////////
}
