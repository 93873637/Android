package com.liz.androidutils;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

@SuppressWarnings("unused, WeakerAccess")
public class AudioUtils {

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
            default:
                return "UNKNOWN AUDIO SOURCE";
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
            default:
                return "UNKNOWN AUDIO FORMAT";
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
            default:
                return "UNKNOWN CHANNEL CONFIG";
        }
    }

    public static int channelNumByConfig(int channelConfig) {
        switch (channelConfig) {
            case AudioFormat.CHANNEL_CONFIGURATION_MONO: return 1;
            case AudioFormat.CHANNEL_CONFIGURATION_STEREO: return 2;
            case AudioFormat.CHANNEL_IN_MONO: return 1;
            case AudioFormat.CHANNEL_IN_STEREO: return 2;   // i.e. CHANNEL_OUT_STEREO
            case AudioFormat.CHANNEL_OUT_MONO: return 1;
            case AudioFormat.CHANNEL_OUT_QUAD: return 1;
            default:
                return -1;  // unknown channel config
        }
    }

    public static int bitNumByAudioFormat(int audioFormat) {
        return byteNumByAudioFormat(audioFormat) * 8;
    }

    public static int byteNumByAudioFormat(int audioFormat) {
        switch (audioFormat) {
            case AudioFormat.ENCODING_PCM_8BIT: return 1;
            case AudioFormat.ENCODING_PCM_16BIT: return 2;
            default:
                return -1;  // unknown audio format
        }
    }

    /**
     * transfer pcm file to wave file, and rename file name to *.wav
     *
     * @param pcmFileAbsolute: pcm file name with full path to read from(*.pcm)
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
     * @param pcmFileAbsolute: pcm file name with full path to read from(*.pcm)
     * @param wavFileAbsolute: wave file name with full path to save to(*.wav)
     * @param sampleRate:
     * @param recorderBufferSize:
     * @param audioFormat:
     * @param channelConfig: AudioFormat.CHANNEL_CONFIGURATION_MONO
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
        int subchunk2Size = (int)FileUtils.getFileSize(pcmFileAbsolute);

        // ChunkSize = 36 + SubChunk2Size
        int chunkSize = subchunk2Size + 36;

        FileUtils.addHeader(pcmFileAbsolute, wavFileAbsolute,
                buildWaveFileHeader(chunkSize, numChannels, sampleRate, byteRate, blockAlign, bitsPerSample, subchunk2Size));

        if (deletePCM) {
            FileUtils.delete(pcmFileAbsolute);
        }
    }

    /**
     * The canonical WAVE format starts with the RIFF header:
     *
     * 0         4   ChunkID          Contains the letters "RIFF" in ASCII form
     *                                (0x52494646 big-endian form).
     * 4         4   ChunkSize        36 + SubChunk2Size, or more precisely:
     *                                4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
     *                                This is the size of the rest of the chunk
     *                                following this number.  This is the size of the
     *                                entire file in bytes minus 8 bytes for the
     *                                two fields not included in this count:
     *                                ChunkID and ChunkSize.
     * 8         4   Format           Contains the letters "WAVE"
     *                                (0x57415645 big-endian form).
     *
     * The "WAVE" format consists of two subchunks: "fmt " and "data":
     * The "fmt " subchunk describes the sound data's format:
     *
     * 12        4   Subchunk1ID      Contains the letters "fmt "
     *                                (0x666d7420 big-endian form).
     * 16        4   Subchunk1Size    16 for PCM.  This is the size of the
     *                                rest of the Subchunk which follows this number.
     * 20        2   AudioFormat      PCM = 1 (i.e. Linear quantization)
     *                                Values other than 1 indicate some
     *                                form of compression.
     * 22        2   NumChannels      Mono = 1, Stereo = 2, etc.
     * 24        4   SampleRate       8000, 44100, etc.
     * 28        4   ByteRate         == SampleRate * NumChannels * BitsPerSample/8
     * 32        2   BlockAlign       == NumChannels * BitsPerSample/8
     *                                The number of bytes for one sample including
     *                                all channels. I wonder what happens when
     *                                this number isn't an integer?
     * 34        2   BitsPerSample    8 bits = 8, 16 bits = 16, etc.
     *           2   ExtraParamSize   if PCM, then doesn't exist
     *           X   ExtraParams      space for extra parameters
     *
     * The "data" subchunk contains the size of the data and the actual sound:
     *
     * 36        4   Subchunk2ID      Contains the letters "data"
     *                                (0x64617461 big-endian form).
     * 40        4   Subchunk2Size    == NumSamples * NumChannels * BitsPerSample/8
     *                                This is the number of bytes in the data.
     *                                You can also think of this as the size
     *                                of the read of the subchunk following this
     *                                number.
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

        byte[] header = new byte[44];

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
        header[40] = (byte) (subchunk2Size & 0xff);
        header[41] = (byte) ((subchunk2Size >> 8) & 0xff);
        header[42] = (byte) ((subchunk2Size >> 16) & 0xff);
        header[43] = (byte) ((subchunk2Size >> 24) & 0xff);

        return header;
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

    public static void playPCM16(final ArrayList<Integer> dataList, final int bufferSize, int sampleRate, int channelMask) {
        if (dataList == null || dataList.isEmpty()) {
            LogUtils.e("playPCM: data list empty");
            return;
        }

        int encodingBits = AudioFormat.ENCODING_PCM_16BIT;
        final int byteNum = byteNumByAudioFormat(encodingBits);
        final int bufferSizeInShorts = bufferSize / byteNum;

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
                bufferSize,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
        );

        // AudioTrack need play first
        audioTrack.play();

        // run read and play task
        new Thread() {
            @Override
            public void run() {
                try {
                    short[] buffer = new short[bufferSizeInShorts];
                    int dataLeft = dataList.size();
                    int readCount;
                    while (dataLeft > 0) {
                        readCount = (dataLeft > bufferSizeInShorts) ? bufferSizeInShorts : dataLeft;
                        for (int i=0; i<readCount; i++) {
                            buffer[i] = (short)(dataList.get(i) & 0xffff);
                        }
                        audioTrack.write(buffer, 0, readCount);
                        dataLeft -= bufferSizeInShorts;
                    }
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
