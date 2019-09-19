package com.liz.whatsai.logic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.liz.androidutils.LogUtils;

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
                    pcmToWave(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());
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
                objFile.createNewFile();
                return objFile;
            } catch (IOException e) {
                LogUtils.e("WhatsaiAudio: createFile exception: " + e.toString());
                e.printStackTrace();
            }
        }
        return null;
    }

    private void pcmToWave(String inFileName, String outFileName) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen = 0;
        long longSampleRate = AUDIO_SAMPLE_RATE;
        long totalDataLen = 36;
        int channels = 1;  //你录制是单声道就是1 双声道就是2（如果错了声音可能会急促等）
        long byteRate = 16 * longSampleRate * channels / 8;
        byte[] data = new byte[mRecorderBufferSize];
        try {
            in = new FileInputStream(inFileName);
            out = new FileOutputStream(outFileName);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
    任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
    FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的，
     */
    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                     int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
}
