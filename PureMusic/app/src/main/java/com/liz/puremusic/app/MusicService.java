package com.liz.puremusic.app;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.liz.puremusic.logic.ComDef;
import com.liz.puremusic.logic.DataLogic;
import com.liz.puremusic.utils.LogUtils;

import java.text.SimpleDateFormat;

public class MusicService extends Service {

    //////////////////////////////////////////////////////////////////////////////////
    // Static APIs

    public static void startService(Context context) {
        Intent intent = new Intent(context, MusicService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void stopService(Context context) {
        context.unbindService(mServiceConnection);
        Intent intent = new Intent(context, MusicService.class);
        context.stopService(intent);
    }

    public static boolean isPlayerActive() {
        switch (getPlayStatus()) {
            case ComDef.PLAY_STATUS_INITIALIZED:
            case ComDef.PLAY_STATUS_PREPARED:
            case ComDef.PLAY_STATUS_STARTED:
            case ComDef.PLAY_STATUS_PAUSED:
            case ComDef.PLAY_STATUS_STOPPED:
                return true;
            default:
                return false;
        }
    }

    public static String getFormatCurrentDuration() {
        if (isPlayerActive()) {
            return new SimpleDateFormat(ComDef.DATE_FORMAT_PATTERN).format(mMusicService.getPlayer().getDuration());
        }
        else {
            return ComDef.INVALID_DURATION_FORMAT;
        }
    }

    public static String getFormatCurrentPosition() {
        if (isPlayerActive()) {
            return new SimpleDateFormat(ComDef.DATE_FORMAT_PATTERN).format(mMusicService.getPlayer().getCurrentPosition());
        }
        else {
            return ComDef.START_POSITION_FORMAT;
        }
    }

    public static int getCurrentPlayPosition() {
        if (isPlayerActive()) {
            return mMusicService.getPlayer().getCurrentPosition();
        }
        else {
            return ComDef.PLAY_START_POSITION;
        }
    }

    public static int getCurrentPlayDuration() {
        if (isPlayerActive()) {
            return mMusicService.getPlayer().getDuration();
        }
        else {
            return ComDef.INVALID_DURATION;
        }
    }

    public static void seekTo(int progress) {
        if (mMusicService != null) {
            mMusicService.getPlayer().seekTo(progress);
        }
    }

    public static void startPlay() {
        if (mMusicService != null) {
            mMusicService.start();
        }
    }

    public static void stopPlay() {
        if (mMusicService != null) {
            mMusicService.stop();
        }
    }

    public static void pausePlay() {
        if (mMusicService != null) {
            mMusicService.pause();
        }
    }

    public static void resetPlay() {
        if (mMusicService != null) {
            mMusicService.reset();
        }
    }

    public static boolean isPlaying() {
        if (mMusicService != null) {
            return mMusicService.getPlayer().isPlaying();
        }
        else {
            return false;
        }
    }

    public static boolean loadMusic(String musicFile, boolean playAfterLoad) {
        if (mMusicService != null) {
            return mMusicService.load(musicFile, playAfterLoad);
        }
        else {
            LogUtils.d("MusicService.loadMusic: No service");
            return false;
        }
    }

    public static String getPlayStatusName() {
        return ComDef.PLAY_STATUS[DataLogic.getPlayStatus()];
    }

    public static int getPlayStatus() {
        return mPlayStatus;
    }

    public static void setPlayStatus(int status) {
        mPlayStatus = status;
    }

    public static void setMusicServiceListener(MusicServiceListener musicServiceListener) {
        mMusicServiceListener = musicServiceListener;
    }

    public interface MusicServiceListener {
        void onServiceConnected();
        void onPlayCompletion();
    }

    // Static APIs
    //////////////////////////////////////////////////////////////////////////////////

    /**
     * Singleton Object
     */
    private static MusicService mMusicService = null;

    // Variables
    private static MusicServiceListener mMusicServiceListener = null;
    private static int mPlayStatus = ComDef.PLAY_STATUS_NO_SERVICE;
    public static String mDataSource = "";

    /*
    public static MusicService getService() {
        return mMusicService;
    }
    //*/

    public MusicService() {
        LogUtils.d("MusicService.MusicService");
        mMediaPlayer = new MediaPlayer();
        setPlayStatus(ComDef.PLAY_STATUS_NO_SERVICE);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                LogUtils.d("MusicService: onCompletion");
                mMusicServiceListener.onPlayCompletion();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                LogUtils.d("MusicService: onError: what=" + what + ", extra=" + extra);
                mMediaPlayer.reset();
                setPlayStatus(ComDef.PLAY_STATUS_IDLE);
                mPlayAfterLoad = false;
                return true;  // return true in case calling onCompletion
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                LogUtils.d("MusicService: onPrepared");
                setPlayStatus(ComDef.PLAY_STATUS_PREPARED);
                if (mPlayAfterLoad) {
                    startPlay();
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d("MusicService.onServiceConnected");
            mMusicService = ((MusicService.MusicServiceBinder) (service)).getService();
            setPlayStatus(ComDef.PLAY_STATUS_IDLE);
            mMusicServiceListener.onServiceConnected();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d("MusicService.onServiceDisconnected");
            mMusicService = null;
            setPlayStatus(ComDef.PLAY_STATUS_NO_SERVICE);
        }
    };

    private MusicServiceBinder mBinder = new MusicServiceBinder();
    private class MusicServiceBinder extends Binder {
        private MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.d("MusicService.onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d("MusicService.onBind");
        return mBinder;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Class MusicService

    private MediaPlayer mMediaPlayer = null;
    private boolean mPlayAfterLoad = false;

    public MediaPlayer getPlayer() {
        return mMediaPlayer;
    }

    public boolean load(String musicFile, boolean playAfterLoad) {
        LogUtils.d("MusicService.load");

        if (TextUtils.isEmpty(musicFile)) {
            reset();
            LogUtils.i("MusicService.load: no music file to load");
            return false;
        }

        try {
            mMediaPlayer.seekTo(ComDef.PLAY_START_POSITION);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(musicFile);
            mMediaPlayer.prepare();
            //mMediaPlayer.setLooping(true);
            setPlayStatus(ComDef.PLAY_STATUS_INITIALIZED);
            mPlayAfterLoad = playAfterLoad;
        } catch (Exception e) {
            LogUtils.e("MusicService.load: exception " + e.toString());
            return false;
        }

        mDataSource = musicFile;
        LogUtils.d("MusicService.load: success, mDataSource=" + mDataSource);
        return true;
    }

    public void reset() {
        mMediaPlayer.reset();
        setPlayStatus(ComDef.PLAY_STATUS_IDLE);
        mPlayAfterLoad = false;
    }

    public boolean canStart() {
        switch (getPlayStatus()) {
            //case ComDef.PLAY_STATUS_INITIALIZED:
            case ComDef.PLAY_STATUS_PREPARED:
            //case ComDef.PLAY_STATUS_STARTED:
            case ComDef.PLAY_STATUS_PAUSED:
            //case ComDef.PLAY_STATUS_STOPPED:
            case ComDef.PLAY_STATUS_COMPLETION:
                return true;
            default:
                return false;
        }
    }

    public void start() {
        LogUtils.d("MusicService.start");
        if (canStart()) {
            mMediaPlayer.start();
            setPlayStatus(ComDef.PLAY_STATUS_STARTED);
        }
        else {
            LogUtils.d("MusicService.start: can't start in status: " + ComDef.PLAY_STATUS[mPlayStatus]);
        }
    }

    public boolean canPause() {
        switch (getPlayStatus()) {
            //case ComDef.PLAY_STATUS_INITIALIZED:
            //case ComDef.PLAY_STATUS_PREPARED:
            case ComDef.PLAY_STATUS_STARTED:
            case ComDef.PLAY_STATUS_PAUSED:
            //case ComDef.PLAY_STATUS_STOPPED:
                return true;
            default:
                return false;
        }
    }

    public void pause() {
        LogUtils.d("MusicService.pause");
        if (canPause()) {
            mMediaPlayer.pause();
            setPlayStatus(ComDef.PLAY_STATUS_PAUSED);
        }
        else {
            LogUtils.d("MusicService.pause: can't pause in status: " + ComDef.PLAY_STATUS[mPlayStatus]);
        }
    }

    public boolean canStop() {
        switch (getPlayStatus()) {
            //case ComDef.PLAY_STATUS_INITIALIZED:
            case ComDef.PLAY_STATUS_PREPARED:
            case ComDef.PLAY_STATUS_STARTED:
            case ComDef.PLAY_STATUS_PAUSED:
            case ComDef.PLAY_STATUS_STOPPED:
                return true;
            default:
                return false;
        }
    }

    public void stop() {
        LogUtils.d("MusicService.stop");
        if (canStop()) {
            mMediaPlayer.stop();
            setPlayStatus(ComDef.PLAY_STATUS_STOPPED);
        }
        else {
            LogUtils.d("MusicService.stop: can't stop in status: " + ComDef.PLAY_STATUS[mPlayStatus]);
        }
    }
}
