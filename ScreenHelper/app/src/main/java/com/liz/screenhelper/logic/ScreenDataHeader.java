package com.liz.screenhelper.logic;

import com.liz.androidutils.NumUtils;

/**
 * Total Length: 13
 * Flag(5), Seq(4), Size(4)
 */
@SuppressWarnings("unused, WeakerAccess")
public class ScreenDataHeader {

    public static int seq = 0;

    public static final int HEADER_FLAG_LEN = ComDef.SCREEN_IMAGE_HEADER_FLAG_LEN;
    public static final int HEADER_SEQ_LEN = 4;   //sizeof(int)
    public static final int HEADER_SIZE_LEN = 4;   //sizeof(int)
    public static final int HEADER_LEN = HEADER_FLAG_LEN + HEADER_SEQ_LEN + HEADER_SIZE_LEN;

    public byte[] bytes;

    public ScreenDataHeader(int size) {
        bytes = new byte[HEADER_LEN];
        System.arraycopy(ComDef.SCREEN_IMAGE_HEADER_FLAG_BYTES, 0, bytes, 0, ComDef.SCREEN_IMAGE_HEADER_FLAG_LEN);
        System.arraycopy(NumUtils.int2Bytes(++seq), 0, bytes, ComDef.SCREEN_IMAGE_HEADER_FLAG_LEN, HEADER_SEQ_LEN);
        System.arraycopy(NumUtils.int2Bytes(size), 0, bytes, ComDef.SCREEN_IMAGE_HEADER_FLAG_LEN + HEADER_SEQ_LEN, HEADER_SIZE_LEN);
    }
}
