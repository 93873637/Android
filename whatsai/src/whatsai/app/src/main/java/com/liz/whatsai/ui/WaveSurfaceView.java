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

import com.liz.androidutils.LogUtils;

import java.util.List;


public class WaveSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

	private static final double DEFAULT_WAVE_ITEM_WIDTH = 1;  // unit by pixel
	private static final double DEFAULT_WAVE_ITEM_SPACE = 0;  // unit by pixel
	private static final int MIN_RECT_WIDTH = 2;  // item width not enough to one pixel, just draw a line

	// 16bits pcm
	private static final double DEFAULT_WAVE_MAX_VALUE = 32768;

	private static final int WAVE_ITEM_COLOR = Color.rgb(79, 208, 89);
	private static final int CANVAS_BG_A = 255;
	private static final int CANVAS_BG_R = 0;
	private static final int CANVAS_BG_G = 0;
	private static final int CANVAS_BG_B = 0;
	private static final int CANVAS_GRID_COLOR = Color.rgb(160, 160, 160);
	private static final int CANVAS_GRID_COLOR_MIDDLE = Color.rgb(255, 255, 255);

	private Paint mWavePaint;
	private Paint mGridPaint;
	private Paint mGridMiddlePaint;

	private double mMaxValue = DEFAULT_WAVE_MAX_VALUE;
	private int mAmplitudeHeight;

	private double mWaveItemWidth = DEFAULT_WAVE_ITEM_WIDTH;
	private double mWaveItemSpace = DEFAULT_WAVE_ITEM_SPACE;
	private int mItemShowNum = 0;

	public WaveSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		initSurfaceView(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	public void setMaxValue(double maxValue) {
		mMaxValue = maxValue;
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

	private void initSurfaceView(final SurfaceView sfv) {
		mWavePaint = new Paint();
		mWavePaint.setColor(WAVE_ITEM_COLOR);
		mGridPaint = new Paint();
		mGridPaint.setColor(CANVAS_GRID_COLOR);
		mGridMiddlePaint = new Paint();
		mGridMiddlePaint.setColor(CANVAS_GRID_COLOR_MIDDLE);
		new Thread() {
			public void run() {
				Canvas canvas = sfv.getHolder().lockCanvas(
						new Rect(0, 0, sfv.getWidth(), sfv.getHeight()));
				if (canvas == null) {
					LogUtils.e("ERROR: initSurfaceView: canvas null");
				}
				else {
					mAmplitudeHeight = canvas.getHeight() / 2;
					drawBackground(canvas);
					sfv.getHolder().unlockCanvasAndPost(canvas);
				}
			}
		}.start();
	}

	public String getSurfaceInfo() {
		String info = "";
		info += " <font color=\"#ff0000\">" + mMaxValue + "</font>";
		info += " | <font color=\"#ff0000\">" + mItemShowNum + "</font>";
		info += " | <font color=\"#ff0000\">" + mAmplitudeHeight + "</font>";
		info += " | <font color=\"#ff0000\">" + mWaveItemWidth + "</font>";
		info += " | <font color=\"#ff0000\">" + mWaveItemSpace + "</font>";
		return info;
	}

	private void drawBackground(Canvas canvas) {
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();

		// draw background
		canvas.drawARGB(CANVAS_BG_A, CANVAS_BG_R, CANVAS_BG_G, CANVAS_BG_B);

		// draw center line
		int midHeight = canvasHeight / 2;
		canvas.drawLine(0, midHeight, canvasWidth, midHeight, mGridMiddlePaint);

		///*
		// draw y grids(log10)
		int[] yGrid = {10, 100, 1000, 10000};
		for (int y : yGrid) {
			int h = (int)(Math.log10(y) / Math.log10(mMaxValue) * mAmplitudeHeight);
			canvas.drawLine(0, midHeight + h, canvasWidth, midHeight + h, mGridPaint);
			canvas.drawLine(0, midHeight - h, canvasWidth, midHeight - h, mGridPaint);
		}
		//*/

		mItemShowNum = (int)(canvasWidth / getWaveUnitWidth());

		/*
		// draw y grids(log)
		//int[] yGrid = {(int)Math.pow(2, 2), (int)Math.pow(2, 6), (int)Math.pow(2, 10), (int)Math.pow(2, 14)};
		int[] yGrid = {4, 64, 1024, 16384};
		for (int y : yGrid) {
			int h = (int)(Math.log(y) / Math.log(mMaxValue) * mAmplitudeHeight);
			canvas.drawLine(0, midHeight + h, canvasWidth, midHeight + h, mGridPaint);
			canvas.drawLine(0, midHeight - h, canvasWidth, midHeight - h, mGridPaint);
		}
		//*/
	}

	private static int calcItemHeight(int value, int maxValue, int maxHeight) {
		//return (int) (value / maxValue * maxHeight);

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

	public void onUpdateSurfaceData(@NonNull List<Integer> dataList, int maxValue) {
		LogUtils.d("onUpdateSurfaceData: thread id = " + Thread.currentThread().getId());
		Canvas canvas = this.getHolder().lockCanvas(
				new Rect(0, 0, this.getWidth(), this.getHeight()));
		if (canvas == null) {
			LogUtils.e("ERROR: updateWaveSurface: canvas null");
			return;
		}

		drawBackground(canvas);

		int canvasHeight = canvas.getHeight();
		double unitWidth = this.getWaveUnitWidth();

		int listSize = dataList.size();
		if (listSize <= 0) {
			LogUtils.i("list empty");
		}
		else {
			int startIndex = 0;
			int showSize = listSize;
			if (listSize > mItemShowNum) {
				//canvas not enough to show all items, only show last items
				startIndex = listSize - mItemShowNum;
				showSize = mItemShowNum;
			}
			LogUtils.d("updateWaveSurface: startIndex = " + startIndex + ", showSize = " + showSize);

			// draw each power based on middle line
			int posX;
			int startY = canvasHeight / 2;
			int stopY;
			int itemValue;
			int itemHeight;  //NOTE: the height is signed
			int l, t, r, b;
			for (int i = 0; i < showSize; i++) {
				itemValue = dataList.get(i + startIndex);
				itemHeight = calcItemHeight(itemValue, maxValue, mAmplitudeHeight);
				if (itemValue != 0) {
					if (mWaveItemWidth < MIN_RECT_WIDTH) {
					    posX = (int)(i * unitWidth);
						stopY = startY - itemHeight;
						canvas.drawLine(posX, startY, posX, stopY, mWavePaint);
					}
					else {
					    l = (int)(i * unitWidth);
						r = (int)(l + mWaveItemWidth);
						t = startY;
						b = t - itemHeight;
						canvas.drawRect(new Rect(l, t, r, b), mWavePaint);
					}
				}
			}

            /*
            //draw line/rect based on bottom
            int itemHeight;
            if (DEFAULT_WAVE_ITEM_WIDTH == 1) {
                // just draw a line for one power value
                int posX, posY;
                for (int i = 0; i < showSize; i++) {
                    itemHeight = canvasHeight * powerList.get(i+startIndex) / POWER_MAX;
                    posX = i * DEFAULT_WAVE_UNIT_WIDTH;
                    posY = canvasHeight - itemHeight;
                    canvas.drawLine(posX, posY, posX, canvasHeight, mWavePaint);
                }
            }
            else {
                //draw a rect for one power value
                //left, top, right, bottom of rect
                int l, t, r, b;
                for (int i = 0; i < showSize; i++) {
                    itemHeight = canvasHeight * powerList.get(i+startIndex) / POWER_MAX;
                    l = i * DEFAULT_WAVE_UNIT_WIDTH;
                    r = l + DEFAULT_WAVE_ITEM_WIDTH;
                    t = canvasHeight - itemHeight;
                    b = canvasHeight;
                    canvas.drawRect(new Rect(l, t, r, b), mWavePaint);
                }
            }
            //*/
		}

		this.getHolder().unlockCanvasAndPost(canvas);
	}

}
