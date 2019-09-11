package com.liz.whatsai.logic;

/**
 * Task.java
 * Created by liz on 2018/9/17.
 */

public class WhatsaiText extends WhatsaiFile {
    private boolean done = false;

    public WhatsaiText() {
        super();
        done = false;
    }

    public WhatsaiText(String name) {
        super(name);
        done = false;
    }

    @Override
    public int getType() {
        return ComDef.NODE_TYPE_TEXT;
    }
}
