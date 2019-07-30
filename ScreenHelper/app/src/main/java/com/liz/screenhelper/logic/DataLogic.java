package com.liz.screenhelper.logic;

import android.graphics.Bitmap;
import android.media.Image;

import com.liz.screenhelper.utils.BitmapQueue;
import com.liz.screenhelper.utils.LogUtils;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DataLogic {

    private static final BitmapQueue mBmpQueue = new BitmapQueue();
    private static int mImageWidth = 0;
    private static int mImageHeight = 0;

    private static Timer mDataTimer;
    private static int mFrameCount = 0;

    public static void init() {
        startDataTimer();
    }

    private static void startDataTimer() {
        mDataTimer = new Timer();
        mDataTimer.schedule(new TimerTask() {
            public void run () {
                //LogUtils.i("***FrameRate: " + mFrameCount);
                mFrameCount = 0;
            }
        }, 1000, 1000);
    }

    private static void stopDataTimer() {
        if (mDataTimer != null) {
            mDataTimer.cancel();
            mDataTimer = null;
        }
    }

    public static int getFrameRate() {
        return mFrameCount;
    }

    public static int getQueueSize() {
        return mBmpQueue.size();
    }

    public static String getImageSize() {
        return mImageWidth + "x" + mImageHeight;
    }

    private static Bitmap convertImageToBitmap(Image image) {
//        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//        byte[] bytes = new byte[buffer.capacity()];
//        buffer.get(bytes);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    public static void enQueueScreenImage(Image image) {
        synchronized (mBmpQueue) {
            if (mBmpQueue.size() == ComDef.MAX_SCREEN_BUFFER_QUEUE_SIZE) {
                mBmpQueue.poll();
            }

            Bitmap bmpScreen = convertImageToBitmap(image);
            int width = bmpScreen.getWidth();
            int height = bmpScreen.getHeight();
            mImageWidth = width / 4;
            mImageHeight = height / 4;
            Bitmap bmpQueue = Bitmap.createScaledBitmap(bmpScreen, mImageWidth, mImageHeight, true);
            mBmpQueue.add(bmpQueue);
            mBmpQueue.notifyAll();

            mFrameCount ++;
            //LogUtils.cbLog("DataLogic:enQueueScreenImage: size=" + mBmpQueue.size());
        }
    }

    public static Bitmap deQueueScreenImage() {
        synchronized (mBmpQueue) {
            try {
                if (mBmpQueue.size() == 0) {
                    mBmpQueue.wait();
                }

                //LogUtils.cbLog("DataLogic:deQueueScreenImage: size=" + mBmpQueue.size());
                return mBmpQueue.poll();
            } catch (Exception e) {
                LogUtils.cbLog("ERROR: DataLogic: dequeue screen image failed, ex=" + e.toString());
                e.printStackTrace();
                return null;
            }
        }
    }
}
