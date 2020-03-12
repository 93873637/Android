package com.liz.whatsai.logic;

/**
 * Task.java
 * Created by liz on 2018/9/17.
 */

public class WSText extends WSFile {
    private int scroll_x;
    private int scroll_y;

    public WSText() {
        super();
        scroll_x = 0;
        scroll_y = 0;
    }

    public WSText(String name) {
        super(name);
        scroll_x = 0;
        scroll_y = 0;
    }

    @Override
    public int getType() {
        return ComDef.NODE_TYPE_TEXT;
    }

    public int getScrollX() {
        return scroll_x;
    }

    public void setScrollX(int x) {
        scroll_x = x;
    }

    public int getScrollY() {
        return scroll_y;
    }

    public void setScrollY(int y) {
        scroll_y = y;
    }
}
