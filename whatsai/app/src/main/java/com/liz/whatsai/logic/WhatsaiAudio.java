package com.liz.whatsai.logic;

public class WhatsaiAudio {
    private static boolean is_open = false;

    public static boolean isOpen() {
        return is_open;
    }

    public static void switchAudio() {
        if (is_open) {
            closeAudio();
        }
        else {
            openAudio();
        }
    }

    public static void openAudio() {
        //####@:
        is_open = true;
    }

    public static void closeAudio() {
        //####@:
        is_open = false;
    }
}
