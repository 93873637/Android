package com.liz.puremusic.logic;

import android.text.TextUtils;

import com.liz.puremusic.app.MusicService;
import com.liz.puremusic.app.ThisApp;
import com.liz.puremusic.utils.FileUtils;
import com.liz.puremusic.utils.LogUtils;
import com.liz.puremusic.utils.MediaUtils;
import com.liz.puremusic.utils.StrUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.liz.puremusic.logic.ComDef.PLAY_STATUS_COMPLETION;
import static com.liz.puremusic.logic.ComDef.PLAY_STATUS_STARTED;

/**
 * DataLogic:
 * Created by liz on 2018/3/8.
 */

@SuppressWarnings("unused")
public class DataLogic extends MusicService {
    private static ArrayList<PlayItem> mPlayList = new ArrayList<>();

    public static List<PlayItem> getPlayList() {
        return mPlayList;
    }

    public static boolean isPlayListEmpty() {
        return (mPlayList != null) && mPlayList.size() > 0;
    }
    private static final int LIST_INDEX_BEGIN = 0;
    private static int mCurrentListPos = LIST_INDEX_BEGIN;  //zero based
    private static int mPlayMode = ComDef.PLAY_MODE_DEFAULT;

    public static void init() {
        LogUtils.d("DataLogic.init");

        //loadTestData();
        loadDefaultMusic();

        mCurrentListPos = LIST_INDEX_BEGIN;
        mPlayMode = ComDef.PLAY_MODE_DEFAULT;

        setMusicServiceListener(new MusicService.MusicServiceListener() {
            @Override
            public void onServiceConnected() {
                DataLogic.loadCurrentMusic(false);
            }

            @Override
            public void onPlayCompletion() {
                DataLogic.onPlayCompletion();
            }
        });

        startService(ThisApp.getAppContext());
    }

    public static void loadDefaultMusic() {
        LogUtils.d("DataLogic.loadDefaultMusic");
        File fileDir = new File(ComDef.PURE_MUSIC_DEFAULT_PATH);
        DataLogic.onAddMusic(fileDir);
    }

    public static void setPlayMode(int mode) {
        mPlayMode = mode;
    }

    public static int getPlayMode() {
        return mPlayMode;
    }

    public static String getPlayModeName() {
        return ComDef.PLAY_MODE_NAME[mPlayMode];
    }

    public static void switchPlayOrPause() {
        LogUtils.d("DataLogic.switchPlayOrPause");
        if (DataLogic.isPlaying()) {
            DataLogic.pausePlay();
        } else {
            DataLogic.startPlay();
        }
    }

    public static void switchPlayMode() {
        switch (mPlayMode) {
            case ComDef.PLAY_MODE_LIST:
                mPlayMode = ComDef.PLAY_MODE_LIST_LOOP;
                break;
            case ComDef.PLAY_MODE_LIST_LOOP:
                mPlayMode = ComDef.PLAY_MODE_SINGLE_LOOP;
                break;
            case ComDef.PLAY_MODE_SINGLE_LOOP:
                mPlayMode = ComDef.PLAY_MODE_SINGLE;
                break;
            case ComDef.PLAY_MODE_SINGLE:
                mPlayMode = ComDef.PLAY_MODE_RANDOM;
                break;
            case ComDef.PLAY_MODE_RANDOM:
                mPlayMode = ComDef.PLAY_MODE_LIST;
                break;
            default:
                LogUtils.e("ERROR: DataLogic.switchPlayMode: unknown play mode " + mPlayMode);
                break;
        }
    }

    public static void stopMusicService() {
        MusicService.stopService(ThisApp.getAppContext());
    }

    /* for test only
    protected static void loadTestData() {
        addToList( ComDef.PURE_MUSIC_DEFAULT_PATH + "/music.mp3");
        addToList( ComDef.PURE_MUSIC_DEFAULT_PATH + "/music2.mp3");
        addToList( ComDef.PURE_MUSIC_DEFAULT_PATH + "/music3.mp3");
    }
    //*/

    public static String getPlayInfo() {
        return getPlayListInfo() + " [" + DataLogic.getCurrentMusicName() +  "] [" + getPlayStatusName() + "]";
    }

    public static int getCurrentListPos() {
        return mCurrentListPos;
    }

    public static void onAddMusic(String filePath) {
        addToList(filePath);
        checkAutoLoad();
    }

    public static void onAddMusic(File file) {
        addToList(file);
        checkAutoLoad();
    }

    protected static void addToList(String filePath) {
        LogUtils.d("DataLogic.addToList: filePath=" + filePath);
        PlayItem item = new PlayItem(filePath);
        item.setDuration(MediaUtils.getMediaDuration(filePath));
        mPlayList.add(item);
    }

    protected static void addToList(File file) {
        if (file.isDirectory()) {
            // add all sub files of the folder
            File[] children = file.listFiles();
            if (children != null) {
                for (File f : children) {
                    //only add file, not dir
                    if (!f.isDirectory() && validFileExt(f)) {
                        addToList(f.getPath());
                    }
                    else {
                        LogUtils.i("DataLogic.addToList: skip file=" + f.getAbsolutePath());
                    }
                }
            }
        }
        else {
            // add one file
            addToList(file.getPath());
        }
    }

    private static boolean validFileExt(File f) {
        if (f == null)
            return false;
        String extName = FileUtils.ext(f.getName());
        if (TextUtils.isEmpty(extName))
            return false;
        return ( extName.equals("mp3")
                || extName.equals("m4a")
                || extName.equals("wma")
        );
    }

    public static void checkAutoLoad() {
        LogUtils.d("DataLogic.checkAutoLoad: playlist size=" + mPlayList.size() + ", play status=" + getPlayStatusName());
        if ((mPlayList.size() > 0) && (getPlayStatus() == ComDef.PLAY_STATUS_IDLE)) {
            LogUtils.d("DataLogic.addToList: auto load first music when idle...");
            loadCurrentMusic(false);
        }
    }

    public static void removeFromList(int pos) {
        LogUtils.d("DataLogic.removeFromList: pos=" + pos);
        if (pos == getCurrentListPos()) {
            MusicService.stopPlay();
        }
        mPlayList.remove(pos);
        checkCurrentPos();
    }

    public static void loadPlayListHome() {
        clearPlayList();
        loadDefaultMusic();
    }

    public static void clearPlayList() {
        MusicService.stopPlay();
        MusicService.resetPlay();
        mPlayList.clear();
        checkCurrentPos();
    }

    public static void sortPlayList() {
        Collections.sort(mPlayList, new Comparator<PlayItem>() {
            @Override
            public int compare(PlayItem o1, PlayItem o2) {
                int ret = 0;

                //66 Revelation (10).mp3
                //66 Revelation (2).mp3
                String name1 = o1.getFileName();
                String name2 = o2.getFileName();
                ret = StrUtils.compareByBracketedInteger(name1, name2);

                LogUtils.d("sortPlayList: name1=" + name1 + ", name2=" + name2 + ", ret =" + ret);
                return ret;
            }
        });
    }

    public static String getMusicFilePath(int pos) {
        if (isValidPos(pos)) {
            return mPlayList.get(pos).getFilePath();
        }
        else {
            return "";
        }
    }

    public static String getMusicFileDuration(int pos) {
        if (isValidPos(pos)) {
            return mPlayList.get(pos).getDuration();
        }
        else {
            return "";
        }
    }

    public static boolean isValidPos(int pos) {
        //LogUtils.d("DataLogic.isValidPos: pos=" + pos);

        if (mCurrentListPos < LIST_INDEX_BEGIN) {
            return false;
        }

        if (mPlayList == null) {
            LogUtils.d("DataLogic.isValidPos: list null");
            return false;
        }

        int listSize = mPlayList.size();
        //LogUtils.d("DataLogic.isValidPos: listSize=" + listSize);

        if (listSize <= 0) {
            LogUtils.d("DataLogic.isValidPos: list empty");
            return false;
        }

        if (pos >= listSize) {
            LogUtils.d("DataLogic.isValidPos: pos(" + pos + ") exceed list size(" + listSize + ")");
            return false;
        }

        return true;
    }

    private static int firstPos() {
        return LIST_INDEX_BEGIN;
    }

    private static int lastPos() {
        if (mPlayList != null && mPlayList.size() > 0) {
            return mPlayList.size() - 1;
        }
        else {
            return LIST_INDEX_BEGIN;
        }
    }

    public static void checkCurrentPos() {
        LogUtils.d("DataLogic.checkCurrentPos: before->mCurrentListPos=" + mCurrentListPos);

        if (mCurrentListPos < LIST_INDEX_BEGIN) {
            mCurrentListPos = LIST_INDEX_BEGIN;
        }

        if (mPlayList == null) {
            LogUtils.d("DataLogic.checkCurrentPos: list null");
            mCurrentListPos = LIST_INDEX_BEGIN;
            return;
        }

        int listSize = mPlayList.size();
        LogUtils.d("DataLogic.checkCurrentPos: listSize=" + listSize);

        if (listSize <= 0) {
            LogUtils.d("DataLogic.checkCurrentPos: list empty");
            mCurrentListPos = LIST_INDEX_BEGIN;
            return;
        }

        if (mCurrentListPos >= listSize) {
            LogUtils.d("DataLogic.checkCurrentPos: mCurrentListPos(" + mCurrentListPos + ") exceed list size(" + listSize + "), move to end");
            mCurrentListPos = listSize - 1;
        }

        LogUtils.d("DataLogic.checkCurrentPos: after->mCurrentListPos=" + mCurrentListPos);
    }

    public static String getPlayListInfo() {
        if (mPlayList == null) {
            return ComDef.TEXT_PLAY_LIST_NULL;
        }
        else {
            if (mPlayList.size() > 0) {
                return "(" + (mCurrentListPos + 1) + "/" + mPlayList.size() + ")";
            }
            else {
                return "(" + mCurrentListPos + "/" + mPlayList.size() + ")";
            }
        }
    }

    public static boolean isRoot(File dir) {
        return TextUtils.equals(dir.getPath(), ComDef.ROOT_PATH.getPath());
    }

    public static String getCurrentMusicFile() {
        checkCurrentPos();
        if (mPlayList.isEmpty()) {
            return "";
        }
        else {
            return mPlayList.get(mCurrentListPos).getFilePath();
        }
    }

    public static String getCurrentMusicName() {
        if (mPlayList == null) {
            LogUtils.w("DataLogic.getCurrentMusicName: null play list");
            return ComDef.TEXT_PLAY_LIST_NULL;
        }

        int playListSize = mPlayList.size();
        if (playListSize <= 0) {
            //LogUtils.d("DataLogic.getCurrentMusicName: empty play list");
            return ComDef.TEXT_PLAY_LIST_EMPTY;
        }

        if (mCurrentListPos < 0 && mCurrentListPos >= playListSize) {
            LogUtils.e("ERROR: DataLogic.getCurrentMusicName: invalid mCurrentListPos=" + mCurrentListPos + ", playListSize=" + playListSize);
            return ComDef.TEXT_INVALID_POS;
        }

        return mPlayList.get(mCurrentListPos).getFileName();
    }

    public static boolean isPlayFirst() {
        return mCurrentListPos == LIST_INDEX_BEGIN;
    }

    public static boolean isPlayLast() {
        return (mPlayList.size() > 0) && (mCurrentListPos == (mPlayList.size() - 1));
    }

    public static void onStartPlay() {
        //check if music file changed
        if (TextUtils.equals(getCurrentMusicFile(), mDataSource)) {
            //not change, just start playing
            if (getPlayStatus() != PLAY_STATUS_STARTED) {
                startPlay();
            }
        }
        else {
            LogUtils.i("DataLogic.onStartPlay: music file changed, reload to start");
            loadCurrentMusic(true);
        }
    }

    public static void onPausePlay() {
        //check if music file changed
        if (TextUtils.equals(getCurrentMusicFile(), mDataSource)) {
            pausePlay();
        } else {
            LogUtils.i("DataLogic.onPausePlay: music file changed, reload");
            loadCurrentMusic(false);
        }
    }

    //manually go to next music by list
    public static void goNext() {
        LogUtils.d("DataLogic.goNext: mCurrentListPos=" + mCurrentListPos + ", mPlayList.size=" + mPlayList.size());
        while (mCurrentListPos < (mPlayList.size() - 1)) {
            mCurrentListPos++;
            if (loadCurrentMusic(isPlaying())) {
                LogUtils.d("DataLogic.goNext: load ok, mCurrentListPos=" + mCurrentListPos);
                break;
            }
            else {
                LogUtils.d("DataLogic.goNext: load failed, mCurrentListPos=" + mCurrentListPos);
            }
        }
    }

    public static void goPrev() {
        while (mCurrentListPos > 0) {
            mCurrentListPos--;
            if (loadCurrentMusic(isPlaying())) {
                break;
            }
        }
    }

    public static void goTo(int pos) {
        if (isValidPos(pos)) {
            mCurrentListPos = pos;
            loadCurrentMusic(isPlaying());
        }
    }

    public static void onPlayCompletion() {
        LogUtils.d("DataLogic.onPlayCompletion: mCurrentListPos=" + mCurrentListPos + ", mPlayMode=" + mPlayMode);
        setPlayStatus(PLAY_STATUS_COMPLETION);

        boolean needPlay = false;
        boolean needLoad = false;
        switch (mPlayMode) {
            case ComDef.PLAY_MODE_LIST:
                mCurrentListPos++;
                if (mCurrentListPos >= mPlayList.size()) {
                    //already go to end of play list, stop
                    mCurrentListPos = mPlayList.size() - 1;
                    needPlay = false;
                    needLoad = false;
                }
                else {
                    //play next music
                    needPlay = true;
                    needLoad = false;
                }
                break;
            case ComDef.PLAY_MODE_LIST_LOOP:
                mCurrentListPos++;
                if (mCurrentListPos >= mPlayList.size()) {
                    mCurrentListPos = LIST_INDEX_BEGIN;
                }
                needPlay = true;
                needLoad = true;
                break;
            case ComDef.PLAY_MODE_RANDOM:
                Random random = new Random();
                mCurrentListPos = Math.abs(random.nextInt()) % mPlayList.size();
                needPlay = true;
                needLoad = true;
                break;
            case ComDef.PLAY_MODE_SINGLE:
                //no next, just stop
                needPlay = false;
                needLoad = false;
                break;
            case ComDef.PLAY_MODE_SINGLE_LOOP:
                //no next, just replay current
                needPlay = true;
                needLoad = false;
                break;
            default:
                LogUtils.e("unknown play mode: " + mPlayMode);
                break;
        }

        LogUtils.d("DataLogic.onPlayCompletion: needPlay=" + needPlay + ", needLoad=" + needLoad);
        if (needPlay) {
            if (needLoad) {
                loadOnPlayCompletion();
            }
            else {
                startPlay();
            }
        }
        else {
            onStopPlay();
        }
    }

    private static void loadOnPlayCompletion() {
        LogUtils.d("DataLogic.loadOnPlayCompletion: mCurrentListPos = " + mCurrentListPos);
        final int maxReloadTime = mPlayList.size();
        int reloadCount = 0;
        while (!loadCurrentMusic(true) && reloadCount < maxReloadTime) {
            reloadCount ++;
            LogUtils.d("DataLogic.loadOnPlayCompletion: load failed, go to load next, count=" + reloadCount);
            if (goNextOnLoadError()) {
                LogUtils.d("DataLogic.loadOnPlayCompletion: go next on error, pos=" + mCurrentListPos);
            }
            else {
                LogUtils.d("DataLogic.loadOnPlayCompletion: no next to load, stop.");
                break;
            }
        }
    }

    private static boolean goNextOnLoadError() {
        switch (mPlayMode) {
            case ComDef.PLAY_MODE_LIST:
                mCurrentListPos++;
                if (mCurrentListPos >= mPlayList.size()) {
                    //already be the last one, no next
                    mCurrentListPos = lastPos();
                    return false;
                }
                else {
                    return true;
                }
            case ComDef.PLAY_MODE_LIST_LOOP:
                mCurrentListPos++;
                if (mCurrentListPos >= mPlayList.size()) {
                    mCurrentListPos = firstPos();
                }
                return true;
            case ComDef.PLAY_MODE_RANDOM:
                Random random = new Random();
                mCurrentListPos = random.nextInt() % mPlayList.size();
                if (mCurrentListPos >= mPlayList.size()) mCurrentListPos = lastPos();
                if (mCurrentListPos < 0) mCurrentListPos = firstPos();
                return true;
            case ComDef.PLAY_MODE_SINGLE:
            case ComDef.PLAY_MODE_SINGLE_LOOP:
                break;
            default:
                LogUtils.e("DataLogic.goNextOnLoadError: unknown play mode: " + mPlayMode);
                break;
        }

        //default just stop
        return false;
    }

    public static void onStopPlay() {
        MusicService.stopPlay();
        loadCurrentMusic(false);
    }

    public static boolean loadCurrentMusic(boolean playAfterLoad) {
        return loadMusic(getCurrentMusicFile(), playAfterLoad);
    }
}
