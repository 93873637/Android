package com.liz.screenhelper.logic;

import android.graphics.Bitmap;
import android.media.Image;

import com.liz.screenhelper.utils.BitmapQueue;
import com.liz.screenhelper.utils.LogUtils;

import java.nio.ByteBuffer;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DataLogic {

    private static BitmapQueue mBmpQueue = new BitmapQueue();

    public static int getQueueSize() {
        return mBmpQueue.size();
    }

    public static void enQueueScreenImage(Image image) {
        synchronized (mBmpQueue) {
            if (mBmpQueue.size() == ComDef.MAX_SCREEN_BUFFER_QUEUE_SIZE) {
                mBmpQueue.poll();
            }

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);

            mBmpQueue.add(bitmap);
            //LogUtils.cbLog("DataLogic:enQueueScreenImage: size=" + mBmpQueue.size());
            mBmpQueue.notifyAll();
        }
    }

    public static Bitmap deQueueScreenImage() {
        synchronized (mBmpQueue) {
            try {
                if (mBmpQueue.size() == 0) {
                    mBmpQueue.wait();
                }

                Bitmap bmp = mBmpQueue.poll();
                //LogUtils.cbLog("DataLogic:deQueueScreenImage: size=" + mBmpQueue.size());
                return bmp;
            } catch (Exception e) {
                LogUtils.cbLog("ERROR: DataLogic: dequeue screen image failed, ex=" + e.toString());
                e.printStackTrace();
                return null;
            }
        }
    }
}
