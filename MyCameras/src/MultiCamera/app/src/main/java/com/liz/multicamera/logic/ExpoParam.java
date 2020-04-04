package com.liz.multicamera.logic;

/**
 * ExpoParam: parameters for exposure once
 * Created by liz on 2018/12/29.
 */

public class ExpoParam {
    public int iso_value;
    public float exposure_time;    //unit by ms
    public int frame_num;    //picture number with the exposure parameters

    public ExpoParam(int iso_value, float exposure_time, int frame_num) {
        this.iso_value = iso_value;
        this.exposure_time = exposure_time;
        this.frame_num = frame_num;
    }

    public float expVal() {
        return iso_value * exposure_time;
    }
}
