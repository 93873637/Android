package com.liz.mydish.logic;

import android.media.Image;

public class Dish {

    public String name;
    public Image picture;
    public boolean dad_can;
    public boolean mum_can;
    public boolean grandma_can;

    public Dish(String name) {
        this.name = name;
    }
}
