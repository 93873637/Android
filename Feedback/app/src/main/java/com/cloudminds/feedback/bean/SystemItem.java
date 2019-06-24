package com.cloudminds.feedback.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by root on 17-7-14.
 */

public class SystemItem {
    public String itemName;
    public String packName;
    public Drawable icon;

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getPackName() {
        return packName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }
}
