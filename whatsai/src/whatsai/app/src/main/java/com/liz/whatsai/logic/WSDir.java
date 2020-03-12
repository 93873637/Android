package com.liz.whatsai.logic;

/**
 * WSDir:
 * Created by liz on 2019/2/15.
 */

@SuppressWarnings("unused")
public class WSDir extends Node {

    public WSDir() {
        super();
    }

    public WSDir(String name) {
        super(name);
    }

    public int getType() {
        return ComDef.NODE_TYPE_DIR;
    }
}
