package com.liz.whatsai.logic;

public class AudioFrame {
    private byte[] mAudioData;

    public AudioFrame() {
        mAudioData = null;
    }

    public AudioFrame(byte[] data) {
        mAudioData = data;
    }
}
