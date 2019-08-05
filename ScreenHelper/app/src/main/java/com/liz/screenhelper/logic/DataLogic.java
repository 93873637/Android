package com.liz.screenhelper.logic;

import android.graphics.Bitmap;
import android.media.Image;

import com.liz.androidutils.BitmapQueue;
import com.liz.androidutils.ByteBufferQueue;
import com.liz.androidutils.LogUtils;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DataLogic {

    private static final BitmapQueue mBmpQueue = new BitmapQueue();
    private static final ByteBufferQueue mByteBufferQueue = new ByteBufferQueue();

    private static int mImageWidth = 0;
    private static int mImageHeight = 0;

    private static Timer mScreenChangeTimer;

    private static int mFrameCount = 0;
    private static int mFrameRate = 0;
    private static Timer mDataTimer;

    public static void init() {
        startDataTimer();
        //startScreenChangeTimer();
    }

    private static void startDataTimer() {
        mDataTimer = new Timer();
        mDataTimer.schedule(new TimerTask() {
            public void run () {
                mFrameRate = mFrameCount;
                LogUtils.d("***FrameRate: " + mFrameRate);
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

    private static void startScreenChangeTimer() {
        mScreenChangeTimer = new Timer();
        mScreenChangeTimer.schedule(new TimerTask() {
            public void run () {
                LogUtils.d("I'm Screen Changer!!!");
            }
        }, 1000, 10);
    }

    private static void stopScreenChangeTimer() {
        if (mScreenChangeTimer != null) {
            mScreenChangeTimer.cancel();
            mScreenChangeTimer = null;
        }
    }

    public static int getFrameRate() {
        return mFrameRate;
    }

    public static int getQueueSize() {
        //return mBmpQueue.size();
        return mByteBufferQueue.size();
    }

    public static String getImageSize() {
        return mImageWidth + "x" + mImageHeight;
    }

    private static Bitmap Image2Bitmap(Image image) {
//        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//        byte[] bytes = new byte[buffer.capacity()];
//        buffer.get(bytes);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        int width = image.getWidth();
        int height = image.getHeight();
        //LogUtils.d("image Size: " + width + "x" + height);
        //LogUtils.d("image Format: " + image.getFormat());

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        //LogUtils.d("buffer position=" + buffer.position());
        //LogUtils.d("buffer limit=" + buffer.limit());
        //LogUtils.d("buffer capacity=" + buffer.capacity());

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        //LogUtils.d("pixelStride=" + pixelStride);
        //LogUtils.d("rowStride=" + rowStride);

        int rowPadding = rowStride - pixelStride * width;
        //LogUtils.d("rowPadding=" + rowPadding);

        ////////////////////////////////////////////////////////////////////
        //image Size: 1440 x 2392
        //bmp Size: 1472 x 2392
        //pixelStride = 4
        //rowStride = 5888 (64*23)
        //rowPadding = 128
        //buffer capacity = 14084096 = 5888*2392 = (1440 + 32) * 4 * 2392
        ////////////////////////////////////////////////////////////////////

        int bmpWidth = width + rowPadding / pixelStride;
        //LogUtils.d("bmp Size: " + bmpWidth + "x" + height);

        Bitmap bitmap = Bitmap.createBitmap(bmpWidth, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Bitmap bmpScreen = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        return Bitmap.createScaledBitmap(bmpScreen, width/4, height/4, true);
    }

    public static void enQueueScreenImage(Image image) {
        enQueueScreenBuffer(image);
    }

    public static ByteBuffer deQueueScreenImage() {
        return deQueueScreenBuffer();
    }

    public static void enQueueScreenBmp(Image image) {
        synchronized (mBmpQueue) {
            if (mBmpQueue.size() == ComDef.MAX_SCREEN_BUFFER_QUEUE_SIZE) {
                mBmpQueue.poll();
            }

            Bitmap bmp = Image2Bitmap(image);
            if (bmp == null) {
                LogUtils.e("ERROR: convert image to bitmap failed.");
            }
            else {
                mImageWidth = bmp.getWidth();
                mImageHeight = bmp.getHeight();
                mBmpQueue.add(bmp);
                mBmpQueue.notifyAll();
                mFrameCount++;
                //LogUtils.v("DataLogic:enQueueScreenImage: size=" + mBmpQueue.size());
            }
        }
    }

    public static Bitmap deQueueScreenBmp() {
        synchronized (mBmpQueue) {
            try {
                if (mBmpQueue.size() == 0) {
                    mBmpQueue.wait();
                }

                //LogUtils.d("DataLogic:deQueueScreenImage: size=" + mBmpQueue.size());
                return mBmpQueue.poll();
            } catch (Exception e) {
                LogUtils.d("ERROR: DataLogic: dequeue screen image failed, ex=" + e.toString());
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void enQueueScreenBuffer(Image image) {
        synchronized (mByteBufferQueue) {
            if (mByteBufferQueue.size() == ComDef.MAX_SCREEN_BUFFER_QUEUE_SIZE) {
                mByteBufferQueue.poll();
            }

            mImageWidth = image.getWidth();
            mImageHeight = image.getHeight();

            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            mByteBufferQueue.add(buffer);
            mByteBufferQueue.notifyAll();
            mFrameCount++;
        }
    }

    public static ByteBuffer deQueueScreenBuffer() {
        synchronized (mByteBufferQueue) {
            try {
                if (mByteBufferQueue.size() == 0) {
                    mByteBufferQueue.wait();
                }

                //LogUtils.d("DataLogic:deQueueScreenImage: size=" + mBmpQueue.size());
                return mByteBufferQueue.poll();
            } catch (Exception e) {
                LogUtils.d("ERROR: DataLogic: dequeue screen image failed, ex=" + e.toString());
                e.printStackTrace();
                return null;
            }
        }
    }
}
