package com.liz.whatsai.logic;

import com.liz.androidutils.LogUtils;

public class WSRecorder extends WSListener {

    public static final int RECORDER_MAX_POWER_SIZE = 8192;

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
        setMaxPowerListSize(RECORDER_MAX_POWER_SIZE);
        setWaveSamplingRate(ComDef.AUDIO_RECORD_WAVE_SAMPLING_RATE);
        setAutoSave(true);
    }

    // Singleton
    ///////////////////////////////////////////////////////////////////////////////////////////////
}
