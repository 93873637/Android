package com.liz.androidutils;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;

/**
 * TroubleMaker.java: simulate creating some exceptions in android system
 * Created by liz on 2018/3/9.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class TroubleMaker {

    public static void createThreadException(Context context) throws IOException {

        final Context c = context;
        class CustomerThread extends Thread {
            public CustomerThread(String threadName) {
                super(threadName);
            }
            @Override
            public void run() {
                TroubleMaker.createAppException(c);
            }
        }
        new CustomerThread("CustomerThread").start();
    }

    public static void createAppException(Context context) {
        createNullException(context);
    }

    public static void createNullException(Context context) {
        String a = null;
        if (a.equals(context.toString())) {
            a.isEmpty();
        }
    }

    public static void createAppANR(Context context) {
        for (;;) {
            try {
                Toast.makeText(context, "trap into a dead loop...", Toast.LENGTH_LONG).show();
                Thread.currentThread().sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createAppCrash(Context context) {
        Toast.makeText(context, "TODO: make a native crash...", Toast.LENGTH_LONG).show();
    }
}
