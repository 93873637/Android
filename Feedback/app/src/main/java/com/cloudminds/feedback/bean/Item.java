package com.cloudminds.feedback.bean;

/**
 * Created by root on 17-7-14.
 */

public class Item {
    public String itemName;
    public String itemImageKey;

    public String getName() {
        return itemName;
    }

    public void setName(String name) {
        this.itemName = name;
    }

    public String getImageKey() {
        return itemImageKey;
    }

    public void setImageKey(String imageKey) {
        this.itemImageKey = imageKey;
    }

}
