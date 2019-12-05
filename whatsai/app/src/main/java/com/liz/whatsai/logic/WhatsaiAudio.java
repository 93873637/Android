package com.liz.whatsai.logic;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.liz.androidutils.AudioUtils;
import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.whatsai.app.AudioListAdapter;

import java.io.File;
import java.io.FileInputStream;
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
        LogUtils.d("WhatsaiAudio:init");
        mWhatsaiAudio = new WhatsaiAudio();
    }

    public static void switchAudio() {
        mWhatsaiAudio._switchAudio();
    }

    public static boolean isRecording() {
        return mWhatsaiAudio.mIsRecording;
    }

    public interface WhatsaiAudioCallback {
        void onAudioFileGenerated();
    }

    public static void setAudioCallback(WhatsaiAudioCallback cb) {
        mWhatsaiAudio.mAudioCallback = cb;
    }

    public static int getPlayItemPos() {
        return mPlayItemPos;
    }

    public static void startPlay(int pos) {
        LogUtils.d("WhatsaiAudio:startPlay: pos = " + pos);
        if (pos == mPlayItemPos) {
            LogUtils.d("WhatsaiAudio: the play item already be " + pos);
        }
        else {
            mPlayItemPos = pos;
            File f = AudioListAdapter.getAudioFile(pos);
            AudioListAdapter.onDataChanged();
            WhatsaiAudio.playPCM(f, mWhatsaiAudio.mRecorderBufferSize, AUDIO_SAMPLE_RATE);
        }
    }

    public static void stopPlay(int pos) {
        LogUtils.d("WhatsaiAudio:stopPlay: pos = " + pos);

        //####@:
    }

    // Interfaces
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private WhatsaiAudioCallback mAudioCallback = null;

    public static final int AUDIO_SAMPLE_RATE = 44100;  //unit by Hz
    public static final int AUDIO_DATE_SIZE = 320;
    public static final int EXECUTOR_CORE_POOL_SIZE = 2;
    public static final int EXECUTOR_MAX_POOL_SIZE = 2;
    public static final int EXECUTOR_KEEP_ALIVE_TIME = 60;

    private static WhatsaiAudio mWhatsaiAudio;
    private static int mPlayItemPos = ComDef.INVALID_LIST_POS;

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
        LogUtils.d("WhatsaiAudio:WhatsaiAudio");
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
        LogUtils.d("WhatsaiAudio:_switchAudio: mIsRecording = " + mIsRecording);
        if (mIsRecording) {
            stopRecord();
        }
        else {
            startRecord();
        }
    }

    private void startRecord() {
        LogUtils.d("WhatsaiAudio:startRecord: mIsRecording = " + mIsRecording);
        if (mIsRecording) {
            LogUtils.d("WhatsaiAudio: startRecord: already started");
            return;
        }
        mIsRecording = true;

        //19.1103.173655
        String strFileTime = new SimpleDateFormat("yy.MMdd.HHmmss").format(new java.util.Date());
        String pcmFileName = strFileTime + "_" + AUDIO_SAMPLE_RATE + ".pcm";
        final File pcmFile = createFile(pcmFileName);
        if (pcmFile == null) {
            LogUtils.e("WhatsaiAudio: startRecord: create pcm file failed");
            return;
        }
        String wavFileName = strFileTime + "_" + AUDIO_SAMPLE_RATE + ".wav";
        final File wavFile = createFile(wavFileName);
        if (wavFile == null) {
            LogUtils.e("WhatsaiAudio: startRecord: create wav file failed");
            return;
        }

        mAudioRecord.startRecording();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.d("Save audio data to pcm file...");
                    FileOutputStream outputStream = new FileOutputStream(pcmFile.getAbsoluteFile());
                    while (mIsRecording) {
                        mAudioRecord.read(mAudioData, 0, mAudioData.length);
                        outputStream.write(mAudioData);
                    }
                    outputStream.close();

                    LogUtils.d("Convert pcm file to wave...");
                    AudioUtils.pcmToWave(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath(),
                            AUDIO_SAMPLE_RATE, mRecorderBufferSize, AudioUtils.AUDIO_TRACK_SINGLE);
                    if (mAudioCallback != null) {
                        mAudioCallback.onAudioFileGenerated();
                    }

                    LogUtils.d("Remove pcm file");
                    FileUtils.removeFile(pcmFile);
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
        LogUtils.d("WhatsaiAudio:stopRecord: mIsRecording = " + mIsRecording);
        if (!mIsRecording) {
            LogUtils.d("WhatsaiAudio: stopRecord: already stopped");
            return;
        }
        mIsRecording = false;
        mAudioRecord.stop();
    }

    private File createFile(String fileName) {
        String filePath = ComDef.WHATSAI_AUDIO_DATA_PATH + "/" + fileName;
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

    public static void playPCM(File pcmFile, int bufferSize, int sampleRate) {
        final int bufferSizeInBytes = bufferSize;
        final AudioTrack audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_CONFIGURATION_MONO)
                        .build(),
                bufferSizeInBytes,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
        );
        audioTrack.play();  //this mode need play first
        final FileInputStream fileInputStream;
        if (pcmFile.exists()) {
            try {
                fileInputStream = new FileInputStream(pcmFile);
            } catch (FileNotFoundException e) {
                LogUtils.e("FileNotFoundException of file " + pcmFile);
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[bufferSizeInBytes];
                        while (fileInputStream.available() > 0) {
                            int readCount = fileInputStream.read(buffer); //一次次的读取
                            //检测错误就跳过
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                                continue;
                            }
                            if (readCount != -1 && readCount != 0) {
                                //可以在这个位置用play()
                                //输出音频数据
                                audioTrack.write(buffer, 0, readCount); //一次次的write输出播放
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mPlayItemPos = ComDef.INVALID_LIST_POS;
                        AudioListAdapter.onDataChanged();
                    }
                    Log.i("TAG", "STREAM模式播放完成");
                }
            }.start();
        }
    }
}
