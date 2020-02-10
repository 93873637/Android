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

	private static final int WAVE_ITEM_WIDTH = 1;  // unit by pixel
	private static final int WAVE_ITEM_SPACE = 0;  // unit by pixel
	private static final int WAVE_UNIT_WIDTH = WAVE_ITEM_WIDTH + WAVE_ITEM_SPACE;
	private static final int WAVE_ITEM_COLOR = Color.rgb(79, 208, 89);
	private static final int CANVAS_BG_A = 255;
	private static final int CANVAS_BG_R = 43;
	private static final int CANVAS_BG_G = 43;
	private static final int CANVAS_BG_B = 43;
	private static final int CANVAS_GRID_COLOR = Color.rgb(212, 212, 212);
	private static final int CANVAS_GRID_COLOR_MIDDLE = Color.rgb(255, 255, 255);

	private Paint mWavePaint;
	private Paint mGridPaint;
	private Paint mGridMiddlePaint;

	private int mAmplitude;

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

	public void initSurfaceView(final SurfaceView sfv) {
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
					mAmplitude = canvas.getHeight() / 2;
					drawBackground(canvas);
					sfv.getHolder().unlockCanvasAndPost(canvas);
				}
			}
		}.start();
	}

	private void drawBackground(Canvas canvas) {
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();

		// draw background
		canvas.drawARGB(CANVAS_BG_A, CANVAS_BG_R, CANVAS_BG_G, CANVAS_BG_B);

		// draw Y grid
		int maxHeight = canvasHeight / 2;
		int midHeight = canvasHeight / 2;
		int[] yGrid = {10, 100, 1000, 10000};
		int maxValue = 32768;
		for (int i = 0; i < yGrid.length; i++) {
			int h = (int)(Math.log10(yGrid[i]) / Math.log10(maxValue) * maxHeight);
			canvas.drawLine(0, midHeight + h, canvasWidth, midHeight + h, mGridPaint);
			canvas.drawLine(0, midHeight - h, canvasWidth, midHeight - h, mGridPaint);
		}
		canvas.drawLine(0, midHeight, canvasWidth, midHeight, mGridMiddlePaint);
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
		Canvas canvas = this.getHolder().lockCanvas(
				new Rect(0, 0, this.getWidth(), this.getHeight()));
		if (canvas == null) {
			LogUtils.e("ERROR: updateWaveSurface: canvas null");
			return;
		}

		drawBackground(canvas);

		int canvasWidth = this.getWidth();
		int canvasHeight = this.getHeight();

		int listSize = dataList.size();
		if (listSize <= 0) {
			LogUtils.i("list empty");
		}
		else {
			double lastPower = dataList.get(listSize - 1);
			LogUtils.d("updateWaveSurface: listSize = " + listSize + ", lastPower = " + lastPower);

			int maxVisibleSize = canvasWidth / WAVE_UNIT_WIDTH;
			int startIndex = 0;
			int showSize = listSize;
			if (listSize > maxVisibleSize) {
				//canvas not enough to show all items, only show last items
				startIndex = listSize - maxVisibleSize;
				showSize = maxVisibleSize;
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
				itemHeight = calcItemHeight(itemValue, maxValue, mAmplitude);
				if (itemValue != 0) {
					if (WAVE_ITEM_WIDTH == 1) {
						posX = i * WAVE_UNIT_WIDTH;
						stopY = startY - itemHeight;
						canvas.drawLine(posX, startY, posX, stopY, mWavePaint);
					}
					else {
						l = i * WAVE_UNIT_WIDTH;
						r = l + WAVE_ITEM_WIDTH;
						t = startY;
						b = t - itemHeight;
						canvas.drawRect(new Rect(l, t, r, b), mWavePaint);
					}
				}
			}

            /*
            //draw line/rect based on bottom
            int itemHeight;
            if (WAVE_ITEM_WIDTH == 1) {
                // just draw a line for one power value
                int posX, posY;
                for (int i = 0; i < showSize; i++) {
                    itemHeight = canvasHeight * powerList.get(i+startIndex) / POWER_MAX;
                    posX = i * WAVE_UNIT_WIDTH;
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
                    l = i * WAVE_UNIT_WIDTH;
                    r = l + WAVE_ITEM_WIDTH;
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
