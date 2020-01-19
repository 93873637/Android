package com.liz.multidialer.net;

import com.jcraft.jsch.SftpProgressMonitor;
import com.liz.androidutils.LogUtils;

import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SFTPMonitor implements SftpProgressMonitor, Runnable {

    private static final long SCHEDULE_DELAY = 1;  //unit by seconds
    private static final long SCHEDULE_PERIOD = 5;  //unit by seconds

    private long mTotal;
    private long mStartTime = 0L;
    private long mUploaded = 0;
    private boolean mIsScheduled = false;
    private ScheduledExecutorService mExecutorService;

    SFTPMonitor(long total) {
        this.mTotal = total;
    }

    @Override
    public void run() {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        String value = format.format((mUploaded / (double) mTotal));
        LogUtils.d("已传输：" + mUploaded / 1024 + "KB, 传输进度：" + value);
        if (mUploaded == mTotal) {
            stop();
            long endTime = System.currentTimeMillis();
            LogUtils.d("传输完成！用时：" + (endTime - mStartTime) / 1000 + "s");
        }
    }

    /**
     * 输出每个时间段的上传大小
     */
    @Override
    public boolean count(long count) {
        //LogUtils.d("本次上传大小：" + count / 1024 + "KB,");
        if (!mIsScheduled) {
            createTread();
        }
        mUploaded += count;
        if (count > 0) {
            return true;
        }
        return false;
    }

    /**
     * 文件上传结束时调用
     */
    @Override
    public void end() {
        LogUtils.d("文件传输结束");
    }

    /**
     * 文件上传时开始调用
     */
    @Override
    public void init(int op, String src, String dest, long max) {
        LogUtils.d("开始上传文件：" + src + "至远程：" + dest + "文件总大小:" + (mTotal/1024) + "KB");
        mStartTime = System.currentTimeMillis();
    }

    /**
     * 创建一个线程每隔一定时间，输出一下上传进度
     */
    private void createTread() {
        mExecutorService = Executors.newSingleThreadScheduledExecutor();
        // 1秒钟后开始执行，每2杪钟执行一次
        mExecutorService.scheduleWithFixedDelay(this, SCHEDULE_DELAY, SCHEDULE_PERIOD, TimeUnit.SECONDS);
        mIsScheduled = true;
    }

    /**
     * 停止方法
     */
    private void stop() {
        boolean isShutdown = mExecutorService.isShutdown();
        if (!isShutdown) {
            mExecutorService.shutdown();
        }
    }
}
