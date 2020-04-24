package com.liz.mydish.logic;

import android.media.Image;

public class Dish {

    public String name;
    public Image picture = null;
    public boolean dad_can = false;
    public boolean mum_can = false;
    public boolean grandma_can = false;
    public int resId;

    public Dish(String name, int resId) {
        this.name = name;
        this.resId = resId;
    }
}
