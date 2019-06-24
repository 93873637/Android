package com.liz.puretorch.color;

/**
 * HSL:
 * Created by liz on 2019/1/6.
 * HSL: hue（色相）、saturation（饱和度）、lightness（亮度）
 */
public class HSL {

    private float h = 0;
    private float s = 0;
    private float l = 0;

    public HSL() {
    }

    public HSL(float h, float s, float l) {
        setH(h);
        setS(s);
        setL(l);
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        if (h < 0) {
            this.h = 0;
        } else if (h > 360) {
            this.h = 360;
        } else {
            this.h = h;
        }
    }

    public float getS() {
        return s;
    }

    public void setS(float s) {
        if (s < 0) {
            this.s = 0;
        } else if (s > 255) {
            this.s = 255;
        } else {
            this.s = s;
        }
    }

    public float getL() {
        return l;
    }

    public void setL(float l) {
        if (l < 0) {
            this.l = 0;
        } else if (l > 255) {
            this.l = 255;
        } else {
            this.l = l;
        }
    }

    public String toString() {
        return (int)h + "," + (int)s + "," + (int)l;
    }
}
