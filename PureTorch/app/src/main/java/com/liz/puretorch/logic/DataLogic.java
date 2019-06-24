package com.liz.puretorch.logic;

import com.liz.puretorch.color.ColorUtils;
import com.liz.puretorch.color.RGB;
import com.liz.puretorch.utils.NumOp;

/**
 * DataLogic.java
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class DataLogic extends ComDef {

    public static int red = 0;
    public static int green = 0;
    public static int blue = 0;

    public static int lux = 0;

    public static void init() {
        red = RED_MAX;
        green = GREEN_MAX;
        blue = BLUE_MAX;
        lux = BRIGHT_MAX;
    }

    public static String getTorchInfo() {
        StringBuilder sb = new StringBuilder();

        sb.append("RGB=");
        sb.append(red);
        sb.append(",");
        sb.append(green);
        sb.append(",");
        sb.append(blue);
        sb.append("(");
        sb.append(NumOp.int2hex(red));
        sb.append(",");
        sb.append(NumOp.int2hex(green));
        sb.append(",");
        sb.append(NumOp.int2hex(blue));
        sb.append(")");
        sb.append("  ");

        sb.append("HSV=");
        sb.append(getHSV());
        sb.append("  ");

        sb.append("HSL=");
        sb.append(getHSL());
        sb.append("  ");

        sb.append("L=");
        sb.append(lux);
        return sb.toString();
    }

    public static String getHSV() {
        return ColorUtils.RGB2HSV(new RGB(red, green, blue)).toString();
    }

    public static String getHSL() {
        return ColorUtils.RGB2HSL(new RGB(red, green, blue)).toString();
    }
}
