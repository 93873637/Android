package com.liz.androidutils;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public class AudioUtils {

    public static final int AUDIO_TRACK_SINGLE = 1;
    public static final int AUDIO_TRACK_DOUBLE = 2;

    public static String audioSourceName(int audioSource) {
        switch (audioSource) {
            case MediaRecorder.AudioSource.MIC: return "MIC";
            case MediaRecorder.AudioSource.VOICE_CALL: return "VOICE_CALL";
            case MediaRecorder.AudioSource.VOICE_COMMUNICATION: return "VOICE_COMMUNICATION";
            case MediaRecorder.AudioSource.VOICE_DOWNLINK: return "VOICE_DOWNLINK";
            case MediaRecorder.AudioSource.VOICE_RECOGNITION: return "VOICE_RECOGNITION";
            case MediaRecorder.AudioSource.VOICE_UPLINK: return "VOICE_UPLINK";
            case MediaRecorder.AudioSource.CAMCORDER: return "CAMCORDER";
            case MediaRecorder.AudioSource.REMOTE_SUBMIX: return "REMOTE_SUBMIX";
            case MediaRecorder.AudioSource.UNPROCESSED: return "UNPROCESSED";
            default: return "UNKNOWN";
        }
    }

    public static String audioFormatName(int audioFormat) {
        switch (audioFormat) {
            case AudioFormat.ENCODING_DEFAULT: return "DEFAULT";
            case AudioFormat.ENCODING_PCM_16BIT: return "ENCODING_PCM_16BIT";
            case AudioFormat.ENCODING_PCM_8BIT: return "ENCODING_PCM_8BIT";
            case AudioFormat.ENCODING_PCM_FLOAT: return "ENCODING_PCM_FLOAT";
            case AudioFormat.ENCODING_AC3: return "ENCODING_AC3";
            case AudioFormat.ENCODING_E_AC3: return "ENCODING_E_AC3";
            case AudioFormat.ENCODING_DTS: return "ENCODING_DTS";
            case AudioFormat.ENCODING_DTS_HD: return "ENCODING_DTS_HD";
            case AudioFormat.ENCODING_AAC_LC: return "ENCODING_AAC_LC";
            case AudioFormat.ENCODING_AAC_HE_V1: return "ENCODING_AAC_HE_V1";
            case AudioFormat.ENCODING_AAC_HE_V2: return "ENCODING_AAC_HE_V2";
            case AudioFormat.ENCODING_IEC61937: return "ENCODING_IEC61937";
            case AudioFormat.ENCODING_DOLBY_TRUEHD: return "ENCODING_DOLBY_TRUEHD";
            case AudioFormat.ENCODING_AAC_ELD: return "ENCODING_AAC_ELD";
            case AudioFormat.ENCODING_AAC_XHE: return "ENCODING_AAC_XHE";
            case AudioFormat.ENCODING_AC4: return "ENCODING_AC4";
            case AudioFormat.ENCODING_E_AC3_JOC: return "ENCODING_E_AC3_JOC";
            case AudioFormat.ENCODING_DOLBY_MAT: return "ENCODING_DOLBY_MAT";
            default: return "UNKNOWN";
        }
    }

    public static String channelConfigName(int channelConfig) {
        switch (channelConfig) {
            case AudioFormat.CHANNEL_CONFIGURATION_MONO: return "CHANNEL_CONFIGURATION_MONO";
            case AudioFormat.CHANNEL_CONFIGURATION_STEREO: return "CHANNEL_CONFIGURATION_STEREO";
            case AudioFormat.CHANNEL_IN_MONO: return "CHANNEL_IN_MONO";
            case AudioFormat.CHANNEL_IN_STEREO: return "CHANNEL_IN_STEREO";  //same as CHANNEL_OUT_STEREO
            case AudioFormat.CHANNEL_OUT_MONO: return "CHANNEL_OUT_MONO";
            case AudioFormat.CHANNEL_OUT_QUAD: return "CHANNEL_OUT_QUAD";
            default: return "UNKNOWN";
        }
    }

    //
    //channels: 1-single track, 2-double track (sound will be fast if wrong)
    //
    public static void pcmToWave(String pcmFileAbsolute, String wavFileAbsolute, long sampleRate, int recorderBufferSize, int audioFormat, int channels) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen = 0;
        long totalDataLen = 36;
        long byteRate = (audioFormat == AudioFormat.ENCODING_PCM_8BIT?8:16)* sampleRate * channels / 8;
        byte[] data = new byte[recorderBufferSize];
        try {
            in = new FileInputStream(pcmFileAbsolute);
            out = new FileOutputStream(wavFileAbsolute);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);
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
    public static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
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

    public static void playPCM(String pcmFilePath, int bufferSize, int sampleRate, int encodingBits, int channelMask) {
        playPCM(new File(pcmFilePath), bufferSize, sampleRate, encodingBits, channelMask);
    }

    public static void playPCM(File pcmFile, int bufferSize, int sampleRate, int encodingBits, int channelMask) {
        if (pcmFile == null || !pcmFile.exists()) {
            LogUtils.e("playPCM: pcm file not exists");
            return;
        }

        final FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(pcmFile);
        } catch (Exception e) {
            LogUtils.e("playPCM: file input stream exception " + e.toString());
            return;
        }

        final int bufferSizeInBytes = bufferSize;
        final AudioTrack audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(encodingBits)
                        .setChannelMask(channelMask)
                        .build(),
                bufferSizeInBytes,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
        );

        // AudioTrack need play first
        audioTrack.play();

        // run read and play task
        new Thread() {
            @Override
            public void run() {
                try {
                    byte[] buffer = new byte[bufferSizeInBytes];
                    while (fileInputStream.available() > 0) {
                        int readCount = fileInputStream.read(buffer);
                        if (readCount < 0) {
                            LogUtils.e("playPCM: read error " + readCount);
                        } else {
                            //play by audio track
                            audioTrack.write(buffer, 0, readCount);
                        }
                    }
                    fileInputStream.close();
                } catch (Exception e) {
                    LogUtils.e("playPCM: exception " + e.toString());
                    e.printStackTrace();
                } finally {
                    audioTrack.stop();
                    audioTrack.release();
                }
            }
        }.start();
    }
}
