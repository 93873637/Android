package com.liz.whatsai.test;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.liz.androidutils.AudioUtils;
import com.liz.whatsai.R;
import com.liz.whatsai.logic.ComDef;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class testAudioRecordActivity extends AppCompatActivity implements Runnable {

    private Button mBtnStartRecord;
    private Button mBtnStopRecord;
    private Button mBtnPlayPCM;

    //指定音频源 这个和MediaRecorder是相同的 MediaRecorder.AudioSource.MIC指的是麦克风
    private static final int mAudioSource = MediaRecorder.AudioSource.MIC;

    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz = 44100;

    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道

    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mBufferSizeInBytes;

    private File mRecordingFile;//储存AudioRecord录下来的文件
    private boolean isRecording = false; //true表示正在录音
    private AudioRecord mAudioRecord = null;
    private File mFileRoot = null;//文件目录
    //存放的目录路径名称
    private static final String mPathName = ComDef.WHATSAI_AUDIO_DIR;
    //保存的音频文件名
    private static final String mFileName = "audio_record_test.pcm";
    //缓冲区中数据写入到数据，因为需要使用IO操作，因此读取数据的过程应该在子线程中执行。
    private Thread mThread;
    private DataOutputStream mDataOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_audio_record);

        initData();
        initUI();
    }

    private void initData() {
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);//计算最小缓冲区
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRateInHz, mChannelConfig,
                mAudioFormat, mBufferSizeInBytes);//创建AudioRecorder对象
        mFileRoot = new File(mPathName);
        if (!mFileRoot.exists()) {
            mFileRoot.mkdirs();
        }
    }

    private void initUI() {
        mBtnStartRecord = findViewById(R.id.btn_start_record);
        mBtnStartRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });

        mBtnStopRecord = findViewById(R.id.btn_stop_record);
        mBtnStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });

        mBtnPlayPCM = findViewById(R.id.btn_play_or_pause);
        mBtnPlayPCM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPCM();
            }
        });
    }

    public void startRecord() {

        //AudioRecord.getMinBufferSize的参数是否支持当前的硬件设备
        if (AudioRecord.ERROR_BAD_VALUE == mBufferSizeInBytes || AudioRecord.ERROR == mBufferSizeInBytes) {
            throw new RuntimeException("Unable to getMinBufferSize");
        } else {
            destroyThread();
            isRecording = true;
            if (mThread == null) {
                mThread = new Thread(this);
                mThread.start();//开启线程
            }
        }
    }

    /**
     * 销毁线程方法
     */
    private void destroyThread() {
        try {
            isRecording = false;
            if (null != mThread && Thread.State.RUNNABLE == mThread.getState()) {
                try {
                    Thread.sleep(500);
                    mThread.interrupt();
                } catch (Exception e) {
                    mThread = null;
                }
            }
            mThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mThread = null;
        }
    }

    //停止录音
    public void stopRecord() {
        isRecording = false;
        //停止录音，回收AudioRecord对象，释放内存
        if (mAudioRecord != null) {
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {//初始化成功
                mAudioRecord.stop();
            }
            if (mAudioRecord != null) {
                mAudioRecord.release();
            }
        }
    }

    @Override
    public void run() {

        //标记为开始采集状态
        isRecording = true;
        //创建一个流，存放从AudioRecord读取的数据
        mRecordingFile = new File(mFileRoot, mFileName);
        if (mRecordingFile.exists()) {//音频文件保存过了删除
            mRecordingFile.delete();
        }
        try {
            mRecordingFile.createNewFile();//创建新文件
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("lu", "创建储存音频文件出错");
        }

        try {
            //获取到文件的数据流
            mDataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mRecordingFile)));
            byte[] buffer = new byte[mBufferSizeInBytes];

            //判断AudioRecord未初始化，停止录音的时候释放了，状态就为STATE_UNINITIALIZED
            if (mAudioRecord.getState() == mAudioRecord.STATE_UNINITIALIZED) {
                initData();
            }

            mAudioRecord.startRecording();//开始录音
            //getRecordingState获取当前AudioReroding是否正在采集数据的状态
            while (isRecording && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                int bufferReadResult = mAudioRecord.read(buffer, 0, mBufferSizeInBytes);
                for (int i = 0; i < bufferReadResult; i++) {
                    mDataOutputStream.write(buffer[i]);
                }
            }
            mDataOutputStream.close();
        } catch (Throwable t) {
            Log.e("lu", "Recording Failed");
            stopRecord();
        }
    }

    private void playPCM() {
        AudioUtils.playPCM(mRecordingFile, mBufferSizeInBytes, mSampleRateInHz, mAudioFormat, mChannelConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyThread();
        stopRecord();
    }
}
