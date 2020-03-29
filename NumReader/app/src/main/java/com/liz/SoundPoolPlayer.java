package com.liz;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

@SuppressWarnings("WeakerAccess")
public class SoundPoolPlayer {
	
    private static final int MAX_SOUNDS = 5;
	private static SoundPool mSoundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);

	final static int NUM_CNT = 10;
	static AssetFileDescriptor[] mNumFd = new AssetFileDescriptor[NUM_CNT];
	static Integer[] mSoundId = new Integer[NUM_CNT];

	public static void init(Context context) {
		AssetManager am = context.getAssets();
		for (int i=0; i<NUM_CNT; i++) {
			try {
				mNumFd[i] = am.openFd("wav/chn0-9/" + i + ".wav");
				mSoundId[i] = mSoundPool.load(mNumFd[i], 1);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void release() {
		for (int i=0; i<NUM_CNT; i++) {
			mSoundPool.unload(mSoundId[i]);
		}
		mSoundPool.release();
	}

    public static void playNumber(int num) {
        playNumberString("" + num);
    }

	public static void playNumberString(String strNum) {
		int len = strNum.length();
		for (int i=0; i<len; i++) {
			int numeric = Integer.parseInt(strNum.substring(i, i + 1));
			//Toast.makeText(activity, strNum + ":" + numeric, Toast.LENGTH_SHORT).show();
			mSoundPool.play(mSoundId[numeric], 1.0f, 1.0f, 1, 0, NumReader.mPlayRate);
			if (i < (len - 1)) {
                try {
                    //sleep a while for play speed control
                    Thread.currentThread();
                    Thread.sleep(NumReader.mDigitSpan);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
		}
	}
}
