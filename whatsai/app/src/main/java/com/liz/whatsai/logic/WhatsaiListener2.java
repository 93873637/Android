package com.liz.whatsai.logic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.liz.androidutils.AudioUtils;
import com.liz.androidutils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

@SuppressWarnings("WeakerAccess")
public class WhatsaiListener2 {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Interfaces

    public static boolean isListening() {
        return mListener.mIsListening;
    }

    public static void switchListening() {
        mListener._switchListening();
    }

    // Interfaces
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int DEFAULT_AUDIO_SAMPLE_RATE = 44100;  //unit by Hz, 8k, 16k, 32k, 44.1k, 48k

    private static WhatsaiListener2 mListener = new WhatsaiListener2();

    private AudioRecord mAudioRecord;
    private int mSampleRate = DEFAULT_AUDIO_SAMPLE_RATE;  // unit by Hz
    private int mAudioFormat = AudioFormat.ENCODING_PCM_8BIT;
    private int mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int mRecorderBufferSize;  // unit by byte
    private byte[] mAudioData;
    private boolean mIsListening = false;
    private int mFrameCount = 0;
    private int mFramePower = 0;
    private int mTotalSize = 0;
    private String mPCMFilePath = "";

    // Singleton Constructor
    private WhatsaiListener2() {
        LogUtils.d("WhatsaiListener:WhatsaiListener");

        mRecorderBufferSize = AudioRecord.getMinBufferSize(mSampleRate,
                mChannelConfig,
                mAudioFormat);
        LogUtils.d("WhatsaiListener:WhatsaiListener: mRecorderBufferSize = " + mRecorderBufferSize);

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                mSampleRate,
                mChannelConfig,
                mAudioFormat,
                mRecorderBufferSize);

        mAudioData = new byte[mRecorderBufferSize];
    }

    private void _switchListening() {
        LogUtils.d("WhatsaiListener:_switchListening: mIsListening = " + mIsListening);
        if (mIsListening) {
            stopListening();
        }
        else {
            startListening();
        }
    }

    private void startListening() {
        LogUtils.d("WhatsaiListener:startListening: mIsListening = " + mIsListening);
        if (mIsListening) {
            LogUtils.d("WhatsaiListener:startListening: already started");
            return;
        }
        mIsListening = true;

        final FileOutputStream outputStream = getPCMOutputStream();
        if (outputStream == null) {
            LogUtils.e("WhatsaiListener:startListening: get output stream for audio buffer failed.");
            return;
        }

        mAudioRecord.startRecording();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.d("WhatsaiListener: Start loop write audio data to pcm file...");
                    while (mIsListening) {
                        int readSize = mAudioRecord.read(mAudioData, 0, mAudioData.length);
                        onReadBuffer(readSize, mAudioData);
                        outputStream.write(mAudioData);
                        outputStream.flush();
                    }
                    outputStream.close();

                    //###@:
                    LogUtils.d("Convert pcm file to wave...");
                    AudioUtils.pcmToWave(mPCMFilePath, mPCMFilePath + ".wav",
                            mSampleRate, mRecorderBufferSize, mAudioFormat, AudioUtils.AUDIO_TRACK_SINGLE);
                    //AudioUtils.playPCM(new File(mPCMFilePath), mRecorderBufferSize, mSampleRate);

                } catch (Exception e) {
                    LogUtils.e("WhatsaiListener: startListening: exception: " + e.toString());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopListening() {
        LogUtils.d("WhatsaiListener:stopRecord: mIsListening = " + mIsListening);
        if (!mIsListening) {
            LogUtils.d("WhatsaiListener: stopRecord: already stopped");
            return;
        }
        mIsListening = false;
        mAudioRecord.stop();
    }

    private void onReadBuffer(int readSize, byte[] audioData) {
        // calc frame power
        int sum = 0;
        for (int i = 0; i < readSize; i++) {
            sum += audioData[i];
        }
        mFramePower = sum;//###@: / readSize;

        /*
        // enlarge volume
        for (int i = 0; i < mAudioData.length; i++) {
            mAudioData[i] = (byte) (mAudioData[i] * 5);
        }
        //*/

        mTotalSize += readSize;
        mFrameCount++;
        LogUtils.v("WhatsaiListener: read #" + mFrameCount + ": " + readSize + " / " + mTotalSize + " / " + mFramePower);
    }

    private FileOutputStream getPCMOutputStream() {
        File pcmFile = createPCMFile();
        if (pcmFile == null) {
            LogUtils.e("WhatsaiListener:getPCMOutputStream: create pcm file failed");
            return null;
        }
        try {
            return new FileOutputStream(pcmFile.getAbsoluteFile());
        }
        catch (Exception e) {
            LogUtils.e("WhatsaiListener:getPCMOutputStream: create output stream exception " + e.toString());
            return null;
        }
    }

    /**
     * create a pcm file to write pcm file
     * @return file which name format as yy.MMdd.HHmmss(19.1103.173655)
     */
    private File createPCMFile() {
        String strFileTime = new SimpleDateFormat("yy.MMdd.HHmmss").format(new java.util.Date());
        String fileName = strFileTime + ".pcm";
        String filePath = ComDef.WHATSAI_AUDIO_DIR + "/" + fileName;
        LogUtils.i("WhatsaiListener:createPCMFile: filePath = " + filePath);
        mPCMFilePath = filePath;

        File objFile = new File(filePath);
        if (!objFile.exists()) {
            try {
                if (!objFile.createNewFile()) {
                    LogUtils.e("WhatsaiListener: createPCMFile failed.");
                    return null;
                }
                return objFile;
            } catch (IOException e) {
                LogUtils.e("WhatsaiListener: createPCMFile exception: " + e.toString());
                e.printStackTrace();
            }
        }

        return null;
    }
}
