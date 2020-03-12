package com.liz.whatsai.logic;

/**
 * WSFile:
 * Created by liz on 2019/2/15.
 */

@SuppressWarnings("WeakerAccess")
public class WSFile extends Node {

    public WSFile() {
        super();
    }

    public WSFile(String name) {
        super(name);
    }

    public int getType() {
        return ComDef.NODE_TYPE_FILE;
    }
}
