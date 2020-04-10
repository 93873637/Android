package com.liz.wsrecorder.logic;

public class AudioTemplate {
    private byte[] mAudioData;

    public AudioTemplate() {
        mAudioData = null;
    }

    public AudioTemplate(byte[] data) {
        mAudioData = data;
    }
}
