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
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height/2, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return null;


        https://ask.csdn.net/questions/259327
        https://blog.csdn.net/itheima6/article/details/7516631

        以下代码拿到的结果图尺寸与原图保持一致

        ByteBuffer mOutBuffer = ByteBuffer.allocate(width*2 * height*2 * 4);
        mOutBuffer.rewind();
        mBitmap.copyPixelsToBuffer(mOutBuffer);
        upscale.upScale(mOutBuffer.array(), mBitmap.getWidth(), mBitmap.getHeight());
        mOutBuffer.rewind();
        mBitmap.copyPixelsFromBuffer(mOutBuffer);

        public void upScale(byte[] bgradata,int width, int height)
        {
            float zoomFactor = 2.0f;
            int format_in = CVImageFormat.CV_PIX_FMT_BGRA8888;
            int w_in = width;
            int h_in = height;
            int s_in = w_in * 4;
            int format_out = format_in;
            int w_out = w_in * (int)zoomFactor;
            int h_out = h_in * (int)zoomFactor;
            int s_out = w_out * 4;
            int rst = CvImageApiBridge.IMAGESDK_INSTANCE.cv_imagesdk_upscale(
                    bgradata,format_in, w_in, h_in, s_in,
                    bgradata, format_out, w_out, h_out, s_out,
                    zoomFactor,2.0f,1,true);
            if (rst != ResultCode.CV_OK.getResultCode()) {
                throw new RuntimeException("Calling cv_imagesdk_upscale method failed! ResultCode=" + rst);
            }
        }

        //return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    public static void enQueueScreenImage(Image image) {
        synchronized (mBmpQueue) {
            if (mBmpQueue.size() == ComDef.MAX_SCREEN_BUFFER_QUEUE_SIZE) {
                mBmpQueue.poll();
            }

            Bitmap bmpScreen = convertImageToBitmap(image);
//            int width = bmpScreen.getWidth();
//            int height = bmpScreen.getHeight();
//            mImageWidth = width / 4;
//            mImageHeight = height / 4;
//            Bitmap bmpQueue = Bitmap.createScaledBitmap(bmpScreen, mImageWidth, mImageHeight, true);
//            mBmpQueue.add(bmpQueue);
//            mBmpQueue.notifyAll();

            mFrameCount ++;
            //LogUtils.d("DataLogic:enQueueScreenImage: size=" + mBmpQueue.size());
        }
    }

    public static Bitmap deQueueScreenImage() {
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
}
