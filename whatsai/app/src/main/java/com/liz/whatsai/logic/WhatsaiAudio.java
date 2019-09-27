package com.liz.whatsai.logic;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
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
import java.util.List;
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

    public interface WhatsaiAudioCallback {
        void onAudioFileGenerated();
    }

    public static void setAudioCallback(WhatsaiAudioCallback cb) {
        whatsaiAudio.mAudioCallback = cb;
    }

    // Interfaces
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private WhatsaiAudioCallback mAudioCallback = null;

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
                    FileOutputStream outputStream = new FileOutputStream(pcmFile.getAbsoluteFile());
                    while (mIsRecording) {
                        mAudioRecord.read(mAudioData, 0, mAudioData.length);
                        outputStream.write(mAudioData);
                    }
                    outputStream.close();
                    AudioUtils.pcmToWave(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath(),
                            AUDIO_SAMPLE_RATE, mRecorderBufferSize, AudioUtils.AUDIO_TRACK_SINGLE);
                    if (mAudioCallback != null) {
                        mAudioCallback.onAudioFileGenerated();
                    }
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


    //音频采样率 (MediaRecoder的采样率通常是8000Hz AAC的通常是44100Hz.设置采样率为44100目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz = 44100;    //声道
    private static final int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    //数据格式  (指定采样的数据的格式和每次采样的大小)    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private static final String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"audiorecordtest.pcm";

    private void playPCM(String mFileName, final int bufferSizeInBytes) throws FileNotFoundException {
//        if (maudioTrack != null){
//            maudioTrack.stop();
//            maudioTrack.release();
//            maudioTrack = null;
//        }
//先估算最小缓冲区大小
        //mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz,mChannelConfig,mAudioFormat);
//创建AudioTrack
        final AudioTrack maudioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(mSampleRateInHz)
                        .setEncoding(mAudioFormat)
                        .setChannelMask(mChannelConfig)
                        .build(),
                bufferSizeInBytes,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
        );
        maudioTrack.play();  //这个模式需要先play
        File file = new File(mFileName); //原始pcm文件
        final FileInputStream fileInputStream;
        if (file.exists()){
            fileInputStream = new FileInputStream(file);
            new Thread(){
                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[bufferSizeInBytes];
                        while(fileInputStream.available() > 0){
                            int readCount = fileInputStream.read(buffer); //一次次的读取
                            //检测错误就跳过
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION|| readCount == AudioTrack.ERROR_BAD_VALUE){
                                continue;
                            }
                            if (readCount != -1 && readCount != 0){
//可以在这个位置用play()
                                //输出音频数据
                                maudioTrack.write(buffer,0,readCount); //一次次的write输出播放
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i("TAG","STREAM模式播放完成");
                }
            }.start();
        }
    }
}
