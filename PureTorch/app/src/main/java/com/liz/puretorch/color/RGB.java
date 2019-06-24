package com.liz.puretorch.color;

/**
 * RGB:
 * Created by liz on 2019/1/6.
 */

public class RGB {
    int red;
    int green;
    int blue;

    public RGB(){ }

    public RGB(int red,int green,int blue){
        this.red = red;
        this.blue = blue;
        this.green = green;
    }

    public String toString() {
        return "RGB {" + red + ", " + green + ", " + blue + "}";
    }
}
