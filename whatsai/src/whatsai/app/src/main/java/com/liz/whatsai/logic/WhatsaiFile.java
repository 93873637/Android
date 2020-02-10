package com.liz.whatsai.logic;

/**
 * WhatsaiFile:
 * Created by liz on 2019/2/15.
 */

@SuppressWarnings("WeakerAccess")
public class WhatsaiFile extends Node {

    public WhatsaiFile() {
        super();
    }

    public WhatsaiFile(String name) {
        super(name);
    }

    public int getType() {
        return ComDef.NODE_TYPE_FILE;
    }
}
