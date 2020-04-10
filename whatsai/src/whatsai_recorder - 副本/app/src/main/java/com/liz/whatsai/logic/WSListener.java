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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("WeakerAccess")
public class WSListener {

    /**
     * default max audio power value(from pcm 16bits)
     */
    private static final int DEFAULT_MAX_POWER = 32768;

    /**
     * limit audio file size in case disk full
     */
    private static final long DEFAULT_MAX_AUDIO_FILE_SIZE = NumUtils.G;
    private static final long DEFAULT_MAX_STORAGE_SIZE = 10 * DEFAULT_MAX_AUDIO_FILE_SIZE;

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
    private boolean mListening = false;
    private String mAudioDir = ComDef.WHATSAI_AUDIO_DIR;

    // audio config
    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = DEFAULT_AUDIO_SAMPLE_RATE;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int mRecordBufferSize;  // unit by byte

    private int mMaxPower = DEFAULT_MAX_POWER;
    private int mMaxBufferSize = DEFAULT_MAX_BUFFER_SIZE;

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
    private ArrayList<AudioFrame> mFrameList = new ArrayList<>();
    private ArrayList<AudioTemplate> mTemplateList = new ArrayList<>();
    final private Object mDataLock = new Object();

    private ListenerCallback mCallback = null;

    private boolean mVoiceRecognition = false;
    final private Object mRecognitionObject = new Object();
    private String mSpeechText = "";

    private long mMaxAudioFileSize = DEFAULT_MAX_AUDIO_FILE_SIZE;
    private long mMaxAudioStorageSize = DEFAULT_MAX_STORAGE_SIZE;  //###@: not take effect

    public WSListener() {
        LogUtils.d("WSListener:WSListener");
        mRecordBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat);
        LogUtils.d("WSListener:WSListener: mRecordBufferSize = " + mRecordBufferSize);
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelConfig, mAudioFormat, mRecordBufferSize);
    }

    public boolean isListening() {
        return mListening;
    }

    public Object getDataLock() {
        return mDataLock;
    }

    public void switchListening() {
        LogUtils.td("switchListening: mListening = " + mListening);
        if (mListening) {
            stopListening();
        }
        else {
            startListening();
        }
    }

    public void startListening() {
        LogUtils.td("startListening: mListening = " + mListening);
        if (mListening) {
            LogUtils.td("startListening: already started");
            return;
        }
        mListening = true;
        resetParams();
        mAudioRecord.startRecording();
        mStartTime = System.currentTimeMillis();
        startThread_RecordingAudioData();
        if (mVoiceRecognition) {
            startThread_VoiceRecognition();
        }
        startWorkingTimer();
        mCallback.onListenStarted();
    }

    public void stopListening() {
        LogUtils.d("WSListener:stopRecord: mListening = " + mListening);
        if (!mListening) {
            LogUtils.d("WSListener: stopRecord: already stopped");
            return;
        }
        mListening = false;
        mAudioRecord.stop();
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
        mCallback.onListenStopped(mAutoSave);
    }

    private byte[] getWaveHeader() {
        return AudioUtils.getWaveHeader(
                0,
                mSampleRate,
                mRecordBufferSize,
                mAudioFormat,
                mChannelConfig
        );
    }

    //return ms
    public static int getWaveDuration(File f) {
        long pcmLen = FileUtils.getFileSize(f) - 44;
        long byteRate = 44100 * 2L; // bytes/second, ###@: mSampleRate * AudioUtils.byteNumByAudioFormat(mAudioFormat));
        return (int)(pcmLen * 1000 / byteRate);
    }

    public void playPCMFile() {
        playPCMFile(getPCMFileAbsolute());
    }

    public void playPCMFile(String fileAbsolute) {
        AudioUtils.playPCM(fileAbsolute, mRecordBufferSize, mSampleRate, mAudioFormat, mChannelConfig);
    }

    public void playWAVFile(String fileAbsolute) {
        File wavFile = new File(fileAbsolute);
        if (!wavFile.exists()) {
            LogUtils.te2("wav file " + fileAbsolute + " not exists");
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(wavFile);
            byte[] headerBytes = new byte[AudioUtils.WAVE_FILE_HEADER_LEN];
            int readSize = fis.read(headerBytes);
            if (readSize != AudioUtils.WAVE_FILE_HEADER_LEN) {
                LogUtils.te2("read wave header failed.");
                fis.close();
                return;
            }
            AudioUtils.playPCM(fis, mRecordBufferSize, mSampleRate, mAudioFormat, mChannelConfig);
        } catch (Exception e) {
            LogUtils.te2("play wav file " + fileAbsolute + " failed, ex = " + e.toString());
        }
    }

    @SuppressWarnings("unused")
    public String getAudioConfigInfoFull() {
        String configInfo = "Audio Source: <font color=\"#ff0000\">" + AudioUtils.audioSourceName(MediaRecorder.AudioSource.MIC) + "</font>";
        configInfo += "<br>Sample Rate(Hz): <font color=\"#ff0000\">" + mSampleRate + "</font>";
        configInfo += "<br>Audio Format: <font color=\"#ff0000\">" + AudioUtils.audioFormatName(mAudioFormat) + "</font>";
        configInfo += "<br>Channel Config: <font color=\"#ff0000\">" + AudioUtils.channelConfigName(mChannelConfig) + "</font>";
        configInfo += "<br>Audio Buffer Size(B): <font color=\"#ff0000\">" + mRecordBufferSize + "</font>";
        configInfo += "<br>Audio Path: <font color=\"#ff0000\">" + getAudioDir() + "</font>";
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
        if (!isListening()) {
            return "IDLE";
        }

        String info = "";
        info += " <font color=\"#ff0000\">" + getPCMFileName() + "</font>";
        info += " | <font color=\"#ff0000\">" + mTimeElapsed + "</font>";
        info += " | <font color=\"#ff0000\">" + NumUtils.formatSize(mTotalSize) + "</font>";
        info += " | FP: <font color=\"#ff0000\">" + mFramePower + "</font>";
        info += " | FS: <font color=\"#ff0000\">" + mFrameSize + "</font>";
        info += " | FC: <font color=\"#ff0000\">" + mFrameCount + "</font>";
        info += " | FR: <font color=\"#ff0000\">" + mFrameRate + "</font>";
        info += " | DR: <font color=\"#ff0000\">" + NumUtils.formatSize(mDataRate) + "</font>";
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

    public String getProgressInfoForNotify() {
        String info = "LISTEN: ";
        if (!isListening()) {
            info += "NO";
        }
        else {
            info += " <font color=\"#ff0000\">" + mTimeElapsed + "</font>";
            info += " | <font color=\"#ff0000\">" + NumUtils.formatSize(mTotalSize) + "</font>";
            info += " | <font color=\"#ff0000\">" + NumUtils.formatSize(mDataRate) + "</font>";
        }
        return info;
    }

    public String getSpeechText() {
        return mSpeechText;
    }

    public String getPCMFileAbsolute() {
        return getAudioDir() + "/" + mNeatFileName + ".pcm";
    }

    public String getWAVFileAbsolute() {
        return getAudioDir() + "/" + mNeatFileName + ".wav";
    }

    public String getPCMFileName() {
        return mNeatFileName + ".pcm";
    }

    public String getNeatFileName() {
        return mNeatFileName;
    }

    public interface ListenerCallback {
        void onListenStarted();
        void onListenStopped(boolean save);
        void onReadAudioData(final int size, final byte[] data);
    }

    public void setCallback(ListenerCallback callback) {
        mCallback = callback;
    }

    public String getAudioDir() { return mAudioDir; }
    public void setAudioDir(String audioDir) { mAudioDir = audioDir; }

    public int getMaxPower() {
        return mMaxPower;
    }
    public void setMaxPower(int maxPower) {
        mMaxPower = maxPower;
    }

    public int getMaxBufferSize() {
        return mMaxBufferSize;
    }
    public void setMaxBufferSize(int maxSize) {
        mRecordBufferSize = maxSize;
    }

    public void setVoiceRecognition(boolean recognition) {
        mVoiceRecognition = recognition;
    }

    public void setAutoSave(boolean autoSave) {
        mAutoSave = autoSave;
    }

    public void saveWavFile(String wavFilePath, boolean deletePCM) {
        AudioUtils.pcm2wav(getPCMFileAbsolute(), wavFilePath,
                mSampleRate, mRecordBufferSize, mAudioFormat, mChannelConfig,
                deletePCM);
    }

    public void setMaxAudioFileSize(long size) {
        mMaxAudioFileSize = size;
    }

    public void setMaxAudioStorageSize(long size) {
        mMaxAudioStorageSize = size;
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
        mFrameList.clear();
        mTemplateList.clear();
    }

    private void startThread_RecordingAudioData() {
        final RandomAccessFile raf = createAudioOutputFile();
        if (raf == null) {
            LogUtils.tw2("file output file null");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.d("WSListener: Start loop write audio data to file...");
                    if (raf != null) {
                        byte[] header = getWaveHeader();
                        raf.seek(0);
                        raf.write(header);
                    }
                    byte[] audioData = new byte[mRecordBufferSize];
                    int readSize;
                    while (mListening) {
                        readSize = mAudioRecord.read(audioData, 0, audioData.length);
                        onReadAudioData(readSize, audioData);
                        if (mTotalSize < mMaxAudioFileSize) {
                            if (raf != null) {
                                raf.write(audioData, 0, readSize);
                                AudioUtils.modifyWaveFileHeader(raf, mTotalSize);
                            }
                        }
                        else {
                            LogUtils.tw2("current file size " + mTotalSize + " exceed max " + mMaxAudioFileSize + ", data not save");
                        }
                        if (mVoiceRecognition) {
                            synchronized (mRecognitionObject) {
                                mRecognitionObject.notify();
                            }
                        }
                    }
                    if (raf != null) {
                        raf.close();
                    }
                } catch (Exception e) {
                    LogUtils.e("WSListener: startListening: listen thread exception " + e.toString());
                    e.printStackTrace();
                }
                LogUtils.d("WSListener: listener stop.");
            }
        }).start();
    }

    /**
     * recording audio data to file using FileOutputStream
     */
    private void startThread_RecordingAudioData2() {
        final FileOutputStream fos = createAudioOutputStream();
        if (fos == null) {
            LogUtils.tw2("file output stream null");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.d("WSListener: Start loop write audio data to pcm file...");
                    byte[] audioData = new byte[mRecordBufferSize];
                    int readSize;
                    while (mListening) {
                        readSize = mAudioRecord.read(audioData, 0, audioData.length);
                        onReadAudioData(readSize, audioData);
                        if (fos != null) {
                            fos.write(audioData, 0, readSize);
                            fos.flush();
                        }
                        if (mVoiceRecognition) {
                            synchronized (mRecognitionObject) {
                                mRecognitionObject.notify();
                            }
                        }
                    }
                    if (fos != null) {
                        fos.close();
                    }
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
                    while (mListening) {
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

    private void onReadAudioData(final int readSize, final byte[] audioData) {
        synchronized (mDataLock) {
            processAudioData(readSize, audioData);

            mTotalSize += readSize;
            mFrameSize = readSize / 2;
            mFramePower = calcFramePower(mFrameSize, audioData);
            mFrameCount++;
            //LogUtils.v("WSListener: read #" + mFrameCount + ": " + readSize + "/" + mTotalSize + "/" + mFramePower);

            // save a period of audio buffer for recognition
            if (mVoiceRecognition) {
                AudioFrame frame = new AudioFrame(audioData);
                if (mTotalSize > mMaxBufferSize) {
                    mFrameList.remove(0);
                }
                mFrameList.add(frame);
            }

            if (mCallback != null) {
                mCallback.onReadAudioData(readSize, audioData);
            }
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

    private boolean loadAudioTemplates() {
        //####@:
        return true;
    }

    private RandomAccessFile createAudioOutputFile() {
        File f = createWaveFile();
        if (f == null) {
            LogUtils.te2("create wave file failed");
            return null;
        }
        try {
            return new RandomAccessFile(f.getAbsoluteFile(), "rw");
        }
        catch (Exception e) {
            LogUtils.te2("create audio output file exception " + e.toString());
            return null;
        }
    }

    private FileOutputStream createAudioOutputStream() {
        File f = createWaveFile();
        if (f == null) {
            LogUtils.te2("create wave file failed");
            return null;
        }
        try {
            return new FileOutputStream(f.getAbsoluteFile());
        }
        catch (Exception e) {
            LogUtils.te2("create output stream exception " + e.toString());
            return null;
        }
    }

    /**
     * create a wave file to write pcm data
     * @return file which name format as yy.MMdd.HHmmss(19.1103.173655)
     */
    private File createWaveFile() {
        String neatFileTime = new SimpleDateFormat("yy.MMdd.HHmmss").format(new java.util.Date());
        String fileName = neatFileTime + ".wav";
        String filePath = getAudioDir() + "/" + fileName;
        LogUtils.ti("createWaveFile: filePath = " + filePath);
        File f = new File(filePath);
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) {
                    LogUtils.te2("create wave file failed.");
                    return null;
                }
                mNeatFileName = neatFileTime;
                return f;
            } catch (IOException e) {
                LogUtils.te2("create wave file exception: " + e.toString());
                e.printStackTrace();
            }
        }
        return null;
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
        String filePath = getAudioDir() + "/" + fileName;
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
