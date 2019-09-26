package com.liz.whatsai.logic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.liz.androidutils.AudioUtils;
import com.liz.androidutils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class WhatsaiAudio {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Interfaces

    public static void init() {
        whatsaiAudio = new WhatsaiAudio();
    }

    public static void switchAudio() {
        whatsaiAudio._switchAudio();
    }

    public static boolean isRecording() {
        return whatsaiAudio.mIsRecording;
    }
    // Interfaces
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int AUDIO_SAMPLE_RATE = 8000;  //unit by Hz
    public static final int AUDIO_DATE_SIZE = 320;
    public static final int EXECUTOR_CORE_POOL_SIZE = 2;
    public static final int EXECUTOR_MAX_POOL_SIZE = 2;
    public static final int EXECUTOR_KEEP_ALIVE_TIME = 60;

    private static WhatsaiAudio whatsaiAudio;

    private ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(
            EXECUTOR_CORE_POOL_SIZE,
            EXECUTOR_MAX_POOL_SIZE,
            EXECUTOR_KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private AudioRecord mAudioRecord;
    private int mRecorderBufferSize;
    private byte[] mAudioData;
    private boolean mIsRecording = false;

    //Singleton Constructor
    private WhatsaiAudio() {
        mRecorderBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                mRecorderBufferSize);
        mAudioData = new byte[AUDIO_DATE_SIZE];
    }

    private void _switchAudio() {
        if (mIsRecording) {
            stopRecord();
        }
        else {
            startRecord();
        }
    }

    private void startRecord() {
        if (mIsRecording) {
            LogUtils.d("WhatsaiAudio: startRecord: already started");
            return;
        }
        mIsRecording = true;

        //19.1103.173655
        String strFileTime = new SimpleDateFormat("yy.MMdd.HHmmss").format(new java.util.Date());
        String pcmfileName = strFileTime + "_" + AUDIO_SAMPLE_RATE + ".pcm";
        final File pcmFile = createFile(pcmfileName);
        if (pcmFile == null) {
            LogUtils.e("WhatsaiAudio: startRecord: create pcm file failed");
            return;
        }
        String wavfileName = strFileTime + "_" + AUDIO_SAMPLE_RATE + ".wav";
        final File wavFile = createFile(wavfileName);
        if (wavFile == null) {
            LogUtils.e("WhatsaiAudio: startRecord: create wav file failed");
            return;
        }

        mAudioRecord.startRecording();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream outputStream = new FileOutputStream(pcmFile.getAbsoluteFile());
                    while (mIsRecording) {
                        mAudioRecord.read(mAudioData, 0, mAudioData.length);
                        outputStream.write(mAudioData);
                    }
                    outputStream.close();
                    AudioUtils.pcmToWave(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath(),
                            AUDIO_SAMPLE_RATE, mRecorderBufferSize, AudioUtils.AUDIO_TRACK_SINGLE);
                } catch (FileNotFoundException e) {
                    LogUtils.e("WhatsaiAudio: startRecord: FileNotFoundException");
                    e.printStackTrace();
                } catch (IOException e) {
                    LogUtils.e("WhatsaiAudio: startRecord: IOException");
                    e.printStackTrace();
                } catch (Exception e) {
                    LogUtils.e("WhatsaiAudio: startRecord: exception: " + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    private void stopRecord() {
        if (!mIsRecording) {
            LogUtils.d("WhatsaiAudio: stopRecord: already stopped");
            return;
        }
        mIsRecording = false;
        mAudioRecord.stop();
    }

    private File createFile(String fileName) {
        String filePath = ComDef.WHATSAI_DATA_PATH + "/" + fileName;
        File objFile = new File(filePath);
        if (!objFile.exists()) {
            try {
                if (!objFile.createNewFile()) {
                    LogUtils.e("WhatsaiAudio: createFile failed.");
                    return null;
                }
                return objFile;
            } catch (IOException e) {
                LogUtils.e("WhatsaiAudio: createFile exception: " + e.toString());
                e.printStackTrace();
            }
        }
        return null;
    }
}
