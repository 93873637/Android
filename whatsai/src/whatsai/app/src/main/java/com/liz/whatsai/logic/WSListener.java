package com.liz.whatsai.logic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.liz.androidutils.AudioUtils;
import com.liz.androidutils.FileUtils;
import com.liz.androidutils.LogUtils;
import com.liz.androidutils.NumUtils;
import com.liz.androidutils.TimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("unused, WeakerAccess")
public class WSListener {

    /**
     * default max audio power value(from pcm 16bits)
     */
    private static final int DEFAULT_MAX_POWER = 32768;


    /**
     * default max power list size used for surface view
     * for A2H, its resolution is 1440*2560
     */
    private static final int DEFAULT_MAX_POWER_LIST_SIZE = 4096;

    /**
     * buffer size for audio power data, unit by byte
     * for pcm16/44100, 1MB is about 10s for pcm16/44100
     * when buffer is full, drop old frame for new frame
     * listening will continue
     * all audio data will save to audio pcm file
     */
    public static final int DEFAULT_MAX_BUFFER_SIZE = 1024*1024;

    public static final int AUDIO_SAMPLE_RATE_8K  =  8000;
    public static final int AUDIO_SAMPLE_RATE_16K = 16000;
    public static final int AUDIO_SAMPLE_RATE_32K = 32000;
    public static final int AUDIO_SAMPLE_RATE_44K = 44100;
    public static final int AUDIO_SAMPLE_RATE_48K = 48000;
    public static final int DEFAULT_AUDIO_SAMPLE_RATE = AUDIO_SAMPLE_RATE_44K;

    // sample rate from all wave sample data for wave data showing
    public static final int DEFAULT_WAVE_SAMPLING_RATE = 128;

    private AudioRecord mAudioRecord;
    private boolean mIsListening = false;

    private int mMaxPower = DEFAULT_MAX_POWER;
    private int mMaxPowerSize = DEFAULT_MAX_POWER_LIST_SIZE;
    private int mMaxBufferSize = DEFAULT_MAX_BUFFER_SIZE;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = DEFAULT_AUDIO_SAMPLE_RATE;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int mRecordBufferSize;  // unit by byte

    /**
     * sampling rate for wave showing, i.e. 100 means pick up one from 100 power data
     */
    private int mWaveSamplingRate = DEFAULT_WAVE_SAMPLING_RATE;

    private String mNeatFileName = "";
    private boolean mAutoSave = false;
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
    private ArrayList<AudioFrame> mFrameList = new ArrayList<>();
    private ArrayList<AudioTemplate> mTemplateList = new ArrayList<>();
    final private Object mDataLock = new Object();

    private ListenerCallback mCallback = null;

    private boolean mVoiceRecognition = false;
    final private Object mRecognitionObject = new Object();
    private String mSpeechText = "";

    public WSListener() {
        LogUtils.d("WSListener:WSListener");
        mRecordBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat);
        LogUtils.d("WSListener:WSListener: mRecordBufferSize = " + mRecordBufferSize);
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelConfig, mAudioFormat, mRecordBufferSize);
    }

    public boolean isListening() {
        return this.mIsListening;
    }

    public Object getDataLock() {
        return mDataLock;
    }

    public void switchListening() {
        LogUtils.d("WSListener:switchListening: mIsListening = " + mIsListening);
        if (mIsListening) {
            stopListening();
            stopWorkingTimer();
            if (mAutoSave) {
                AudioUtils.pcm2wav(
                        getPCMFileAbsolute(),
                        getWAVFileAbsolute(),
                        mSampleRate,
                        mRecordBufferSize,
                        mAudioFormat,
                        mChannelConfig,
                        true);
            }
        }
        else {
            startListening();
            startWorkingTimer();
        }
    }

    public static int getWaveDuration(File f) {
        return (int)(FileUtils.getFileSize(f) - 44) / (44100*2); //###@: mSampleRate * AudioUtils.byteNumByAudioFormat(mAudioFormat));
    }

    public void playPCMFile() {
        playPCMFile(getPCMFileAbsolute());
    }

    public void playPCMFile(String fileAbsolute) {
        AudioUtils.playPCM(fileAbsolute, mRecordBufferSize, mSampleRate, mAudioFormat, mChannelConfig);
    }

    @SuppressWarnings("unused")
    public String getAudioConfigInfoFull() {
        String configInfo = "Audio Source: <font color=\"#ff0000\">" + AudioUtils.audioSourceName(MediaRecorder.AudioSource.MIC) + "</font>";
        configInfo += "<br>Sample Rate(Hz): <font color=\"#ff0000\">" + mSampleRate + "</font>";
        configInfo += "<br>Audio Format: <font color=\"#ff0000\">" + AudioUtils.audioFormatName(mAudioFormat) + "</font>";
        configInfo += "<br>Channel Config: <font color=\"#ff0000\">" + AudioUtils.channelConfigName(mChannelConfig) + "</font>";
        configInfo += "<br>Audio Buffer Size(B): <font color=\"#ff0000\">" + mRecordBufferSize + "</font>";
        configInfo += "<br>Audio Path: <font color=\"#ff0000\">" + ComDef.WHATSAI_AUDIO_DIR + "</font>";
        return configInfo;
    }

    public String getAudioConfigInfo() {
        String configInfo = "<font color=\"#ff0000\">" + AudioUtils.audioSourceName(MediaRecorder.AudioSource.MIC) + "</font>";
        configInfo += " | <font color=\"#ff0000\">" + mSampleRate + "</font>";
        configInfo += " | <font color=\"#ff0000\">" + AudioUtils.audioFormatName(mAudioFormat) + "</font>";
        configInfo += " | <font color=\"#ff0000\">" + AudioUtils.channelConfigName(mChannelConfig) + "</font>";
        configInfo += " | <font color=\"#ff0000\">" + mRecordBufferSize + "</font>";
        return configInfo;
    }

    public String getProgressInfo() {
        String info = "";
        info += " <font color=\"#ff0000\">" + getPCMFileName() + "</font>";
        info += " | <font color=\"#ff0000\">" + mTimeElapsed + "</font>";
        info += " | <font color=\"#ff0000\">" + NumUtils.formatSize(mTotalSize) + "</font>";
        info += " | FP: <font color=\"#ff0000\">" + mFramePower + "</font>";
        info += " | FS: <font color=\"#ff0000\">" + mFrameSize + "</font>";
        info += " | FC: <font color=\"#ff0000\">" + mFrameCount + "</font>";
        info += " | FR: <font color=\"#ff0000\">" + mFrameRate + "</font>";
        info += " | DR: <font color=\"#ff0000\">" + NumUtils.formatSize(mDataRate) + "</font>";
        info += " | <font color=\"#ff0000\">" + mWaveSamplingRate + "</font>";
        info += " | <font color=\"#ff0000\">" + mPowerList.size() + "</font>";
        return info;
    }

    public String getProgressInfoSimple() {
        String info = "";
        info += " <font color=\"#ff0000\">" + getPCMFileName() + "</font>";
        info += " | <font color=\"#ff0000\">" + mTimeElapsed + "</font>";
        info += " | <font color=\"#ff0000\">" + NumUtils.formatSize(mTotalSize) + "</font>";
        info += " | <font color=\"#ff0000\">" + NumUtils.formatSize(mDataRate) + "</font>";
        return info;
    }

    public String getSpeechText() {
        return mSpeechText;
    }

    public String getPCMFileAbsolute() {
        return ComDef.WHATSAI_AUDIO_DIR + "/" + mNeatFileName + ".pcm";
    }

    public String getWAVFileAbsolute() {
        return ComDef.WHATSAI_AUDIO_DIR + "/" + mNeatFileName + ".wav";
    }

    public String getPCMFileName() {
        return mNeatFileName + ".pcm";
    }

    public interface ListenerCallback {
        void onPowerUpdated();
    }

    public void setCallback(ListenerCallback callback) {
        mCallback = callback;
    }

    @SuppressWarnings("unused")
    public int getPowerListSize() {
        if (this.mPowerList == null) {
            return -1;
        }
        else {
            return this.mPowerList.size();
        }
    }

    @SuppressWarnings("unused")
    public double getLastPower() {
        if (this.mPowerList == null || this.mPowerList.isEmpty()){
            return -1;
        }
        else {
            return this.mPowerList.get(this.mPowerList.size() - 1);
        }
    }

    public List<Integer> getPowerList() {
        return this.mPowerList;
    }

    public int getMaxPower() {
        return mMaxPower;
    }
    public void setMaxPower(int maxPower) {
        mMaxPower = maxPower;
    }

    public int getMaxPowerSize() {
        return mMaxPowerSize;
    }
    public void setMaxPowerSize(int maxPowerSize) {
        mMaxPowerSize = maxPowerSize;
    }

    public int getMaxBufferSize() {
        return mMaxBufferSize;
    }
    public void setMaxBufferSize(int maxSize) {
        mRecordBufferSize = maxSize;
    }

    public int getWaveSamplingRate() {
        return mWaveSamplingRate;
    }
    public void setWaveSamplingRate(int samplingRate) {
        mWaveSamplingRate = samplingRate;
    }

    public void setVoiceRecognition(boolean recognition) {
        mVoiceRecognition = recognition;
    }

    public void setAutoSave(boolean autoSave) {
        mAutoSave = autoSave;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Working Timer

    private static final long WORKING_TIMER_DELAY = 0L;
    private static final long WORKING_TIMER_PERIOD = 1000L;

    private Timer mWorkingTimer;

    private void startWorkingTimer() {
        mWorkingTimer = new Timer();
        mWorkingTimer.schedule(new TimerTask() {
            public void run () {
                onWorkingTimer();
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

    private void onWorkingTimer() {
        LogUtils.d("WSListener:onWorkingTimer");
        mFrameRate = mFrameCount - mLastFrameCount;
        mLastFrameCount = mFrameCount;
        mDataRate = mTotalSize - mLastDataSize;
        mLastDataSize = mTotalSize;
        mTimeElapsed = TimeUtils.elapsed(mStartTime);
    }

    private void resetParams() {
        LogUtils.d("WSListener:onStartWorkingTimer");
        clearPCMFile();
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
        mFrameList.clear();
        mTemplateList.clear();
    }

    private void startListening() {
        LogUtils.d("WSListener:startListening: mIsListening = " + mIsListening);
        if (mIsListening) {
            LogUtils.d("WSListener:startListening: already started");
            return;
        }
        mIsListening = true;
        resetParams();
        final FileOutputStream pcmOutputStream = createPCMOutputStream();
        if (pcmOutputStream == null) {
            LogUtils.e("WSListener:startListening: get output stream for audio buffer failed.");
            return;
        }
        mAudioRecord.startRecording();
        mStartTime = System.currentTimeMillis();
        startThread_RecordingAudioData(pcmOutputStream);
        if (mVoiceRecognition) {
            startThread_VoiceRecognition();
        }
    }

    private void startThread_RecordingAudioData(final FileOutputStream outputStream) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.d("WSListener: Start loop write audio data to pcm file...");
                    byte[] audioData = new byte[mRecordBufferSize];
                    int readSize;
                    while (mIsListening) {
                        readSize = mAudioRecord.read(audioData, 0, audioData.length);
                        onReadAudioData(readSize, audioData);
                        outputStream.write(audioData, 0, readSize);
                        outputStream.flush();
                        if (mVoiceRecognition) {
                            synchronized (mRecognitionObject) {
                                mRecognitionObject.notify();
                            }
                        }
                    }
                    outputStream.close();
                } catch (Exception e) {
                    LogUtils.e("WSListener: startListening: listen thread exception " + e.toString());
                    e.printStackTrace();
                }
                LogUtils.d("WSListener: listener stop.");
            }
        }).start();
    }

    private void startThread_VoiceRecognition() {
        if (mTemplateList.isEmpty()) {
            if (!loadAudioTemplates()) {
                LogUtils.te("load audio templates failed.");
                return;
            }
        }
        LogUtils.td("load audio templates ok, size = " + mTemplateList.size());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.td("Start voice recognition...");
                    while (mIsListening) {
                        LogUtils.td("wait recognition...");
                        synchronized (mRecognitionObject) {
                            mRecognitionObject.wait();
                        }
                        LogUtils.td("recognition on frame #" + mFrameCount);
                        onVoiceRecognition();
                    }
                } catch (Exception e) {
                    LogUtils.te("recognition exception: " + e.toString());
                    e.printStackTrace();
                }
                LogUtils.td("voice recognition stop.");
            }
        }).start();
    }

    private void onReadAudioData(final int readSize, byte[] audioData) {
        synchronized (mDataLock) {
            processAudioData(readSize, audioData);

            mFrameSize = readSize / 2;
            updateSurfaceSampleList(mFrameSize, audioData);
            mFramePower = calcFramePower(mFrameSize, audioData);
            mTotalSize += readSize;
            mFrameCount++;
            LogUtils.v("WSListener: read #" + mFrameCount + ": " + readSize + "/" + mTotalSize + "/" + mFramePower);

            // save a period of audio buffer for recognition
            if (mVoiceRecognition) {
                AudioFrame frame = new AudioFrame(audioData);
                if (mTotalSize > mMaxBufferSize) {
                    mFrameList.remove(0);
                }
                mFrameList.add(frame);
            }
        }

        if (mCallback != null) {
            mCallback.onPowerUpdated();
        }
    }

    private void updateSurfaceSampleList(int frameNum, byte[] audioData) {
        for (int i = 0; i < mFrameSize; i += mWaveSamplingRate) {
            mPowerList.add(audioData[i * 2 + 1] << 8 | audioData[i * 2]);
        }
        if (mPowerList.size() > mMaxPowerSize) {
            int orgSize = mPowerList.size();
            int toIndex = orgSize - mMaxPowerSize;
            mPowerList.subList(0, toIndex).clear();
            LogUtils.d("onReadAudioData: power list size " + orgSize + " exceed max " + mMaxPowerSize + ", removed to " + mPowerList.size());
        }
    }

    /**
     * calculate frame power, for pcm16, combined two bytes into one short
     */
    private int calcFramePower(int frameNum, byte[] audioData) {
        int pcmVal;
        int valid = 0;
        double pcmSum = 0;
        for (int i = 0; i < frameNum; i++) {
            pcmVal = (audioData[i * 2 + 1] << 8 | audioData[i * 2]);
            if (pcmVal != 0) {
                pcmSum += pcmVal * pcmVal;
                valid++;
            }
        }
        pcmSum /= valid;
        return (int) Math.sqrt(pcmSum);
    }

    private void processAudioData(final int readSize, byte[] audioData) {
        zoomVolume(readSize, audioData);
    }

    private void zoomVolume(final int readSize, byte[] audioData) {
        /*
        // audio zoom
        int pcmPower = (audioData[i * 2 + 1] << 8 | audioData[i * 2]);
        pcmPower *= 2;
        if (pcmPower > DEFAULT_MAX_POWER) {
            pcmPower = DEFAULT_MAX_POWER;
        }
        audioData[i * 2] = (byte) (pcmPower & 0x000000ff);
        audioData[i * 2 + 1] = (byte) ((pcmPower & 0x0000ff00) >> 8);
        //*/
    }

    public void onVoiceRecognition() {
        //###@: todo: search templates words from frame list...
    }

    private void stopListening() {
        LogUtils.d("WSListener:stopRecord: mIsListening = " + mIsListening);
        if (!mIsListening) {
            LogUtils.d("WSListener: stopRecord: already stopped");
            return;
        }
        mIsListening = false;
        mAudioRecord.stop();
    }

    private boolean loadAudioTemplates() {
        //####@:
        return true;
    }

    private FileOutputStream createPCMOutputStream() {
        File pcmFile = createPCMFile();
        if (pcmFile == null) {
            LogUtils.e("WSListener:createPCMOutputStream: create pcm file failed");
            return null;
        }
        try {
            return new FileOutputStream(pcmFile.getAbsoluteFile());
        }
        catch (Exception e) {
            LogUtils.e("WSListener:createPCMOutputStream: create output stream exception " + e.toString());
            return null;
        }
    }

    /**
     * create a pcm file to write pcm file
     * @return file which name format as yy.MMdd.HHmmss(19.1103.173655)
     */
    private File createPCMFile() {
        String neatFileTime = new SimpleDateFormat("yy.MMdd.HHmmss").format(new java.util.Date());
        String fileName = neatFileTime + ".pcm";
        String filePath = ComDef.WHATSAI_AUDIO_DIR + "/" + fileName;
        LogUtils.i("WSListener:createPCMFile: filePath = " + filePath);

        File objFile = new File(filePath);
        if (!objFile.exists()) {
            try {
                if (!objFile.createNewFile()) {
                    LogUtils.e("WSListener: createPCMFile failed.");
                    return null;
                }
                mNeatFileName = neatFileTime;
                return objFile;
            } catch (IOException e) {
                LogUtils.e("WSListener: createPCMFile exception: " + e.toString());
                e.printStackTrace();
            }
        }
        return null;
    }

    private void clearPCMFile() {
        FileUtils.removeFile(getPCMFileAbsolute());
        mNeatFileName = "";
    }
}
