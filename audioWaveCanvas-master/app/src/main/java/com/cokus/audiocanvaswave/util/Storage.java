package com.cokus.audiocanvaswave.util;

import android.os.Environment;
import java.io.File;

public class Storage {

    public static final String DATA_DIRECTORY = Environment.getExternalStorageDirectory()
            + "/0.sd/temp";

    public static void createDirectory() {
        File file = new File(DATA_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
