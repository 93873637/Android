package com.liz.puretorch.color;

/**
 * HSV:
 * Created by liz on 2019/1/6.
 * HSL 和 HSV（也叫做 HSB）是对RGB 色彩空间中点的两种有关系的表示，它们尝试描述比 RGB 更准确的感知颜色联系，并仍保持在计算上简单。
 * HSL 表示 hue（色相）、saturation（饱和度）、lightness（亮度），HSV 表示 hue、saturation、value(色调) 而 HSB 表示 hue、saturation、brightness（明度）。
 *
 */

public class HSV {

    private double h = 0;
    private double s = 0;
    private double v = 0;

    public HSV(double h, double s, double v) {
        this.h = h;
        this.s = s;
        this.v = v;
    }

    public String toString() {
        return (int)h + "," + (int)s + "," + (int)v;
    }
}
