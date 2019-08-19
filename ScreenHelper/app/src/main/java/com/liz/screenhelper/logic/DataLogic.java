package com.liz.screenhelper.logic;

import android.graphics.Bitmap;
import android.media.Image;

import com.liz.androidutils.ComUtils;
import com.liz.androidutils.ImageUtils;
import com.liz.androidutils.LogUtils;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DataLogic{

    private static final LinkedList<Image> mImageQueue = new LinkedList<>();
    private static final LinkedList<Bitmap> mBmpQueue = new LinkedList<>();
    private static final LinkedList<ByteBuffer> mBufferQueue = new LinkedList<>();
    private static final LinkedList<byte[]> mDataQueue = new LinkedList<>();

    private static int mImageWidth = 0;
    private static int mImageHeight = 0;

    private static Timer mScreenChangeTimer;

    private static int mFrameCount = 0;
    private static int mFrameRate = 0;
    private static Timer mDataTimer;

    public static void init() {
        startDataTimer();
    }

    public static String getImageSize() {
        return mImageWidth + "x" + mImageHeight;
    }

    public static int getFrameRate() {
        return mFrameRate;
    }

    public static void enqueueScreenImage(Image image) {
        //enqueueScreenBuffer(image);
        enqueueScreenData(image);
    }

    public static Image dequeueScreenImage() {
        return mImageQueue.poll();
    }

    public static int getQueueSize() {
        return mDataQueue.size();
    }

    public static void enqueueScreenData(Image image) {
        synchronized (mDataQueue) {
            if (mDataQueue.size() == ComDef.MAX_SCREEN_IMAGE_QUEUE_SIZE) {
                mDataQueue.poll();
            }
            mImageWidth = image.getWidth();
            mImageHeight = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            byte[] data = ComUtils.byteBuf2Arr(buffer);
            mDataQueue.add(data);
            mDataQueue.notifyAll();
            mFrameCount++;
        }
    }

    public static byte[] dequeueScreenData() {
        synchronized (mDataQueue) {
            try {
                if (mDataQueue.size() == 0) {
                    mDataQueue.wait();
                }
                return mDataQueue.poll();
            } catch (Exception e) {
                LogUtils.e("DataLogic: dequeue screen data failed, ex=" + e.toString());
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void enqueueScreenBmp(Image image) {
        synchronized (mBmpQueue) {
            if (mBmpQueue.size() == ComDef.MAX_SCREEN_IMAGE_QUEUE_SIZE) {
                mBmpQueue.poll();
            }

            Bitmap bmp = ImageUtils.image2Bitmap(image);
            if (bmp == null) {
                LogUtils.e("ERROR: convert image to bitmap failed.");
            }
            else {
                mImageWidth = bmp.getWidth();
                mImageHeight = bmp.getHeight();
                mBmpQueue.add(bmp);
                mBmpQueue.notifyAll();
                //LogUtils.v("DataLogic:enqueueScreenImage: size=" + mBmpQueue.size());
            }
        }
    }

    public static Bitmap dequeueScreenBmp() {
        synchronized (mBmpQueue) {
            try {
                if (mBmpQueue.size() == 0) {
                    mBmpQueue.wait();
                }

                return mBmpQueue.poll();
            } catch (Exception e) {
                LogUtils.d("ERROR: DataLogic: dequeue screen image failed, ex=" + e.toString());
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void enqueueScreenBuffer(Image image) {
        synchronized (mBufferQueue) {
            if (mBufferQueue.size() == ComDef.MAX_SCREEN_IMAGE_QUEUE_SIZE) {
                mBufferQueue.poll();
            }
            mImageWidth = image.getWidth();
            mImageHeight = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();

            //####@:
            byte[] data = ComUtils.byteBuf2Arr(buffer);

            mBufferQueue.add(buffer);
            mBufferQueue.notifyAll();
            mFrameCount++;
        }
    }

    public static ByteBuffer deQueueScreenBuffer() {
        synchronized (mBufferQueue) {
            try {
                if (mBufferQueue.size() == 0) {
                    mBufferQueue.wait();
                }
                return mBufferQueue.poll();
            } catch (Exception e) {
                LogUtils.d("ERROR: DataLogic: dequeue screen image failed, ex=" + e.toString());
                e.printStackTrace();
                return null;
            }
        }
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
}
