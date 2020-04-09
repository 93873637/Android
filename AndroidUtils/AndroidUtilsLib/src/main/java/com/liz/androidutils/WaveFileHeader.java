package com.liz.androidutils;

import android.text.TextUtils;

/**
 *
 * The canonical WAVE format starts with the RIFF header:
 *
 * Offset  Size  Name             Description
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
 *
 *
 * As an example, here are the opening 72 bytes of a WAVE file with bytes shown as hexadecimal numbers:
 *
 * 52 49 46 46 24 08 00 00 57 41 56 45 66 6d 74 20 10 00 00 00 01 00 02 00
 * 22 56 00 00 88 58 01 00 04 00 10 00 64 61 74 61 00 08 00 00 00 00 00 00
 * 24 17 1e f3 3c 13 3c 14 16 f9 18 f9 34 e7 23 a6 3c f2 24 f2 11 ce 1a 0d
 *
 * Notes:
 * The default byte ordering assumed for WAVE data files is little-endian. Files written using the big-endian byte ordering scheme have the identifier RIFX instead of RIFF.
 * The sample data must end on an even byte boundary. Whatever that means.
 * 8-bit samples are stored as unsigned bytes, ranging from 0 to 255. 16-bit samples are stored as 2’s-complement signed integers, ranging from -32768 to 32767.
 * There may be additional subchunks in a Wave data stream. If so, each will have a char[4] SubChunkID, and unsigned long SubChunkSize, and SubChunkSize amount of data.
 * RIFF stands for Resource Interchange File Format.
 * General discussion of RIFF files:
 * Multimedia applications require the storage and management of a wide variety of data, including bitmaps, audio data, video data,
 * and peripheral device control information. RIFF provides a way to store all these varied types of data.
 * The type of data a RIFF file contains is indicated by the file extension. Examples of data that may be stored in RIFF files are:
 * Audio/visual interleaved data (.AVI)
 * Waveform data (.WAV)
 * Bitmapped data (.RDI)
 * MIDI information (.RMI)
 * Color palette (.PAL)
 * Multimedia movie (.RMN)
 * Animated cursor (.ANI)
 * A bundle of other RIFF files (.BND)
 * NOTE: At this point, AVI files are the only type of RIFF files that have been fully implemented using the current RIFF specification.
 * Although WAV files have been implemented, these files are very simple, and their developers typically use an older specification in constructing them.
 * For more info see http://www.ora.com/centers/gff/formats/micriff/index.htm
 *
 * References:
 * http://www.ora.com/centers/gff/formats/micriff/index.htm (good).
 * http://premium.microsoft.com/msdn/library/tools/dnmult/d1/newwave.htm
 * http://www.lightlink.com/tjweber/StripWav/WAVE.html
 *
 */

public class WaveFileHeader {

    public static final int CHUNK_ID_LEN = 4;
    public static final int CHUNK_SIZE_LEN = 4;
    public static final int FORMAT_LEN = 4;
    public static final int SUBCHUNK1_ID_LEN = 4;
    public static final int SUBCHUNK1_SIZE_LEN = 4;
    public static final short AUDIO_FORMAT_LEN = 2;
    public static final short NUM_CHANNELS_LEN = 2;
    public static final int SAMPLE_RATE_LEN = 4;
    public static final int BYTE_RATE_LEN = 4;
    public static final short BLOCK_ALIGN_LEN = 2;
    public static final short BITS_PER_SAMPLE_LEN = 2;
    public static final int SUBCHUNK2_ID_LEN = 4;
    public static final int SUBCHUNK2_SIZE_LEN = 4;

    public String chunk_id = "";
    public int chunk_size = 0;
    public String format = "";
    public String subchunk1_id = "";
    public int subchunk1_size = 0;
    public short audio_format = 0;
    public short num_channels = 0;  // 1 - mono, 2 - stereo
    public int sample_rate = 0;
    public int byte_rate = 0;
    public short block_align = 0;
    public short bits_per_sample = 0;
    public String subchunk2_id = "";
    public int subchunk2_size = 0;

    private int[][] data = null;
    private int len = 0;

    public WaveFileHeader() {
    }

    public boolean parse(byte[] headerBytes) {
        int offset = 0;

        chunk_id = readString(headerBytes, offset, CHUNK_ID_LEN);
        if (!chunk_id.equals("RIFF")) {
            loge("parse: invalid chunk id " + chunk_id);
            return false;
        }
        offset += CHUNK_ID_LEN;

        chunk_size = readInt(headerBytes, offset);
        offset += CHUNK_SIZE_LEN;

        format = readString(headerBytes, offset, FORMAT_LEN);
        if (!format.equals("WAVE")) {
            loge("parse: invalid format " + format);
            return false;
        }
        offset += FORMAT_LEN;

        subchunk1_id = readString(headerBytes, offset, SUBCHUNK1_ID_LEN);
        if (!subchunk1_id.equals("fmt ")) {
            loge("parse: invalid subchunk1 id " + subchunk1_id);
            return false;
        }
        offset += SUBCHUNK1_ID_LEN;

        subchunk1_size = readInt(headerBytes, offset);
        offset += SUBCHUNK1_SIZE_LEN;

        audio_format = readShort(headerBytes, offset);
        offset += AUDIO_FORMAT_LEN;

        num_channels = readShort(headerBytes, offset);
        offset += NUM_CHANNELS_LEN;

        sample_rate = readInt(headerBytes, offset);
        offset += SAMPLE_RATE_LEN;

        byte_rate = readInt(headerBytes, offset);
        offset += BYTE_RATE_LEN;

        sample_rate = readInt(headerBytes, offset);
        offset += SAMPLE_RATE_LEN;

        block_align = readShort(headerBytes, offset);
        offset += BLOCK_ALIGN_LEN;

        bits_per_sample = readShort(headerBytes, offset);
        offset += BITS_PER_SAMPLE_LEN;

        subchunk2_id = readString(headerBytes, offset, SUBCHUNK2_ID_LEN);
        if (!TextUtils.equals(subchunk2_id, "RIFF")) {
            loge("parse: invalid subchunk2 id " + subchunk2_id);
            return false;
        }
        offset += SUBCHUNK2_ID_LEN;

        subchunk2_size = readInt(headerBytes, offset);
        offset += SUBCHUNK2_SIZE_LEN;

        System.out.println("parse over, size = " + offset);
        return true;
    }

    private void loge(String msg) {
        System.out.println("ERROR: WaveFileHeader: " + msg);
    }

    private String readString(byte[] headBuf, int offset, int len) {
        byte[] buf = new byte[len];
        System.arraycopy(headBuf, offset, buf, 0, len);
        return new String(buf);
    }

    private int readInt(byte[] headBuf, int offset) {
        return headBuf[offset]
                + (headBuf[offset + 1] << 8)
                + (headBuf[offset + 2] << 16)
                + (headBuf[offset + 3] << 24);
    }

    private short readShort(byte[] headBuf, int offset) {
        short s = (short)(headBuf[offset + 1] << 8);
        s += headBuf[offset];
        return s;
    }
}
