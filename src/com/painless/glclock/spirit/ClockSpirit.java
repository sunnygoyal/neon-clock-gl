package com.painless.glclock.spirit;

import java.util.Date;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.painless.glclock.Grid;
import com.painless.glclock.R;

final class ClockSpirit extends MovableSpirit {

	private static final int[][] DIGIT_CONFIG = new int[][] {
		new int[]{0, 2, 3, 4, 5, 6},
		new int[]{0, 2},
		new int[]{5, 0, 1, 6, 3},
		new int[]{5, 0, 1, 2, 3},
		new int[]{4, 1, 0, 2},
		new int[]{5, 4, 1, 2, 3},
		new int[]{5, 6, 4, 3, 2, 1},
		new int[]{5, 0, 2},
		new int[]{0, 1, 2, 3, 4, 5, 6},
		new int[]{0, 1, 2, 3, 4, 5}
	};

	private final Grid frameGrid;
	private final Grid secondGrid;
	private final Grid hourGrid;
	private final Grid minuteGrid;

	private final Grid digitalHourGrid;

	private final Grid[] digits;

	private int lastHour = 0;
	private boolean analogMode = true;

	public ClockSpirit(Context context, SpiritManager spiritManager) {
		super(context, 4, SpiritManager.NO_SERVICE);

		frameGrid = Grid.getSimpleGrid(240, 240);
		secondGrid = Grid.getSimpleGrid(280, 280);
		hourGrid = Grid.getSimpleGrid(16, 34);
		minuteGrid = Grid.getSimpleGrid(16, 64);

		digitalHourGrid = Grid.getSimpleGrid(128, 256);

		digits = new Grid[10];

		final float factor = 50f/ 512;
		final float end = 200f/ 256;
		for (int i=0; i<10;) {
			digits[i] = getSimpleGrid(50,200, i*factor, ++i * factor, 0, end);
		}
	}

	@Override
	public int[] getResources() {
		if (analogMode) {
			return new int[] { R.drawable.clock_analog, R.drawable.clock_second,
					R.drawable.clock_analog_hour, R.drawable.clock_analog_min };
		} else {
			return new int[] { R.drawable.clock_digital, R.drawable.clock_second};
		}
	}

	@Override
	public void loadTextures(GL10 gl) {
		super.loadTextures(gl);
		if (!analogMode) {
			// Create and bind digit grid
			final Bitmap cache = Bitmap.createBitmap(500, 200, Bitmap.Config.RGB_565);
			final Bitmap digits =
				BitmapFactory.decodeResource(mContext.getResources(), R.drawable.clock_digital_digits);

			// create inverted digits
			Canvas cacheCanvas = new Canvas(cache);
			final Rect dest = new Rect(0, 0, 50, 200);
			final Rect src = new Rect(dest);

			for (int i=DIGIT_CONFIG.length-1; i>=0; i--) {
				for (int j = 0; j<DIGIT_CONFIG[i].length; j++) {
					int dNo = DIGIT_CONFIG[i][j];
					if ((dNo & 1) == 0) {
						dNo = (dNo + 5) % 8 - 1;
					}
					src.offsetTo(50* dNo, 0);
					cacheCanvas.drawBitmap(digits, src, dest, null);
				}
				dest.offset(50, 0);
			}
			digits.recycle();

			// flip Bitmap
			final Bitmap cacheFlipped = Bitmap.createBitmap(512, 256, Bitmap.Config.RGB_565);
			cacheCanvas = new Canvas(cacheFlipped);
			final Matrix flipHorizontalMatrix = new Matrix();
			flipHorizontalMatrix.setScale(-1,1);
			flipHorizontalMatrix.postTranslate(cache.getWidth(),0);
			cacheCanvas.drawBitmap(cache, flipHorizontalMatrix, null);
			cache.recycle();

			bindBitmap(gl, cacheFlipped, textureNames[3], true);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void drawOnPos(GL10 gl) {
		final Date d = new Date();

		// draw clock frame
		bind(gl, 0);
		frameGrid.draw(gl);

		// draw second
		bind(gl, 1);
		gl.glRotatef(-d.getSeconds() * 6, 0, 0, 1);
		secondGrid.draw(gl);
		gl.glPopMatrix();

		if (analogMode) {

			// draw hour
			bind(gl, 2);
			gl.glRotatef(-(d.getHours()*30 + d.getMinutes()*0.5f), 0, 0, 1);
			gl.glTranslatef(0, 17, 0);		//Match the center point
			hourGrid.draw(gl);
			gl.glPopMatrix();

			// draw minute
			bind(gl, 3);
			gl.glRotatef(-d.getMinutes() * 6, 0, 0, 1);
			gl.glTranslatef(0, 24, 0);		//Match the center point
			minuteGrid.draw(gl);
			gl.glPopMatrix();
		} else {

			int hour = d.getHours() % 12;
			if (hour == 0) {
				hour = 12;
			}
			if (lastHour != hour) {
				generateHour(hour, gl);
			}
			bind(gl, 2);
			gl.glTranslatef(-64, 0, 0);
			digitalHourGrid.draw(gl);
			gl.glPopMatrix();


			// draw minutes
			bind(gl, 3);
			gl.glTranslatef(20, 0, 0);
			digits[d.getMinutes() / 10].draw(gl);

			gl.glTranslatef(53, 0, 0);
			gl.glScalef(.66f, .66f, 0);
			digits[d.getMinutes() % 10].draw(gl);
			gl.glPopMatrix();
		}

		gl.glPopMatrix();
	}

	private void generateHour(int hour, GL10 gl) {
		lastHour = hour;

		final Bitmap cache = Bitmap.createBitmap(128, 256, Bitmap.Config.RGB_565);
		final Bitmap digits =
			BitmapFactory.decodeResource(mContext.getResources(), R.drawable.clock_digital_digits);

		final Canvas cacheCanvas = new Canvas(cache);
		final Rect dest = new Rect(63, 26, 113, 226);
		final Rect src = new Rect(0, 0, 50, 200);
		final int[] config = DIGIT_CONFIG[hour % 10];

		for (final int i : config) {
			src.offsetTo(50 * i, 0);
			cacheCanvas.drawBitmap(digits, src, dest, null);
		}
		digits.recycle();

		if (hour > 9) {
			final Bitmap hourBmp =
				BitmapFactory.decodeResource(mContext.getResources(), R.drawable.clock_digital_hour);
			cacheCanvas.drawBitmap(hourBmp, 32, 68, null);
			hourBmp.recycle();
		}

		bindBitmap(gl, cache, textureNames[2], true);
	}

	@Override
	boolean updatePref(SharedPreferences pref) {
		final boolean mode = !pref.getBoolean("clock_mode", !analogMode);
		if (analogMode != mode) {
			analogMode = mode;
			shutdown();
			return true;
		}
		return false;
	}
}
