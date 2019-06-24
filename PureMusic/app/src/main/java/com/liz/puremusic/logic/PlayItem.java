package com.liz.puremusic.logic;

import android.text.TextUtils;

/**
 * Node.java
 * Created by admin on 2018/8/30.
 */

@SuppressWarnings("unused")
public class PlayItem {
    private String mFilePath = "";
    private String mDuration = "";

    PlayItem() {
        this.mFilePath = "";
    }

    PlayItem(String filePath) {
        this.mFilePath = filePath;
    }

    String getFilePath() {
        return mFilePath;
    }

    String getDuration() {
        return mDuration;
    }

    void setDuration(String duration) {
        this.mDuration = duration;
    }

    public void setFilePath(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            this.mFilePath = "";
        }
        else {
            this.mFilePath = filePath;
        }
    }

    public String getFileName() {
        if (TextUtils.isEmpty(mFilePath)) {
            return "";
        }
        else {
            int index = mFilePath.lastIndexOf("/");
            if (index < 0) {
                return mFilePath;
            }
            else {
                return mFilePath.substring(index + 1);
            }
        }
    }
}
