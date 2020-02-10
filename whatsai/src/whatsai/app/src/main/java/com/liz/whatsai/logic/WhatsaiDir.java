package com.liz.whatsai.logic;

import android.graphics.Bitmap;

/**
 * WhatsaiDir:
 * Created by liz on 2019/2/15.
 */

@SuppressWarnings("unused")
public class WhatsaiDir extends Node {

    public WhatsaiDir() {
        super();
    }

    public WhatsaiDir(String name) {
        super(name);
    }

    public int getType() {
        return ComDef.NODE_TYPE_DIR;
    }
}
