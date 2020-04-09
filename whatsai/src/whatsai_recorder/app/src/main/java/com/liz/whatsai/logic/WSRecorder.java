package com.liz.whatsai.logic;

import com.liz.androidutils.FileUtils;
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
        setAudioDir(ComDef.WHATSAI_RECORDER_HOME);
        setAutoSave(true);
        setMaxAudioFileSize(ComDef.MAX_AUDIO_FILE_SIZE);
        setMaxAudioStorageSize(ComDef.MAX_AUDIO_STORAGE_SIZE);
        if (!FileUtils.touchDir(ComDef.WHATSAI_RECORDER_HOME)) {
            LogUtils.e("Touch audio recorder home " + ComDef.WHATSAI_RECORDER_HOME + " failed.");
        }
    }

    // Singleton
    ///////////////////////////////////////////////////////////////////////////////////////////////
}
