package com.liz.whatsai.logic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.liz.androidutils.AudioUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.NumUtils;
import com.liz.androidutils.TimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("WeakerAccess")
public class WhatsaiListener {

    private static final int MAX_POWER = 32768;  // for pcm 16bits

    @SuppressWarnings("unused")
    public static final int AUDIO_SAMPLE_RATE_8K  =  8000;
    @SuppressWarnings("unused")
    public static final int AUDIO_SAMPLE_RATE_16K = 16000;
    @SuppressWarnings("unused")
    public static final int AUDIO_SAMPLE_RATE_32K = 32000;
    public static final int AUDIO_SAMPLE_RATE_44K = 44100;
    @SuppressWarnings("unused")
    public static final int AUDIO_SAMPLE_RATE_48K = 48000;
    public static final int DEFAULT_AUDIO_SAMPLE_RATE = AUDIO_SAMPLE_RATE_44K;

    // sample rate for showing wave data
    public static final int DEFAULT_WAVE_SAMPLING_RATE = 1;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface Functions

    public static boolean isListening() {
        return mListener.mIsListening;
    }

    public static void switchListening() {
        mListener._switchListening();
    }

    public static void playAudio() {
        mListener._playAudio();
    }

    public static String getConfigInfo() {
        return mListener._getConfigInfo();
    }

    public static String getProgressInfo() {
        return mListener._getProgressInfo();
    }

    public static void setItemShowNum(int showNum) {
        mListener.mItemShowNum = showNum;
    }

    public interface ListenerCallback {
        void onPowerUpdated();
    }

    public static void setCallback(ListenerCallback callback) {
        mCallback = callback;
    }

    @SuppressWarnings("unused")
    public static int getPowerListSize() {
        if (mListener.mPowerList == null) {
            return -1;
        }
        else {
            return mListener.mPowerList.size();
        }
    }

    @SuppressWarnings("unused")
    public static double getLastPower() {
        if (mListener.mPowerList == null || mListener.mPowerList.isEmpty()){
            return -1;
        }
        else {
            return mListener.mPowerList.get(mListener.mPowerList.size() - 1);
        }
    }

    public static List<Integer> getPowerList() {
        return mListener.mPowerList;
    }

    public static int getMaxPower() {
        return MAX_POWER;
    }

    // Interface Functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static WhatsaiListener mListener = new WhatsaiListener();
    private static ListenerCallback mCallback = null;

    private AudioRecord mAudioRecord;
    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = DEFAULT_AUDIO_SAMPLE_RATE;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int mAudioBufferSize;  // unit by byte
    private int mWaveSampling = DEFAULT_WAVE_SAMPLING_RATE;

    private boolean mIsListening = false;

    private String mPCMFileName = "";
    private int mFrameSize = 0;
    private int mFrameCount = 0;
    private int mTotalSize = 0;
    private int mLastFrameCount = 0;
    private int mLastDataSize = 0;
    private long mStartTime = 0;
    private int mFrameRate = 0;
    private int mFramePower = 0;
    private int mDataRate = 0;  // unit by b/s(bit/s)
    private String mTimeElapsed = "";  // format as hh:mm:ss
    private ArrayList<Integer> mPowerList = new ArrayList<>();
    private int mItemShowNum = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Working Timer
    private static final long WORKING_TIMER_DELAY = 0L;
    private static final long WORKING_TIMER_PERIOD = 1000L;
    private Timer mWorkingTimer;
    private void startWorkingTimer() {
        mWorkingTimer = new Timer();
        mWorkingTimer.schedule(new TimerTask() {
            public void run () {
                mListener.onWorkingTimer();
            }
        }, WORKING_TIMER_DELAY, WORKING_TIMER_PERIOD);
    }
    private void stopWorkingTimer() {
        if (mWorkingTimer != null) {
            mWorkingTimer.cancel();
            mWorkingTimer = null;
        }
    }
    // Working Timer
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Singleton Constructor
    private WhatsaiListener() {
        LogUtils.d("WhatsaiListener:WhatsaiListener");
        mAudioBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat);
        LogUtils.d("WhatsaiListener:WhatsaiListener: mAudioBufferSize = " + mAudioBufferSize);
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelConfig, mAudioFormat, mAudioBufferSize);
    }

    private void onWorkingTimer() {
        LogUtils.d("WhatsaiListener:onWorkingTimer");
        mFrameRate = mFrameCount - mLastFrameCount;
        mLastFrameCount = mFrameCount;
        mDataRate = mTotalSize - mLastDataSize;
        mLastDataSize = mTotalSize;
        mTimeElapsed = TimeUtils.elapsed(mStartTime);
    }

    private String _getConfigInfo() {
        String configInfo = "Audio Source: <font color=\"#ff0000\">" + AudioUtils.audioSourceName(MediaRecorder.AudioSource.MIC) + "</font>";
        configInfo += "<br>Sample Rate(Hz): <font color=\"#ff0000\">" + mSampleRate + "</font>";
        configInfo += "<br>Audio Format: <font color=\"#ff0000\">" + AudioUtils.audioFormatName(mAudioFormat) + "</font>";
        configInfo += "<br>Channel Config: <font color=\"#ff0000\">" + AudioUtils.channelConfigName(mChannelConfig) + "</font>";
        configInfo += "<br>Audio Buffer Size(B): <font color=\"#ff0000\">" + mAudioBufferSize + "</font>";
        configInfo += "<br>Audio Path: <font color=\"#ff0000\">" + ComDef.WHATSAI_AUDIO_DIR + "</font>";
        configInfo += "<br>Wave Show Sampling Rate: <font color=\"#ff0000\">" + mWaveSampling + "</font>";
        return configInfo;
    }

    private String getDisplayRate() {
        double rate = 1;
        if (mItemShowNum < mPowerList.size()) {
            rate = 1.0 * mItemShowNum / mPowerList.size();
        }
        DecimalFormat df = new DecimalFormat("#.000");
        return df.format(rate);
    }

    private String _getProgressInfo() {
        /*
        String info = "";
        if (mIsListening) {
            info += "Status: <font color=\"#ff0000\"><b>LISTENING...</b></font>";
        }
        else {
            info += "Status: <font color=\"#0000ff\"><b>IDLE</b></font>";
        }
        info += "<br>Frame Power: <font color=\"#ff0000\">" + mFramePower + "</font>";
        info += "<br>Frame Size(B): <font color=\"#ff0000\">" + mFrameSize + "</font>";
        info += "<br>Frame Count: <font color=\"#ff0000\">" + mFrameCount + "</font>";
        info += "<br>Frame Rate: <font color=\"#ff0000\">" + mFrameRate + "</font>";
        info += "<br>Data Rate(b/s): <font color=\"#ff0000\">" + NumUtils.formatSize(mDataRate) + "</font>";
        info += "<br>Duration: <font color=\"#ff0000\">" + mTimeElapsed + "</font>";
        info += "<br>Total Size(B): <font color=\"#ff0000\">" + NumUtils.formatSize(mTotalSize) + "</font>";
        info += "<br>PCM File: <font color=\"#ff0000\">" + mPCMFileName + "</font>";
        //*/
        String info = "";
        info += " <font color=\"#ff0000\">" + mPCMFileName + "</font>";
        info += " | <font color=\"#ff0000\">" + mTimeElapsed + "</font>";
        info += " | <font color=\"#ff0000\">" + NumUtils.formatSize(mTotalSize) + "</font>";
        info += " | FP: <font color=\"#ff0000\">" + mFramePower + "</font>";
        info += " | FS: <font color=\"#ff0000\">" + mFrameSize + "</font>";
        info += " | FC: <font color=\"#ff0000\">" + mFrameCount + "</font>";
        info += " | FR: <font color=\"#ff0000\">" + mFrameRate + "</font>";
        info += " | DR: <font color=\"#ff0000\">" + NumUtils.formatSize(mDataRate) + "</font>";
        info += " | <font color=\"#ff0000\">" + mItemShowNum + "</font>";
        info += " | <font color=\"#ff0000\">" + NumUtils.formatSize(mPowerList.size()) + "</font>";
        return info;
    }

    private void resetParams() {
        LogUtils.d("WhatsaiListener:onStartWorkingTimer");
        mPCMFileName = "";
        mFrameSize = 0;
        mFrameCount = 0;
        mTotalSize = 0;
        mLastFrameCount = 0;
        mLastDataSize = 0;
        mStartTime = 0;
        mFrameRate = 0;
        mFramePower = 0;
        mDataRate = 0;
        mTimeElapsed = "00:00:00";
        mPowerList.clear();
    }

    private void _switchListening() {
        LogUtils.d("WhatsaiListener:_switchListening: mIsListening = " + mIsListening);
        if (mIsListening) {
            stopListening();
            stopWorkingTimer();
        }
        else {
            startListening();
            startWorkingTimer();
        }
    }

    private void startListening() {
        LogUtils.d("WhatsaiListener:startListening: mIsListening = " + mIsListening);
        if (mIsListening) {
            LogUtils.d("WhatsaiListener:startListening: already started");
            return;
        }
        mIsListening = true;

        resetParams();

        final FileOutputStream outputStream = getPCMOutputStream();
        if (outputStream == null) {
            LogUtils.e("WhatsaiListener:startListening: get output stream for audio buffer failed.");
            return;
        }

        mAudioRecord.startRecording();
        mStartTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.d("WhatsaiListener: Start loop write audio data to pcm file...");
                    byte[] audioData = new byte[mAudioBufferSize];
                    int readSize;
                    while (mIsListening) {
                        readSize = mAudioRecord.read(audioData, 0, audioData.length);
                        onReadBuffer(readSize, audioData);
                        outputStream.write(audioData, 0, readSize);
                        outputStream.flush();
                    }
                    outputStream.close();
                } catch (Exception e) {
                    LogUtils.e("WhatsaiListener: startListening: exception: " + e.toString());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void onReadBuffer(final int readSize, byte[] audioData) {
        mFrameSize = readSize / 2;

        // sample for showing
        for (int i = 0; i < mFrameSize; i += mWaveSampling) {
            mPowerList.add(audioData[i * 2 + 1] << 8 | audioData[i * 2]);
            /*
            // audio zoom
            int pcmPower = (audioData[i * 2 + 1] << 8 | audioData[i * 2]);
            pcmPower *= 2;
            if (pcmPower > MAX_POWER) {
                pcmPower = MAX_POWER;
            }
            audioData[i * 2] = (byte)(pcmPower & 0x000000ff);
            audioData[i * 2 + 1] = (byte)((pcmPower & 0x0000ff00) >> 8);
            //*/
        }

        // calculate frame power, for pcm16, combined two bytes into one short
        {
            int pcmVal;
            int valid = 0;
            double pcmSum = 0;
            for (int i = 0; i < mFrameSize; i++) {
                pcmVal = (audioData[i*2 + 1] << 8 | audioData[i*2]);
                if (pcmVal != 0) {
                    pcmSum += pcmVal * pcmVal;
                    valid ++;
                }
            }
            pcmSum /= valid;
            mFramePower = (int)Math.sqrt(pcmSum);
        }

        mTotalSize += readSize;
        mFrameCount++;
        //LogUtils.v("WhatsaiListener: read #" + mFrameCount + ": " + readSize + " / " + mTotalSize + " / " + getLastPower());

        if (mCallback != null) {
            mCallback.onPowerUpdated();
        }
    }

    private void stopListening() {
        LogUtils.d("WhatsaiListener:stopRecord: mIsListening = " + mIsListening);
        if (!mIsListening) {
            LogUtils.d("WhatsaiListener: stopRecord: already stopped");
            return;
        }
        mIsListening = false;
        mAudioRecord.stop();
    }

    private String getPCMFileAbsolute() {
        return ComDef.WHATSAI_AUDIO_DIR + "/" + mPCMFileName;
    }

    private void _playAudio() {
        AudioUtils.playPCM(getPCMFileAbsolute(), mAudioBufferSize, mSampleRate, mAudioFormat, mChannelConfig);
        //##@: AudioUtils.playPCM16(mPowerList, mAudioBufferSize, mSampleRate, mChannelConfig);

        /*
        //save to wav file
        LogUtils.d("Convert pcm file to wave...");
        AudioUtils.pcmToWave(mPCMFileName, mPCMFileName + ".wav",
                mSampleRate, mAudioBufferSize, mAudioFormat, AudioUtils.AUDIO_TRACK_SINGLE);
        //*/
    }

    private FileOutputStream getPCMOutputStream() {
        File pcmFile = createPCMFile();
        if (pcmFile == null) {
            LogUtils.e("WhatsaiListener:getPCMOutputStream: create pcm file failed");
            return null;
        }
        try {
            return new FileOutputStream(pcmFile.getAbsoluteFile());
        }
        catch (Exception e) {
            LogUtils.e("WhatsaiListener:getPCMOutputStream: create output stream exception " + e.toString());
            return null;
        }
    }

    /**
     * create a pcm file to write pcm file
     * @return file which name format as yy.MMdd.HHmmss(19.1103.173655)
     */
    private File createPCMFile() {
        String strFileTime = new SimpleDateFormat("yy.MMdd.HHmmss").format(new java.util.Date());
        String fileName = strFileTime + ".pcm";
        String filePath = ComDef.WHATSAI_AUDIO_DIR + "/" + fileName;
        LogUtils.i("WhatsaiListener:createPCMFile: filePath = " + filePath);

        File objFile = new File(filePath);
        if (!objFile.exists()) {
            try {
                if (!objFile.createNewFile()) {
                    LogUtils.e("WhatsaiListener: createPCMFile failed.");
                    return null;
                }
                mPCMFileName = fileName;
                return objFile;
            } catch (IOException e) {
                LogUtils.e("WhatsaiListener: createPCMFile exception: " + e.toString());
                e.printStackTrace();
            }
        }

        return null;
    }
}
