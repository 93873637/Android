package com.liz.androidutils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.List;

@SuppressWarnings("unused, WeakerAccess")
public class AudioUtils {

    public static final int WAVE_FILE_HEADER_LEN = 44;
    public static final int WAVE_FILE_HEADER_POS_SUBCHUNK2SIZE = 40;

    public static String audioSourceName(int audioSource) {
        switch (audioSource) {
            case MediaRecorder.AudioSource.MIC:
                return "MIC";
            case MediaRecorder.AudioSource.VOICE_CALL:
                return "VOICE_CALL";
            case MediaRecorder.AudioSource.VOICE_COMMUNICATION:
                return "VOICE_COMMUNICATION";
            case MediaRecorder.AudioSource.VOICE_DOWNLINK:
                return "VOICE_DOWNLINK";
            case MediaRecorder.AudioSource.VOICE_RECOGNITION:
                return "VOICE_RECOGNITION";
            case MediaRecorder.AudioSource.VOICE_UPLINK:
                return "VOICE_UPLINK";
            case MediaRecorder.AudioSource.CAMCORDER:
                return "CAMCORDER";
            case MediaRecorder.AudioSource.REMOTE_SUBMIX:
                return "REMOTE_SUBMIX";
            case MediaRecorder.AudioSource.UNPROCESSED:
                return "UNPROCESSED";
            default:
                return "UNKNOWN AUDIO SOURCE";
        }
    }

    public static String audioFormatName(int audioFormat) {
        switch (audioFormat) {
            case AudioFormat.ENCODING_DEFAULT:
                return "DEFAULT";
            case AudioFormat.ENCODING_PCM_16BIT:
                return "ENCODING_PCM_16BIT";
            case AudioFormat.ENCODING_PCM_8BIT:
                return "ENCODING_PCM_8BIT";
            case AudioFormat.ENCODING_PCM_FLOAT:
                return "ENCODING_PCM_FLOAT";
            case AudioFormat.ENCODING_AC3:
                return "ENCODING_AC3";
            case AudioFormat.ENCODING_E_AC3:
                return "ENCODING_E_AC3";
            case AudioFormat.ENCODING_DTS:
                return "ENCODING_DTS";
            case AudioFormat.ENCODING_DTS_HD:
                return "ENCODING_DTS_HD";
            case AudioFormat.ENCODING_AAC_LC:
                return "ENCODING_AAC_LC";
            case AudioFormat.ENCODING_AAC_HE_V1:
                return "ENCODING_AAC_HE_V1";
            case AudioFormat.ENCODING_AAC_HE_V2:
                return "ENCODING_AAC_HE_V2";
            case AudioFormat.ENCODING_IEC61937:
                return "ENCODING_IEC61937";
            case AudioFormat.ENCODING_DOLBY_TRUEHD:
                return "ENCODING_DOLBY_TRUEHD";
            case AudioFormat.ENCODING_AAC_ELD:
                return "ENCODING_AAC_ELD";
            case AudioFormat.ENCODING_AAC_XHE:
                return "ENCODING_AAC_XHE";
            case AudioFormat.ENCODING_AC4:
                return "ENCODING_AC4";
            case AudioFormat.ENCODING_E_AC3_JOC:
                return "ENCODING_E_AC3_JOC";
            case AudioFormat.ENCODING_DOLBY_MAT:
                return "ENCODING_DOLBY_MAT";
            default:
                return "UNKNOWN AUDIO FORMAT";
        }
    }

    public static String channelConfigName(int channelConfig) {
        switch (channelConfig) {
            case AudioFormat.CHANNEL_CONFIGURATION_MONO:
                return "CHANNEL_CONFIGURATION_MONO";
            case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
                return "CHANNEL_CONFIGURATION_STEREO";
            case AudioFormat.CHANNEL_IN_MONO:
                return "CHANNEL_IN_MONO";
            case AudioFormat.CHANNEL_IN_STEREO:
                return "CHANNEL_IN_STEREO";  //same as CHANNEL_OUT_STEREO
            case AudioFormat.CHANNEL_OUT_MONO:
                return "CHANNEL_OUT_MONO";
            case AudioFormat.CHANNEL_OUT_QUAD:
                return "CHANNEL_OUT_QUAD";
            default:
                return "UNKNOWN CHANNEL CONFIG";
        }
    }

    public static int channelNumByConfig(int channelConfig) {
        switch (channelConfig) {
            case AudioFormat.CHANNEL_CONFIGURATION_MONO:
                return 1;
            case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
                return 2;
            case AudioFormat.CHANNEL_IN_MONO:
                return 1;
            case AudioFormat.CHANNEL_IN_STEREO:
                return 2;   // i.e. CHANNEL_OUT_STEREO
            case AudioFormat.CHANNEL_OUT_MONO:
                return 1;
            case AudioFormat.CHANNEL_OUT_QUAD:
                return 1;
            default:
                return -1;  // unknown channel config
        }
    }

    public static int bitNumByAudioFormat(int audioFormat) {
        return byteNumByAudioFormat(audioFormat) * 8;
    }

    public static int byteNumByAudioFormat(int audioFormat) {
        switch (audioFormat) {
            case AudioFormat.ENCODING_PCM_8BIT:
                return 1;
            case AudioFormat.ENCODING_PCM_16BIT:
                return 2;
            default:
                return -1;  // unknown audio format
        }
    }

    /**
     * transfer pcm file to wave file, and rename file name to *.wav
     *
     * @param pcmFileAbsolute:    pcm file name with full path to read from(*.pcm)
     * @param sampleRate:
     * @param recorderBufferSize:
     * @param audioFormat:
     * @param channelConfig:
     */
    public static void pcm2wav(@NonNull String pcmFileAbsolute,
                               int sampleRate,
                               int recorderBufferSize,
                               int audioFormat,
                               int channelConfig,
                               boolean deletePCM) {
        String wavFileAbsolute = FileUtils.replaceFileExtension(pcmFileAbsolute, "wav");
        pcm2wav(pcmFileAbsolute, wavFileAbsolute, sampleRate, recorderBufferSize, audioFormat, channelConfig, deletePCM);
    }

    /**
     * @param pcmFileAbsolute:    pcm file name with full path to read from(*.pcm)
     * @param wavFileAbsolute:    wave file name with full path to save to(*.wav)
     * @param sampleRate:
     * @param recorderBufferSize:
     * @param audioFormat:
     * @param channelConfig:      AudioFormat.CHANNEL_CONFIGURATION_MONO
     */
    public static void pcm2wav(String pcmFileAbsolute,
                               String wavFileAbsolute,
                               int sampleRate,
                               int recorderBufferSize,
                               int audioFormat,
                               int channelConfig,
                               boolean deletePCM) {

        int numChannels = channelNumByConfig(channelConfig);
        int bitsPerSample = bitNumByAudioFormat(audioFormat);

        // BlockAlign == NumChannels * BitsPerSample/8
        // The number of bytes for one sample including all channels.
        int blockAlign = numChannels * bitsPerSample / 8;

        // ByteRate == SampleRate * NumChannels * BitsPerSample/8
        int byteRate = sampleRate * numChannels * bitsPerSample / 8;

        // Subchunk2Size == NumSamples * NumChannels * BitsPerSample/8,
        // This is the number of bytes in the data. i.e. pcm data file size
        int subchunk2Size = (int) FileUtils.getFileSize(pcmFileAbsolute);

        // ChunkSize = 36 + SubChunk2Size
        int chunkSize = subchunk2Size + 36;

        FileUtils.addHeader(pcmFileAbsolute, wavFileAbsolute,
                buildWaveFileHeader(chunkSize, numChannels, sampleRate, byteRate, blockAlign, bitsPerSample, subchunk2Size));

        if (deletePCM) {
            FileUtils.delete(pcmFileAbsolute);
        }
    }

    public static byte[] getWaveHeader(
            int fileSize,
            int sampleRate,
            int recorderBufferSize,
            int audioFormat,
            int channelConfig) {
        int numChannels = channelNumByConfig(channelConfig);
        int bitsPerSample = bitNumByAudioFormat(audioFormat);

        // BlockAlign == NumChannels * BitsPerSample/8
        // The number of bytes for one sample including all channels.
        int blockAlign = numChannels * bitsPerSample / 8;

        // ByteRate == SampleRate * NumChannels * BitsPerSample/8
        int byteRate = sampleRate * numChannels * bitsPerSample / 8;

        // Subchunk2Size == NumSamples * NumChannels * BitsPerSample/8,
        // This is the number of bytes in the data. i.e. pcm data file size
        int subchunk2Size = fileSize;

        // ChunkSize = 36 + SubChunk2Size
        int chunkSize = subchunk2Size + 36;

        return buildWaveFileHeader(chunkSize, numChannels, sampleRate, byteRate, blockAlign, bitsPerSample, fileSize);
    }

    public static void modifyWaveFileHeader(@NonNull RandomAccessFile raf, int fileSize) {
        try {
            // save current pos
            long pos = raf.getFilePointer();

            // modify subchunk2size with fileSize(big endian)
            raf.seek(WAVE_FILE_HEADER_POS_SUBCHUNK2SIZE);
            byte[] sizeBytes = new byte[4];
            sizeBytes[0] = (byte) (fileSize & 0xff);
            sizeBytes[1] = (byte) ((fileSize >> 8) & 0xff);
            sizeBytes[2] = (byte) ((fileSize >> 16) & 0xff);
            sizeBytes[3] = (byte) ((fileSize >> 24) & 0xff);
            raf.write(sizeBytes);

            // restore original pos
            raf.seek(pos);
        } catch (Exception e) {
            LogUtils.te2("update audio file size exception " + e.toString());
        }
    }

    /**
     * The canonical WAVE format starts with the RIFF header:
     * <p>
     * 0         4   ChunkID          Contains the letters "RIFF" in ASCII form
     * (0x52494646 big-endian form).
     * 4         4   ChunkSize        36 + SubChunk2Size, or more precisely:
     * 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
     * This is the size of the rest of the chunk
     * following this number.  This is the size of the
     * entire file in bytes minus 8 bytes for the
     * two fields not included in this count:
     * ChunkID and ChunkSize.
     * 8         4   Format           Contains the letters "WAVE"
     * (0x57415645 big-endian form).
     * <p>
     * The "WAVE" format consists of two subchunks: "fmt " and "data":
     * The "fmt " subchunk describes the sound data's format:
     * <p>
     * 12        4   Subchunk1ID      Contains the letters "fmt "
     * (0x666d7420 big-endian form).
     * 16        4   Subchunk1Size    16 for PCM.  This is the size of the
     * rest of the Subchunk which follows this number.
     * 20        2   AudioFormat      PCM = 1 (i.e. Linear quantization)
     * Values other than 1 indicate some
     * form of compression.
     * 22        2   NumChannels      Mono = 1, Stereo = 2, etc.
     * 24        4   SampleRate       8000, 44100, etc.
     * 28        4   ByteRate         == SampleRate * NumChannels * BitsPerSample/8
     * 32        2   BlockAlign       == NumChannels * BitsPerSample/8
     * The number of bytes for one sample including
     * all channels. I wonder what happens when
     * this number isn't an integer?
     * 34        2   BitsPerSample    8 bits = 8, 16 bits = 16, etc.
     * 2   ExtraParamSize   if PCM, then doesn't exist
     * X   ExtraParams      space for extra parameters
     * <p>
     * The "data" subchunk contains the size of the data and the actual sound:
     * <p>
     * 36        4   Subchunk2ID      Contains the letters "data"
     * (0x64617461 big-endian form).
     * 40        4   Subchunk2Size    == NumSamples * NumChannels * BitsPerSample/8
     * This is the number of bytes in the data.
     * You can also think of this as the size
     * of the read of the subchunk following this
     * number.
     * 44        *   Data             The actual sound data.
     */
    public static byte[] buildWaveFileHeader(int chunkSize,
                                             int numChannels,
                                             int sampleRate,
                                             int byteRate,
                                             int blockAlign,
                                             int bitsPerSample,
                                             int subchunk2Size) {
        System.out.println("buildWaveFileHeader: chunkSize = " + chunkSize
                + ", numChannels = " + numChannels
                + ", sampleRate = " + sampleRate
                + ", byteRate = " + byteRate
                + ", blockAlign = " + blockAlign
                + ", bitsPerSample = " + bitsPerSample
                + ", subchunk2Size = " + subchunk2Size
        );

        byte[] header = new byte[WAVE_FILE_HEADER_LEN];

        // ChunkID
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        // ChunkSize
        header[4] = (byte) (chunkSize & 0xff);
        header[5] = (byte) ((chunkSize >> 8) & 0xff);
        header[6] = (byte) ((chunkSize >> 16) & 0xff);
        header[7] = (byte) ((chunkSize >> 24) & 0xff);

        // Format
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // Subchunk1ID: fmt
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';

        // Subchunk1Size: 16 bytes, static
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;

        // AudioFormat: PCM = 1
        header[20] = 1;
        header[21] = 0;

        // NumChannels: Mono = 1, Stereo = 2, etc.
        header[22] = (byte) numChannels;
        header[23] = 0;

        // 24 4 SampleRate 8000, 44100, etc.
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);

        // 28 4 ByteRate == SampleRate * NumChannels * BitsPerSample/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        // 32 2 BlockAlign   == NumChannels * BitsPerSample/8
        header[32] = (byte) (blockAlign & 0xff);
        header[33] = 0;

        // 34 2 BitsPerSample  8 bits = 8, 16 bits = 16, etc.
        header[34] = (byte) (bitsPerSample & 0xff);
        header[35] = 0;

        // Subchunk2ID: data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';

        // 40 4 Subchunk2Size == NumSamples * NumChannels * BitsPerSample/8
        // big-endian
        header[40] = (byte) (subchunk2Size & 0xff);
        header[41] = (byte) ((subchunk2Size >> 8) & 0xff);
        header[42] = (byte) ((subchunk2Size >> 16) & 0xff);
        header[43] = (byte) ((subchunk2Size >> 24) & 0xff);

        return header;
    }

    public static int getPlayBufferSizeByWavHeader(WaveFileHeader header) {

//        int bufferSize = AudioRecord.getMinBufferSize(
//                header.sample_rate,
//                header.calcPlayBufferSize(),
//                header.audio_format
//        );
        return 0;  //###@:
    }

    public static int getAudioFormatByWavHeader(WaveFileHeader header) {
        return 0;  //###@:

    }

    public static int getChannelMaskByWavHeader(WaveFileHeader header) {
        return 0;  //###@:

    }

    public static void playWAV(String wavFilePath) {
        File wavFile = new File(wavFilePath);
        if (!wavFile.exists()) {
            LogUtils.te2("wav file " + wavFilePath + " not exists");
            return;
        }

        final FileInputStream fis;
        try {
            fis = new FileInputStream(wavFile);
            byte[] headerBytes = new byte[WAVE_FILE_HEADER_LEN];
            int readSize = fis.read(headerBytes);
            if (readSize != WAVE_FILE_HEADER_LEN) {
                LogUtils.te2("read wave header failed.");
                fis.close();
                return;
            }
            WaveFileHeader header = new WaveFileHeader();
            if (!header.parse(headerBytes)) {
                LogUtils.te2("read wave header failed.");
                fis.close();
                return;
            }
            playPCM(fis,
                    getPlayBufferSizeByWavHeader(header),
                    header.sample_rate,
                    getAudioFormatByWavHeader(header),
                    getChannelMaskByWavHeader(header)
            );
        } catch (Exception e) {
            LogUtils.te2("play wav file " + wavFilePath + " failed, ex = " + e.toString());
        }
    }

    public static void playPCM(String pcmFilePath, int bufferSize, int sampleRate, int audioFormat, int channelMask) {
        playPCM(new File(pcmFilePath), bufferSize, sampleRate, audioFormat, channelMask);
    }

    public static void playPCM(File pcmFile, int bufferSize, int sampleRate, int audioFormat, int channelMask) {
        if (pcmFile == null || !pcmFile.exists()) {
            LogUtils.e("playPCM: pcm file not exists");
        } else {
            try {
                playPCM(new FileInputStream(pcmFile), bufferSize, sampleRate, audioFormat, channelMask);
            } catch (Exception e) {
                LogUtils.te2("playPCM: file input stream exception " + e.toString());
            }
        }
    }

    public static void playPCM(final FileInputStream fis,
                               int bufferSize,
                               int sampleRate,
                               int audioFormat,
                               int channelMask) {

        final int bufferSizeInBytes = bufferSize;
//        final AudioTrack audioTrack = new AudioTrack(
//                new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build(),
//                new AudioFormat.Builder()
//                        .setSampleRate(sampleRate)
//                        .setEncoding(audioFormat)
//                        .setChannelMask(channelMask)
//                        .build(),
//                bufferSizeInBytes,
//                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
//        );

        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelMask,
            audioFormat, bufferSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);

        // AudioTrack need play first
        audioTrack.play();

        // run thread to feed audio track data(i.e. play)
        new Thread() {
            @Override
            public void run() {
                try {
                    byte[] buffer = new byte[bufferSizeInBytes];
                    while (fis.available() > 0) {
                        int readCount = fis.read(buffer);
                        if (readCount < 0) {
                            LogUtils.e("playPCM: read error " + readCount);
                        } else {
                            audioTrack.write(buffer, 0, readCount);
                        }
                    }
                } catch (Exception e) {
                    LogUtils.e("playPCM: exception " + e.toString());
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                    } catch (Exception e) {
                        LogUtils.e("close file input stream failed, ex = " + e.toString());
                    }
                    audioTrack.stop();
                    audioTrack.release();
                }
            }
        }.start();
    }

    /**
     * NOTE: take as pcm16
     *
     * @param waveAbsolutePath:
     * @param dataList:
     * @param capacity:
     */
    public static boolean loadWaveProfile(@NonNull String waveAbsolutePath, @NonNull List<Integer> dataList, int capacity) {
        if (capacity <= 0) {
            LogUtils.e2("loadWaveProfile: invalid capacity " + capacity);
            return false;
        }
        dataList.clear();

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(waveAbsolutePath, "r");
            long waveFileLen = raf.length() - WAVE_FILE_HEADER_LEN;
            int sampleBytes = 2;  // pcm16  ###@:
            if (waveFileLen <= capacity) {
                byte[] buf = new byte[(int) waveFileLen];
                raf.read(buf, 0, (int) waveFileLen);
                for (int i = 0; i < waveFileLen; i += 2) {
                    dataList.add(buf[i * 2 + 1] << 8 | buf[i * 2]);
                }
            } else {
                double scale = 1.0 * waveFileLen / capacity;
                byte[] buf = new byte[sampleBytes];
                for (int i = 0; i < capacity; i++) {
                    raf.seek((long) (i * scale) + WAVE_FILE_HEADER_LEN);
                    raf.read(buf);
                    dataList.add(buf[1] << 8 | buf[0]);
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("ERROR: addHeaderSimple: add header to " + waveAbsolutePath + " failed, ex = " + e.toString());
            return false;
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (Exception e) {
                System.out.println("ERROR: addHeaderSimple: close exception, ex = " + e.toString());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TEST MAIN

    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("***Test Begin...");

        RandomAccessFile raf = null;
        String waveAbsolutePath = "D:/Temp/test.wav";
        try {
            raf = new RandomAccessFile(waveAbsolutePath, "rw");
            modifyWaveFileHeader(raf, 1234);
        } catch (Exception e) {
            System.out.println("ERROR: addHeaderSimple: add header to " + waveAbsolutePath + " failed, ex = " + e.toString());
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (Exception e) {
                System.out.println("ERROR: addHeaderSimple: close exception, ex = " + e.toString());
            }
        }

        System.out.println("***Test Successfully.");
        System.out.println("\n");
    }
}
