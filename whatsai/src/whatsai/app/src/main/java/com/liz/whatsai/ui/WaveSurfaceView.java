package com.liz.whatsai.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.liz.androidutils.AudioUtils;
import com.liz.androidutils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class WaveSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

	private static final double DEFAULT_WAVE_ITEM_WIDTH = 1;  // unit by pixel
	private static final double DEFAULT_WAVE_ITEM_SPACE = 0;  // unit by pixel
	private static final int MIN_RECT_WIDTH = 2;  // item width not enough to one pixel, just draw a line

	/**
	 * sample rate from all sample data for showing wave
	 * i.e. 100 means pick up 1 data from 100
	 */
	public static final int DEFAULT_WAVE_SAMPLING_RATE = 128;

	/**
	 * default max data list size used for surface view
	 * for A2H, its resolution is 1440*2560, so the data just larger than 2560 is ok
	 * this value is set in case memory problem for data list increasing unlimited
	 */
	private static final int DEFAULT_MAX_DATA_LIST_SIZE = 4096;

	private static final int WAVE_ITEM_COLOR = Color.rgb(79, 208, 89);
	private static final int CANVAS_BG_A = 255;
	private static final int CANVAS_BG_R = 0;
	private static final int CANVAS_BG_G = 0;
	private static final int CANVAS_BG_B = 0;
	private static final int CANVAS_GRID_COLOR = Color.rgb(160, 160, 160);
	private static final int CANVAS_GRID_COLOR_MIDDLE = Color.rgb(255, 255, 255);

	private boolean mFullMode = false;  // flag to show if show full wave data
	private int mCanvasBgA = CANVAS_BG_A;
	private int mCanvasBgR = CANVAS_BG_R;
	private int mCanvasBgG = CANVAS_BG_G;
	private int mCanvasBgB = CANVAS_BG_B;

	private Paint mWavePaint;
	private Paint mGridPaint;
	private Paint mGridMiddlePaint;
	private boolean mDrawGrid = true;

	private double mWaveItemWidth = DEFAULT_WAVE_ITEM_WIDTH;
	private double mWaveItemSpace = DEFAULT_WAVE_ITEM_SPACE;
	private int mCanvasCapacity = 0;  // number of items which can draw on canvas

	private ArrayList<Integer> mDataList = new ArrayList<>();
	private int mWaveSamplingRate = DEFAULT_WAVE_SAMPLING_RATE;
	private int mMaxDataListSize = DEFAULT_MAX_DATA_LIST_SIZE;
	private int mMaxWaveValue = 0;
	private int mAmplitudeHeight;
	private int mIndexOffset = 0;

	private File mWaveFile;

	public WaveSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LogUtils.trace();
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		LogUtils.trace();
		initSurfaceView();
	}

	protected void initSurfaceView() {
		LogUtils.trace();
		mWavePaint = new Paint();
		mWavePaint.setColor(WAVE_ITEM_COLOR);
		mGridPaint = new Paint();
		mGridPaint.setColor(CANVAS_GRID_COLOR);
		mGridMiddlePaint = new Paint();
		mGridMiddlePaint.setColor(CANVAS_GRID_COLOR_MIDDLE);
		new Thread() {
			public void run() {
				initCanvas();
			}
		}.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		LogUtils.trace();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		LogUtils.trace();
	}

	public void clearCanvas() {
		mDataList.clear();
		redrawSurface();
	}

	public void setDrawGrid(boolean draw) {
		mDrawGrid = draw;
	}

	public void setMaxWaveValue(int maxValue) {
		mMaxWaveValue = maxValue;
	}

	public void setBackground(int alpha, int red, int green, int blue) {
		mCanvasBgA = alpha;
		mCanvasBgR = red;
		mCanvasBgG = green;
		mCanvasBgB = blue;
	}

	/**
	 * take audio data as pcm16
	 * @param data: audio pcm16 data
	 * @param size: data size, unit by bytes
	 */
	public void addAudioData(final byte[] data, final int size) {
		int itemNum = size / 2;  //for pcm16, each item has 2 bytes
		for (int i = 0; i < itemNum; i += mWaveSamplingRate) {
			mDataList.add(data[i * 2 + 1] << 8 | data[i * 2]);
		}

		// In case memory problem for list increasing unlimited.
		// Here remove old data when size exceed max
		if (mDataList.size() > mMaxDataListSize) {
			int orgSize = mDataList.size();
			int toIndex = orgSize - mMaxDataListSize;
			mDataList.subList(0, toIndex).clear();
			LogUtils.d("addAudioData: power list size " + orgSize + " exceed max " + mMaxDataListSize + ", removed to " + mDataList.size());
		}
	}

	public boolean hasData() {
		return mDataList != null && mDataList.size() > 0;
	}

	public int getDataLen() {
		return hasData() ? mDataList.size() : 0;
	}

	public int getCanvasLen() {
		return mCanvasCapacity;
	}

	public boolean isDataEmpty() {
		return !hasData();
	}

	public void updateOffset(int offsetInc) {
	    if (!canFullShow()) {
            mIndexOffset += offsetInc;
            if (mIndexOffset < 0) {
                mIndexOffset = 0;
            }
            int maxOffset = mDataList.size() - mCanvasCapacity;
            if (mIndexOffset > maxOffset) {
                mIndexOffset = maxOffset;
            }
        }
    }

	/**
	 * @return true if all data showing on screen, or not
	 */
	public boolean canFullShow() {
		if (hasData()) {
			return mCanvasCapacity >= mDataList.size();
		}
		else {
			// no data, same as show all data
			return true;
		}
	}

	public void setWaveItemWidth(double itemWidth) {
		mWaveItemWidth = itemWidth;
	}

	public void setWaveItemSpace(double itemSpace) {
		mWaveItemSpace = itemSpace;
	}

	private double getWaveUnitWidth() {
		return mWaveItemWidth + mWaveItemSpace;
	}

	public int getWaveSamplingRate() {
		return mWaveSamplingRate;
	}
	public void setWaveSamplingRate(int samplingRate) {
		mWaveSamplingRate = samplingRate;
	}

	public int getMaxListSize() {
		return mMaxDataListSize;
	}
	public void setMaxListSize(int maxListSize) {
		mMaxDataListSize = maxListSize;
	}

	public boolean getFullMode() {
		return mFullMode;
	}
	public void setFullMode(boolean fullMode) {
		mFullMode = fullMode;
	}

	public String getSurfaceInfo() {
		String info = "";
		info += " <font color=\"#ff0000\">" + mMaxWaveValue + "</font>";
		info += " | <font color=\"#ff0000\">" + mCanvasCapacity + "</font>";
		info += " | <font color=\"#ff0000\">" + mAmplitudeHeight + "</font>";
		info += " | <font color=\"#ff0000\">" + mWaveItemWidth + "</font>";
		info += " | <font color=\"#ff0000\">" + mWaveItemSpace + "</font>";
		return info;
	}

	public void setWaveFile(File wavFile) {
		mWaveFile = wavFile;
	}

	private void loadWaveFile() {
		LogUtils.td("loadWaveFile: capacity = " + mCanvasCapacity);
		mDataList.clear();
		mDataList = AudioUtils.getWaveProfile(mWaveFile, mCanvasCapacity);
		drawSurface();
	}

	private void initCanvas() {
		Canvas canvas = this.getHolder().lockCanvas(
				new Rect(0, 0, this.getWidth(), this.getHeight()));
		if (canvas == null) {
			LogUtils.e("ERROR: initSurfaceView: canvas null");
		}
		else {
			mAmplitudeHeight = canvas.getHeight() / 2;
			mCanvasCapacity = (int)(canvas.getWidth() / getWaveUnitWidth());
			prepareCanvas(canvas);
			this.getHolder().unlockCanvasAndPost(canvas);
		}
	}

	private static int calcItemHeight(int value, int maxValue, int maxHeight) {
		if (value == 0) {
			return 0;
		}
		else {
			double rate = Math.log10(Math.abs(value)) / Math.log10(Math.abs(maxValue));
			int height = (int) (rate * Math.abs(maxHeight));
			if (value < 0) {
				height = -1 * height;
			}
			//LogUtils.v("calcItemHeight: value/maxValue/maxHeight/height = " + value + " / " + maxValue + " / " + maxHeight + " / "+ height);
			return height;
		}
	}

	public void updateSurfaceData(@NonNull List<Integer> dataList, int maxValue) {
		mDataList.clear();
		mDataList = (ArrayList<Integer>)dataList;
		mMaxWaveValue = maxValue;
	}

	public void updateSurface(@NonNull List<Integer> dataList, int maxValue) {
		updateSurfaceData(dataList, maxValue);
		drawSurface();
	}

	public void redrawSurface() {
		drawSurface();
	}

	private List<Integer> prepareSurfaceData() {
		if (isDataEmpty()) {
			LogUtils.d("no data");
			return null;
		}

		if (canFullShow()) {
			return mDataList;
		}

		// canvas not enough to show all items, only show part of list, base on right side
		List<Integer> showList = new ArrayList<>();
		if (mFullMode) {
			// subtract data from list to showlist
			double scale = 1.0 * mDataList.size() / mCanvasCapacity;
			int data_index;
			for (int i = 0; i < mCanvasCapacity; i++) {
				data_index = (int) (i * scale);
				showList.add(mDataList.get(data_index));
			}
		} else {
			int startIndex = mDataList.size() - mIndexOffset - mCanvasCapacity;
			for (int i = 0; i < mCanvasCapacity; i++) {
				showList.add(mDataList.get(i + startIndex));
			}
		}

		return showList;
	}

	private void prepareCanvas(Canvas canvas) {
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();

		// draw background
		canvas.drawARGB(mCanvasBgA, mCanvasBgR, mCanvasBgG, mCanvasBgB);

		// draw center line
		int midHeight = canvasHeight / 2;
		canvas.drawLine(0, midHeight, canvasWidth, midHeight, mGridMiddlePaint);

		// draw y grids(log10)
		if (mDrawGrid) {
			int[] yGrid = {10, 100, 1000, 10000};
			for (int y : yGrid) {
				int h = (int) (Math.log10(y) / Math.log10(mMaxWaveValue) * mAmplitudeHeight);
				canvas.drawLine(0, midHeight + h, canvasWidth, midHeight + h, mGridPaint);
				canvas.drawLine(0, midHeight - h, canvasWidth, midHeight - h, mGridPaint);
			}
    		/*
    		// draw y grids(log)
    		//int[] yGrid = {(int)Math.pow(2, 2), (int)Math.pow(2, 6), (int)Math.pow(2, 10), (int)Math.pow(2, 14)};
    		int[] yGrid = {4, 64, 1024, 16384};
    		for (int y : yGrid) {
    			int h = (int)(Math.log(y) / Math.log(mMaxWaveValue) * mAmplitudeHeight);
    			canvas.drawLine(0, midHeight + h, canvasWidth, midHeight + h, mGridPaint);
    			canvas.drawLine(0, midHeight - h, canvasWidth, midHeight - h, mGridPaint);
    		}
    		//*/
		}
	}

	public void drawSurface() {
		List<Integer> showList = prepareSurfaceData();
		if (showList == null) {
			LogUtils.td("no data to draw");
			return;
		}

		Canvas canvas = this.getHolder().lockCanvas(
				new Rect(0, 0, this.getWidth(), this.getHeight()));
		if (canvas == null) {
			LogUtils.te2("canvas null");
			return;
		}

		prepareCanvas(canvas);

        // draw each item based on middle line
		int canvasHeight = canvas.getHeight();
		double unitWidth = getWaveUnitWidth();
        int posX;
        int startY = canvasHeight / 2;
        int stopY;
        int itemValue;
        int itemHeight;  //NOTE: the height is signed
        int l, t, r, b;
        for (int i = 0; i < showList.size(); i++) {
            itemValue = showList.get(i);
            itemHeight = calcItemHeight(itemValue, mMaxWaveValue, mAmplitudeHeight);
            if (itemValue != 0) {
                if (mWaveItemWidth < MIN_RECT_WIDTH) {
                    posX = (int) (i * unitWidth);
                    stopY = startY - itemHeight;
                    canvas.drawLine(posX, startY, posX, stopY, mWavePaint);
                } else {
                    l = (int) (i * unitWidth);
                    r = (int) (l + mWaveItemWidth);
                    t = startY;
                    b = t - itemHeight;
                    canvas.drawRect(new Rect(l, t, r, b), mWavePaint);
                }
            }
        }

        getHolder().unlockCanvasAndPost(canvas);
	}
}
